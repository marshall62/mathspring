package edu.umass.ckc.wo.login.interv;

import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.db.DbClass;
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
 * Date: 5/1/15
 * Time: 1:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class PreSurvey  extends LoginInterventionSelector {
    public static final String urli = "<iframe src=\"https://docs.google.com/forms/d/1jRutTghnc5sUzJjkVDxFuRrC6t6bPLfsP1VD0yIFJZQ/viewform?embedded=true\" width=\"760\" height=\"500\" frameborder=\"0\" marginheight=\"0\" marginwidth=\"0\">Loading...</iframe>";
    public static final String url = "https://docs.google.com/forms/d/1jRutTghnc5sUzJjkVDxFuRrC6t6bPLfsP1VD0yIFJZQ/viewform?usp=send_form";
    public static final String JSP = "presurvey.jsp";
    public PreSurvey(SessionManager smgr) throws SQLException {
        super(smgr);
    }



    public Intervention selectIntervention (SessionEvent e) throws SQLException {
        long shownTime = this.interventionState.getTimeOfLastIntervention();
        if (shownTime > 0)
            return null;
        else {
            super.selectIntervention(e);
            // A JSP will show nothing more than a "continue" button.
            // The URL comes up in a separate browser window.  When the user is done in the separate window
            // they close it and click the "continue" button.
            servletInfo.getRequest().setAttribute("survey",url);
            return new LoginIntervention(JSP,url,true);
        }
    }

    public void processInput (ServletParams params) throws SQLException {
        // do nothing.
    }


}
