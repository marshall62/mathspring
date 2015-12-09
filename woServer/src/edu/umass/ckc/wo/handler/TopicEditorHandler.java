package edu.umass.ckc.wo.handler;

import edu.umass.ckc.wo.beans.ClassInfo;
import edu.umass.ckc.wo.beans.Classes;
import ckc.servlet.servbase.View;
import edu.umass.ckc.wo.content.TopicMgr;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.event.admin.AdminEditTopicsEvent;
import edu.umass.ckc.wo.event.admin.AdminReorderTopicsEvent;
import edu.umass.ckc.wo.event.admin.AdminTopicControlEvent;
import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.db.DbTopics;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.io.IOException;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 17, 2008
 * Time: 9:58:20 AM
 */
public class TopicEditorHandler {

    private String teacherId;
    private HttpSession sess;

    public static final String JSP = "/teacherTools/orderTopics.jsp";
    public static final String SELECT_PEDAGOGIES_JSP = "/teacherTools/selectPedagogies.jsp";
    public static final String CLASS_INFO_JSP = "/teacherTools/classInfo.jsp";

    public TopicEditorHandler () {}






    public View handleEvent(ServletContext sc, Connection conn, AdminEditTopicsEvent e, HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException, ServletException {
        if (e instanceof AdminReorderTopicsEvent) {
            TopicMgr topicMgr = new TopicMgr();
            AdminReorderTopicsEvent ee = (AdminReorderTopicsEvent) e;

            List<Topic> topics=null;
            if (ee.getDirection().equals("up") || ee.getDirection().equals("down"))
                 topics=topicMgr.moveTopic(conn,(AdminReorderTopicsEvent) e);
            else if (ee.getDirection().equals("omit")) {
                int classId = e.getClassId();
                int topicId = ((AdminReorderTopicsEvent) e).getTopicId();
                topics = topicMgr.omitTopic(conn,classId,topicId);
            }
            else if (ee.getDirection().equals("reactivate")) {
                topicMgr.reactivateTopic(conn,(AdminReorderTopicsEvent) e);
                topics = DbTopics.getClassActiveTopics(conn,ee.getClassId());
            }
//            List<Topic> activetopics = DbTopics.getClassActiveTopics(conn,e.getClassId());
            DbProblem.setTopicNumProbsForClass(conn, e.getClassId(), topics);
            List<Topic> inactiveTopics = DbTopics.getClassInactiveTopics(conn, topics);

            ClassInfo classInfo = DbClass.getClass(conn,e.getClassId());
            ClassInfo[] classes = DbClass.getClasses(conn,e.getTeacherId());
            Classes bean = new Classes(classes);
            Integer adminId = (Integer) req.getSession().getAttribute("adminId"); // determine if this is admin session
            req.setAttribute("sideMenu",adminId != null ? "adminSideMenu.jsp" : "teacherSideMenu.jsp"); // set side menu for admin or teacher
            req.setAttribute("isAdmin",adminId != null);
            req.setAttribute("action","AdminEditTopics");
            req.setAttribute("topics",topics);
            req.setAttribute("inactiveTopics",inactiveTopics);
            req.setAttribute("classId",e.getClassId());
            req.setAttribute("teacherId",e.getTeacherId());
            CreateClassHandler.setTeacherName(conn,req, e.getTeacherId());
            req.setAttribute("classInfo", classInfo);
            req.setAttribute("bean", bean);
            PedagogicalModelParameters params = DbClass.getPedagogicalModelParameters(conn, e.getClassId());
            DbClass.setProblemSelectorParameters(conn,e.getClassId(), params);
            req.setAttribute("params",params);
            req.getRequestDispatcher(JSP).forward(req,resp);
        }
        else if (e instanceof AdminTopicControlEvent) {
            AdminTopicControlEvent ee = (AdminTopicControlEvent) e;
            List<Topic> topics = DbTopics.getClassActiveTopics(conn,e.getClassId());
            DbProblem.setTopicNumProbsForClass(conn, e.getClassId(), topics);
            List<Topic> inactiveTopics = DbTopics.getClassInactiveTopics(conn, topics);
            ClassInfo classInfo = DbClass.getClass(conn,e.getClassId());
            ClassInfo[] classes = DbClass.getClasses(conn,e.getTeacherId());
            Classes bean = new Classes(classes);
            Integer adminId = (Integer) req.getSession().getAttribute("adminId"); // determine if this is admin session
            req.setAttribute("sideMenu",adminId != null ? "adminSideMenu.jsp" : "teacherSideMenu.jsp"); // set side menu for admin or teacher
            req.setAttribute("isAdmin",adminId != null);
            req.setAttribute("action","AdminEditTopics");
            req.setAttribute("topics",topics);
            req.setAttribute("classId",e.getClassId());
            req.setAttribute("teacherId",e.getTeacherId());
            CreateClassHandler.setTeacherName(conn,req, e.getTeacherId());
            req.setAttribute("inactiveTopics",inactiveTopics);
            req.setAttribute("classInfo",classInfo);
            req.setAttribute("bean", bean);
            // send to contructor but time in topic is in incorrect units
            PedagogicalModelParameters params = new PedagogicalModelParameters(ee.getMaxTimeInTopic(), ee.getContentFailureThreshold(), ee.getTopicMastery(), ee.getMinNumProbsPerTopic(), ee.getMinTimeInTopic(), ee.getDifficultyRate(), ee.getExternalActivityTimeThreshold(), ee.getMaxNumProbsPerTopic(),
                    true, true);
            params.setMaxTimeInTopicMinutes(ee.getMaxTimeInTopic());  // now pass in as minutes
            params.setMinTimeInTopicMinutes(ee.getMinTimeInTopic());  // now pass in as minutes
            DbClass.setProblemSelectorParameters(conn,e.getClassId(), params);
            req.setAttribute("params",params);
            req.getRequestDispatcher(JSP).forward(req,resp);
        }
        else {
            // fetch a list of topics for the class sorted in the order they will be presented
            List<Topic> topics = DbTopics.getClassActiveTopics(conn,e.getClassId());
            DbProblem.setTopicNumProbsForClass(conn, e.getClassId(), topics);

            List<Topic> inactiveTopics = DbTopics.getClassInactiveTopics(conn, topics);
            ClassInfo[] classes = DbClass.getClasses(conn,e.getTeacherId());
            Classes bean = new Classes(classes);
            ClassInfo classInfo = DbClass.getClass(conn,e.getClassId());
            Integer adminId = (Integer) req.getSession().getAttribute("adminId"); // determine if this is admin session
            req.setAttribute("sideMenu",adminId != null ? "adminSideMenu.jsp" : "teacherSideMenu.jsp"); // set side menu for admin or teacher
            req.setAttribute("isAdmin",adminId != null);
            // forward to the JSP page that allows reordering the list and omitting topics.
            req.setAttribute("action","AdminEditTopics");
            req.setAttribute("bean",bean);
            req.setAttribute("topics",topics);
            req.setAttribute("inactiveTopics",inactiveTopics);
            req.setAttribute("teacherId",e.getTeacherId());
            CreateClassHandler.setTeacherName(conn,req, e.getTeacherId());
            req.setAttribute("classId",e.getClassId());
            req.setAttribute("classInfo",classInfo);
            PedagogicalModelParameters params = DbClass.getPedagogicalModelParameters(conn, e.getClassId());
            // If parameters are not stored for this particular class, a default set should be stored
            // in classconfig table for classId=1.   If nothing there, then use the defaults created
            // in the default PedagogicalModelParameters constructor
            if (params == null) {
                params = DbClass.getPedagogicalModelParameters(conn, 1);
                if (params == null)
                    params = new PedagogicalModelParameters();
            }
            req.setAttribute("params",params);
            req.getRequestDispatcher(JSP).forward(req,resp);
        }

        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
