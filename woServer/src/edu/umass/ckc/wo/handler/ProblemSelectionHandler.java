package edu.umass.ckc.wo.handler;

import edu.umass.ckc.wo.beans.ClassInfo;
import edu.umass.ckc.wo.beans.Classes;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.event.admin.AdminProblemSelectionEvent;
import edu.umass.ckc.wo.event.admin.AdminSelectTopicProblemsEvent;
import edu.umass.ckc.wo.event.admin.AdminActivateProblemsEvent;
import ckc.servlet.servbase.View;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.beans.SATProb;
import edu.umass.ckc.wo.db.DbTopics;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.util.ProbPlayer;
import edu.umass.ckc.wo.cache.ProblemMgr;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 23, 2008
 * Time: 1:55:29 PM
 */
public class ProblemSelectionHandler {
    public static final String JSP = "/teacherTools/problemSelectionTopicsList.jsp";
    public static final String PROBLEMS_JSP = "/teacherTools/selectProblems.jsp";

    public View handleEvent(ServletContext sc, Connection conn, AdminProblemSelectionEvent event, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws SQLException, IOException, ServletException {

        String svr = sc.getServerInfo();
//        String url1 = servletRequest.getRequestURL();

        if (event instanceof AdminSelectTopicProblemsEvent)  {
            int topicId = ((AdminSelectTopicProblemsEvent) event).getTopicId();
            DbProblem probMgr = new DbProblem();
            Topic topic = ProblemMgr.getTopic(topicId);
            List<Problem> problems = probMgr.getProblemsInTopic(conn, topicId);
            // get the problems omitted for this topic
            List<SATProb> satProbs = probMgr.getTopicOmittedProblems(conn,event.getClassId(),problems, topicId);
            ClassInfo[] classes1 = DbClass.getClasses(conn, event.getTeacherId());
            Classes bean1 = new Classes(classes1);
            ClassInfo classInfo = DbClass.getClass(conn,event.getClassId());
            servletRequest.setAttribute("action","AdminProblemSelection");
            servletRequest.setAttribute("problems",satProbs);
            servletRequest.setAttribute("classId",event.getClassId());
            servletRequest.setAttribute("teacherId",event.getTeacherId());
            servletRequest.setAttribute("topicId",topicId);
            servletRequest.setAttribute("bean", bean1);
            servletRequest.setAttribute("classInfo", classInfo);
            String uri = Settings.problemPreviewerPath();
            servletRequest.setAttribute("probPlayerHost", uri);
            servletRequest.setAttribute("html5ProblemURI",Settings.html5ProblemURI );
            servletRequest.setAttribute("topicName",topic.getName());
            servletRequest.setAttribute("standards",topic.getStandards());
            servletRequest.setAttribute("summary",topic.getSummary());
            servletRequest.getRequestDispatcher(PROBLEMS_JSP).forward(servletRequest,servletResponse);
        }
        else if (event instanceof AdminActivateProblemsEvent) {
            int topicId = ((AdminActivateProblemsEvent) event).getTopicId();
            int classId = event.getClassId();
            // DM 4/09   Hopeful bug fix.   CLasses that do not have an alterred topic list
            // will not have a ClassLessonPlan row but this is necessary if problems are being
            // omitted from topics even if the topic sequence remains as the default.
            // So we must check if a ClassLessonPlan exists for this class and if not, build one.

            if (!DbTopics.hasLessonPlan(conn,classId))
                    DbTopics.insertLessonPlanWithDefaultTopicSequence(conn,classId);
            //  The event holds a list of ids of all the problems that are checked as active.   De-activated (unchecked) problems are not sent from the HTML form submission
            String[] activeProblemIds =  ((AdminActivateProblemsEvent) event).getActivatedIds();
            int[] activatedIds = new int[activeProblemIds.length];
            // convert ids to ints
            for (int i=0;i<activeProblemIds.length;i++)
                activatedIds[i] = Integer.parseInt(activeProblemIds[i]);
            ProblemMgr.getAllProblems();
            DbProblem probMgr = new DbProblem();
            List<Problem> problemsInTopic = probMgr.getProblemsInTopic(conn, topicId);
            List<SATProb> satProbs = probMgr.activateTopicProblems(conn, problemsInTopic,activatedIds);
            List<Integer> deactivatedIds = new ArrayList<Integer>();
            ClassInfo[] classes1 = DbClass.getClasses(conn, event.getTeacherId());
            Classes bean1 = new Classes(classes1);
            ClassInfo classInfo = DbClass.getClass(conn,event.getClassId());
            for (SATProb prob:satProbs)
                if (!prob.isActivated())
                    deactivatedIds.add(prob.getId());
            probMgr.updateOmittedProbsList(conn,classId,topicId,deactivatedIds);
            servletRequest.setAttribute("action","AdminProblemSelection");
            servletRequest.setAttribute("problems",satProbs);
            servletRequest.setAttribute("classId",event.getClassId());
            servletRequest.setAttribute("teacherId",event.getTeacherId());
            servletRequest.setAttribute("topicId",topicId);
            servletRequest.setAttribute("bean", bean1);
            servletRequest.setAttribute("classInfo", classInfo);
            String uri = ProbPlayer.getURLToProbPlayer(servletRequest);
            servletRequest.setAttribute("probPlayerHost",uri);
            servletRequest.getRequestDispatcher(PROBLEMS_JSP).forward(servletRequest,servletResponse);
            // TODO fetch all ids of all problems within topic
            // do a setDiff on these with activeProblemIds yielding inactiveProblems
            // The ClassOmittedProblems table holds a list of all the omitted problems for a class.  This will need to have an additional column added for topicId
            // ow we can't remove all the omitted problems for the current topic and then reinsert them based on what is calculated above.
            // N.B. This will have an effect on the Adaptive problem selector so make sure its query still does the right thing to fetch all the ids (it probably should work
            // without modification as long as it doesn't mention the new topicId column)
        }
        else  {
            List<Topic> topics = DbTopics.getClassTopicsSequence(conn,event.getClassId());
            ClassInfo[] classes1 = DbClass.getClasses(conn, event.getTeacherId());
            Classes bean1 = new Classes(classes1);
            ClassInfo classInfo = DbClass.getClass(conn,event.getClassId());
            servletRequest.setAttribute("action","AdminProblemSelection");
            servletRequest.setAttribute("topics",topics);
            servletRequest.setAttribute("classId",event.getClassId());
            servletRequest.setAttribute("teacherId",event.getTeacherId());
            servletRequest.setAttribute("bean",bean1);
            servletRequest.setAttribute("classInfo",classInfo);
            servletRequest.getRequestDispatcher(JSP).forward(servletRequest,servletResponse);
        }
        return null;
    }


}