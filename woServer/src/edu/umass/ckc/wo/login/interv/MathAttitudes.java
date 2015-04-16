package edu.umass.ckc.wo.login.interv;

import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbSession;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 4/14/15
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class MathAttitudes extends LoginIntervention  {

    public MathAttitudes(SessionManager smgr) {
        super(smgr);
    }

    private static final String JSP = "login/js/mathAttitudes.jsp";

    public String f (SessionManager smgr) {
        return JSP;
    }
}
