package edu.umass.ckc.wo.login.interv;

import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbSession;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.login.LoginParams;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.tutormeta.Intervention;

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
public class GetNeighbors extends LoginInterventionSelector {
    private static final String JSP = "neighbors.jsp";


    public GetNeighbors(SessionManager smgr) throws SQLException {
        super(smgr);
    }




    public Intervention selectIntervention (SessionEvent e) throws SQLException {
        long shownTime = this.interventionState.getTimeOfLastIntervention();
        if (shownTime > 0)
            return null;
        else {
            super.selectIntervention(e);
            List<User> students = DbClass.getClassStudents(conn,smgr.getStudentClass(smgr.getStudentId()));
            HttpServletRequest req = this.servletInfo.getRequest();
            req.setAttribute(LoginParams.STUDENTS,students);
            return new LoginIntervention(JSP);
        }
    }

    public void processInput (ServletParams params) throws SQLException {
        int left = params.getInt(LoginParams.LEFT);
        int right = params.getInt(LoginParams.RIGHT);
        DbUser.setFlankingUsers(servletInfo.getConn(), smgr.getStudentId(), left, right);
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
