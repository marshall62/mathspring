package edu.umass.ckc.wo.tutor.pedModel;

import ckc.servlet.servbase.UserException;
import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbTopics;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.model.TopicModel;
import edu.umass.ckc.wo.tutor.probSel.TopicModelParameters;
import edu.umass.ckc.wo.tutor.studmod.StudentProblemData;
import edu.umass.ckc.wo.tutor.studmod.StudentProblemHistory;
import edu.umass.ckc.wo.tutormeta.TopicSelector;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 1/24/14
 * Time: 2:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class TopicSelectorImpl implements TopicSelector {


    private static Logger logger = Logger.getLogger(TopicSelectorImpl.class);
    public static final int SOLVE_RECENCY_DAYS = 4;
    public static final int EXAMPLE_RECENCY_DAYS = 4;
    public static final int SOLVE_RECENCY_MS = SOLVE_RECENCY_DAYS * 24 * 60 * 60 * 1000;
    public static final int EXAMPLE_RECENCY_MS = EXAMPLE_RECENCY_DAYS * 24 * 60 * 60 * 1000;

    public int classID;
    public List<Problem> probsInTopic;

    private SessionManager smgr;
    private Connection conn;
    private TopicModelParameters tmParameters;
    private TopicModel.difficulty nextProbDesiredDifficulty;
    private PedagogicalModel pedagogicalModel;

    public TopicSelectorImpl() {
    }

    public TopicSelectorImpl(SessionManager smgr, TopicModelParameters tmParams) throws SQLException {
        this.smgr = smgr;
        this.conn = smgr.getConnection();
        this.pedagogicalModel=smgr.getPedagogicalModel();
        this.tmParameters = tmParams;
        this.classID = smgr.getClassID();  // get either the default class (with default lesson plan) or the actual class (with a custom plan)

    }




    private List<StudentProblemData> getHistoryProblemsInTopic(SessionManager smgr, int topicId) {
        StudentProblemHistory studentProblemHistory = smgr.getStudentModel().getStudentProblemHistory();
        return studentProblemHistory.getTopicHistoryMostRecentEncounters(topicId);
    }





    /**
     * Returns a list of problem Ids for the given topic.   Only problems active for the class and that haven't been solved (or examples) are returned.
     *
     * @param theTopicId
     * @param includeTestProblems
     * @return
     * @throws Exception
     */
    public List<Integer> getUnsolvedProblems(int theTopicId, int classId, boolean includeTestProblems) throws Exception {

        // studentID and classID were set in init method.
        List<Integer> topicProbs = getClassTopicProblems(theTopicId, classId, includeTestProblems);
        List<StudentProblemData> probEncountersInTopic = getHistoryProblemsInTopic(smgr, theTopicId);
        List<Integer> recentProbs = pedagogicalModel.getRecentExamplesAndCorrectlySolvedProblems(probEncountersInTopic);
        topicProbs.removeAll(recentProbs);
        return topicProbs;
    }

    private void removeTestProblems(List<Integer> topicProbs) throws SQLException {
        Iterator<Integer> itr = topicProbs.iterator();
        while (itr.hasNext()) {
            int pid = itr.next();
            if (ProblemMgr.isTestProb(pid))
                itr.remove();
        }
    }




    // This is a very dumb way of doing this.   We are taking the middle problem from the topics list.  This problem
    // is then wasted and it may not serve as a good example that represents what this topic is about.
    // Better alternative:   the topic has a defined "demo"  which is hard-wired and is custom built to represent
    // the topic and is built in HTML5 so that it can play without clicking a "next step" button which was imposed
    // by the way Flash problems (being used as demos) work
    public Problem getDemoProblem(int curTopic) throws Exception {
        List<Integer> probs = getClassTopicProblems(curTopic, classID, DbUser.isShowTestControls(conn, smgr.getStudentId()));
        List<StudentProblemData> probEncountersInTopic = getHistoryProblemsInTopic(smgr, curTopic);
        List<Integer> recentProbs = pedagogicalModel.getRecentExamplesAndCorrectlySolvedProblems(probEncountersInTopic);
        probs.removeAll(recentProbs);
        if (probs.size() == 0) {
            return null;
        }
        int ix = (probs.size() - 1) / 2;
        int pid = probs.get((probs.size()-1)/2);

        Problem p = ProblemMgr.getProblem(pid);
        // If the problem doesn't have hints,  its not good to use for a demo
        if (p.getNumHints() > 0) {
            smgr.getStudentState().setCurProblemIndexInTopic((probs.size()-1)/2);
            return p;
        }
        else {   // the middle problem didn't have hints,  pick the next easiest problem until one has hints and then use it
            do {
                pid = probs.get(ix--);
                p = ProblemMgr.getProblem(pid);
                if (p.getNumHints() > 0) {
                    smgr.getStudentState().setCurProblemIndexInTopic((probs.size()-1)/2);
                    return p;
                }
            } while (ix >= 1);
            return null; // can't find one.
        }
    }


    /**
     * Determine if the end of topic has been reached because of a threshold being exceeded or because we can't find a problem of the appropriate
     * difficulty (content failure)
     * @param probElapsedTime
     * @param difficulty
     * @return
     * @throws Exception
     */
    public EndOfTopicInfo isEndOfTopic(long probElapsedTime, TopicModel.difficulty difficulty) throws Exception {
        boolean maxProbsReached=false, maxTimeReached=false, topicMasteryReached=false, contentFailure=false;
        // if maxProbs then we're done
        if (smgr.getStudentState().getTopicNumPracticeProbsSeen() >= tmParameters.getMaxNumberProbs())
            maxProbsReached=true;

        //if maxTime then we're done
        else if (smgr.getStudentState().getTimeInTopic() + probElapsedTime >= tmParameters.getMaxTimeInTopic())
            maxTimeReached=true;

        else if (smgr.getStudentModel().getTopicMastery(smgr.getStudentState().getCurTopic()) >= tmParameters.getTopicMastery()) {
            topicMasteryReached=true;
        }
        EndOfTopicInfo info = isContentFailure(difficulty);
        info.setMaxProbsReached(maxProbsReached);
        info.setMaxTimeReached(maxTimeReached);
        info.setTopicMasteryReached(topicMasteryReached);
        return info;
    }


     // checks two flags which say whether the current topic has an easier problem or a harder problem
    private EndOfTopicInfo isContentFailure (TopicModel.difficulty diff) throws Exception {
        boolean failEasier=false, failHarder=false,failSame=false;

        boolean fail=false;
        if (diff == TopicModel.difficulty.EASIER)
            failEasier= !smgr.getStudentState().curTopicHasEasierProblem();
        else if (diff == TopicModel.difficulty.HARDER)
            failHarder =  !smgr.getStudentState().curTopicHasHarderProblem();
        else if (diff == TopicModel.difficulty.SAME)
            failSame = !(smgr.getStudentState().curTopicHasHarderProblem() || smgr.getStudentState().curTopicHasEasierProblem());
        EndOfTopicInfo info = new EndOfTopicInfo(false,false,failEasier,failHarder, failSame, false);
        return info;

    }


    public void initializeTopic(int curTopic, StudentState state) throws Exception {
        List<Integer> probs = getClassTopicProblems(curTopic, classID, DbUser.isShowTestControls(conn,smgr.getStudentId()));
        List<StudentProblemData> probEncountersInTopic = getHistoryProblemsInTopic(smgr, curTopic);
        List<Integer> recentProbs = pedagogicalModel.getRecentExamplesAndCorrectlySolvedProblems(probEncountersInTopic);
        probs.removeAll(recentProbs);
        // If there is only one problem in the list, this is the example so there will not be content next time
        // It is assumed that the topic has at least one problem, since we just called getNextTopicWithAvailableContent
        if (probs.size() == 1) {
            smgr.getStudentState().setCurTopicHasEasierProblem(false);
            smgr.getStudentState().setCurTopicHasHarderProblem(false);
        }
        // If there are 2 problems in the list, the first is the example so there will only be a harder one next time
        else if (probs.size() == 2)
            smgr.getStudentState().setCurTopicHasEasierProblem(false);
    }


    // Cycle through the topics looking for one that is available (i.e. has problems that haven't been solved,omitted, or given
    // as examples).   If it goes through the whole list of topics and returns to the current topic,  then it returns -1
    public int getNextTopicWithAvailableProblems(Connection conn, int topicID,
                                                 StudentState state) throws Exception {
        int nextTopicId;

        logger.debug("Finding a next topic: getNextTopicWithAvailableProblems");
        //  A sidelined topic may exist if the student left a topic early to go pursue some other topic using the MPP.
        //  When the student completes the topic they pursued,  the system tries to return them to the sidelined topic.
        // Exceptions to this:  A student may leave topic X to go pursue topic Y.  He may then leave topic Y to pursue topic X (through
        // an MPP action).   When topic X is complete,  the sidelined topic is X but it doesn't make sense to now resume work in topic
        // X.  In a case where the last topic a student was in is the same as the sidelined topic, we remove the sidelined topic and
        // let this select the next topic
        //
        nextTopicId = state.getSidelinedTopic();
        state.setSidelinedTopic(-1);
        if (nextTopicId != -1 && nextTopicId != topicID)   {
            logger.debug("getNextTopicWithAvailableProblems  next topic is : " + nextTopicId);
            return nextTopicId;
        }
        // TODO Note:  We should be using the idea of a 'playable' topic as in the commented line below.  This is because
        // we shouldn't be working with topics that have no ready problems which is possible with the method we are using.
        List<Integer> topics = DbClass.getClassLessonTopics(conn, classID);
//        List<Topic> topics = DbTopics.getClassPlayableTopics(conn,classID,smgr.showTestableContent());
        // if -1 is passed as the current topic, then return the first topic in the list as this is presumably a session that hasn't been
        // assigned a topic
        if (topicID == -1)
            return topics.get(0);
        int nextTopicPos = topics.indexOf(topicID);
        do {
            nextTopicPos = nextTopicPos+1;
            if (nextTopicPos >= topics.size()) {
                nextTopicPos = -1;
            }

            // will return -1 if we are at the end of the lesson plan
            if (nextTopicPos != -1)
                nextTopicId = topics.get(nextTopicPos);
            else {// if all prob groups have been used, start at beginning.
                //  Get the topic with the lowest seqNum (NB inactive topics have seqNum=-1 so we must omit those)
                nextTopicId = topics.get(0);
            }
        } while (!hasReadyContent(nextTopicId) && nextTopicId != topicID);

        // if it did a full cycle through all topics,  there are no more problems to show
        if (nextTopicId == topicID)   {
            logger.debug("getNextTopicWithAvailableProblems  next topic is : " + -1);
            return -1;
        }
        else {
            logger.debug("getNextTopicWithAvailableProblems  next topic is : " + nextTopicId);
            return nextTopicId;
        }
    }

    /**
     * Return all the problems in a topic for a given class.   This means removing the classOmittedProblems for this topic.
     * @param topicId
     * @param classId
     * @param includeTestProblems
     * @return
     * @throws SQLException
     */
    public List<Integer> getClassTopicProblems(int topicId, int classId, boolean includeTestProblems) throws Exception {
        // studentID and classID were set in init method.
        List<Integer> topicProbs = ProblemMgr.getTopicProblemIds(topicId);  // operates on a clone so destruction is ok
        // TODO:  Issue:  If all the problems in a topic are marked as TESTABLE and there are no ready problems, the
        // list of problems becomes empty if the includeTestProblems flag is false.   Then we have a bug because
        // we can't find a problem in an empty list.

        if (!includeTestProblems)  {
            removeTestProblems(topicProbs);
            if (topicProbs.size() == 0)
                throw new UserException("Cannot find a problem in topic " + topicId + ".  For a non-test user, the topic must contain READY problems.  Try running the system with isTest=true");

        }
        List<Integer> omittedProbIds = DbClass.getClassOmittedProblems(conn, classID, topicId); // problems omitted for this class
        topicProbs.removeAll(omittedProbIds);
        return topicProbs;
    }

    public boolean hasReadyContent(int topicId) throws Exception {
        List<Integer> topicProbs =getClassTopicProblems(topicId, classID, DbUser.isShowTestControls(conn,smgr.getStudentId()));
        List<StudentProblemData> probEncountersInTopic = getHistoryProblemsInTopic(smgr, topicId);
        List<Integer> recentProbs = pedagogicalModel.getRecentExamplesAndCorrectlySolvedProblems(probEncountersInTopic);
        topicProbs.removeAll(recentProbs);
        return topicProbs.size() > 0;
    }


    @Override
    public void init() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
