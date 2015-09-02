package edu.umass.ckc.wo.login;

import ckc.servlet.servbase.ServletAction;
import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.db.DbSession;
import edu.umass.ckc.wo.event.tutorhut.TutorHomeEvent;
import edu.umass.ckc.wo.html.tutor.TutorPage;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutormeta.LearningCompanion;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.woserver.ServletInfo;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 12, 2012
 * Time: 7:33:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class Login4 implements ServletAction {
    protected String next_jsp;

    public Login4() {
    }

    public String process(Connection conn, ServletContext servletContext, ServletParams params, HttpServletRequest req, HttpServletResponse resp, StringBuffer servletOutput) throws Exception {
        int left = params.getInt(LoginParams.LEFT);
        int right = params.getInt(LoginParams.RIGHT);

        int sessionId = params.getInt(LoginParams.SESSION_ID);
        // Make a DB call to save this info   TODO might want a full servlet path rather than ""
        SessionManager smgr = new SessionManager(conn, sessionId).buildExistingSession();
        DbUser.setFlankingUsers(conn,smgr.getStudentId(),left,right);
        LearningCompanion lc = smgr.getLearningCompanion();
        req.setAttribute("sessionId", sessionId);
        req.setAttribute("learningCompanion", (lc != null) ? lc.getCharactersName() : "");
        // subclass binds clientType so it is either adult or k12.  Store in session row
        ServletInfo si = new ServletInfo(servletContext,null,req,resp,params,servletOutput,null,servletContext.getContextPath(),"TutorBrain");
        //new TutorPage(si,smgr).handleRequest(new TutorHomeEvent(sessionId));
        new LandingPage(si,smgr).handleRequest();

        return null;


    }
}
