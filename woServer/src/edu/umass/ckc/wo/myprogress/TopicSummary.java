package edu.umass.ckc.wo.myprogress;

import ckc.servlet.servbase.UserException;
import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbTopics;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.model.LessonModel;
import edu.umass.ckc.wo.tutor.pedModel.TopicSelectorImpl;
import edu.umass.ckc.wo.tutor.probSel.TopicModelParameters;
import edu.umass.ckc.wo.tutor.studmod.StudentProblemData;
import edu.umass.ckc.wo.tutor.studmod.StudentProblemHistory;
import edu.umass.ckc.wo.tutormeta.ProblemSelector;
import edu.umass.ckc.wo.tutormeta.TopicSelector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Copyright (c) University of Massachusetts
 */
public class TopicSummary {

    /*
    *
    *  the variables of a student in the topic
    *
    */
    Topic topic;

    int studId;
    String userName;

    int topicId;
    String topicName;
    int classId;

    int sessionId;
    Connection conn;

    TopicSelector curTopicLoader;


    double masteryThreshold = 0.88;
    double problemDifficultyThreshold = 0.65;


    int classID;
    int currentProblemId;

    int problemsDone;
    int problemsDoneWithEffort;
    int numProbsSolved;
    int totalProblems = 0;
    int masteredTimes;


    private int goBackTill = 5;
    int limitValue = 5;

    private float[] masteryArray = new float[goBackTill];
    private String[] effortArray = new String[goBackTill];


    // output variables
    double mastery = 0;
    boolean effort_disengaged = false;
    String topicState = "";
    String testing = "";


    boolean fastResponse;
    boolean problemDifficult = false;
    boolean hasAvailableContent;
    private String reasonExhausted;

    public TopicSummary(Topic t) {
        topicId = t.getId();
        topicName = t.getName();
    }

    public void loadStudentData(SessionManager smgr) throws Exception {

        studId = smgr.getStudentId();
        classId = smgr.getClassID();
        conn = smgr.getConnection();
        sessionId = smgr.getSessionNum();
        curTopicLoader = new TopicSelectorImpl(smgr, (TopicModelParameters) DbClass.getClassLessonModelParameters(conn, classId));
        ProblemSelector psel = smgr.getPedagogicalModel().getProblemSelector();

//        this.hasAvailableContent = psel.topicHasRemainingContent(smgr, topicId);
        LessonModel lm = smgr.getPedagogicalModel().getLessonModel();
        try {
            this.hasAvailableContent = lm.hasReadyContent(topicId);
        } catch (UserException ue) {
            this.hasAvailableContent = false;
        }
        try {
            List<Integer> l = curTopicLoader.getClassTopicProblems(topicId, classId, DbUser.isShowTestControls(conn, smgr.getStudentId()));
            totalProblems = l.size();
        } catch (UserException ue) {
            totalProblems=0;
        }
        StudentProblemHistory h = smgr.getStudentModel().getStudentProblemHistory();
        List<StudentProblemData> probHist = h.getTopicHistory(topicId);

        this.userName = getStudentName();
        this.problemsDone = getNumProbsSeen(probHist);
        this.problemsDoneWithEffort = getNumProbsWithEffort(probHist);
        this.numProbsSolved = getNumProbsSolved(probHist);

        if (problemsDone == 0  && numProbsSolved == 0) {
            topicState = "topicEmpty";
        } else {
            determineTopicState();
        }
    }

    private int getNumProbsSolved (List<StudentProblemData> hist) {
        int count = 0;
        for (StudentProblemData d: hist) {
            if (d.isSolved())
                count++;
        }
        return count;
    }

    // counts the number of DISTINCT problems seen in this topic (i.e. a problem repeated several times because
    // a student exited without solving is only counted ONCE)
    private int getNumProbsSeen (List<StudentProblemData> hist) {
        int count = 0;
        Set<Integer> probIdSet = new HashSet<Integer>(hist.size());
        for (StudentProblemData d: hist) {
            if (d.getProblemEndTime() > 0 &&  Problem.isPracticeProblem(d.getMode()) )
                probIdSet.add(d.getProbId());
        }
        return probIdSet.size();
    }

    private int getNumProbsWithEffort (List<StudentProblemData> hist) {
        int count = 0;
        for (StudentProblemData d: hist) {
            if (d.getEffort() != null &&
                    (d.getEffort().equals("SOF") || d.getEffort().equals("ATT") || d.getEffort().equals("SHINT")))
                count++;
        }
        return count;
    }
    
    private double getTopicMastery () throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select value, entered from studentTopicMastery where studId=? and topicId=?";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,studId);
            stmt.setInt(2,topicId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                double m= rs.getDouble(1);
                boolean entered= rs.getBoolean(2);
                return m;
            }
            return 0;
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        } 
    }

    // Dovans algorithm for determining the state of the topic given student past problem solving history within the topic
    private void determineTopicState() throws Exception {
        int i;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String s;
        try {

            s = "select effort, mastery from studentproblemhistory where studId=? and topicId=? and problemEndTime>0 order by ProblemBeginTime DESC ";

            stmt = conn.prepareStatement(s);
            stmt.setInt(1, studId);
            stmt.setInt(2, topicId);

            rs = stmt.executeQuery();

            for (i = 0; i < goBackTill; i++) {

                effortArray[i] = "";

                if (rs.next()) {
                    String effort = rs.getString("effort");
                    if (!rs.wasNull())
                        effortArray[i] = effort;

                    masteryArray[i] = rs.getFloat("mastery");
                }


            }
        } finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }


//        mastery = masteryArray[0];
        mastery = getTopicMastery(); // do not calculate a mastery, get it from the table
        try {
            s = "select  mastery from studentproblemhistory where studId=? and topicId=? and problemEndTime>0 ";
            stmt = conn.prepareStatement(s);
            stmt.setInt(1, studId);
            stmt.setInt(2, topicId);

            //here is a problem
            rs = stmt.executeQuery();

            while (rs.next()) {


                if (rs.getFloat("mastery") >= masteryThreshold) masteredTimes++;

            }
        } finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }

        //check if last problem was difficult
        try {
            s = "select problemId from studentproblemhistory where studId=? and topicId=? and problemEndTime>0 order by ProblemBeginTime DESC ";
            stmt = conn.prepareStatement(s);
            stmt.setInt(1, studId);
            stmt.setInt(2, topicId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                currentProblemId = rs.getInt("problemId");
            }

            if (problemDifficult(currentProblemId, conn) == true)
                problemDifficult = true;


            // check  efforts


            if (effortArray[0].equals("SOF")) {

                boolean correctForTheFirstTime = true;
                boolean mastered = false;


                for (i = 1; i < goBackTill; i++) {
                    if (effortArray[i].equals("SOF")) correctForTheFirstTime = false;
                }

                if (correctForTheFirstTime == true) {
                    topicState = "correctForTheFirstTime";
                } else {
                    if (masteryArray[0] >= masteryThreshold) { //mastered
                        System.out.println("mastered" + masteryArray[0]);

                        if (masteryArray[1] >= masteryThreshold) {
                            topicState = "inMastery";
                        } else {

                            if (masteredTimes == 1) topicState = "justMastered";
                            else if (masteredTimes > 1) topicState = "reMastered";
                        }
                    } else {   //correct but not mastered
                        topicState = "inProgress";
                    }

                }

            } else if (effortArray[0].equals("SHINT")) {

                topicState = "SHINT";
            } else if (effortArray[0].equals("ATT")) {

                if (problemDifficult == true) topicState = "ATT_hardProblem";
            } else if (effortArray[0].equals("GUESS") || effortArray[0].equals("BOTTOMOUT")) {


                checkIfProblematicLearningTrend(conn);

            } else if (effortArray[0].equals("GIVEUP")) {

                topicState = "GIVEUP";
                checkIfProblematicLearningTrend(conn);

            } else if (effortArray[0].equals("NOTR")) {
                topicState = "NOTR";
                checkIfProblematicLearningTrend(conn);

            }
        } finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }


    private String getStudentName() throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String q = "select userName from student where id=?";
            ps = conn.prepareStatement(q);
            ps.setInt(1, studId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
            return "";
        } finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
        }
    }


    boolean tooFastResponse() {     // if the student responded too fast in the problem

        return false;

    }


    boolean problemDifficult(int problemID, Connection conn) throws Exception {     // if the problem is difficult
        float difficultyLevel = 0;

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            String q = "select diff_level from overallprobdifficulty where problemID=?";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, problemID);
            rs = stmt.executeQuery();
            if (rs.next()) {
                difficultyLevel = rs.getFloat("diff_level");
            }
            if (difficultyLevel > problemDifficultyThreshold) return true;
            else return false;
        } finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }

    }

    boolean doneTooFewProblems(Connection conn) throws Exception {     // if the problem is difficult


        double averageProblemsDone = 0;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            String q = "select avg(a.problemsDone) from (select studId,count(*) as problemsDone from " +
                    "studentproblemhistory where studId in (select id from student " +
                    "where classID = (SELECT classID FROM student where id=?)) and topicId=? group by studId) as a";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, studId);
            stmt.setInt(2, topicId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                averageProblemsDone = rs.getDouble(1);
                return problemsDone < 5 && averageProblemsDone > 10;
            }
            return false;
        } finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }


    void checkeffortBehavior(Connection conn) throws Exception {

        int GUESS_Count = 0;
        int GIVEUP_Count = 0;
        int BOTTOMOUT_Count = 0;
        int NOTR_Count = 0;

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            String q = "select effort from studentproblemhistory where studId=? and problemEndTime>0 order by ProblemBeginTime DESC limit 0,?";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, studId);
            stmt.setInt(2, limitValue);
            rs = stmt.executeQuery();

            for (int i = 0; i < limitValue; i++) {

                if (rs.next()) {
                    String effort = rs.getString(1);
                    if (effort == null)
                        continue;
                    if (effort.equals("GUESS")) GUESS_Count++;
                    if (effort.equals("BOTTOMOUT")) BOTTOMOUT_Count++;

                    if (effort.equals("GIVEUP")) GIVEUP_Count++;

                    if (effort.equals("NOTR")) NOTR_Count++;

                }

                if (effortArray[0].equals("GUESS") && (GUESS_Count >= 3)) {
                    topicState = "GUESS_helpAvoidance";
                }
                if (effortArray[0].equals("BOTTOMOUT") && (BOTTOMOUT_Count >= 3)) {
                    topicState = "BOTTOMOUT_helpMisuse";
                }
                //  if ((GIVEUP_Count)>=3) {feedbackList.add("givingUp");}
                // if ((NOTR_Count)>=3) {feedbackList.add("NotReading_2");}

                if ((GUESS_Count + BOTTOMOUT_Count + NOTR_Count + GIVEUP_Count) >= 4) {
                    effort_disengaged = true;
                }


            }
        } finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }


    }

    boolean checkIfProblematicLearningTrend(Connection conn) throws Exception {

        if (doneTooFewProblems(conn) == true && mastery < 0.3) {
            topicState = "TooLittlePractice";
        }       // check low mastery
        if (doneTooFewProblems(conn) == false && mastery < 0.3) {
            topicState = "FailedTutoring";
        }

        checkeffortBehavior(conn);

//        checkAffectProfile();
//        checkKnowledgeProfile();


        return false;
    }


    public Topic getTopic() {
        return topic;
    }

    public int getTopicId() {
        return topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getUserName() {
        return userName;
    }

    public double getMastery() {
        return mastery;
    }

    public String getTopicState() {
        return topicState;
    }

    public Boolean getTopicDisengaged() {
        return effort_disengaged;
    }

    public int getProblemsDone() {
        return problemsDone;
    }

    public int getProblemsDoneWithEffort() {
        return problemsDoneWithEffort;
    }

    public int getTotalProblems() {
        return totalProblems;
    }

    public boolean isHasAvailableContent() {
        return hasAvailableContent;
    }

    public void setHasAvailableContent(boolean hasAvailableContent) {
        this.hasAvailableContent = hasAvailableContent;
    }

    public String getReasonExhausted() {
        return reasonExhausted;
    }

    public void setReasonExhausted(String reasonExhausted) {
        this.reasonExhausted = reasonExhausted;
    }

    public static List<TopicSummary> getTopicSummaries(SessionManager smgr) throws Exception {

        List<Topic> topics = DbTopics.getClassActiveTopics(smgr.getConnection(), smgr.getClassID());
        List<TopicSummary> topicSummaries = new ArrayList<TopicSummary>();
        for (Topic t : topics) {
            TopicSummary s = new TopicSummary(t);
            s.loadStudentData(smgr);
            topicSummaries.add(s);
        }
        return topicSummaries;

    }

    public int getNumProbsSolved() {
        return numProbsSolved;
    }
}