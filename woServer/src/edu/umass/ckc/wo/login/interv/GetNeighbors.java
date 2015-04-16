package edu.umass.ckc.wo.login.interv;

import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbSession;
import edu.umass.ckc.wo.login.LoginParams;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.User;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 4/14/15
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetNeighbors extends LoginIntervention {
    private static final String JSP = "login/js/neighbors.jsp";

    public GetNeighbors(SessionManager smgr) {
        super(smgr);
    }

    public String f (SessionManager smgr) throws SQLException {
        int oldSessId = DbSession.findActiveSession(smgr.getConnection(), smgr.getStudentId());
        if (oldSessId != -1)
            request.setAttribute(LoginParams.MESSAGE, "You had an old session which has been automatically logged out.");
        List<User> students = DbClass.getClassStudents(smgr.getConnection(), smgr.getStudentClass(smgr.getStudentId()));
        request.setAttribute(LoginParams.STUDENTS,students);
        return JSP;
    }
}
