package edu.umass.ckc.wo.login;

import edu.umass.ckc.wo.db.DbSession;
import ckc.servlet.servbase.ServletAction;
import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.User;
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
 * Time: 6:49:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class Login2 implements ServletAction {
    public static String login1_jsp;
    protected String login_getName_jsp;
    protected String login_getFlankingUsers_jsp;
    protected String login_existingSess_jsp;
    protected String clientType;



    public String process(Connection conn, ServletContext servletContext, ServletParams params, HttpServletRequest req, HttpServletResponse response,
                          StringBuffer servletOutput) throws Exception {
        String uName = params.getString(LoginParams.USER_NAME,"");
        String pw = params.getString(LoginParams.PASSWORD,"");
        boolean logoutExistingSession = params.getBoolean(LoginParams.LOGOUT_EXISTING_SESSION,false);
        SessionManager smgr = new SessionManager(conn);
        LoginResult lr = smgr.login(uName,pw,System.currentTimeMillis(), logoutExistingSession);

        if (!lr.isFailed())
        // subclass binds clientType so it is either adult or k12.  Store in session row
         DbSession.setClientType(conn, smgr.getSessionNum(), clientType);

        // want to save the flashClient with the studId as the key so that we can build URLs later
        if (lr.getStatus()== LoginResult.ALREADY_LOGGED_IN) {
            req.setAttribute(LoginParams.MESSAGE,lr.getMessage());
            req.setAttribute(LoginParams.USER_NAME,uName);
            req.setAttribute(LoginParams.PASSWORD,pw);
            return login_existingSess_jsp;
        }

        // student exists, but class is gone.
        if (lr.isFailed()) {
            req.setAttribute(LoginParams.MESSAGE,lr.getMessage());
            return login1_jsp;
        }
        else {
            return null;



        }
    }
}
