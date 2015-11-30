package edu.umass.ckc.wo.login;

import ckc.servlet.servbase.BaseServlet;
import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.CCContentMgr;
import edu.umass.ckc.wo.content.LessonMgr;
import edu.umass.ckc.wo.login.interv.LoginInterventionSelector;
import edu.umass.ckc.wo.mrcommon.Names;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutor.probSel.BaseExampleSelector;
import edu.umass.ckc.wo.tutor.vid.BaseVideoSelector;
import edu.umass.ckc.wo.woserver.ServletInfo;
import edu.umass.ckc.wo.woserver.ServletUtil;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import java.sql.Connection;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: Dec 1, 2009
 * Time: 2:17:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class WoLoginServlet extends BaseServlet {
    private static Logger logger = Logger.getLogger(WoLoginServlet.class);

    public String getDataSource(ServletConfig servletConfig) {
         return servletConfig.getServletContext().getInitParameter("wodb.datasource");
     }

    protected boolean handleRequest(ServletContext servletContext, Connection conn, HttpServletRequest request,
                                    HttpServletResponse response, ServletParams params, StringBuffer servletOutput) throws Exception {
        LoginServletAction action = ActionFactory.buildAction(params);
        ServletInfo servletInfo = new ServletInfo(servletContext,conn,request,response,params,servletOutput,hostPath,contextPath,this.getServletName());

        logger.info(">>" + params.toString());
        // after the user/pw has been accepted all the other actions are LoginEvent or LoginInterventionInput
//        if (action instanceof LoginEvent)   {
//            LoginSequence ls = new LoginSequence(servletInfo,params.getInt("sessionId"));
//            ls.processAction(params);
//            return false;
//        }
//        // When an intervention is complete, the form is submitted with an action=LoginInterventionInput and interventionClass=InterventionSelector
//        // so that we can send the form inputs to the intervention selector that generated the intervention.
//        else
        // All actions are either inputs to a Login intervention or the first login screen with user/pw
        if (action instanceof LoginInterventionInput) {
            String cl = params.getString("interventionClass");
            int sessId = params.getInt("sessionId");
            Class c = Class.forName(cl);
            SessionManager smgr = new SessionManager(conn,sessId,servletInfo.getHostPath(),servletInfo.getContextPath()).buildExistingSession();
            LoginInterventionSelector is = (LoginInterventionSelector) c.getConstructor(SessionManager.class).newInstance(smgr);
            is.init(servletInfo);
            is.processInput(params);
            // Now find the next intervention
            LoginSequence ls = new LoginSequence(servletInfo,params.getInt("sessionId"));
            ls.processAction(params);
            return false;
        }
        else {  // processes the first login event which is the user id/pw
            LoginResult lr = action.process(servletInfo);
            // state variables that prevent login interventions from running twice might
            // be leftover from a previous login sequence that failed recently.  This
            // will clean them out so that this sequence is fresh.
            if (lr.isNewSession()) {
                LoginSequence ls = new LoginSequence(servletInfo,lr.getSessId());
                ls.clearInterventionState();

            }
            // Sometimes the login is processed by forwarding to a JSP, so just return false because a page is already generated
            if (lr.isForwardedToJSP()) {
                return false;
            }
            else {
                LoginSequence ls = new LoginSequence(servletInfo,lr.getSessId());
                ls.processAction(params);
                return false;
            }

        }
    }

    protected void initialize(ServletConfig servletConfig, ServletContext servletContext, Connection connection) throws Exception {
        logger.debug("Begin init of WOLoginServlet");
        ServletUtil.initialize(servletContext, connection);
        Settings.formalityServletURI = servletConfig.getInitParameter(Names.FORMALITY_SERVLET_URI);
        servletContext.setAttribute("flashClientURI", Settings.flashClientPath);
        Settings.getSurveys(connection); // loads the pre/post Survey URLS
        // Loads all content into a cache for faster access during runtime
        if (!ProblemMgr.isLoaded())  {
            ProblemMgr problemMgr = new ProblemMgr(new BaseExampleSelector(), new BaseVideoSelector());
            problemMgr.loadProbs(connection);
            CCContentMgr.getInstance().loadContent(connection);
            LessonMgr.getAllLessons(connection);  // only to check integrity of content so we see errors early

        }
        logger.debug("end init of WOLoginServlet");

    }

    
}
