package edu.umass.ckc.wo.woreports;

//import edu.umass.ckc.wo.event.admin.AdminViewReportEvent;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.event.admin.AdminViewReportEvent;

import edu.umass.ckc.wo.beans.ClassInfo;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.db.DbTopics;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.tutor.studmod.BaseStudentModel;
import edu.umass.ckc.wo.woreports.js.JSFile;
import edu.umass.ckc.wo.woreports.js.JSFunction;
import edu.umass.ckc.wo.tutor.studmod.StudentModelMasteryHeuristic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

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
public class StudentAllTopicsMasteryTrajectoryReport extends TopicTrajectoryReport {

    TopicMasterySimulator topicMasteryTracker;
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
    private boolean endProblem;
    private List<EventLogEntry> userEvents;
    List<Integer> pidList;



    class MasteryPoint {
        MasteryPoint(int probId, double mastery) {
            this.probId = probId;
            this.mastery = mastery;
        }

        int probId;     // not strictly necessary but putting in for verification that things are in synch as the data is gathered
        double mastery;

        public String toString () {
            return String.format("%d:%4.3f",probId,mastery);
        }
    }

    class TopicData {
        int topicId;
        List<MasteryPoint> history;
        int rowIndex;

        TopicData (int tid) {
            topicId = tid;
            history = new ArrayList<MasteryPoint>();
        }
    }

    List<TopicData> data;
    TopicData curTopicData;
    List<String> topicNames;

    // this is where data is collected as we walk the event log.   We store each problem seen and the mastery level at the end of that problem.
//    List<Integer> probIds;
//    List<Double> masteryHistory;


    public void createReport(Connection conn, int classId, AdminViewReportEvent e, HttpServletRequest req) throws Exception {
        this.classId =classId;
        this.studId = e.getStudId();
        this.pidList = new ArrayList<Integer>();
        data = new ArrayList<TopicData>();
        userEvents = new ArrayList<EventLogEntry>();
        ClassInfo cl = DbClass.getClass(conn,classId);
        int studId = e.getStudId();
        User u =DbUser.getStudent(conn,studId);
        String className = getClassName(cl);
        this.src.append(generateHeaderWithJS("Topic Mastery history for student - " + u.getUname() + " in class " + className, new JSFile[]{},
                new JSFunction[] {JSFunction.NAVIGATION_PULLDOWN_MENU}));
        this.src.append("<h3>Topic Mastery history for student " + u.getUname() + "</h3>\n");
        addNavLinks(classId,cl.getTeachid());
        // Similar to StudentTopicMasteryTrajectoryReport I've replaced this with a buffered approach.   See comments in that file for
        // details
        // Old call that processes each row
//        collectStudentMasteryHistory(conn, this.studId, req);
        // new calls that fill buffer and then process it
        collectStudentEventHistory(conn,this.studId);
        processEventHistory(conn);
        getTopicNames(conn);
        saveLineChartDataInSession(req);
        this.src.append("<p><img src=\"LineGraphServlet\">"); // inserts an image that calls BarChartServlet which gens a jpeg from data in HttpSession
        insertProbLister(req,conn,pidList);

    }

    private void getTopicNames(Connection conn) throws SQLException {
        topicNames = new ArrayList<String>();
        for (TopicData d: data) {
            topicNames.add(DbTopics.getTopicName(conn,d.topicId));
        }

    }


    private void collectStudentEventHistory (Connection conn, int studId) throws Exception {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            String q = "SELECT l.id, l.sessNum,l.action,l.isCorrect,l.elapsedTime,l.probElapsed,l.problemId,l.hintId,l.activityName,l.curTopicId FROM eventlog l WHERE l.studid=? and l.problemId != 999 ORDER BY l.sessnum, l.elapsedTime ";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, studId);
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









    // Walk over the buffer (userEvents) and use the masteryTrackers to create a set of records.
    private void processEventHistory (Connection conn) throws SQLException {
        topicMasteryTracker = new TopicMasterySimulator(conn,new StudentModelMasteryHeuristic(conn), BaseStudentModel.INITIAL_TOPIC_MASTERY_ESTIMATE_FL);
        // cycle through the event log for the student and the topic
        int lastSess = -1,lastTopicId = -1;
        isFirstProbOfSess = true;
        for (EventLogEntry entry: userEvents) {
                if (entry.sessNum != lastSess) {
                    isFirstProbOfSess = true;
                    lastSess = entry.sessNum;
                } else isFirstProbOfSess = false;
                topicId = entry.curTopicId;
                // When topic changes let the topic updater know
                if (topicId != -1 && topicId != lastTopicId) {
                    this.topicMasteryTracker.newTopic(topicId);
                    // add a new topic data record to the list
                    curTopicData = new TopicData(topicId);
                    this.data.add(curTopicData);
                    lastTopicId = topicId;
                }
                if (entry.problemId != -1)
                    processEvent(entry);
        }
    }
    


    // This is a revised processor based on using events saved in the userEvents list rather than working directly off
    // rows coming from the query.   The reason I changed it is so that it is easy to move back and forth to find
    // a given problem's type.   There is some event prior to the problem's BeginProblem event that has activityName with
    // a value saying what the mode of the next problem will be.   The problem is that the event log (in past times) hasn't consistently
    // placed value in a row with a certain action type.   So we need to search back for it.   Hence the use of the userEvent list
    // which is kept in order that the events came in and allows this search backward from the point that problem begins.

    // The other major change is that this strictly ignores counting anything other than practice problems because only
    // practice problems affect mastery.


    private void processEvent(EventLogEntry entry) throws SQLException {
        String type = TrajectoryUtil.getProblemType(userEvents,entry.id);
        boolean isExample = type.equals("ExampleProblem");
        boolean isPractice = type.equals("PracticeProblem");
        boolean topicIntro = type.equals(Problem.TOPIC_INTRO_PROB_TYPE);
        // We've just hit a different problem.  Process the last one (if this isn't the first prob in the session)
        if (isPractice && entry.action.equalsIgnoreCase("beginProblem")) {
            this.pidList.add(entry.problemId);
            beginProblemTime = entry.elapsedTime;
            endProblem=false;
            timeToChoose = 0;
            timeToAnswer = 0;
            timeToHint = 0;
            numHints = 0;
            solved = 0;
            incAttempts = 0;
            timeToFirstAttempt=0;
        }
         else if (isPractice && entry.action.equalsIgnoreCase("endProblem")) {

            endProblem=true;
            // update the topic Mastery based on whats happened in this problem.
            topicMasteryTracker.updateMastery(entry.problemId, numHints,solved,incAttempts, timeToFirstAttempt, 0, "practice");   //!!!!Check mode and number of practice problems

            if ( topicMasteryTracker.getMastery() > 0 )
            curTopicData.history.add(new MasteryPoint(entry.problemId,topicMasteryTracker.getMastery()));


        }  // end endProblem
        else if (isPractice && entry.action.equalsIgnoreCase("attempt")) {
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
                              ResultSet rs, HttpServletRequest req) throws Exception {


        // We've just hit a different problem.  Process the last one (if this isn't the first prob in the session)
        if (action.equalsIgnoreCase("beginProblem")) {

            this.pidList.add(probId);
            beginProblemTime = rs.getLong("elapsedTime");
            endProblem=false;
            timeToChoose = 0;
            timeToAnswer = 0;
            timeToHint = 0;
            numHints = 0;
            solved = 0;
            incAttempts = 0;
            timeToFirstAttempt=0;

        }
        // don't do anything with events after endProblem and before next beginProblem
        if (endProblem)
            ;
        else if (action.equalsIgnoreCase("attempt")) {
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
        } else if (action.toLowerCase().startsWith("hint")) {
            if (timeToHint == 0)
                timeToHint = Integer.parseInt(rs.getString("probElapsed"));

            if (numHints < 7)   // A threshold just in case students make hectic choose_a clicks
                numHints++;
        } else if (action.equalsIgnoreCase("endProblem")) {
            endProblem=true;
            // update the topic Mastery based on whats happened in this problem.
            topicMasteryTracker.updateMastery(probId, numHints,solved,incAttempts, timeToFirstAttempt, 0, "practice");   //!!!!Check mode and number of practice problems

            if ( topicMasteryTracker.getMastery() > 0 )
            curTopicData.history.add(new MasteryPoint(probId,topicMasteryTracker.getMastery()));

        }  // end endProblem
    }





    private void saveLineChartDataInSession(HttpServletRequest req) {

        String xAxisTitle = "Problem Ids";
        String yAxisTitle = "Topic Mastery";

        String title = "Full Topic Mastery History";
        int nProbs=0;
        for (TopicData d: this.data)
            nProbs += d.history.size();
        String[] xAxisLabels = new String[nProbs];
        int nrows = determineRowIndices();
        int i = 0;

//        for (Integer pid: this.pidList) {
//           xAxisLabels[i++] = Integer.toString(pid);
//        }

        for (TopicData d: this.data) {
            for (MasteryPoint p: d.history)
                xAxisLabels[i++] = Integer.toString(p.probId);
        }



        //  I think I can stick in nulls for each label that doesn't need a value as I go through the data.
        // Not that simple.
        // Each topic needs to create a point for each label in the x-axis.   There are problems that repeated many times
        // so we can't just use the pid in the x-axis to find a mastery level in the history.
        // I think it will either be the first one in the history and we can pop it OR it won't be there and then we just use NULL.
        
        Random rand = new Random();
        IAxisDataSeries dataSeries = new DataSeries( xAxisLabels, xAxisTitle, yAxisTitle, title );

        double[][] masteryData = new double[nrows][nProbs];
        int j = 0;  // indexs through PIDs
        for (TopicData d: this.data) {
            int curRowIx = d.rowIndex;
            for (MasteryPoint p: d.history) {
                assert(p.probId == this.pidList.get(j));
                // first put in all NaNs for every row at column j
                for (int k=0;k<nrows;k++)
                    masteryData[k][j] = Double.NaN;
                // now set the value in column j to the correct mastery
                masteryData[curRowIx][j] = p.mastery;
                j++;
            }

        }

//        System.out.println("data is");
//        for (int ii=0;ii<nrows;ii++)  {
//            this.src.append("<tr>");
//                for (int jj=0;jj<nProbs;jj++)
//                    this.src.append("<td>" + String.format("%3.3f",data[ii][jj]) + "</td>");
//                this.src.append("</tr>");
//        }
//        this.src.append("</table>");

//        double[][] data = new double[][]{{.4, .45, .53, .845, .3, .25, .654, .768, .81, .99}};
//        double[][] data = new double[][]{avgGain_per_category};
//        String[] legendLabels = {"topic 10", "topic 1"};  // TODO compute list of labels
        String[] legendLabels = new String[topicNames.size()];
        int kk=0;
        for (String n: topicNames)
            legendLabels[kk++] = n;   
        HttpSession sess = req.getSession();
        sess.setAttribute("dataSeries",dataSeries);
        sess.setAttribute("data",masteryData);
        sess.setAttribute("legendLabels",legendLabels);
    }

    // for each TopicData store a rowIndex which is the row in the table that will represent this topic in the output
    // graph that is sent to the LineGraphServlet. Returns the number of rows
    private int determineRowIndices() {
        int count=0;
        List<Integer[]> alist = new ArrayList<Integer[]>();
        int ix;
        for (TopicData d: this.data) {
            if ( (ix = getIxinAlist(d.topicId, alist)) == -1) {
                d.rowIndex = count++;
                alist.add(new Integer[] {d.topicId,count-1});
            }
            else d.rowIndex = ix;

        }
        return count;
    }

    // the alist stores pairs of ints <topicId, rowIndex>
    // Return the rowIndex of the given topicId or -1
    private int getIxinAlist(int topicId, List<Integer[]> alist) {
        for (Integer[] pair: alist) {
            if (pair[0] == topicId)
                return pair[1];

        }
        return -1;
    }
}