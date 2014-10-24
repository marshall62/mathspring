package edu.umass.ckc.wo.login;

import ckc.servlet.servbase.ServletAction;
import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.db.DbUserProfile;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.db.DbClass;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 12, 2012
 * Time: 7:16:50 PM
 * This takes the input from the login2a.jsp page which provides a first and last initial.   If good inputs are given,  the next page is login2b.jsp
 */
public class Login3 implements ServletAction {


    protected String next_jsp ;

     public String process(Connection conn, ServletContext servletContext, ServletParams params,
                           HttpServletRequest req, HttpServletResponse resp, StringBuffer servletOutput) throws Exception {

         int sessId = params.getInt(LoginParams.SESSION_ID);
         String fname = params.getString(LoginParams.FNAME);
         String lini = params.getString(LoginParams.LINI);
         int confidence = params.getInt(LoginParams.CONFIDENCE,0);
         int excitement = params.getInt(LoginParams.EXCITEMENT,0);
         int frustration = params.getInt(LoginParams.FRUSTRATION,0);
         int interest = params.getInt(LoginParams.INTEREST,0);
         // TODO might want to put in a correct servlet path rather than ""
         SessionManager smgr = new SessionManager(conn,sessId, "", "").buildExistingSession();
         DbUser.setUserNames(conn,smgr.getStudentId(),fname,lini);
         DbUserProfile.deleteProfile(conn, smgr.getStudentId())  ;
         DbUserProfile.setValues(conn, smgr.getStudentId(), confidence, excitement, interest, frustration);
         req.setAttribute(LoginParams.SESSION_ID, sessId);
         List<User> students = DbClass.getClassStudents(conn,smgr.getStudentClass(smgr.getStudentId()));
         req.setAttribute(LoginParams.STUDENTS,students);
         return next_jsp;   // will be login2b.jsp
    }
}
