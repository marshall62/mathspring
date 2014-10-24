package edu.umass.ckc.wo.login;

import edu.umass.ckc.wo.db.DbSession;
import ckc.servlet.servbase.ServletAction;
import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.handler.UserRegistrationHandler;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.tutormeta.LearningCompanion;
import edu.umass.ckc.wo.woserver.ServletInfo;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 12, 2012
 * Time: 10:40:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class GuestLogin implements ServletAction {

    public String process(Connection conn, ServletContext servletContext, ServletParams params, HttpServletRequest req,
                          HttpServletResponse resp, StringBuffer servletOutput) throws Exception {
        int studId = UserRegistrationHandler.registerTemporaryUser(conn, edu.umass.ckc.wo.db.DbClass.GUEST_USER_CLASS_NAME, User.UserType.guest);
        SessionManager smgr = new SessionManager(conn).guestLoginSession(studId);
        DbSession.setClientType(conn, smgr.getSessionNum(), params.getString(LoginParams.CLIENT_TYPE));
        LearningCompanion lc = smgr.getLearningCompanion();
        req.setAttribute("sessionId", smgr.getSessionNum());
        req.setAttribute("learningCompanion", (lc != null) ? lc.getCharactersName() : "");
        ServletInfo si = new ServletInfo(servletContext,null,req,resp,params,servletOutput,null,servletContext.getContextPath(),"TutorBrain");
        new LandingPage(si,smgr).handleRequest();
        return null;
    }
}
