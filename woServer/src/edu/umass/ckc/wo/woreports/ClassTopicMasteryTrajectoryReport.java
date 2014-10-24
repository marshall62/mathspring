package edu.umass.ckc.wo.woreports;

import edu.umass.ckc.wo.event.admin.AdminViewReportEvent;
import edu.umass.ckc.wo.beans.ClassInfo;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.db.DbTopics;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.woreports.js.JSFile;
import edu.umass.ckc.wo.woreports.js.JSFunction;
import edu.umass.ckc.wo.tutor.studmod.StudentModelMasteryHeuristic;
import edu.umass.ckc.wo.tutor.studmod.RawMasteryHeuristic;
import edu.umass.ckc.wo.tutor.studmod.BaseStudentModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import org.jCharts.chartData.interfaces.IAxisDataSeries;
import org.jCharts.chartData.DataSeries;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Nov 11, 2011
 * Time: 12:58:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassTopicMasteryTrajectoryReport extends TopicTrajectoryReport {

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
    private int numPracticeProbsThisTopic;
    private int exampleProbId;
    private int practiceProbId ;

    List<Double> avgProcessMasteryHistory;
    List<Double> avgAnswerMasteryHistory;

    // this is where data is collected as we walk the event log.   We store each problem seen and the mastery level at the end of that problem.
    class StudentMasteryHistory {
        int studId;
        List<Integer> probIds;
        List<Double> masteryHistory;
        List<Double> rawMasteryHistory;

        StudentMasteryHistory() {
            probIds = new ArrayList<Integer>();
            masteryHistory = new ArrayList<Double>();
            rawMasteryHistory = new ArrayList<Double>();
        }
    }

    private List<StudentMasteryHistory> classData;


    public void createReport(Connection conn, int classId, AdminViewReportEvent e, HttpServletRequest req) throws Exception {
        this.classId = classId;
        classData = new ArrayList<StudentMasteryHistory>();
//        probIds = new ArrayList<Integer>();
//        masteryHistory = new ArrayList<Double>();
//        rawMasteryHistory = new ArrayList<Double>();

        ClassInfo cl = DbClass.getClass(conn, classId);

        String topicX = e.getExtraParam();
        this.topicId = Integer.parseInt(topicX);
        topicName = DbTopics.getTopicName(conn, topicId);
        User u = DbUser.getStudent(conn, studId);
        String className = getClassName(cl);
//        this.src.append(generateHeader2("Topic Mastery history for student - " + u.getUname() + " in class " + className ));
        this.src.append(generateHeaderWithJS("Topic Mastery history for class " + className,
                new JSFile[]{}, new JSFunction[]{JSFunction.NAVIGATION_PULLDOWN_MENU}));
        this.src.append("<h3>Topic Mastery history for class " + className + "</h3>\n");
        addNavLinks(classId, cl.getTeachid());
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select id from student where classId=? and trialUser=0";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,classId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int studId= rs.getInt(1);
                StudentMasteryHistory smh = new StudentMasteryHistory();
                classData.add(smh);
                collectStudentMasteryHistory(conn, studId, this.topicId, req, smh);
            }
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }

        this.src.append("<p><img src=\"LineGraphServlet\">"); // inserts an image that calls BarChartServlet which gens a jpeg from data in HttpSession
        computeClassMasteries(classData);
        saveLineChartDataInSession(req);
//        insertProbLister(req, conn, probIds);


    }




    // process the event and save the topicMastery updates whenever and endProblem is hit.
    private void processProblem(Connection conn, int studId, String username, int sessNum, int probId, int topicId, String action,
                                ResultSet rs, HttpServletRequest req, int evId, ClassTopicMasteryTrajectoryReport.StudentMasteryHistory smh) throws Exception {

        boolean isExample = this.isExampleProblem(conn,evId,sessNum, probId);
        boolean isPractice = this.isPracticeProblem(conn,evId,sessNum, probId);

        // We've just hit a different problem.  Process the last one (if this isn't the first prob in the session)
        if (action.equalsIgnoreCase("beginProblem")) {
            if (isExample)
                exampleProbId = probId;

            else if (isPractice)  {
                practiceProbId = probId ;
                numPracticeProbsThisTopic ++ ;
            }

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
        else if (action.equalsIgnoreCase("attempt") && !isExample) {
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
        } else if (action.toLowerCase().startsWith("hint") && !isExample) {
            if (timeToHint == 0)
                timeToHint = Integer.parseInt(rs.getString("probElapsed"));

            if (numHints < 7)   // A threshold just in case students make hectic choose_a clicks
                numHints++;
        } else if (action.equalsIgnoreCase("endProblem") && !isExample) {
            endProblem = true;
            // update the topic Mastery based on whats happened in this problem.
            topicMasteryTracker.updateMastery(probId, numHints, solved, incAttempts, timeToFirstAttempt, numPracticeProbsThisTopic, "practice");   //Check this problem mode
            rawMasteryTracker.updateMastery(probId, numHints, solved, incAttempts, timeToFirstAttempt, numPracticeProbsThisTopic, "practice");  //Check this problem mode
            smh.probIds.add(probId);
            smh.masteryHistory.add(topicMasteryTracker.getMastery());
            smh.rawMasteryHistory.add(rawMasteryTracker.getMastery());
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
     * @param smh
     */
    private void collectStudentMasteryHistory(Connection conn, int studId, int topicId, HttpServletRequest req,
                                              ClassTopicMasteryTrajectoryReport.StudentMasteryHistory smh) throws Exception {
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
                    numPracticeProbsThisTopic=this.topicMasteryTracker.getTopicNumPracticeProbs() ;
                    this.rawMasteryTracker.newTopic(topicId);
                    lastTopicId = topicId;
                }
                String action = rs.getString("action");
                String userInput = rs.getString("userInput");
                if (rs.getString("problemId") != null) {
                    probId = Integer.parseInt(rs.getString("problemId"));
                    processProblem(conn, studId, username, sessNum, probId, topicId, action, rs, req, eventId, smh);

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
              if (probId==exampleProbId)
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

    /**
     * Go through all the students mastery histories and compute 2 trajectories that represent the whole class
     * One is answer-oriented-mastery (toms) and the other is the process-oriented-mastery (ivons)
     * @param classData
     */
    private void computeClassMasteries(List<StudentMasteryHistory> classData) {
        avgProcessMasteryHistory = new ArrayList<Double>();  // keeps an avg process mastery for each time slice t
        List<Integer> avgProcessCounters = new ArrayList<Integer>(); // keeps track of how many process-masteries encountered for each time slice t
        avgAnswerMasteryHistory = new ArrayList<Double>();
        List<Integer> avgAnswerCounters = new ArrayList<Integer>();
        int weight = 0;
        for (StudentMasteryHistory h: classData) {
            updateAverages(avgProcessMasteryHistory,h.masteryHistory,weight, avgProcessCounters);
            updateAverages(avgAnswerMasteryHistory,h.rawMasteryHistory,weight, avgAnswerCounters);
            weight++;
        }
    }

    // Update the values in avgMast using the student data.   The avgMast is a weighted average where the value in the avgMast gets weight and
    // the student's value gets weight 1.   The list of counters keeps track of how many masteries at time slice t have been encountered
    private void updateAverages(List<Double> avgMast, List<Double> studentMasteries, int weight, List<Integer> counters) {
        int i=0;
        for (double studMast: studentMasteries) {
            if (counters.size() <= i)
                counters.add(1);
            else counters.set(i,counters.get(i)+1);
            int w = counters.get(i);
            double prevAvg = (counters.get(i) == 1) ? 0.0 : avgMast.get(i);
            double newAvg = (prevAvg * (w-1) + studMast) / w;
            if (w == 1)
                avgMast.add(newAvg);
            else avgMast.set(i,newAvg);
            i++;
        }
    }

    private void saveLineChartDataInSession(HttpServletRequest req ) {

        String xAxisTitle = "Time Increments";
        String yAxisTitle = "Topic Mastery";

        String title = "Class Topic Mastery History for Topic " + this.topicId + " " + this.topicName;


        String[] xAxisLabels = new String[avgProcessMasteryHistory.size()];
//        String[] xAxisLabels = new String[10];
//        for (int i=0;i<10;i++)
//            xAxisLabels[i] = "n"+i;

//      This will label the x-axis with time increments
        for (int i = 0; i < avgProcessMasteryHistory.size(); i++)
            xAxisLabels[i] = Integer.toString(i+1);

        IAxisDataSeries dataSeries = new DataSeries(xAxisLabels, xAxisTitle, yAxisTitle, title);
        double[][] data = new double[2][avgProcessMasteryHistory.size()];
        for (int i = 0; i < avgProcessMasteryHistory.size(); i++) {
            data[0][i] = avgProcessMasteryHistory.get(i);
            data[1][i] = avgAnswerMasteryHistory.get(i);
        }

//        double[][] data = new double[][]{{.4, .45, .53, .845, .3, .25, .654, .768, .81, .99}};
//        double[][] data = new double[][]{avgGain_per_category};
        String[] legendLabels = {"process-oriented mastery", "answer-oriented mastery"};
        HttpSession sess = req.getSession();
        sess.setAttribute("dataSeries", dataSeries);
        sess.setAttribute("data", data);
        sess.setAttribute("legendLabels", legendLabels);
    }
}