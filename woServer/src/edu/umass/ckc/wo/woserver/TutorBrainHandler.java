package edu.umass.ckc.wo.woserver;


import edu.umass.ckc.wo.assistments.AssistmentsHandler;
import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.db.*;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.event.*;
import ckc.servlet.servbase.UserException;
import edu.umass.ckc.wo.handler.*;
import ckc.servlet.servbase.ServletEvent;
import ckc.servlet.servbase.View;
import edu.umass.ckc.wo.html.tutor.TutorPage;
import edu.umass.ckc.wo.log.TutorLogger;
import edu.umass.ckc.wo.login.LandingPage;
import edu.umass.ckc.wo.login.LoginAdult_2;
import edu.umass.ckc.wo.login.LoginK12_2;
import edu.umass.ckc.wo.login.LoginParams;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.LearningCompanion;
import edu.umass.ckc.wo.content.Problem;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TutorBrainHandler {
    private TutorBrainEventFactory eventFactory;
    private static Logger logger =   Logger.getLogger(TutorBrainHandler.class);
    private ServletInfo servletInfo;




    public  TutorBrainHandler (ServletInfo info)  {
        this.servletInfo = info;
        eventFactory = new TutorBrainEventFactory();
    }




    private String quickQuery (Connection conn) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select count(*) from administrator";
            stmt = conn.prepareStatement(q);
            rs = stmt.executeQuery();
            int c=0;
            while (rs.next()) {
                c= rs.getInt(1);
            }

            return "num admins: " + c + " db host: " + "dont know";

        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }

    private String slowQuery (Connection conn) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select count(*) from woproperty p,  eventlog e where  e.studId = p.objid";
            stmt = conn.prepareStatement(q);
            rs = stmt.executeQuery();
            int c=0;
            while (rs.next()) {
                c= rs.getInt(1);
            }

            return "num woproperty: " + c + " db host: " + "dont know";

        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }



    // All servlet requests handled through this method
    public boolean handleRequest() throws Throwable {
        ServletEvent e = eventFactory.buildEvent(servletInfo.params, "tutorhut");
        if (e instanceof TutorHomeEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((SessionEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            TutorPage tutorPage = new TutorPage(this.servletInfo,smgr);
            tutorPage.handleRequest((TutorHomeEvent) e);
            return false;
        }
        else if (e instanceof SystemTestLoginEvent) {
            servletInfo.getOutput().append("<systemTest>working</systemTest>");
        }
        else if (e instanceof DbTestEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((DbTestEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            StudentState state = smgr.getStudentState();
            long begin = System.currentTimeMillis();
            for (int j=0;j<5;j++) {
                for (int i=0;i<50;i++)
                    state.setProp(smgr.getStudentId(),"Prop"+i,"Val"+i);
            }
            long end = System.currentTimeMillis();
            servletInfo.getOutput().append("<html><body> Executed 250 setProps Time: " + (end - begin) + "ms </body></html>");
        }
        else if (e instanceof TeachTopicEvent) {

            return new AssistmentsHandler(servletInfo).teachTopic((TeachTopicEvent) e);

        }
        else if (e instanceof GetProblemDataEvent) {

            return new AssistmentsHandler(servletInfo).getProblemData((GetProblemDataEvent) e);

        }
        else if (e instanceof AdventurePSolvedEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((AdventurePSolvedEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            servletInfo.getOutput().append(smgr.adventureProblemSolved((AdventurePSolvedEvent) e));

        }
        else if (e instanceof  EnterTutorEvent) {
            EnterTutorEvent ee = (EnterTutorEvent) e;
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((EnterTutorEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            smgr.getStudentState().newSession(smgr);
            boolean showMPP = smgr.getPedagogicalModel().isShowMPP();
            smgr.getPedagogicalModel().newSession(smgr.getSessionNum());
            new TutorPage(servletInfo,smgr).createTutorPageFromState(ee.getElapsedTime(), 0, -1, -1,
                    "practice", Problem.PRACTICE, Problem.PRACTICE, true, null, null, true, showMPP);
            return false;
        }
        else if (e instanceof HomeEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((HomeEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();

            new DashboardHandler(this.servletInfo.getServletContext(),smgr,smgr.getConnection(),servletInfo.getRequest(),servletInfo.getResponse()).showSplashPage(LandingPage.JSP,false);
            new TutorLogger(smgr).logHomeEvent((HomeEvent) e);
            return false;
        }
        else if (e instanceof NavigationEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((NavigationEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            View v = new NavigationHandler(servletInfo.getServletContext(), smgr, servletInfo.getConn(), servletInfo.getRequest(), servletInfo.getResponse()).handleRequest((NavigationEvent) e);
            if (v == null)
               return false;  // if a jsp is used we don't want to have the caller write to output
            else servletInfo.getOutput().append(v.getView());
        }
        // DM 2/09 These are new events that are based on characters or various drawing tools

        // system 1 has the CharacterHandler process this event.   System 2 uses the learning companion within the pedagogy
        // to process it (it gets passed to the TutorHutHandler below)

        else if (e instanceof GetTutorHutQuestionAnswerEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((GetTutorHutQuestionAnswerEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();

            Problem p = new DbProblem().getProblem(servletInfo.getConn(),smgr.getStudentState().getCurProblem());
            servletInfo.getOutput().append("&correctAnswer="+ p.getAnswer());
        }
        else if (e instanceof SplashPageEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((SplashPageEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            new MyProgressHandler(servletInfo.getServletContext(),smgr,smgr.getConnection(),servletInfo.getRequest(),servletInfo.getResponse()).handleRequest((SplashPageEvent) e);
        }

        else if (e instanceof ShowProgressEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((ShowProgressEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            new MyProgressHandler(servletInfo.getServletContext(),smgr,smgr.getConnection(),servletInfo.getRequest(),servletInfo.getResponse()).handleRequest((ShowProgressEvent) e);
        }
        // This event handling was in Pedagogical Model but I moved it here since nothing really happens except logging
        else if (e instanceof MPPTopicEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((MPPTopicEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();

            // If its the hybrid tutor we want to generate a new TutorPage and plug it with the selected response (a problem?)
            if (Settings.useHybridTutor)  {
                new MPPTutorHandler(servletInfo, smgr).handleRequest((MPPTopicEvent) e);  // may add stuff to event for logging

                return false;  // indicates to caller we forwarded to JSP
            }
            else servletInfo.getOutput().append(new Response().getView()); // old system just wants ack=true
        }
        else if (e instanceof TopicDetailEvent || e instanceof SaveCommentEvent) {
            SessionEvent ee = (SessionEvent) e;
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((SessionEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            new MyProgressHandler(servletInfo.getServletContext(),smgr,servletInfo.getConn(),servletInfo.getRequest(),servletInfo.getResponse()).handleRequest(ee);
            return false;
        }
        else if (e instanceof GetProblemListForTesterEvent) {
            List<Problem> probs = ProblemMgr.getAllProblems();
            RequestDispatcher disp=null;

            String jsp = "selectProblemDialog.jsp";
            disp = servletInfo.getRequest().getRequestDispatcher(jsp);
            servletInfo.getRequest().setAttribute("problems",probs);
            disp.forward(servletInfo.getRequest(),servletInfo.getResponse());
            return false;

        }
        // N.B.  The above new events have to come before this one because they all inherit from
        // TutorHutEvent and we don't want TutorHutEventHandler processing their events
        else if (e instanceof TutorHutEvent) {
            ((TutorHutEvent) e).setServletResponse(servletInfo.getResponse());  // This is because processing might result in forward to a JSP
            ((TutorHutEvent) e).setServletRequest(servletInfo.getRequest());  // This is because processing might result in forward to a JSP
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((TutorHutEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            View v = new TutorHutEventHandler(smgr).handleRequest((TutorHutEvent) e);
            // if we get a null View, this means we forwarded to a JSP so we return false so the server doesn't flush output
            if (v == null)
                return false;
            servletInfo.getOutput().append(v.getView());
        }

        // these are parts of the system (e.g. pretests that still rely on StudentActionEvents or EndActivityEvents)

        else if (e instanceof EndActivityEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((EndActivityEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            View v = new StudentActionHandler(smgr,servletInfo.getConn()).handleRequest((EndActivityEvent) e);
            servletInfo.getOutput().append(v.getView());
        }

        else if (e instanceof StudentActionEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((StudentActionEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            smgr.getStudentState().setProbElapsedTime(((StudentActionEvent) e).getProbElapsed()) ;
            View v = new StudentActionHandler(smgr,servletInfo.getConn()).handleRequest((StudentActionEvent) e);
            servletInfo.getOutput().append(v.getView());

        }

        else if (e instanceof SystemTestLoginEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn());
            servletInfo.getOutput().append(smgr.loginSystemTester((SystemTestLoginEvent) e));

        }

        else if (e instanceof KillSessionsEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((KillSessionsEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            // if user wishes not to login and kill other sessions, killAll will be true.  We then
            // kill the temporary session that was set up to allow the this transaction
            if (((KillSessionsEvent) e).isKillAll()) {
                smgr.inactivateTempUserSessions(((KillSessionsEvent) e).getSessionId());
                servletInfo.getOutput().append(smgr.getLoginView(null, null, null,-1,-1, null));
            }
            // the user wishes to force login and kill other sessions beside this one.
            else {
                smgr.inactivateUserSessions();
                LearningCompanion lc = null;
                lc = smgr.getPedagogicalModel().getLearningCompanion();
                servletInfo.getOutput().append(smgr.getLoginView(SessionManager.LOGIN_USER_PASS,NavigationHandler.TRUE,null,
                        smgr.getSessionNum(),smgr.getStudentId(), lc));
            }
            // killsessions is similar to login except it first kills other sessions
            // It then returns acknowledgement + sessionNumber

        }
        else if (e instanceof GetClassesEvent) {
            servletInfo.getOutput().append(new GetClassHandler().handleRequest(servletInfo.getConn()));
        }
        else if (e instanceof LogoutEvent) {
            String ipAddr = servletInfo.getRequest().getRemoteAddr();
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((LogoutEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            servletInfo.getOutput().append(smgr.logoutStudent((LogoutEvent) e, ipAddr));
            smgr.getStudentState().clearTutorHutState();
            String clientType = DbSession.getClientType(servletInfo.getConn(), smgr.getSessionNum());
            RequestDispatcher disp=null;

            String loginJSP = clientType.equals(LoginParams.ADULT) ? LoginAdult_2.LOGIN_JSP : LoginK12_2.LOGIN_JSP;
            disp = servletInfo.getRequest().getRequestDispatcher(loginJSP);
            disp.forward(servletInfo.getRequest(),servletInfo.getResponse());
            logger.info("<< JSP: " + loginJSP);
            return false;
        } else if (e instanceof SetMFRResultEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((SetMFRResultEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            // want to log a transaction for this event.  Need some new values for that
            // One problem is that the server doesn't have problem Ids for each problem.
            // want to update the MFRScore table
            View v = new MFRHandler().handleEvent(servletInfo.getConn(), (SetMFRResultEvent) e, smgr.getStudentId(), smgr.getSessionNum());
            servletInfo.getOutput().append(v.getView());
        } else if (e instanceof SetMRResultEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((SetMRResultEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            View v = new MRHandler().handleEvent(servletInfo.getConn(), (SetMRResultEvent) e, smgr.getStudentId(), smgr.getSessionNum());
            servletInfo.getOutput().append(v.getView());
        } else if (e instanceof SetPrePostResultEvent) {
            SessionManager smgr = new SessionManager(servletInfo.getConn(),((SetPrePostResultEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
            View v = new PrePostHandler().handleEvent(servletInfo.getConn(), (SetPrePostResultEvent) e, smgr.getStudentId(), smgr.getSessionNum());
            servletInfo.getOutput().append(v.getView());
        }

        else if (e instanceof CleanOutSystemTestDataEvent) {
           SessionManager smgr = new SessionManager(servletInfo.getConn(),((CleanOutSystemTestDataEvent) e).getSessionId(), servletInfo.getHostPath(), servletInfo.getContextPath()).buildExistingSession();
           smgr.removeTestSessionData();
            servletInfo.getOutput().append("All data removed for session data removed.");
        }

        else
            throw new UserException("Unknown Event");
        return true;
    }






    public String getTopicStartAnchor(String client, int sessId,String lc, int topic ) {
        String url = Settings.flashClientPath + client;

        String args = "?sessnum="+sessId+"&learningHutChoice=true&elapsedTime=0&mode=teachTopic" + ((lc !=null) ? ("&learningCompanion="+lc) : "") +"&topicId="+topic; //"&problemIdString='+problemId;
        System.out.println("URL TO call flash is " + (url+args));
        return "<a href=\"" +(url+args)+ "\">here</a>";
    }
}