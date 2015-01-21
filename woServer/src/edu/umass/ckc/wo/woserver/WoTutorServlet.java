package edu.umass.ckc.wo.woserver;

import edu.umass.ckc.wo.assistments.AssistmentsHandler;
import edu.umass.ckc.wo.beans.Teacher;
import edu.umass.ckc.wo.beans.TeacherEntity;
import edu.umass.ckc.wo.content.CCContentMgr;
import edu.umass.ckc.wo.content.LessonMgr;
import edu.umass.ckc.wo.db.HibernateUtil;
import edu.umass.ckc.wo.exc.AssistmentsBadInputException;
import edu.umass.ckc.wo.mrcommon.Names;
import ckc.servlet.servbase.BaseServlet;
import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutor.vid.BaseVideoSelector;
import edu.umass.ckc.wo.tutor.probSel.BaseExampleSelector;
import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.db.DbUtil;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.classic.Session;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.io.File;
import java.util.List;

/**
 * This servlet replaces TutorBrainServlet.  It handles all requests coming from the wayang client software.
 */
public class WoTutorServlet extends BaseServlet {
    private static Logger logger = Logger.getLogger(WoTutorServlet.class);
    private String policyFile;
    private ProblemMgr problemMgr; // ProblemMgr is held here explicitely (even though not by necessity).
    // It maintains a
    // static cache of sat hut Problems, hints, etc that persist throughout
    // the duration of the servlet engine run.   The WoAdminServlet even
    // calls the object's static methods.

    public String getDataSource(ServletConfig servletConfig) {
        return servletConfig.getServletContext().getInitParameter("wodb.datasource");
    }

    /**
     * Overrides the initialize method in BaseServlet.  Called once at servlet load time.
     *
     * @param servletConfig
     * @param servletContext
     * @param connection
     */
    protected void initialize(ServletConfig servletConfig, ServletContext servletContext, Connection connection) throws Exception {
        try {

            ServletUtil.initialize(servletContext);
            logger.debug("Begin init of WoTutorServlet");
            // machine learning problem selector needs to read a policy file
            Settings.policyFile = servletConfig.getInitParameter(Names.POLICY_FILE);
            Settings.mlLogFile = servletConfig.getInitParameter(Names.ML_LOG_FILE);
            String useLearningCompanions = servletContext.getInitParameter(Names.USE_LEARNING_COMPANIONS);
            if (useLearningCompanions != null)
                Settings.useLearningCompanions = Boolean.parseBoolean(useLearningCompanions);
            String externActPct = servletConfig.getInitParameter(Names.EXTERNAL_ACTIVITY_PERCENTAGE);
            Settings.externalActivityPercentage= Double.parseDouble(externActPct);
            // Flash client must be on same machine but can be served by other than servletEngine
            // (e.g. it is best served by apache)
            Settings.getSurveys(connection); // loads the pre/post Survey URLS
//            AssistmentsHandler.assistmentsLogbackURL = servletConfig.getInitParameter(Names.ASSISTMENTS_LOGBACK_URL);
            String videoURI = servletConfig.getInitParameter(Names.VIDEO_URI);
            Settings.videoURI = ServletUtil.getURIForEnvironment(Settings.isDevelopmentEnv,Settings.host,Settings.port,
                    servletContext.getContextPath(),Settings.webContentPath, videoURI);
            Settings.emoteServletURI = servletConfig.getInitParameter(Names.EMOTE_SERVLET_URI);
            Settings.formalityServletURI = servletConfig.getInitParameter(Names.FORMALITY_SERVLET_URI);
            ServletUtil.startSessionCleanupDemon(connection);

            String host = DbUtil.getHost(connection);
            String emailLogFilename = servletConfig.getInitParameter(Names.EMAIL_LOG_FILENAME);
            if (emailLogFilename != null)
                Settings.emailLogFile = new File(emailLogFilename);
            System.out.println("Db Host: " + host);

            // test hibernate
//            hibernateTest();
            // loads all the Problems from the db into an in-memory cache
            // One troubling thing is that it needs an ExampleSelector and VideoSelector in order to predetermine
            // each Problem's example and video
            // Loads all content into a cache for faster access during runtime
            if (!ProblemMgr.isLoaded())  {
                this.problemMgr = new ProblemMgr(new BaseExampleSelector(), new BaseVideoSelector());
                problemMgr.loadProbs(connection);
                CCContentMgr.getInstance().loadContent(connection);
                LessonMgr.getAllLessons(connection);  // only to check integrity of content so we see errors early

            }
            logger.debug("end init of WoTutorServlet");

        } catch (Exception e) {
            logger.debug("fail init of WoTutorServlet");

            e.printStackTrace();
            throw e;
        }
    }

    private void hibernateTest() {
        Session session = HibernateUtil.getSessionFactory().openSession();

//        session.beginTransaction();
//        TeacherEntity teacher = new TeacherEntity();
//        teacher.setEmail("marshall5862@gmail.com");
//        teacher.setFname("Dave");
//        teacher.setLname("Marshall");
//        teacher.setUserName("marshall62");
//        teacher.setPassword("passpass");
//        session.save(teacher);
        Query q =  session.createQuery("from TeacherEntity where userName= :un");
        q.setParameter("un","marshall62");
        List<TeacherEntity> l = q.list();
        for (TeacherEntity e : l)
            System.out.println(e.getId());
//        session.getTransaction().commit();
    }


    /**
     * Overrides the handleRequest method in BaseServlet.  This handles all requests coming into this servlet.
     *
     * @param servletContext
     * @param conn
     * @param request
     * @param response
     * @param params
     * @param servletOutput
     * @return whether to flush output to the servlet output stream
     */
    protected boolean handleRequest(ServletContext servletContext, Connection conn, HttpServletRequest request,
                                    HttpServletResponse response, ServletParams params, StringBuffer servletOutput) throws Exception {
        try {
            logger.info(">>" + params.toString());
            setHostAndContextPath(this.getServletName(),servletContext,request);
            ServletInfo servletInfo = new ServletInfo(servletContext,conn, request, response, params, servletOutput, hostPath, contextPath, this.getServletName());
//            boolean res = new TutorBrainHandler(servletContext, conn, request, response, params, servletOutput, this.hostPath, this.contextPath).handleRequest();
            boolean res = new TutorBrainHandler(servletInfo).handleRequest();
            if (res)
                logger.info("<<" + servletOutput.toString());
            return res;
        }
        catch (AssistmentsBadInputException e) {
            // sends a 500 error with message that Assistments will need to deal with.
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
            // pretty sure the above puts this on the output stream of the servlet so that we don't need to do anything
            // more to return stuff to caller
            return false;
        }
        catch (Throwable e) {
            logger.info("", e);
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            servletOutput.append("ack=false&message=" + e.getMessage());
            return true;
        }
    }



}
