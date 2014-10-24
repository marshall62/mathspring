package edu.umass.ckc.wo.woreports;

import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.util.ProbPlayer;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Dec 9, 2011
 * Time: 9:25:45 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TopicTrajectoryReport extends Report {
    // cadmium/wayang2/flash/Problems/probplayer.swf?
    protected void insertProbLister(HttpServletRequest req, Connection conn, List<Integer> probIds) throws SQLException {
        StringBuilder sb = new StringBuilder();

        for (int pid: probIds) {
            Problem prob = new DbProblem().getProblem(conn,pid);
            if (prob != null) {
                String url = ProbPlayer.getURLToProbPlayer(req) + "?questionNum=" + prob.getProbNumber();
                sb.append( "<option value=\"" +url+ "\">" + pid + ": " + prob.getName() + ": " + prob.getNickname() + "\n");
            }
        }

        this.src.append("<form name=\"form\">\n" +
                "<select name=\"site\" size=1>\n" +
                sb.toString() +
                "</select>\n" +
                "<input type=button value=\"Preview\" onClick=\"javascript:formHandler(this)\">\n" +
                "</form>");
    }
}
