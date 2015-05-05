package edu.umass.ckc.wo.login.interv;

import ckc.servlet.servbase.ServletParams;
import ckc.servlet.servbase.UserException;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.login.LoginParams;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutormeta.Intervention;
import org.jdom.Element;

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
//    public static final String urli = "<iframe src=\"https://docs.google.com/forms/d/1ailDyQ9tChd9Abh6TEUsCoyALYJSLi8mWoIiHzMZcpA/viewform?embedded=true\" width=\"760\" height=\"500\" frameborder=\"0\" marginheight=\"0\" marginwidth=\"0\">Loading...</iframe>";
//    public static final String url = "https://docs.google.com/forms/d/1ailDyQ9tChd9Abh6TEUsCoyALYJSLi8mWoIiHzMZcpA/viewform?usp=send_form";
    public static final String JSP = "presurvey.jsp";
    public static final String JSPI = "presurveyIframe.jsp";

    private String url;
    private boolean embed=true;
    public PreSurvey(SessionManager smgr) throws SQLException, UserException {
        super(smgr);

    }

    public void init (SessionManager smgr, PedagogicalModel pm) throws Exception {
        if (configXML == null)
            throw new UserException("PreSurvey expects config xml");
        Element e =this.configXML.getChild("url");

        if (e != null)
            this.url= e.getTextTrim();
        else throw new UserException("Must provide URL to config of PreSurvey LoginIntervention Selector in logins.xml");
        e =this.configXML.getChild("embed");
        if (e != null)
            this.embed = Boolean.parseBoolean(e.getTextTrim());
    }



    public Intervention selectIntervention (SessionEvent e) throws SQLException {
        long shownTime = this.interventionState.getTimeOfLastIntervention();
        boolean isFirstLogin = DbUser.isFirstLogin(smgr.getConnection(),smgr.getStudentId());
        if (!isFirstLogin || shownTime > 0)
            return null;
        else {
            super.selectIntervention(e);
            // Shows the survey in an embedded iframe
            if (this.embed) {
                servletInfo.getRequest().setAttribute("surveyIframe",url);
                return new LoginIntervention(JSPI);
            }
            else {
            // A JSP will show nothing more than a "continue" button.
            // The URL comes up in a separate browser window.  When the user is done in the separate window
            // they close it and click the "continue" button.
                servletInfo.getRequest().setAttribute("survey",url);
                return new LoginIntervention(JSP,url,true);
            }
        }
    }

    public void processInput (ServletParams params) throws SQLException {
        // do nothing.
    }


}
