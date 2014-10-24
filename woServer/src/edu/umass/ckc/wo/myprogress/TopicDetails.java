package edu.umass.ckc.wo.myprogress;

import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.CCStandard;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.pedModel.TopicSelectorImpl;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.tutormeta.TopicSelector;
import edu.umass.ckc.wo.util.SqlQuery;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


/**
 * Copyright (c) University of Massachusetts
 */
public class TopicDetails {

    /*
    *
    *  the variables of a student in the topic
    *
    */
    Topic topic;

    int studId;

    int topicId;
    String topicName;
    String topicState = "";
    int classId;

    int sessionId;
    String client = null;

    String effortBuffer = "";

    Connection conn;

    TopicSelector curTopicLoader;
    Problem curProblem;

    List<problemDetails> problemDetailsList = new ArrayList<problemDetails>();


    int numOfVariables = 4;


    double masteryThreshold = 0.75;
    double problemDifficultyThreshold = 0.65;


    int classID;
    int currentProblemId;
    int problemId;
    String problemName;

    int problemsDone;
    int totalProblems = 0;
    int masteredTimes;


    private int goBackTill = 5;
    int limitValue = 5;


    // output variables
    double mastery = 0;

    // TODO: If StudentProblemHistory.java is updated dynamically, should probably use that instead of running own query
    public TopicDetails(SessionManager smgr, int topic_id) throws Exception {


        topicId = topic_id;
        studId = smgr.getStudentId();
        classId = smgr.getClassID();

        //topicName=t.getName();

        conn = smgr.getConnection();


        curTopicLoader = new TopicSelectorImpl(smgr, smgr.getPedagogicalModelParameters(), smgr.getPedagogicalModel());
        List<Integer> problemIdList = curTopicLoader.getClassTopicProblems(topicId, classId, smgr.isTestUser());
        List<Problem> problemList = new ArrayList<Problem>();
        for (int id : problemIdList)
            problemList.add(ProblemMgr.getProblem(id));
        totalProblems = problemList.size();


        if (topicState == "topicEmpty") {
        } else {

            for (int i = 0; i < totalProblems; i++) {

                problemId = problemList.get(i).getId();
                problemName = problemList.get(i).getName();
                List<CCStandard> stds = problemList.get(i).getStandards();


                SqlQuery q = new SqlQuery();
                ResultSet rs;


                String s = "select effort, numAttemptsToSolve, numHints from studentProblemHistory where studId=" + studId + " and topicId=" + topicId + " and problemId=" + problemId + " and mode='practice' and problemEndTime>0 ORDER BY  problemBeginTime DESC";

                rs = q.read(conn, s);


                if (rs.next()) {

                    String effort = rs.getString("effort");
                    if (!rs.wasNull())
                        effortBuffer = effort;
                    else effortBuffer = "null";

                    problemDetails newProblemDetails = new problemDetails(problemId, problemName, effortBuffer, rs.getInt("numAttemptsToSolve"), rs.getInt("numHints"), stds);
                    problemDetailsList.add(newProblemDetails);

                } else {


                    problemDetails newProblemDetails = new problemDetails(problemId, problemName, "empty", 0, 0, stds);
                    problemDetailsList.add(newProblemDetails);


                }

            }

        }
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

    public double getMastery() {
        return mastery;
    }


    public List<problemDetails> getProblemDetailsList() {

        return problemDetailsList;
    }


}
