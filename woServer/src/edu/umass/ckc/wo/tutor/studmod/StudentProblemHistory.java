package edu.umass.ckc.wo.tutor.studmod;

import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbStudentProblemHistory;
import edu.umass.ckc.wo.db.DbTopics;
import edu.umass.ckc.wo.event.tutorhut.BeginProblemEvent;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.state.StudentState;
import edu.umass.ckc.wo.tutormeta.StudentEffort;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: 6/13/12
 * Time: 11:36 AM
 * This is an in-memory representation of a students problem history.   It works with the DbStudentProblemHistory class
 * Note: A problem shown to the student more than once has multiple entries
 */
public class StudentProblemHistory {

    private static Logger logger =   Logger.getLogger(StudentProblemHistory.class);

    private List<StudentProblemData> history;
    private StudentProblemData curProb;

    public StudentProblemHistory() {
        history = new ArrayList<StudentProblemData>();
    }

    public StudentProblemHistory (Connection conn, int studId) throws SQLException {
        history = new ArrayList<StudentProblemData>();
        DbStudentProblemHistory.loadHistory(conn,studId,history);
        if (history.size() > 0)
            curProb = history.get(history.size()-1);
    }

    // maybe for now we don't need the in-memory model since nothing is reasoning about it.   Its just record keeping for the time being, so we'll
    // go direct to the db.

    public StudentProblemData beginProblem (SessionManager smgr, BeginProblemEvent e) throws SQLException {
        StudentState state = smgr.getStudentState();
        long now = System.currentTimeMillis();
        // if student forced a problem + topic we log that topic, o/w we use the topic that the student is actually in
        int topicId = (state.getStudentSelectedTopic() != -1) ? state.getStudentSelectedTopic() : state.getCurTopic();
        // in some situations (e.g. student comes into tutor without being in a topic) we won't have a legal topic.  Thus
        // we need to get a dummy topic to insert into the history so that foreign keys work.    In other situations
        // (MPP) the topic id gets lost and then it sends illegal values which also cause failures.   This will repair the topicId but
        // logs with a warning because this should be fixed.
        if (topicId < 0) {
            logger.error("StudentProblemHistory.beginProblem:  ILLEGAL TOPIC ID " + topicId + " passed from client.   Replacing with dummy value.");
            topicId = DbTopics.getDummyTopic(smgr.getConnection());
            state.setCurTopic(topicId); // puts the student in this dummy topic so future transactions don't have failures.
        }
        String params = "";
        Problem p = ProblemMgr.getProblem(state.getCurProblem());
        if (p != null && p.isParametrized()) {
            params = state.getProblemBinding();
        }
        DbStudentProblemHistory.beginProblem(smgr.getConnection(), e.getSessionId(), smgr.getStudentId(), state.getCurProblem(), topicId,
            now, smgr.getTimeInSession(), now - state.getTutorEntryTime(), state.getCurProblemMode(), params, smgr.getCollaboratingWith());

        curProb = new StudentProblemData(state.getCurProblem(),state.getCurTopic(),e.getSessionId(),
                now,smgr.getTimeInSession(), now-state.getTutorEntryTime(),state.getCurProblemMode());
        addProblem(curProb);
        return curProb;

    }

    // return the effort on the last 3 practice problems
    public StudentEffort getEffort (int thisSessionId) {
        List<StudentProblemData> hist = getReverseHistory();

        String[] effs = new String[] {"?","?","?"} ;
        int i=0;
        for (StudentProblemData d : hist) {
            if (d.isPracticeProblem() && d.getSessId() == thisSessionId)
                effs[i++] = d.getEffort();
            else if (d.isTopicIntro()&& d.getSessId() == thisSessionId)
                effs[i++] = "INTRO";
            else if (d.isExample()&& d.getSessId() == thisSessionId)
                effs[i++] = "EXAMPLE";
            else if (d.isDemo()&& d.getSessId() == thisSessionId)
                effs[i++]= "DEMO";
            if (i== 3) break;

        }
        return new StudentEffort(effs[2],effs[1],effs[0]) ;
    }



    public void endProblem(SessionManager smgr, StudentProblemData curProbData, int topicId) throws Exception {
        Connection conn = smgr.getConnection();
        StudentState state = smgr.getStudentState();
        int studProbHistId = DbStudentProblemHistory.getMostRecentStudentProblemHistoryRecord(conn,smgr.getStudentId());
        int numAttemptsToSolve = (state.isProblemSolved() ? (state.getNumMistakesOnCurProblem() + 1) : 0);
        long now = System.currentTimeMillis();
        String hint = state.getCurHint();
        EffortHeuristic effortComputer = new EffortHeuristic();
       // TODO do not set effort if the current problem is Topic Intro or Example
        String effort = effortComputer.computeEffort(state, numAttemptsToSolve);
        DbStudentProblemHistory.endProblem(smgr.getConnection(),studProbHistId,state.getNumHintsBeforeCorrect(),
                numAttemptsToSolve,state.getTimeToSolve(),
               state.getTimeToFirstHint(),state.getTimeToFirstAttempt(),state.isProblemSolved(), now,
                state.getNumMistakesOnCurProblem(),
                state.getNumHintsGivenOnCurProblem(), state.isSolutionHintGiven(),
                smgr.getStudentModel().getTopicMastery(topicId),
                effort,state.isVideoShown(),state.getProbExamplesShown(),state.isTextReaderUsed());
        curProbData.setNumAttemptsToSolve(numAttemptsToSolve);
        curProbData.setNumHintsBeforeCorrect(state.getNumHintsBeforeCorrect());
        curProbData.setTimeToSolve(state.getTimeToSolve());  // should be zero if problem has not been solved.
        curProbData.setTimeToFirstHint(state.getTimeToFirstHint());
        curProbData.setTimeToFirstAttempt(state.getTimeToFirstAttempt());
        curProbData.setSolved(state.isProblemSolved());
        curProbData.setProblemEndTime(now);
        curProbData.setNumMistakes(state.getNumMistakesOnCurProblem());
        curProbData.setGivenAnswerHint(state.isSolutionHintGiven());
        curProbData.setMastery( smgr.getStudentModel().getTopicMastery(topicId));
        curProbData.setEffort(effort);
        curProbData.setSeenVideo(state.isVideoShown());
        curProbData.setSeenExample(state.isExampleShown());
        curProbData.setUsedTextReader(state.isTextReaderUsed());


    }

    public void attempt (SessionManager smgr, boolean isCorrect, long probElapsedTime) throws SQLException {
        int histRecId = DbStudentProblemHistory.getMostRecentStudentProblemHistoryRecord(smgr.getConnection(),smgr.getStudentId());
        if (curProb.getNumAttemptsToSolve() == 0) {
            curProb.setTimeToFirstAttempt(probElapsedTime);
            DbStudentProblemHistory.updateVar(histRecId, smgr.getConnection(),DbStudentProblemHistory.TIME_TO_FIRST_ATTEMPT,probElapsedTime) ;

        }

        if (!isCorrect) {
            curProb.setNumMistakes(curProb.getNumMistakes()+1);
            DbStudentProblemHistory.updateVar(histRecId, smgr.getConnection(),DbStudentProblemHistory.NUM_MISTAKES,curProb.getNumMistakes()) ;
        }
        else {
            curProb.setNumAttemptsToSolve(curProb.getNumAttemptsToSolve()+1);
            DbStudentProblemHistory.updateVar(histRecId, smgr.getConnection(),DbStudentProblemHistory.NUM_ATTEMPTS_TO_SOLVE,curProb.getNumMistakes()) ;
            DbStudentProblemHistory.updateVar(histRecId, smgr.getConnection(),DbStudentProblemHistory.IS_SOLVED,1) ;
            DbStudentProblemHistory.updateVar(histRecId, smgr.getConnection(),DbStudentProblemHistory.TIME_TO_SOLVE,probElapsedTime) ;

        }
    }

    public void hint (SessionManager smgr, long probElapsedTime, boolean givesSolution) throws SQLException {
        int histRecId = DbStudentProblemHistory.getMostRecentStudentProblemHistoryRecord(smgr.getConnection(),smgr.getStudentId());

        if (curProb.getNumHints() == 0) {
            curProb.setTimeToFirstHint(probElapsedTime);
            DbStudentProblemHistory.updateVar(histRecId, smgr.getConnection(),DbStudentProblemHistory.TIME_TO_FIRST_HINT,probElapsedTime) ;
        }
        curProb.setGivenAnswerHint(givesSolution);
        DbStudentProblemHistory.updateVar(histRecId, smgr.getConnection(),DbStudentProblemHistory.SOLUTION_HINT_GIVEN,curProb.getGivenAnswerHint()) ;

        curProb.setNumHintsBeforeCorrect(curProb.getNumHintsBeforeCorrect() + 1);
        curProb.setNumHints(curProb.getNumHints() + 1);
        DbStudentProblemHistory.updateVar(histRecId, smgr.getConnection(),DbStudentProblemHistory.NUM_HINTS_BEFORE_SOLVE,curProb.getNumHintsBeforeCorrect()) ;
        DbStudentProblemHistory.updateVar(histRecId, smgr.getConnection(),DbStudentProblemHistory.NUM_HINTS,curProb.getNumHints()) ;


    }

    public void updateEmotions (SessionManager smgr, String emotion, int level) throws SQLException {
        int studProbHistId = DbStudentProblemHistory.getMostRecentStudentProblemHistoryRecord(smgr.getConnection(),smgr.getStudentId());

        DbStudentProblemHistory.updateEmotions(smgr.getConnection(),studProbHistId,emotion,level);
    }

    public void addProblem(StudentProblemData d) {
        history.add(d);
    }

    public StudentProblemData getCurProblem () {
        return curProb;
    }

    public List<StudentProblemData> getHistory() {
        return history;
    }

    public List<StudentProblemData> getReverseHistory() {
        List<StudentProblemData> h= (List<StudentProblemData>) ((ArrayList<StudentProblemData>) history).clone();
        Collections.reverse(h);
        return h;
    }

    public int getTimesEncountered(int probId) {
        int timesEncountered = 0;
        for (StudentProblemData d: history) {
            if (d.getProbId() == probId) {
               timesEncountered++;
            }
        }
        return timesEncountered;
    }

    // get problems in the history that are in the topic and were ended within N days of the given time.
    public List<StudentProblemData> getTopicHistory(int topicID, int withinNDays) {
        List<StudentProblemData> result = new ArrayList<StudentProblemData>();
        long now = System.currentTimeMillis();
        for (StudentProblemData d: history) {
            long probEndTime=d.getProblemEndTime();
            int daysDiff = (int) (now - probEndTime) / (1000 * 60 * 60 * 24);
            if (d.getTopicId() == topicID && daysDiff <= withinNDays)
                result.add(d);
        }
        return result;
    }


    // get problems in the history that are sorted by students most recent encounter (this means a problem seen more than
    // once will have the most recent encounter earlier in the list.
    public List<StudentProblemData> getTopicHistoryMostRecentEncounters (int topicID) {
        List<StudentProblemData> result = getTopicHistory(topicID);
        // sort so that list is in descending order based on endTime
        Collections.sort(result,new Comparator<StudentProblemData>() {
            public int compare(StudentProblemData o1, StudentProblemData o2) {
                return (o1.getProblemEndTime() < o2.getProblemEndTime()) ? 1 : (o1.getProblemEndTime() == o2.getProblemEndTime() ? 0 : -1);
            }
        });
        return result;

    }

        // get problems in the history that are in the topic
        public List<StudentProblemData> getTopicHistory(int topicID) {
            List<StudentProblemData> result = new ArrayList<StudentProblemData>();
            long now = System.currentTimeMillis();
            for (StudentProblemData d: history) {
                if (d.getTopicId() == topicID )
                    result.add(d);
            }
            return result;
        }

    public List<String> getTopicProblemsSeen(int topicId) {
        List<StudentProblemData> topicHist = getTopicHistory(topicId);
        List<String> ids = new ArrayList<String>();
        for (StudentProblemData d: topicHist) {
            if (d.isPracticeProblem())
                ids.add(Integer.toString(d.getProbId()));
        }
        return ids;
    }

    public List<String> getTopicProblemsSolved(int topicId) {
        List<StudentProblemData> topicHist = getTopicHistory(topicId);
        List<String> ids = new ArrayList<String>();
        for (StudentProblemData d: topicHist) {
            if (d.isPracticeProblem() && d.isSolved())
                ids.add(Integer.toString(d.getProbId()));
        }
        return ids;
    }

    public List<String> getTopicProblemsSolvedOnFirstAttempt(int topicId) {
        List<StudentProblemData> topicHist = getTopicHistory(topicId);
        List<String> ids = new ArrayList<String>();
        for (StudentProblemData d: topicHist) {
            if (d.isPracticeProblem() && d.getNumAttemptsToSolve()==1)
                ids.add(Integer.toString(d.getProbId()));
        }
        return ids;
    }

    public int getNumPracticeProbsSeenInTopicAcrossSessions(int topicID) {
        List<String> probs = getTopicProblemsSeen(topicID);
        if (probs != null)
            return probs.size();
        else return 0;
    }
}
