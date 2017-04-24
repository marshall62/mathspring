package edu.umass.ckc.wo.handler;

import ckc.servlet.servbase.ServletEvent;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rezecib on 4/10/2017.
 */
public abstract class EditProblemFormatHandler {
    public static final String JSP = "/teacherTools/editProblemFormat.jsp";

    public static void handleEvent(ServletEvent e, ServletContext sc, Connection conn, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        List<String> templates = getSettingsFromDatabase(conn, "quickauthformattemplates", "problemFormat", false);
        List<String> fonts = getSettingsFromDatabase(conn, "quickauthformatfonts", "font", true);
        List<String> colors = getSettingsFromDatabase(conn, "quickauthformatcolors", "color", true);
        req.setAttribute("templates", templates);
        req.setAttribute("fonts", fonts);
        req.setAttribute("colors", colors);
        int problemId = e.getServletParams().getInt("problemId", -1);
        if(problemId != -1) {
            String problemFormat = getProblemFormat(conn, problemId);
            if(problemFormat != null) req.setAttribute("problemFormat", problemFormat);
        }
        req.getRequestDispatcher(JSP).forward(req, resp);
    }

    private static List<String> getSettingsFromDatabase(Connection conn, String table, String column, boolean addQuotes) throws SQLException {
        List<String> settings = new ArrayList<String>();
        String query = "SELECT " + column + " FROM " + table + ";";
        PreparedStatement ps = conn.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        try {
            while(rs.next()) {
                String option = rs.getString(1);
                if(option != null) {
                    if(addQuotes) option = "'" + option + "'";
                    settings.add(option);
                }
            }
        } finally {
            if(rs != null) rs.close();
            if(ps != null) ps.close();
        }
        return settings;
    }

    private static String getProblemFormat(Connection conn, int problemId) throws SQLException {
        String query = "SELECT problemFormat FROM problem WHERE id=" + problemId + ";";
        PreparedStatement ps = conn.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        try {
            while(rs.next()) {
                return rs.getString(1);
            }
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
        return null;
    }
}
