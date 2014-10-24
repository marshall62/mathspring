package edu.umass.ckc.wo.handler;


import edu.umass.ckc.wo.admin.PedagogyAssigner;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.enumx.Actions;
import edu.umass.ckc.wo.event.admin.*;
import edu.umass.ckc.wo.html.admin.*;
import ckc.servlet.servbase.ServletEvent;
import ckc.servlet.servbase.View;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.email.Emailer;
import edu.umass.ckc.wo.util.ServletURI;
import edu.umass.ckc.wo.util.ThreeTuple;
import edu.umass.ckc.wo.beans.ClassInfo;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles all events for creating a user.
 */
public class UserRegistrationHandler {
    public static final Logger logger = Logger.getLogger(UserRegistrationHandler.class);

    public UserRegistrationHandler() {
    }

    /**
     * Creating a user provides the student with a page with entries for all the fields in the Student and
     * Class tables. Upon gathering this, a field for each property in the UserProp table is requested.
     * A row in the Class table holds the
     * id that maps the class to a PropGroup.  This PropGroup is then connected to the UserProp rows using
     * the PropGroupMap table.
     * <p/>
     * The states of user creation are:
     * START: The initial request: generate a page requesting fields in Student/Class tables
     * STUD:  the authentication fields in the Student table come in (if valid goto CLASS; else regenerate page with error msg)
     * CLASS: the fields of the Class table come in (if valid goto GETPROPS; else regenerate page with error msg)
     * GETPROPS:  additional user property fields in the UserProps table come in (goto to final)
     * FINAL: Generate a message saying the user has been created successfully.
     */
    public View handleEvent(ServletContext sc, HttpServletRequest servletRequest, Connection conn, ServletEvent e) throws Exception {
        if (e instanceof UserRegistrationStartEvent) // START state
            return generateStudPage(servletRequest,(UserRegistrationStartEvent) e);
        else if (e instanceof UserRegistrationAuthenticationInfoEvent) // STUD state
            return processStudentInfo(servletRequest, conn, (UserRegistrationAuthenticationInfoEvent) e);
        else if (e instanceof UserRegistrationClassSelectionEvent) // CLASS state
            return processClassInfo(servletRequest, conn, (UserRegistrationClassSelectionEvent) e);
        else if (e instanceof UserRegistrationMoreInfoEvent) // GETPROPS state
            return processPropertyInfo(servletRequest, conn, (UserRegistrationMoreInfoEvent) e);
        else
            return null; // should never reach
    }


    /**
     * Generate the inputs that collect the user name, email address, password, etc.
     * @param req
     * @return
     * @throws Exception
     */
    private View generateStudPage(HttpServletRequest req, UserRegistrationEvent e) throws Exception {
        String url = ServletURI.getURI(req);



        Variables v = new Variables(req.getServerName(),
                req.getServletPath(),
                req.getServerPort());

        return new UserRegistrationAuthenticationInfoPage(url, null, e);
    }

    /**
     * Validate the inputs from the first registration page and regenerate with error messages or generate the next page
     * which asks them to select their class/teacher from a list.
     * @param req
     * @param conn
     * @param e
     * @return
     * @throws Exception
     */
    private View  processStudentInfo(HttpServletRequest req, Connection conn, UserRegistrationAuthenticationInfoEvent e) throws Exception {
        Variables v = new Variables(req.getServerName(),
                req.getServletPath(),
                req.getServerPort());
        String url = ServletURI.getURI(req);
        if (e.getPassword()==null || e.getPassword().trim().equals(""))
            return new UserRegistrationAuthenticationInfoPage(url, "Please retry.  You must provide a password.", e);
        int studId = DbUser.getStudent(conn, e.getUserName(),e.getPassword());
        if (studId != -1)
           return new UserRegistrationAuthenticationInfoPage(url, "Please retry.  That user name is already taken.", e);
        else {
            boolean isTestUser = e.isTestUser();
            // We either have a real student user who is registering in a class or a test user registering in a class.
            if (isTestUser)
                studId = DbUser.createUser(conn,e.getFname(),e.getLname(),e.getUserName(),e.getPassword(),e.getEmail(), User.UserType.test);
            else
                studId = DbUser.createUser(conn,e.getFname(),e.getLname(),e.getUserName(),e.getPassword(),e.getEmail(), User.UserType.student);

            Emailer.sendPassword("no-reply@wayangoutpost.net", Settings.mailServer,e.getUserName(),e.getPassword(),e.getEmail());
            return new UserRegistrationClassSelectionPage(url, studId, conn, Actions.createUser3,  e);
        }
    }

    public static String genName (Connection conn, String prefix) throws SQLException {
        int count = DbUser.getGuestUserCounter(conn);
        return prefix + count;
    }



    // currently usertypes are either assistmentStudent or assistmentTest
    // The assumption is that we are willing to create a mathspring user for this external user so we need to generate a name.
    public static int registerExternalUser(Connection conn, String assistmentsClassName, String externalUserName, User.UserType ut) throws Exception {
        int count = DbUser.getGuestUserCounter(conn);
        String user = externalUserName + count;   // create a userName from the external name + counter
        int studId = DbUser.createUser(conn,"","", user,"","", ut);
        ClassInfo cl = DbClass.getClassByName(conn, assistmentsClassName);
        int classId = cl.getClassid();
        DbUser.updateStudentClass(conn, studId, classId);
        // Now that the student is in a class, he is assigned a Pedagogy from one of the pedagogies
        // that the class uses.
        int pedId = PedagogyAssigner.assignPedagogy(conn,studId, classId);
        // store the pedagogy id in the student table row for this user.
        DbUser.setStudentPedagogy(conn,studId,pedId);
        return studId;
    }


    public static int registerTemporaryUser(Connection conn, String className, User.UserType userType) throws Exception {
        int count = DbUser.getGuestUserCounter(conn);
        String prefix="temp";
        if (userType == User.UserType.guest)
            prefix = "guest";
        else if (userType == User.UserType.externalTempTest)
            prefix = "externalTempTest";
        String user = genName(conn,prefix);
        int studId = DbUser.createUser(conn,"","", user,"","", userType);
        ClassInfo cl = DbClass.getClassByName(conn, className);
        int classId = cl.getClassid();
        DbUser.updateStudentClass(conn, studId, classId);
        // Now that the student is in a class, he is assigned a Pedagogy from one of the pedagogies
        // that the class uses.
        int pedId = PedagogyAssigner.assignPedagogy(conn,studId, classId);
        // store the pedagogy id in the student table row for this user.
        DbUser.setStudentPedagogy(conn,studId,pedId);
        return studId;
    }

    public static int registerStudentUser(Connection conn, String userName, String pw, ClassInfo classInfo) throws Exception {

        int studId = DbUser.createUser(conn,"","", userName,pw,"", User.UserType.student);
        int classId = classInfo.getClassid();
        DbUser.updateStudentClass(conn, studId, classId);
        // Now that the student is in a class, he is assigned a Pedagogy from one of the pedagogies
        // that the class uses.
        int pedId = PedagogyAssigner.assignPedagogy(conn,studId, classId);
        // store the pedagogy id in the student table row for this user.
        DbUser.setStudentPedagogy(conn,studId,pedId);
        return studId;
    }




    /**
     * Given the classId, return a List of ThreeTuples <id, internalPropName, externalPropName> that are found in the
     * PropGroup associated with that class.  Note this uses the internalName column.
     * ; the name column is for display (in the html page) purposes only
     */
    public static List getUserProperties(Connection conn, int classId) throws Exception {
        int propGroupId = getPropGroup(conn, classId); // get the propGroup for this class
        if (propGroupId == -1)
            return new ArrayList();
        String q = "select b.internalName, b.name,b.id from PropGroupMap a, UserProp b where b.id = a.propId AND " +
                "a.groupId = ?";
        PreparedStatement ps = conn.prepareStatement(q);
        ps.setInt(1, propGroupId);
        ResultSet rs = ps.executeQuery();
        ArrayList l = new ArrayList();
        while (rs.next()) {
            String ipname = rs.getString(1); // internal name
            String epname = rs.getString(2); // external name
            int id = rs.getInt(3);
            ThreeTuple tt = new ThreeTuple(new Integer(id), ipname, epname);
            l.add(tt);
        }
        return l;
    }

    public static int getPropGroup(Connection conn, int classId) throws Exception {
        String q = "select propGroupId from Class where id=?";
        PreparedStatement ps = conn.prepareStatement(q);
        ps.setInt(1, classId);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
            return rs.getInt(1);
        else
           return -1;
    }

    /**
     * The student has selected a class.  Now generate a page that requests a more information about the user.
     * @param req
     * @param conn
     * @param e
     * @return
     * @throws Exception
     */
    private View processClassInfo(HttpServletRequest req, Connection conn, UserRegistrationClassSelectionEvent e) throws Exception {

        String uri = ServletURI.getURI(req);
        DbUser.updateStudentClass(conn, e.getStudId(), e.getClassId());
        // Now that the student is in a class, he is assigned a Pedagogy from one of the pedagogies
        // that the class uses.
        int pedId = PedagogyAssigner.assignPedagogy(conn,e.getStudId(), e.getClassId());
        // store the pedagogy id in the student table row for this user.
        DbUser.setStudentPedagogy(conn,e.getStudId(),pedId);
        return new UserRegistrationUserPropertiesPage(uri, e, null, conn);
    }


    /**
     * Given the column info for a UserPropVal create a new row.  This is the value for a given property
     * of a given student.
     */
    private void updateStudPropVal(Connection conn, int studId, Integer pid, String pname, String val) throws Exception {
        String q = "insert into UserPropVal (propId, studId,value) values (?,?,?)";
        PreparedStatement ps = conn.prepareStatement(q);
        ps.setInt(1, pid.intValue());
        ps.setInt(2, studId);
        ps.setString(3, val);
        ps.execute();
    }

    /**
     * At this point we are getting a bunch of properties but we don't know what
     * params they are.  We have to go to the database tables UserProps for the given
     * PropGroup that this students class is assigned to and get all the property names.
     * Using these names, we extract them from the param list and save the associated value
     * into the StudPropValues table
     */
    //todo: put these variables somewhere more appropriate
    private static final String GROUP = "problemSequenceGroup";
    private static final int DEFAULT_GROUP = 18;
    private static int currentGroup = DEFAULT_GROUP;

    private View processPropertyInfo(HttpServletRequest req, Connection conn, UserRegistrationMoreInfoEvent e) throws Exception {
        Variables v = new Variables(req.getServerName(),
                req.getServletPath(),
                req.getServerPort());
        String url = ServletURI.getURI(req);
        int studId = e.getStudId();
        int classId = e.getClassId();

        List propNames = getUserProperties(conn, classId);  // returns a list of tuples <id, internalPropName, externalPropName>
        Iterator iter = propNames.iterator();
        while (iter.hasNext()) {
            ThreeTuple tt = (ThreeTuple) iter.next();
            String ipname = (String) tt.getSecond();  // the property name
            Integer pid = (Integer) tt.getFirst();  // the property id
            String pval = e.getServletParams().getString(ipname); // extract the property value HTML form inputs (held in servlet params)

            //make sure group is valid
            pval = validateGroupParam(ipname, pval);
            updateStudPropVal(conn, studId, pid, ipname, pval);
        }
        return new UserRegistrationCompletePage(url, e);
    }

    private String validateGroupParam(String ipname, String pval) {
        if (ipname.equals(this.GROUP)){
            boolean validGroup = false;
            try{
                int group = Integer.parseInt(pval);
                if (groupIsValid(group)){
                    validGroup = true;
                }
            }
            catch(Exception ex){ }
            //else give a valid group
            if (!validGroup){
                pval = Integer.toString(UserRegistrationHandler.currentGroup);
                UserRegistrationHandler.currentGroup++;
                if (!groupIsValid(UserRegistrationHandler.currentGroup)){
                    UserRegistrationHandler.currentGroup = UserRegistrationHandler.DEFAULT_GROUP;
                }
            }
        }
        return pval;
    }


    private boolean groupIsValid(int val){
        //allows ld group
        //return (val >=0 && val <=4) || (val >=10 && val <=17);
        return (val >=15 && val <=20);
    }

}