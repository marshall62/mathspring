package edu.umass.ckc.wo.woreports;

//import edu.umass.ckc.wo.event.admin.AdminViewReportEvent;
import ckc.servlet.servbase.View;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.event.admin.AdminViewReportEvent;

import edu.umass.ckc.wo.beans.ClassInfo;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.db.DbTopics;
import edu.umass.ckc.wo.handler.ReportHandler;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.woreports.js.JSFile;
import edu.umass.ckc.wo.woreports.js.JSFunction;
import edu.umass.ckc.wo.tutor.studmod.StudentModelMasteryHeuristic;
import edu.umass.ckc.wo.tutor.studmod.RawMasteryHeuristic;
import edu.umass.ckc.wo.tutor.studmod.BaseStudentModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import edu.umass.ckc.wo.woreports.util.EventLogEntry;
import edu.umass.ckc.wo.woreports.util.TrajectoryUtil;
import org.jCharts.chartData.interfaces.IAxisDataSeries;
import org.jCharts.chartData.DataSeries;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Nov 11, 2011
 * Time: 12:58:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class StudentTopicMasteryTrajectoryReport extends TopicTrajectoryReport {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(StudentTopicMasteryTrajectoryReport.class);


    TopicMasterySimulator topicMasteryTracker;  // Ivon's heuristic for progress bars that student sees
    TopicMasterySimulator rawMasteryTracker;    // Tom's heuristic for mastery level that teachers see in report
    int classId;
    int studId;
    int topicId;
    String topicName;

    private boolean isFirstProbOfSess;
    private long beginProblemTime;
    private long timeToChoose;
    private long timeToAnswer;
    private long timeToHint;
    private long timeToFirstAttempt;
    private int numHints;
    private int solved;
    private int incAttempts;
    private int numAttempts;
    private boolean endProblem;
    private int solvedOnAttemptN;
    private int numProbsThisTopic;
    private int exampleProbId;
    private int practiceProbId;
    private List<EventLogEntry> userEvents;

    // this is where data is collected as we walk the event log.   We store each problem seen and the mastery level at the end of that problem.
    List<Integer> probIds;
    List<Double> masteryHistory;
    List<Double> rawMasteryHistory;


    public void createReportOld (Connection conn, int classId, AdminViewReportEvent e, HttpServletRequest req) throws Exception {
        this.classId = classId;
        this.studId = e.getStudId();
        probIds = new ArrayList<Integer>();
        masteryHistory = new ArrayList<Double>();
        rawMasteryHistory = new ArrayList<Double>();
        userEvents =  new ArrayList<EventLogEntry>(); // query results stored here for ease of processing
        ClassInfo cl = DbClass.getClass(conn, classId);
        int studId = e.getStudId();
        String topicX = e.getExtraParam();
        this.topicId = Integer.parseInt(topicX);
        topicName = DbTopics.getTopicName(conn, topicId);
        User u = DbUser.getStudent(conn, studId);
        String className = getClassName(cl);
//        this.src.append(generateHeader2("Topic Mastery history for student - " + u.getUname() + " in class " + className ));
        this.src.append(generateHeaderWithJS("Topic Mastery history for student - " + u.getUname() + " in class " + className,
                new JSFile[]{}, new JSFunction[]{JSFunction.NAVIGATION_PULLDOWN_MENU}));
        this.src.append("<h3>Topic Mastery history for student " + u.getUname() + "</h3>\n");
        addNavLinks(classId, cl.getTeachid());
        // two new methods that read database rows into buffer and then process the buffer instead of processing each row directly
        collectStudentEventHistory(conn,this.studId,this.topicId);
        processEventHistory(conn);
        // old way that processes rows directly
//        collectStudentMasteryHistory(conn, this.studId, this.topicId, req);
        saveLineChartDataInSession(req);
        this.src.append("<p><img src=\"LineGraphServlet\">"); // inserts an image that calls BarChartServlet which gens a jpeg from data in HttpSession

        insertProbLister(req, conn, probIds);


    }
    public View createReport(Connection conn, int classId, AdminViewReportEvent e, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        this.classId = classId;
        this.studId = e.getStudId();
        probIds = new ArrayList<Integer>();
        masteryHistory = new ArrayList<Double>();
        rawMasteryHistory = new ArrayList<Double>();
        userEvents =  new ArrayList<EventLogEntry>(); // query results stored here for ease of processing
        ClassInfo cl = DbClass.getClass(conn, classId);
        int studId = e.getStudId();
        String topicX = e.getExtraParam();
        this.topicId = Integer.parseInt(topicX);
        topicName = DbTopics.getTopicName(conn, topicId);
        User u = DbUser.getStudent(conn, studId);
        String className = getClassName(cl);
        collectStudentEventHistory(conn,this.studId,this.topicId);
        processEventHistory(conn);
        List<Problem> problems = new ArrayList<Problem>(probIds.size());
        for (int pid: probIds) {
            Problem prob = new DbProblem().getProblem(conn,pid);
            if (prob != null)
                problems.add(prob);
        }
        req.setAttribute("xLabels",getLabelSequence(probIds));
        req.setAttribute("rawMasterySequence",getFloatSequence(probIds,rawMasteryHistory));
        req.setAttribute("masterySequence",getFloatSequence(probIds, masteryHistory));
        req.setAttribute("teacherId",cl.getTeachid());
        req.setAttribute("classId",classId);
        req.setAttribute("problems",problems);


        req.getRequestDispatcher(ReportHandler.STUDENT_TOPIC_MASTERY_TRAJECTORY_JSP).forward(req,resp);
        return null;
    }

    private String getLabelSequence(List<Integer>pids) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<pids.size();i++) {
            int pid = pids.get(i);
            // 899 is a dummy problem and should be eliminated from the report
            if (pid != 899)
                sb.append("[" + i + ", '" + pid + "'],");
//            sb.append("[" + i+1 + " , " + d.toString() + "],");
        }
        String s = sb.toString();
        return s.substring(0,s.length()-1);
    }

    private String getFloatSequence(List<Integer>pids, List<Double> floatSeq) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<pids.size();i++) {
            Double d = floatSeq.get(i);
            int pid = pids.get(i);
            sb.append("[" + i + "," + d.toString() + "],");
        }
        String s = sb.toString();
        return s.substring(0,s.length()-1);
    }


    // This is given the id of the entry in the eventHistory.   We then go into the userEvents list (which is sorted in order of
    // event occurrence) and find the row just prior to this event which says what type of problem we are working with.


    // This is a revised processor based on using events saved in the userEvents list rather than working directly off
    // rows coming from the query.   The reason I changed it is so that it is easy to move back and forth to find
    // a given problem's type.   There is some event prior to the problem's BeginProblem event that has activityName with
    // a value saying what the mode of the next problem will be.   The problem is that the event log (in past times) hasn't consistently
    // placed value in a row with a certain action type.   So we need to search back for it.   Hence the use of the userEvent list
    // which is kept in order that the events came in and allows this search backward from the point that problem begins.

    // The other major change is that this strictly ignores counting anything other than practice problems because only
    // practice problems affect mastery.

    // N.B.  Formality problems are not processed because the eventlog has problems with these events not having correct elapsed times
    // and other anomolies.
    private void processEvent(EventLogEntry entry) throws SQLException {
        String type = TrajectoryUtil.getProblemType(userEvents,entry.id);
        boolean isExample = type.equals("ExampleProblem");
        boolean isPractice = type.equals("PracticeProblem");
        boolean topicIntro = type.equals(Problem.TOPIC_INTRO_PROB_TYPE);
        // We've just hit a different problem.  Process the last one (if this isn't the first prob in the session)
        if (isPractice && entry.action.equalsIgnoreCase("beginProblem")) {
            beginProblemTime = entry.elapsedTime;
            endProblem = false;
            timeToChoose = 0;
            timeToAnswer = 0;
            timeToHint = 0;
            numHints = 0;
            solved = 0;
            incAttempts = 0;
            numAttempts = 0;
            timeToFirstAttempt = 0;
            solvedOnAttemptN = 0;
            numProbsThisTopic++;
            this.topicMasteryTracker.incrementNumProblems();
        }
         else if (isPractice && entry.action.equalsIgnoreCase("endProblem")) {
            endProblem = true;
            // update the topic Mastery based on whats happened in this problem.
            topicMasteryTracker.updateMastery(entry.problemId, numHints, solved, incAttempts, timeToFirstAttempt, numProbsThisTopic,"practice");    //check this, not sure it should be practice

            rawMasteryTracker.updateMastery(entry.problemId, numHints, solved, incAttempts, timeToFirstAttempt, numProbsThisTopic, "practice");
            this.probIds.add(entry.problemId);
            this.masteryHistory.add(topicMasteryTracker.getMastery());
            this.rawMasteryHistory.add(rawMasteryTracker.getMastery());
        }  // end endProblem
        else if (isPractice && entry.action.equalsIgnoreCase("attempt")) {
            numAttempts++;
            // The first time the problem is solved,   record the attempt #
            if (entry.isCorrect) {
                solved = 1;
                if (solvedOnAttemptN == 0)
                    solvedOnAttemptN = numAttempts;
            }
            if (entry.isCorrect && (timeToAnswer == 0)) {
                timeToAnswer = entry.probElapsed;

                if (timeToChoose == 0)  //this is the first attempt, and it is correct
                    solved = 1;

            }
            // if its the first attempt
            if (timeToChoose == 0) {
                timeToChoose = entry.probElapsed;
                timeToFirstAttempt = timeToChoose;
            }
            if (!entry.isCorrect && incAttempts < 4)  // A hack because somehow students could get more than 4 incorrect answers
                incAttempts++;
        }
        else if (isPractice && entry.action.toLowerCase().startsWith("hint")  ) {
            if (timeToHint == 0)
                timeToHint = entry.probElapsed;

            if (numHints < 7)   // A threshold just in case students make hectic choose_a clicks
                numHints++;
        }
    }


    // process the event and save the topicMastery updates whenever and endProblem is hit.
    private void processProblem(Connection conn, int studId, String username, int sessNum, int probId, int topicId, String action,
                                ResultSet rs, HttpServletRequest req, int evId) throws java.lang.Exception {

        boolean isExample = this.isExampleProblem(conn,evId,sessNum, probId);
        boolean isPractice = this.isPracticeProblem(conn,evId,sessNum, probId);

        // We've just hit a different problem.  Process the last one (if this isn't the first prob in the session)
        if (action.equalsIgnoreCase("beginProblem")) {
            if (isExample)
                exampleProbId = probId;
            beginProblemTime = rs.getLong("elapsedTime");
            endProblem = false;
            timeToChoose = 0;
            timeToAnswer = 0;
            timeToHint = 0;
            numHints = 0;
            solved = 0;
            incAttempts = 0;
            numAttempts = 0;
            timeToFirstAttempt = 0;
            solvedOnAttemptN = 0;
            numProbsThisTopic++;
            this.topicMasteryTracker.incrementNumProblems();

        }
        // don't do anything with events after endProblem and before next beginProblem
        if (endProblem)
            ;
        else if ((action.equalsIgnoreCase("attempt") ) && !isExample) {
            numAttempts++;
            // The first time the problem is solved,   record the attempt #
            if (Integer.parseInt(rs.getString("isCorrect")) == 1) {
                solved = 1;
                if (solvedOnAttemptN == 0)
                    solvedOnAttemptN = numAttempts;
            }
            if (((Integer.parseInt(rs.getString("isCorrect"))) == 1) &&
                    (timeToAnswer == 0)) {
                timeToAnswer = Integer.parseInt(rs.getString("probElapsed"));

                if (timeToChoose == 0)  //this is the first attempt, and it is correct
                    solved = 1;

            }
            // if its the first attempt
            if (timeToChoose == 0) {
                timeToChoose = Integer.parseInt(rs.getString("probElapsed"));
                timeToFirstAttempt = timeToChoose;
            }
            if ((Integer.parseInt(rs.getString("isCorrect"))) == 0
                    && incAttempts < 4)  // A hack because somehow students could get more than 4 incorrect answers
                incAttempts++;
        } else if ((action.toLowerCase().startsWith("hint"))  && isPractice ) {
            if (timeToHint == 0)
                timeToHint = Integer.parseInt(rs.getString("probElapsed"));

            if (numHints < 7)   // A threshold just in case students make hectic choose_a clicks
                numHints++;
        } else if ((action.equalsIgnoreCase("endProblem") ) && isPractice ) {
            endProblem = true;

            // update the topic Mastery based on whats happened in this problem.
            topicMasteryTracker.updateMastery(probId, numHints, solved, incAttempts, timeToFirstAttempt, numProbsThisTopic,"practice");    //check this, not sure it should be practice
            
            rawMasteryTracker.updateMastery(probId, numHints, solved, incAttempts, timeToFirstAttempt, numProbsThisTopic, "practice");
            this.probIds.add(probId);
            this.masteryHistory.add(topicMasteryTracker.getMastery());
            this.rawMasteryHistory.add(rawMasteryTracker.getMastery());
        }  // end endProblem
    }


    /**
     * Goes through a given students eventlog entries for a given topic and builds a topic mastery history.
     * The data is saved in the servlet session and then an HTML page with an <img href > makes a call to LineGraphServlet to fetch
     * data out of the HttpSession and build a graph which is returned as the image.
     *
     * @param conn
     * @param studId
     * @param topicId
     * @param req     @throws SQLException
     */
    private void collectStudentMasteryHistory(Connection conn, int studId, int topicId, HttpServletRequest req) throws Exception {
        topicMasteryTracker = new TopicMasterySimulator(conn,new StudentModelMasteryHeuristic(conn), BaseStudentModel.INITIAL_TOPIC_MASTERY_ESTIMATE_FL);
        rawMasteryTracker = new TopicMasterySimulator(conn,new RawMasteryHeuristic(conn),0.0);   // initial mastery is 0.0
        // cycle through the event log for the student and the topic
        exampleProbId = -1;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            String q = "SELECT l.*, s.username FROM eventlog l, Student s WHERE l.studid=? and l.studId=s.id and curTopicId=? and l.problemId != 999 ORDER BY l.sessnum, l.elapsedTime ";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, studId);
            stmt.setInt(2, topicId);
            rs = stmt.executeQuery();
            int lastSess = -1;
            int lastProbId = -1;
            int lastTopicId = -1;
            int probId = -1;
            int lastStudId = -1;
            isFirstProbOfSess = true;
            while (rs.next()) {
                int eventId = rs.getInt("id");
                System.out.println("Event ID " + eventId);
                String username = rs.getString("username");
//                int pedId = rs.getInt("pedagogyId");
                int sessNum = rs.getInt("sessnum");

                if (sessNum != lastSess) {
                    isFirstProbOfSess = true;
                    lastSess = sessNum;
                } else isFirstProbOfSess = false;
                int tid = rs.getInt("curtopicId");  // for older events this will be null
                if (rs.wasNull())
                    topicId = -1;
                // When topic changes let the topic updater know
                if (topicId != -1 && topicId != lastTopicId) {
                    this.topicMasteryTracker.newTopic(topicId);
                    numProbsThisTopic= this.topicMasteryTracker.getTopicNumProbs();
                    this.rawMasteryTracker.newTopic(topicId);
                    lastTopicId = topicId;
                }
                String action = rs.getString("action");
                String userInput = rs.getString("userInput");
                if (rs.getString("problemId") != null) {
                    probId = Integer.parseInt(rs.getString("problemId"));
                    processProblem(conn, studId, username, sessNum, probId, topicId, action, rs, req, eventId);

                }
            } //while

        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }

    }


    // Walk over the buffer (userEvents) and use the masteryTrackers to create a set of records.
    private void processEventHistory (Connection conn) throws SQLException {
        topicMasteryTracker = new TopicMasterySimulator(conn,new StudentModelMasteryHeuristic(conn), BaseStudentModel.INITIAL_TOPIC_MASTERY_ESTIMATE_FL);
        rawMasteryTracker = new TopicMasterySimulator(conn,new RawMasteryHeuristic(conn),0.0);   // initial mastery is 0.0
        // cycle through the event log for the student and the topic
        exampleProbId = -1;
        int lastSess = -1,lastTopicId = -1;
        isFirstProbOfSess = true;
        for (EventLogEntry entry: userEvents) {
                logger.debug("Event ID " + entry.id);
                if (entry.sessNum != lastSess) {
                    isFirstProbOfSess = true;
                    lastSess = entry.sessNum;
                } else isFirstProbOfSess = false;
                topicId = entry.curTopicId;
                // When topic changes let the topic updater know
                if (topicId != -1 && topicId != lastTopicId) {
                    this.topicMasteryTracker.newTopic(topicId);
                    numProbsThisTopic= this.topicMasteryTracker.getTopicNumProbs();
                    this.rawMasteryTracker.newTopic(topicId);
                    lastTopicId = topicId;
                }
                if (entry.problemId != -1)
                    processEvent(entry);
        }
    }



    private void collectStudentEventHistory (Connection conn, int studId, int topicId) throws Exception {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            String q = "SELECT l.id, l.sessNum,l.action,l.isCorrect,l.elapsedTime,l.probElapsed,l.problemId,l.hintId,l.activityName,l.curTopicId FROM eventlog l WHERE l.studid=? and curTopicId=? and l.problemId != 999 ORDER BY l.sessnum, l.elapsedTime ";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, studId);
            stmt.setInt(2, topicId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                int sessNum = rs.getInt(2); String action=rs.getString(3); boolean isCorrect=rs.getBoolean(4); long elapsedTime=rs.getLong(5);
                long probElapsed= rs.getLong(6);
                int problemId=rs.getInt(7);
                if (rs.wasNull())
                    problemId=-1;
                int hintId=rs.getInt(8);
                String activityName=rs.getString(9);
                if (rs.wasNull())
                    activityName="";
                int curTopicId=rs.getInt(10);
                if (rs.wasNull())
                    curTopicId=-1;
                EventLogEntry ee = new EventLogEntry(id,sessNum,action,isCorrect,elapsedTime,probElapsed,problemId,hintId,activityName,curTopicId);
                userEvents.add(ee);
            }
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }

    }






    private boolean isExampleProblem(Connection conn, int eventId, int sessNum, int probId) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            if (probId==exampleProbId)
                return true;
            String q = "select activityName, problemId from eventlog where id=(select max(id) from eventLog where id<? and sessNum=? and action='NextProblem')";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,eventId);
            stmt.setInt(2,sessNum);
            rs = stmt.executeQuery();
            if (rs.next()) {
                String s = rs.getString(1);
                if (s.equals("ExampleProblem")) {
                    int pid= rs.getInt(2);
                    return true;
                }

            }
            return false;
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }

    private boolean isPracticeProblem(Connection conn, int eventId, int sessNum, int probId) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            if (probId==practiceProbId)
                return true;
            String q = "select activityName, problemId from eventlog where id=(select max(id) from eventLog where id<? and sessNum=? and action='NextProblem')";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,eventId);
            stmt.setInt(2,sessNum);
            rs = stmt.executeQuery();
            if (rs.next()) {
                String s = rs.getString(1);
                if (s.equals("PracticeProblem")) {
                    int pid= rs.getInt(2);
                    return true;
                }

            }
            return false;
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }

    private void saveLineChartDataInSession(HttpServletRequest req) {

        String xAxisTitle = "Problem Ids";
        String yAxisTitle = "Topic Mastery";

        String title = "Topic Mastery History for Topic " + this.topicId + " " + this.topicName;

        String[] xAxisLabels = new String[this.probIds.size()];
//        String[] xAxisLabels = new String[10];
//        for (int i=0;i<10;i++)
//            xAxisLabels[i] = "n"+i;

//      This will label the x-axis with problem IDs.
        for (int i = 0; i < this.probIds.size(); i++)
            xAxisLabels[i] = Integer.toString(this.probIds.get(i));

        IAxisDataSeries dataSeries = new DataSeries(xAxisLabels, xAxisTitle, yAxisTitle, title);
        double[][] data = new double[2][this.probIds.size()];
//        double[][] rawdata = new double[1][this.probIds.size()];
        for (int i = 0; i < this.probIds.size(); i++) {
            data[0][i] = this.masteryHistory.get(i);
            data[1][i] = this.rawMasteryHistory.get(i);
        }

//        double[][] data = new double[][]{{.4, .45, .53, .845, .3, .25, .654, .768, .81, .99}};
//        double[][] data = new double[][]{avgGain_per_category};
        String[] legendLabels = {"topic mastery", "raw mastery"};
        HttpSession sess = req.getSession();
        sess.setAttribute("dataSeries", dataSeries);
        sess.setAttribute("data", data);
        sess.setAttribute("legendLabels", legendLabels);
    }
}
