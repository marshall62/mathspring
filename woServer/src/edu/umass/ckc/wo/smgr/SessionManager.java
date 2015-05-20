package edu.umass.ckc.wo.smgr;

import edu.umass.ckc.wo.admin.PedagogyRetriever;
import edu.umass.ckc.wo.db.*;
import edu.umass.ckc.wo.event.AdventurePSolvedEvent;
import edu.umass.ckc.wo.event.tutorhut.LogoutEvent;
import edu.umass.ckc.wo.exc.DeveloperException;
import ckc.servlet.servbase.UserException;
import edu.umass.ckc.wo.handler.NavigationHandler;
import edu.umass.ckc.wo.login.LoginResult;
import edu.umass.ckc.wo.mrcommon.Names;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import edu.umass.ckc.wo.tutormeta.LearningCompanion;
import edu.umass.ckc.wo.tutormeta.PedagogyParams;
import edu.umass.ckc.wo.tutormeta.StudentModel;
import edu.umass.ckc.wo.util.WoProps;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Random;


public class SessionManager {

    private static final Logger logger = Logger.getLogger(SessionManager.class);


    public static final String LOGIN_USER_PASS = "uname_password_login_check";
    private static final String OTHER_ACTIVE_SESSIONS = "other_active_sessions";
    private static final String WRIST_BRACELET_ID_ = "wrist_bracelet_active_session";
    private static final String LOGIN_USER_MOM = "uname_momname_login_check";
    private static final String MESSAGE = "message";
    public static final int NUM_GROUPS = 20;
    public static final int STUDENT_TYPED_GROUP = 5;

    private static int guestIDCounter = 0; // used for generating guest user IDS

    private Connection connection;


    private int studId = -1;
    private int classId = -1;
    private int sessionId = -1;
    private int pedagogyId = -1;
    private String client = null ; // the name of the Flash client that the user should use
    private WoProps woProps;
    private StudentState studState; // state variables about student stored in db
    //    private StudentProfile profile; // contains satv,satm, gender, and group
    private Random ran = new Random();
    private StudentModel studentModel = null;
    private PedagogicalModel pedagogicalModel = null;
    private String loginResponse = null;
    private LearningCompanion learningCompanion=null;
    private LoginResult loginResult;
    private long timeInSession;
    private String hostPath; // The piece of the request URL that gives http://chinacat.../
    private String contextPath; // The full URL up to /TutorBrain (e.g.  http://localhost:8082/wo4
    private boolean assistmentsUser= false;
    private long elapsedTime =0;
    private boolean testUser;
    private User user;

    public SessionManager(Connection connection) {
        this.connection = connection;
        timeInSession=0;
    }




    public LoginResult login(String uname, String password, long sessBeginTime, boolean logoutExistingSession) throws Exception {
        return attemptSessionCreation(uname, password, sessBeginTime, logoutExistingSession);
    }


    /**
     * Constructor only used when the user is logging in from Assistments
     *
     */
    public SessionManager assistmentsLoginSession(int studId) throws Exception {
        int newSessId = DbSession.newSession(connection, studId, System.currentTimeMillis(), true);
        timeInSession=0;
        loginResult = new LoginResult(newSessId, null);
        sessionId = loginResult.getSessId();
        buildSession(connection, loginResult.getSessId());
        studState.getSessionState().initializeState();
        return this;
    }

    /**
     * Constructor only used when the user is logging in as guest.
     *
     */
    public SessionManager guestLoginSession (int studId) throws Exception {
        int newSessId = DbSession.newSession(connection, studId, System.currentTimeMillis(), false);
        timeInSession=0;
        loginResult = new LoginResult(newSessId, null);
        sessionId = loginResult.getSessId();
        buildSession(connection, loginResult.getSessId());
        studState.getSessionState().initializeState();
        return this;
    }

    /**
     * Build a SessionManager which is a container for all student information for this student's request.
     * <p/>
     * Recent addition: Using the group number a student is assigned to, a Pedagogy object is retrieved.
     * From that we can get the name of the StudentModel class that is appropriate for the student.
     * We then can construct the StudentModel now and have it ready thoughout the life of this HTTP request.
     *
     * The hostPath is a partial URL to the servlet (just through the host and port).   This is necessary for
     * calls that will eventually come from a JSP client page to find the Flash client.   It is presumed to be running on the same host.
     *
     *
     *
     * @param connection
     * @param sessionId
     * @param hostPath
     * @param contextPath
     * @throws Exception
     */
    public SessionManager(Connection connection,
                          int sessionId, String hostPath, String contextPath) throws Exception {
        this.connection = connection;
        this.sessionId = sessionId;
        this.hostPath = hostPath;
        this.contextPath = contextPath;
    }

    public SessionManager buildExistingSession () throws Exception {
        buildSession(connection, sessionId);
        return this;
    }

    // will look something like http://cadmium.cs.umass.edu/  or http://localhost/  (port removed)
    public String getHostPath () {
        return this.hostPath;
    }

    // returns something like http://localhost:8082/wo4
    public String getContextPath () {
        return this.contextPath;
    }


    public void createSessionForStudent (int studId) {

    }

    public LearningCompanion getLearningCompanion () {
        return learningCompanion;
    }

    public String getLoginResponse() {
        return loginResponse;
    }

    public LoginResult getLoginResult () {
        return loginResult;
    }



    private void buildSession(Connection connection, int sessionId) throws Exception {
        DbSession.updateSessionLastAccessTime(connection, sessionId);
        int[] ids = DbSession.setSessionInfo(connection, sessionId);
        this.studId=ids[0];
        this.classId=ids[1];
        this.user = DbUser.getStudent(connection,this.studId);
        this.setClient(DbSession.getClientType(connection, sessionId));
        woProps = new WoProps(connection);
        woProps.load(studId);   // get all properties for studId
        Timestamp lastLoginTime= DbSession.getLastLogin(connection,studId);
        if (lastLoginTime != null)
            this.timeInSession = System.currentTimeMillis()- lastLoginTime.getTime();

        setStudentState(woProps);   // pull out student state props from all properties

        Pedagogy ped = PedagogyRetriever.getPedagogy(connection, studId);
        // these are the parameters as dfined in the XML file pedagogies.xml

        this.pedagogyId = Integer.parseInt(ped.getId());
        // pedagogical model needs to be instantiated as last thing because its constructor takes the smgr instance (this)
        // and makes calls to get stuff so we want this as fully constructed as possible before the call to instantiate
        // so that the smgr is fully functional except for its ped model.
//        PedagogicalModelParameters pedModelParams = getPedagogicalModelParametersForUser(connection, ped);
       // build the Pedagogical model for the student.  The PedagogicalModel constructor is responsible for
       // creating the StudentModel which also gets set in the below method
       instantiatePedagogicalModel(ped);



        // set theparams on the ped model
//        pedagogicalModel.setParams(pedModelParams);

//            if (userParams != null)
//                ((ConfigurablePedagogy) pedagogicalModel).configure(userParams);
//       }
       this.learningCompanion = this.pedagogicalModel.getLearningCompanion();
    }




    // only used by test driver
    public void assignUserToGroup(String group_string) throws SQLException {
        int group = 0;

        group = ran.nextInt(NUM_GROUPS);
        this.pedagogyId = group;

    }


    private void setStudentState(WoProps props) throws SQLException {
        studState = new StudentState(this.getConnection(), this);
        studState.setObjid(this.getStudentId());
        studState.extractProps(props);  // pull out student state props from all properties
    }

    public StudentState getStudentState() {
        return studState;
    }

    public ProblemState getProblemState() {
        return studState.getProblemState();
    }






    private String getXML(boolean success) {
        return Names.XML_HEADER + (success ? Names.LOGIN_SUCCESS : Names.LOGIN_FAILURE);

    }


    public String adventureProblemSolved(AdventurePSolvedEvent e) throws Exception {
        long elapsedTime = e.getElapsedTime();
        String userInput = e.getUserInput();
        int probId = e.getProbId();
        int studId = getStudentId();

//        if (elapsedTime <= 0 || probId <= 0 || studId <= 0)
        if (elapsedTime <= 0 || studId <= 0)
            return Names.ADVENTURE_PROBLEM_STORED_FAILURE;

        DbAdventureLogger.logAdventureProblemSolved(getConnection(), e, elapsedTime, userInput, probId, studId, this.getSessionNum(), e.getAdventureName());
        return Names.ADVENTURE_PROBLEM_STORED_SUCCESS;
    }



    public int getStudentClass(int studId) throws SQLException {
        return DbUser.getStudentClass(this.getConnection(), studId);
    }

    public int getClassID () {
        return this.classId;
    }






    public int findMaxActiveSession(int studId) throws SQLException {
        return DbSession.findMaxActiveSession(getConnection(), studId);
    }


    private GregorianCalendar calcCurTime(String clientBeginTime) {
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;
        String[] cbt = {"", ""};

        if (clientBeginTime != null) {
            cbt = clientBeginTime.split(",");
            if (cbt.length > 1) {
                String[] clientTime = cbt[0].split(":");
                String[] clientDate = cbt[1].split("-");
                if (clientDate.length > 1) {
                    // the month in Calendar is 0 indexed, so need to subtract 1
                    month = (new Integer(clientDate[0])).intValue() - 1;
                    day = (new Integer(clientDate[1])).intValue();
                    year = (new Integer(clientDate[2])).intValue();
                }
                if (clientTime.length > 1) {
                    hour = (new Integer(clientTime[0])).intValue();
                    minute = (new Integer(clientTime[1])).intValue();
                    second = (new Integer(clientTime[2])).intValue();
                }
            }
        }


        GregorianCalendar cal = new GregorianCalendar();
        cal.set(year, month, day, hour, minute, second);
        return cal;
    }

    private void clearDemoUsers(String uname) throws java.lang.Exception {
        String demoUsers = "'test','6thtest1', '6thtest2', 'testch2', 'testch3', " +
                "'Jakedemo6', 'Janedemo6','Jakedemo10','Jakedemo10'," +
                "'testch21', 'testch22'";

        if (demoUsers.contains("'" + uname + "'")) {

            String sql = "delete from episodicdata2 where studid in " +
                    "(select id from student where username='" + uname + "')";
            PreparedStatement ps = this.getConnection().prepareStatement(sql);
            ps.executeUpdate();

            sql = "delete from woproperty where property not like '%pretest%' " +
                    "and objid in (select id from student where username ='" + uname + "')";

            ps = this.getConnection().prepareStatement(sql);
            ps.executeUpdate();
        }
    }





    public String getLoginView(String check, String checkVal, String message, int sessionId, int studentId, LearningCompanion learningCompanion) {
        StringBuffer result = new StringBuffer();
        result.append(NavigationHandler.ACK + "=" + NavigationHandler.TRUE + "\n");
        if (check != null && checkVal != null)
            result.append("&" + check + "=" + checkVal + "\n");
        if (message != null)
            result.append("&" + MESSAGE + "=" + message + "\n");
        result.append("&allow_skip_intro=" + NavigationHandler.TRUE);  // fixed value for now
        result.append("&sessionId=" + sessionId); // dam added 6/14/05 so client keeps session state
        result.append(getLC(learningCompanion));  // dam 6/24/05  LC provided by PedagogicalModel which is constructed before this call
        return result.toString();
    }

    private String getLC(LearningCompanion lc) {

        if (lc != null)
            return ("&learningCompanion=" + lc.getCharactersName());
        else return "";
    }

    private Pedagogy getPedagogy(int studentId) {

        Pedagogy ped = null;

        if (studentId > -1) {
            try {
                ped = PedagogyRetriever.getPedagogy(connection, studentId);
                this.pedagogyId = Integer.parseInt(ped.getId());
            } catch (Exception e) {
                System.out.println(e);
            }
        }

       return ped;
    }

    /**
     * If the user/pw is valid combo,  this looks for an active session for this user.   If it finds one, it deactivates it (we don't
     * want a user logged in twice).   It then creates a new session
     *
     * @param uname
     * @param password
     * @param sessBeginTime
     * @param logoutExistingSession
     * @return
     * @throws Exception
     */
    public LoginResult attemptSessionCreation(String uname, String password, long sessBeginTime, boolean logoutExistingSession) throws Exception {
        if (uname.trim().length() == 0 || password.trim().length() == 0)
            return new LoginResult(-1,"Invalid user name/password combination",LoginResult.ERROR);
        else {
            studId = DbUser.getStudent(this.getConnection(), uname, password);

            if (studId == -1) {
               return new LoginResult(-1,"Invalid user name/password combination",LoginResult.ERROR);
            }
            else {
              this.user = DbUser.getStudent(this.getConnection(),studId);
              int classId = DbUser.getStudentClass(this.getConnection(),studId);
              this.classId = classId;
                // We delete a student that does not have a classID because we expect them to re-register with
                    // the same user/passwd and need to get this old bogus user out of the way.
                if (classId == -1) {
                    DbUser.deleteStudent(connection, studId);
                     return new LoginResult(-1, "This user is invalid because it is not in a class.   You need to re-register and select a class", LoginResult.ERROR);
                }
                int oldSessId = DbSession.findActiveSession(getConnection(), studId);
                Pedagogy ped;
                if (oldSessId != -1)  {
                    String msg = String.format("The user name <b>%s</b> whose name is <b>%s %s</b> is already logged into the system.  If you are not this person, please check with your teacher or double check that you are using the correct user name",user.getUname(),user.getFname(),user.getLname());
                    if (!logoutExistingSession)
                        return new LoginResult(-1,msg, LoginResult.ALREADY_LOGGED_IN);
                    else {
                        inactivateAllUserSessions();
                        this.sessionId = DbSession.newSession(getConnection(), studId, sessBeginTime, false);
                        ped = getPedagogy(studId);
                        woProps = new WoProps(connection);
                        woProps.load(studId);   // get all properties for studId
                        Timestamp lastLoginTime= DbSession.getLastLogin(connection,studId);
                        if (lastLoginTime != null)
                            this.timeInSession = System.currentTimeMillis()- lastLoginTime.getTime();
                        setStudentState(woProps);   // pull out student state props from all properties
                        studState.getSessionState().initializeState();
                        instantiatePedagogicalModel(ped);
                        pedagogicalModel.newSession(sessionId); // tells the pedagogical that its a new session so it can initialize.
                        return new LoginResult(sessionId, null,LoginResult.NEW_SESSION);
                    }
                }
                else {
                    ped = getPedagogy(studId);
                    woProps = new WoProps(connection);
                    woProps.load(studId);   // get all properties for studId
                    Timestamp lastLoginTime= DbSession.getLastLogin(connection,studId);
                    if (lastLoginTime != null)
                        this.timeInSession = System.currentTimeMillis()- lastLoginTime.getTime();

                    setStudentState(woProps);   // pull out student state props from all properties
                    this.sessionId = DbSession.newSession(getConnection(), studId, sessBeginTime, false);
                    studState.getSessionState().initializeState();
                    instantiatePedagogicalModel(ped);
                    return new LoginResult(sessionId, null);
                }

            }
        }
    }




    /**
     * Only used by defunct server test code.
     *
     * @param uname
     * @param momName
     * @param password
     */
    public String serverTester_LoginStudent(String uname, String momName, String password, long clientBeginTime, String wristID) throws Exception {


        clearDemoUsers(uname);

        StringBuffer result = new StringBuffer();
        String loginCheck = null;
        int studId = -1;
        // given a password so use uname/pwd
        if (password != null) {
            studId = DbUser.getStudent(this.getConnection(), uname, password);
            int sessId = DbSession.findActiveSession(getConnection(), studId);
            if (studId == -1)
                return getLoginView(LOGIN_USER_PASS,
                        NavigationHandler.FALSE, "Invalid user name/password combination", -1, -1, null);
                // login was rejected because of other active
                // sessions and then prompt user to see if they want to log them all out automatically and relogin.
                // Give them a sessionId so that if they choose to logout the other sessions, this session will be left
                // active.
            else if (sessId != -1) {
                int newSessId = DbSession.newSession(getConnection(), studId, clientBeginTime, false);
                return getLoginView(OTHER_ACTIVE_SESSIONS, NavigationHandler.TRUE, null, newSessId, studId, null);
            } else
                loginCheck = LOGIN_USER_PASS;
        } else if (momName != null) {
            studId = DbUser.getStudentByMomsName(this.getConnection(), uname, momName);
            if (studId == -1)
                return getLoginView(LOGIN_USER_MOM,
                        NavigationHandler.FALSE, "Invalid user name/mothers name combination", -1, -1, null);
            else
                loginCheck = LOGIN_USER_MOM;
        } else {
            throw new UserException("must provide uname and password or uname and momName");
        }

        this.sessionId = DbSession.newSession(getConnection(), studId, clientBeginTime, false);
        // At login time we need a PedagogicalModel for the user (so that a learning companion can be returned to the client)
        // Only way to get it is to construct another (full) smgr using our new sessionID.   We can then
        // get its PedagogicalModel's learning companion
        SessionManager smgr = new SessionManager(this.getConnection(), this.sessionId, hostPath, contextPath).buildExistingSession();  // doing this because we need a PedagogicalModel for the user
        PedagogicalModel pm = smgr.getPedagogicalModel();
        LearningCompanion lc = pm.getLearningCompanion();  // A PM must return an LC or null (no longer in the pedagogies.xml)
//        setNumProbsSolved(studId);  // no longer need to figure this out from EpiData - its in the student state.
        return getLoginView(loginCheck, NavigationHandler.TRUE, null, this.getSessionNum(), studId, lc);
    }



    public String logoutStudent(LogoutEvent logoutEvent, String ipAddr) throws Exception {

        DbSession.inactivateSession(getConnection(), logoutEvent.getSessionId());
        // create an HttpSessionObject for this student id
        return "ack=true";
    }


    public int getStudentId() {
        return this.studId;
    }

    public String getUserName () {
        return this.user.getUname();
    }


    public int getSessionNum() throws Exception {
        if (sessionId != -1) {
            return sessionId;
        } else
            throw new UserException("Attempt to get a session failed.  Make sure you are logged in");
    }

    public long getElapsedTime(){
        return elapsedTime;
    }


    /**
     * Extract the PedagogicalModel class name from the Pedagogy object and
     * construct the actual object.   PedagogicalModel constructors are also responsible for creating
     * a StudentModel.
     */
    public void instantiatePedagogicalModel(Pedagogy p) {
        try {
            Class c = Class.forName(p.getPedagogicalModelClass());
            Constructor constr = c.getConstructor(SessionManager.class, Pedagogy.class);
            logger.debug("Instantiating a pedagogical model of type: " + c.getName());
            this.pedagogicalModel = (PedagogicalModel) constr.newInstance(this, p);
            studentModel = this.pedagogicalModel.getStudentModel();
            if (studentModel == null) {
                throw new DeveloperException("A StudentModel object was not created by the constructor of " + p.getPedagogicalModelClass());
            }
            studentModel.init(woProps, studId, classId);
        } catch (Exception e) {
            logger.fatal(this.pedagogicalModel, e);
        }
    }

    // can go away when we eliminate the old event model of WO
    private StudentModel setStudentModelClassname(String smClassName) {

        StudentModel sm = null;

        try {
            Class c = Class.forName(smClassName);
            Constructor constr = c.getConstructor(Connection.class);
            sm = (StudentModel) constr.newInstance(connection);
        } catch (Exception e) {
            System.out.println(e);
        }
        return sm;
    }

    // can go away when we eliminate the old event model of WO
    private LearningCompanion setLearningCompanion(String learningCompanionClassName) {

        LearningCompanion lc = null;

        try {
            Class c = Class.forName(learningCompanionClassName);
            lc = (LearningCompanion) c.newInstance();
        } catch (Exception e) {
            System.out.println(e);
        }
        return lc;
    }

    public void setStudentModel(StudentModel studentModel) {
        this.studentModel = studentModel;
    }

    public StudentModel getStudentModel() {
        return this.studentModel;
    }


    public Connection getConnection() {
        return this.connection;
    }



    // To remove all evidence of a student in the system, delete the eventlog for his session, delete the studentproblemhistory for his session
    // delete the session
    public void removeTestSessionData() throws Exception {
        PreparedStatement ps = null;
        try {

            String q = "delete from eventLog where sessNum=?";
            ps = this.connection.prepareStatement(q);
            ps.setInt(1, this.getSessionNum());
            ps.executeUpdate();
            q = "delete from session where id=?";
            ps = this.connection.prepareStatement(q);
            ps.setInt(1, this.getSessionNum());
            ps.executeUpdate();
//            studState.clearUserProperties(this.studId);
        } finally {
            ps.close();
        }
    }



    public void inactivateTempUserSessions(int sessionId) throws SQLException {
        String q = "delete from session where id=?";
        PreparedStatement ps = this.getConnection().prepareStatement(q);
        long now = System.currentTimeMillis();
        ps.setInt(1, sessionId);
        int u = ps.executeUpdate();

    }

    /**
     * Make all sessions for a given studId inactive except for the current session
     *
     * @throws SQLException
     */
    public void inactivateUserSessions() throws Exception {
        String q = "update session set isActive=0, endTime=? where isActive = 1 and studId = ? and id != ?";
        PreparedStatement ps = this.getConnection().prepareStatement(q);
        long now = System.currentTimeMillis();
        ps.setTimestamp(1, new Timestamp(now));
        ps.setInt(2, studId);
        ps.setInt(3, this.getSessionNum());
        System.out.println(ps.toString());
        int u = ps.executeUpdate();
        System.out.println(u);
    }

    public void inactivateAllUserSessions() throws Exception {
        String q = "update session set isActive=0, endTime=? where isActive=1 and studId=?";
        PreparedStatement ps = this.getConnection().prepareStatement(q);
        long now = System.currentTimeMillis();
        ps.setTimestamp(1, new Timestamp(now));
        ps.setInt(2, this.getStudentId());
        ps.executeUpdate();
    }





    public static void loadDbDriver() {
        String dbDriver = "com.mysql.jdbc.Driver";
        try {
            Driver d = (Driver) Class.forName(dbDriver).newInstance(); // MySql
            System.out.println(d);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static Connection getAConnection() throws SQLException {
        String dbPrefix = "jdbc:mysql";
        String dbHost = Settings.dbhost;
        String dbSource = "wayangoutpostdb";
        String dbUser = "WayangServer";
        String dbPassword = "jupiter";

        String url;
        if (dbPrefix.equals("jdbc:mysql"))
            url = dbPrefix + "://" + dbHost + "/" + dbSource + "?user=" + dbUser + "&password=" + dbPassword; // preferred by MySQL
        else // JDBCODBCBridge
            url = dbPrefix + ":" + dbSource;
//        url = "jdbc:mysql://localhost:3306/test";
//        url = "jdbc:mysql://localhost/rashidb"; // this works
        try {
            logger.info("connecting to db on url " + url);
            return DriverManager.getConnection(url, dbUser, dbPassword);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        }
    }

    private void checkSessionTimes(Connection conn, int id) throws SQLException {
        String s = "select beginTime, endTime, lastAccessTime from session where id=?";
        PreparedStatement ps = conn.prepareStatement(s);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Timestamp bt = rs.getTimestamp(1);
            Timestamp et = rs.getTimestamp(2);
            Timestamp lt = rs.getTimestamp(3);
        }
    }

    /**
     *   Difficulty rating is the divisor that the problem selector uses to compute its next index.  The divisor is
     * used to increase or decrease the difficulty of the problem.   If the divisor is 2 and the student got the last problem correct,
     * the index will be 1/2 the way into the list of harder problems.   If the divisor is 3, we'd go 1/3 of the way.
     * @return
     * @throws SQLException
     */
    public int getClassDifficultyRate () throws SQLException {
        return DbClass.getDifficultyRate(connection, this.classId);
    }

    public double getClassMasteryThreshold () throws SQLException {
        return DbClass.getClassMastery(connection, this.classId);
    }

    public static void main(String[] args) {
        try {
            loadDbDriver();
            Connection conn = getAConnection();
            SessionManager smgr = new SessionManager(conn);
            smgr.checkSessionTimes(conn, 15574);
//            DbSession.cleanupStaleSessions(conn);
//            FileReader fis = new FileReader("u:\\wo\\100users\\sequenceGroupTest.csv");
//            BufferedReader bis = new BufferedReader(fis);
//            String line;
//            line = bis.readLine(); // get rid of headers
//            while ((line = bis.readLine()) != null) {
//                String[] x = CSVParser.parse(line);
//                String fname= x[2];
//                String lname= x[3];
//                String password= fname;
//                String userName = fname+lname.substring(0,1);
//                int group = Integer.parseInt(x[1]);
//                int id = smgr.createUser(conn,fname,lname,userName,password,"");
//                StudentProfile prof = new StudentProfile(0,0,id,"",group);
//                smgr.insertStudentProfile(prof);
//                smgr.updateProfileGroup(id,group);
//                System.out.println(fname + " " + lname + ": id=" + id + " userName=" + userName + " password=" + password
//                + " group=" + group);
//            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    // This is a stub that replaces StudentProfile.getStudentSequenceGroup.
    // Currently there is no mechanism for assigning a sequence group to a student so
    // we used a fixed sequence group of zero which is what the comment in ProblemSetProblemSelector
    // stated was happening anyway.
    // Hopefully, we can get by with this.   If we need different students using different
    // problem sequences, then we've got a problem because students are simply assigned a pedagogy
    // as bunch of selectors - not a bunch of selectors + some flags like what sequenceGroup to use.
    public int getStudentSequenceGroup() {
        return 0;  //TODO write the code for this.  To change body of created methods use File | Settings | File Templates.
    }

    public PedagogicalModel getPedagogicalModel() {
        return pedagogicalModel;
    }

    public void setPedagogicalModel (PedagogicalModel pm) {
        this.pedagogicalModel = pm;
    }

    public void clearUserProperties() {
        int studId = this.getStudentId();

    }




    public long getTimeInSession() {
        return timeInSession;
    }

    public boolean isFirstLogin() throws SQLException {
        return DbSession.getStudentSessions(connection,this.getStudentId()).size() == 1;
    }

    public void setAssistmentsUser(boolean assistmentsUser) {
        this.assistmentsUser = assistmentsUser;
    }

    public boolean isAssistmentsUser() {
        return assistmentsUser;
    }
//
//    public void initializeTopicTeaching(int topicId) throws SQLException {
//        studState.setTutorEntryTime(System.currentTimeMillis());
//        studState.setCurTopic(topicId);
//        studState.setTopicNumProbsSeen(0);
//        studState.setTimeInTopic(0);
//        studState.setTutorEntryTime(System.currentTimeMillis());
//        studState.setNumProblemsThisTutorSession(0);
//        studState.setNumRealProblemsThisTutorSession(0);
//        studState.setNumProbsSinceLastIntervention(0);
//        studState.setStudentSelectedTopic(-1);
//        studState.setInReviewMode(false);
//        studState.setInChallengeMode(false);
//    }

    public String getClassTeacher() throws SQLException {
        return DbClass.getClass(getConnection(),this.getStudentClass(this.getStudentId())).getTeacherName();
    }




    public String getClient() {
        return client+".swf";
    }

    public void setClient(String client) {
        this.client = client;
    }

    public WoProps getStudentProperties () {
        return this.woProps;
    }

    public boolean showTestUserControls() throws SQLException {
        return DbUser.isShowTestControls(this.connection, this.studId);
    }

    public boolean isTestUser() throws SQLException {
       return DbUser.isTestUser(this.connection,this.studId);
    }

    public void setPedagogyId(int pedagogyId) {
        this.pedagogyId = pedagogyId;
    }
}