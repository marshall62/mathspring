package edu.umass.ckc.wo.db;

import edu.umass.ckc.wo.content.Hint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * An object which is in charge of database mgmt of Problem objects.
 * <p/>
 * Copyright (c) University of Massachusetts
 * Written by: David Marshall
 * Date: Jun 25, 2007
 * Time: 12:02:49 PM
 */
public class DbHint extends BaseMgr {
    private static final Logger logger = Logger.getLogger(DbHint.class);

    /**
     * Gets all the hints associated with the problem
     *
     * @param probId
     * @return
     * @throws Exception
     */
    public static List<Hint> getHintsForProblemOld(Connection conn, int probId) throws Exception {
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            List<Hint> result = new ArrayList<Hint>();
            String q = "select " + Hint.ID + "," + Hint.PROBLEM_ID + "," + Hint.NAME + "," + Hint.IS_ROOT + "," + Hint.GIVES_ANSWER +
                    " from Hint where " + Hint.PROBLEM_ID + "=" + probId;
            ps = conn.prepareStatement(q);
            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(Hint.ID);
                int problemId = rs.getInt(Hint.PROBLEM_ID);
                String label = rs.getString(Hint.NAME);
                int givesAnswer = rs.getInt(Hint.GIVES_ANSWER);
                int isroot = rs.getInt(Hint.IS_ROOT);
                result.add(new Hint(id, label, problemId, givesAnswer == 1, isroot == 1));
            }
            return result;
        } finally {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        }
    }

    // rewrote the above method to determine isRoot status of a hint based on solutionpath rather than the isRoot column in
    // the hint table which is error prone.
    public static List<Hint> getHintsForProblem(Connection conn, int probId) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String q = "select id, name, givesAnswer, statementHTML, audioResource, hoverText from Hint " +
                    "where problemid= ? ";
            ps = conn.prepareStatement(q);
            ps.setInt(1, probId);
            rs = ps.executeQuery();
            List<Hint> hints = new ArrayList<Hint>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                boolean givesAnswer = rs.getBoolean("givesAnswer");
                String audio = rs.getString("audioResource");
                String hoverText = rs.getString("hoverText");
                String stmtHTML = rs.getString("statementHTML");

                Hint h = new Hint(id, name, probId, givesAnswer, false, stmtHTML, audio, hoverText);
                setIsRoot(conn, h);
                hints.add(h);
            }
            return hints;
        } finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();

        }

    }

    private static void setIsRoot(Connection conn, Hint h) throws SQLException {
        boolean isSrc = hintInColumn(conn,h.getId(),"sourceHint");
        boolean isTgt = hintInColumn(conn,h.getId(),"targetHint");
        h.setIs_root(isSrc && !isTgt);
    }

    private static boolean hintInColumn (Connection conn, int hintId, String colName) throws SQLException {
        boolean inCol=false;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String q = "select * from solutionpath where " +colName+ "=?";
            ps = conn.prepareStatement(q);
            ps.setInt(1, hintId);
            rs = ps.executeQuery();
            if (rs.next()) {
                inCol=true;
            }
            return inCol;
        } finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();

        }
    }

}