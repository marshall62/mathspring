package edu.umass.ckc.wo.handler;

import ckc.servlet.servbase.ServletEvent;
import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.event.admin.AdminDeactivateLiveProblemEvent;
import edu.umass.ckc.wo.event.admin.AdminReloadProblemsEvent;
import edu.umass.ckc.wo.event.admin.AdminTutorEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 6/24/14
 * Time: 12:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class TutorAdminHandler {

    public void processEvent (HttpServletRequest servletRequest, HttpServletResponse servletResponse, ServletEvent e, Connection conn) throws Exception {
        if (e instanceof AdminDeactivateLiveProblemEvent) {
            ProblemMgr.deactivateProblem(((AdminDeactivateLiveProblemEvent) e).getProbId(),
                    ((AdminDeactivateLiveProblemEvent) e).getProbName(), conn);
        }
        else if (e instanceof AdminReloadProblemsEvent) {
            ProblemMgr.dumpCache();
            ProblemMgr.loadProbs(conn);
            servletRequest.setAttribute("teacherId", ((AdminReloadProblemsEvent) e).getTeacherId());
            servletRequest.setAttribute("message", "Problems successfully reloaded.");
            servletRequest.getRequestDispatcher("/teacherTools/adminTutor.jsp").forward(servletRequest,servletResponse);
        }
        else if (e instanceof AdminTutorEvent) {
            servletRequest.setAttribute("teacherId", ((AdminTutorEvent) e).getTeacherId());
            servletRequest.getRequestDispatcher("/teacherTools/adminTutor.jsp").forward(servletRequest, servletResponse);
        }
    }
}
