package edu.umass.ckc.wo.state;

import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.event.tutorhut.BeginProblemEvent;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.util.State;
import edu.umass.ckc.wo.util.WoProps;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 12/9/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProblemState extends State {

    private static final String CUR_HINT = "st.curHint";
    private static final String CUR_HINT_ID = "st.curHintId";
    private static final String PROB_ELAPSED_TIME = "st.probElapsedTime";
    private static final String PROB_START_TIME = "st.probStartTime";
    private static final String HINT_START_TIME = "st.hintStartTime";
    private static final String ATTEMPT_START_TIME = "st.attemptTime";
    private static final String CUR_PROB_NUM_ATTEMPTS = "st.curProbNumAttempts";
    private static final String CUR_PROB_AVG_TIME_BETWEEN_ATTEMPTS = "st.curProbAvgTimeBetweenAttempts";
    private static final String CUR_PROB_NUM_MISTAKES = "st.curProbNumMistakes";
    private static final String CUR_PROB_NUM_HINTS_GIVEN = "st.curProbNumHintsGiven";
    private static final String CUR_PROB_NUM_HELPAIDS_GIVEN = "st.curProbNumHelpAidsGiven";
    private static final String CUR_PROB_MAX_HINTS = "st.curProbMaxHints";
    private static final String PROBLEM_SOLVED = "st.problemSolved";
    private static final String TIME_TO_SOLVE = "st.timeToSolve";
    private static final String TIME_TO_FIRST_EVENT = "st.timeToFirstEvent";
    private static final String TIME_TO_FIRST_HINT = "st.timeToFirstHint";
    private static final String TIME_TO_FIRST_ATTEMPT = "st.timeToFirstAttempt";
    private static final String TIME_IN_HINTS_BEFORE_CORRECT = "st.timeInHintsBeforeCorrect";
    private static final String NUM_HINTS_BEFORE_CORRECT = "st.numHintsBeforeCorrect";
    private static final String NUM_HELPAIDS_BEFORE_CORRECT = "st.numHelpAidsBeforeCorrect";
    private static final String FIRST_EVENT = "st.firstEvent";
    private static final String LAST_EVENT = "st.lastEvent";
    private static final String STRATEGIC_HINT_SHOWN= "st.strategicHintShown";
    private static final String PROB_IDLE_TIME = "st.problemIdleTime";
    private static final String VIDEO_SHOWN = "st.videoShown";
    private static final String PROB_EXAMPLES_SHOWN = "st.probExampleShown";

    private static final String TEXT_READER_USED = "st.textReaderUsed";
    private static final String SOLUTION_HINT_GIVEN = "st.solutionHintGiven";
    private static final String CUR_INTERVENTION = "st.curIntervention";
    private static final String INTERVENTION_START_TIME = "st.interventionStartTime";
    private static final String PROBLEM_BINDING = "st.problemBinding";
    private static final String PROBLEM_ANSWER = "st.problemAnswer";
    private static final String POSSIBLE_SHORT_ANSWERS = "st.possibleShortAnswers";

    private static String[] ALL_PROPS = new String[] { CUR_HINT, CUR_HINT_ID, PROB_ELAPSED_TIME, PROB_START_TIME, HINT_START_TIME, ATTEMPT_START_TIME,
            CUR_PROB_NUM_ATTEMPTS, CUR_PROB_AVG_TIME_BETWEEN_ATTEMPTS, CUR_PROB_NUM_MISTAKES, CUR_PROB_NUM_HINTS_GIVEN, CUR_PROB_NUM_HELPAIDS_GIVEN,
            CUR_PROB_MAX_HINTS, PROBLEM_SOLVED, TIME_TO_SOLVE, TIME_TO_FIRST_EVENT, TIME_TO_FIRST_HINT, TIME_TO_FIRST_ATTEMPT, TIME_IN_HINTS_BEFORE_CORRECT,
            NUM_HINTS_BEFORE_CORRECT, NUM_HELPAIDS_BEFORE_CORRECT, FIRST_EVENT, LAST_EVENT, STRATEGIC_HINT_SHOWN, PROB_IDLE_TIME, VIDEO_SHOWN,
            TEXT_READER_USED, SOLUTION_HINT_GIVEN, CUR_INTERVENTION, INTERVENTION_START_TIME, PROB_EXAMPLES_SHOWN, PROBLEM_BINDING, PROBLEM_ANSWER,POSSIBLE_SHORT_ANSWERS};
         // N.B.  If you add a new field above,  make sure clearState deletes its value


    private String curHint;
    private int curHintId;
    private long probElapsedTime;
    private long probStartTime;
    private long hintStartTime;
    private long attemptStartTime;
    private double curProblemAvgTimeBetweenAttempts;
    private int numAttemptsOnCurProblem;
    private int numMistakesOnCurProblem;
    private int numHintsGivenOnCurProblem;
    private int numHelpAidsGivenOnCurProblem;
    private int curProblemMaxHintNum;
    private boolean problemSolved;
    private long timeToFirstEvent;
    private long timeToFirstHint;
    private long timeToFirstAttempt;
    private long timeInHintsBeforeCorrect;
    private int numHintsBeforeCorrect;
    private int numHelpAidsBeforeCorrect;
    private long timeToSolve;
    private String firstEvent;
    private String lastEvent;
    private boolean strategicHintShown;
    private long problemIdleTime;
    private boolean videoShown;
    private int probExamplesShown;
    private boolean textReaderUsed;
    private boolean solutionHintGiven;
    private String curIntervention;
    private long interventionStartTime;
    private boolean inProblem;
    private String problemBinding;
    private String problemAnswer;
    private List<String> possibleShortAnswers;


    public ProblemState(Connection conn) {
        this.conn = conn;
    }

    public void extractProps(WoProps props) throws SQLException {
        Map m = props.getMap();
        this.curHint = mapGetPropString(m, CUR_HINT);
        this.curHintId = mapGetPropInt(m, CUR_HINT_ID, -1);
        this.probElapsedTime = mapGetPropLong(m, PROB_ELAPSED_TIME, 0);
        this.probStartTime = mapGetPropLong(m, PROB_START_TIME, -1);
        this.hintStartTime = mapGetPropLong(m, HINT_START_TIME, -1);
        this.attemptStartTime = mapGetPropLong(m, ATTEMPT_START_TIME, -1);
        this.curProblemAvgTimeBetweenAttempts = mapGetPropDouble(m, CUR_PROB_AVG_TIME_BETWEEN_ATTEMPTS, 0);
        this.numAttemptsOnCurProblem = mapGetPropInt(m, CUR_PROB_NUM_ATTEMPTS, 0);
        this.numMistakesOnCurProblem = mapGetPropInt(m, CUR_PROB_NUM_MISTAKES, 0);
        this.numHintsGivenOnCurProblem = mapGetPropInt(m, CUR_PROB_NUM_HINTS_GIVEN, 0);
        this.numHelpAidsGivenOnCurProblem = mapGetPropInt(m, CUR_PROB_NUM_HELPAIDS_GIVEN, 0);
        this.curProblemMaxHintNum = mapGetPropInt(m, CUR_PROB_MAX_HINTS, 0);
        this.problemSolved = mapGetPropBoolean(m, PROBLEM_SOLVED, false);
        this.timeToFirstEvent = mapGetPropLong(m, TIME_TO_FIRST_EVENT, 0);
        this.timeToFirstHint = mapGetPropLong(m, TIME_TO_FIRST_HINT, -1);
        this.timeToFirstAttempt = mapGetPropLong(m, TIME_TO_FIRST_ATTEMPT, -1);
        this.timeInHintsBeforeCorrect = mapGetPropLong(m, TIME_IN_HINTS_BEFORE_CORRECT, 0);
        this.numHintsBeforeCorrect = mapGetPropInt(m, NUM_HINTS_BEFORE_CORRECT, 0);
        this.numHelpAidsBeforeCorrect = mapGetPropInt(m, NUM_HELPAIDS_BEFORE_CORRECT, 0);
        this.timeToSolve = mapGetPropLong(m, TIME_TO_SOLVE, -1);
        this.firstEvent = mapGetPropString(m, FIRST_EVENT, null); // first thing student did in this problem
        this.lastEvent = mapGetPropString(m, LAST_EVENT, null); // last thing student did in this problem
        this.strategicHintShown = mapGetPropBoolean(m, STRATEGIC_HINT_SHOWN, false);
        this.problemIdleTime = mapGetPropLong(m, PROB_IDLE_TIME, 0);
        this.videoShown = mapGetPropBoolean(m, VIDEO_SHOWN, false);
        this.probExamplesShown = mapGetPropInt(m, PROB_EXAMPLES_SHOWN, 0);
        this.textReaderUsed = mapGetPropBoolean(m,TEXT_READER_USED,false);
        this.solutionHintGiven =  mapGetPropBoolean(m,SOLUTION_HINT_GIVEN,false);
        this.problemBinding = mapGetPropString(m,PROBLEM_BINDING,"");
        this.problemAnswer = mapGetPropString(m,PROBLEM_ANSWER,"");
        this.possibleShortAnswers = mapGetPropList(m,POSSIBLE_SHORT_ANSWERS);
    }

    void initializeProblemState() throws SQLException {

        this.setNumHintsBeforeCorrect(0);
        this.setNumHelpAidsBeforeCorrect(0);
        this.setTimeToFirstEvent(-1);
        this.setTimeToFirstAttempt(-1);
        this.setTimeInHintsBeforeCorrect(0);
        this.setFirstEvent(null);
        this.setLastEvent(null);
        this.setCurHint(null); // name of current hint
        this.setCurHintId(-1);   // id of current hint
        this.setHintStartTime(-1);
        this.setSolutionHintGiven(false);
        this.setTimeToFirstHint(-1);
        this.setInterventionStartTime(-1);
        this.setProblemIdleTime(0);
        this.setCurIntervention(null);   // no current intervention on beginning a problem
        this.setNumHintsGivenOnCurProblem(0);
        this.setNumHelpAidsGivenOnCurProblem(0);
        this.setNumMistakesOnCurProblem(0);
        this.setNumAttemptsOnCurProblem(0);
        this.setCurProblemAvgTimeBetweenAttempts(-1);
        this.setProbElapsedTime(0);
        this.setProblemSolved(false);
        this.setAttemptStartTime(-1);
        this.setStrategicHintShown(false);
        this.setTimeToSolve(-1);
        this.setIsVideoShown(false);
        this.setProbExamplesShown(0);
        this.setIsTextReaderUsed(false);
        this.possibleShortAnswers = null;
//        this.setProblemBinding("");
//        this.setProblemAnswer("");
    }

    public static void clearState (Connection conn, int objid) throws SQLException {
        for (String prop : ALL_PROPS)
            clearProp(conn,objid,prop)  ;
    }



    // This is called when a problem is put on-screen in Flash.
    public void beginProblem(SessionManager smgr, BeginProblemEvent e) throws SQLException {
        this.setProbStartTime(e.getElapsedTime());
    }




    public void setCurHint(String curHint) throws SQLException {
        this.curHint = curHint;
        setProp(this.objid, CUR_HINT, curHint);
    }

    public String getCurHint() {
        return curHint;
    }

    public void setCurHintId(int curHintId) throws SQLException {
        this.curHintId = curHintId;
        setProp(this.objid, CUR_HINT_ID, this.curHintId);
    }

    public int getCurHintId() {
        return curHintId;
    }

    public void setProbElapsedTime(long probElapsedTime) throws SQLException {
        this.probElapsedTime = probElapsedTime;
        setProp(this.objid, PROB_ELAPSED_TIME, probElapsedTime);
    }

    public long getProbElapsedTime() {
        return probElapsedTime;
    }

    public void setProbStartTime(long probStartTime) throws SQLException {
        this.probStartTime = probStartTime;
        setProp(this.objid, PROB_START_TIME, probStartTime);
    }

    public long getProbStartTime() {
        return probStartTime;
    }

    public void setHintStartTime(long hintStartTime) throws SQLException {
        this.hintStartTime = hintStartTime;
        setProp(this.objid, HINT_START_TIME, hintStartTime);
    }

    public long getHintStartTime() {
        return hintStartTime;
    }

    public void setAttemptStartTime(long attemptStartTime) throws SQLException {
        this.attemptStartTime = attemptStartTime;
        setProp(this.objid, ATTEMPT_START_TIME, attemptStartTime);
    }

    public long getAttemptStartTime() {
        return attemptStartTime;
    }

    public void setCurProblemAvgTimeBetweenAttempts(double curProblemAvgTimeBetweenAttempts) throws SQLException {
        this.curProblemAvgTimeBetweenAttempts = curProblemAvgTimeBetweenAttempts;
        setProp(this.objid, CUR_PROB_AVG_TIME_BETWEEN_ATTEMPTS, curProblemAvgTimeBetweenAttempts);
    }

    public double getCurProblemAvgTimeBetweenAttempts() {
        return curProblemAvgTimeBetweenAttempts;
    }

    public void setNumAttemptsOnCurProblem(int numAttemptsOnCurProblem) throws SQLException {
        this.numAttemptsOnCurProblem = numAttemptsOnCurProblem;
        setProp(this.objid, CUR_PROB_NUM_ATTEMPTS, numAttemptsOnCurProblem);
    }

    public int getNumAttemptsOnCurProblem() {
        return numAttemptsOnCurProblem;
    }

    public void setNumMistakesOnCurProblem(int numMistakesOnCurProblem) throws SQLException {
        this.numMistakesOnCurProblem = numMistakesOnCurProblem;
        setProp(this.objid, CUR_PROB_NUM_MISTAKES, numMistakesOnCurProblem);
    }

    public int getNumMistakesOnCurProblem() {
        return numMistakesOnCurProblem;
    }

    public void setNumHintsGivenOnCurProblem(int numHintsGivenOnCurProblem) throws SQLException {
        this.numHintsGivenOnCurProblem = numHintsGivenOnCurProblem;
        setProp(this.objid, CUR_PROB_NUM_HINTS_GIVEN, numHintsGivenOnCurProblem);
    }

    public int getNumHintsGivenOnCurProblem() {
        return numHintsGivenOnCurProblem;
    }

    public void setNumHelpAidsGivenOnCurProblem(int numAidsGivenOnCurProblem) throws SQLException {
        this.numHelpAidsGivenOnCurProblem = numAidsGivenOnCurProblem;
        setProp(this.objid, CUR_PROB_NUM_HELPAIDS_GIVEN, numHelpAidsGivenOnCurProblem);
    }

    public int getNumHelpAidsGivenOnCurProblem() {
        return numHelpAidsGivenOnCurProblem;
    }

    public void setCurProblemMaxHintNum(int curProblemMaxHintNum) throws SQLException {
        this.curProblemMaxHintNum = curProblemMaxHintNum;
        setProp(this.objid, CUR_PROB_MAX_HINTS, curProblemMaxHintNum);
    }

    public int getCurProblemMaxHintNum() {
        return curProblemMaxHintNum;
    }

    public void setProblemSolved(boolean problemSolved) throws SQLException {
        this.problemSolved = problemSolved;
        setProp(this.objid, PROBLEM_SOLVED, problemSolved);
    }

    public boolean isProblemSolved() {
        return problemSolved;
    }

    public void setTimeToFirstEvent(long timeToFirstEvent) throws SQLException {
        this.timeToFirstEvent = timeToFirstEvent;
        setProp(this.objid, TIME_TO_FIRST_EVENT, timeToFirstEvent);
    }

    public long getTimeToFirstEvent() {
        return timeToFirstEvent;
    }

    public void setTimeToFirstHint(long timeToFirstHint) throws SQLException {
        this.timeToFirstHint = timeToFirstHint;
        setProp(this.objid, TIME_TO_FIRST_HINT, timeToFirstHint);
    }

    public long getTimeToFirstHint() {
        return timeToFirstHint;
    }

    public void setTimeToFirstAttempt(long timeToFirstAttempt) throws SQLException {
        this.timeToFirstAttempt = timeToFirstAttempt;
        setProp(this.objid, TIME_TO_FIRST_ATTEMPT, timeToFirstAttempt);
    }

    public long getTimeToFirstAttempt() {
        return timeToFirstAttempt;
    }

    public void setTimeInHintsBeforeCorrect(long timeInHintsBeforeCorrect) throws SQLException {
        this.timeInHintsBeforeCorrect = timeInHintsBeforeCorrect;
        setProp(this.objid, TIME_IN_HINTS_BEFORE_CORRECT, timeInHintsBeforeCorrect);
    }

    public long getTimeInHintsBeforeCorrect() {
        return timeInHintsBeforeCorrect;
    }

    public void setNumHintsBeforeCorrect(int numHintsBeforeCorrect) throws SQLException {
        this.numHintsBeforeCorrect = numHintsBeforeCorrect;
        setProp(this.objid, NUM_HINTS_BEFORE_CORRECT, numHintsBeforeCorrect);
    }

    public int getNumHintsBeforeCorrect() {
        return numHintsBeforeCorrect;
    }

    public void setNumHelpAidsBeforeCorrect(int numHelpAidsBeforeCorrect) throws SQLException {
        this.numHelpAidsBeforeCorrect = numHelpAidsBeforeCorrect;
        setProp(this.objid, NUM_HELPAIDS_BEFORE_CORRECT, numHelpAidsBeforeCorrect);
    }

    public int getNumHelpAidsBeforeCorrect() {
        return numHelpAidsBeforeCorrect;
    }

    // this is called when student answers problem correctly
    public void setTimeToSolve(long timeToSolve) throws SQLException {
        this.timeToSolve = timeToSolve;
        setProp(this.objid, TIME_TO_SOLVE, timeToSolve);
    }

    public long getTimeToSolve() {
        return timeToSolve;
    }

    public void setFirstEvent(String firstEvent) throws SQLException {
        this.firstEvent = firstEvent;
        setProp(this.objid, FIRST_EVENT, firstEvent);
    }

    public String getFirstEvent() {
        return firstEvent;
    }

    public void setLastEvent(String lastEvent) throws SQLException {
        this.lastEvent = lastEvent;
        setProp(this.objid, LAST_EVENT, lastEvent);
    }

    public String getLastEvent() {
        return lastEvent;
    }

    public void setStrategicHintShown(boolean strategicHintShown) throws SQLException {
        this.strategicHintShown = strategicHintShown;
        setProp(this.objid,STRATEGIC_HINT_SHOWN,strategicHintShown);
    }

    public boolean isStrategicHintShown() {
        return strategicHintShown;
    }

    public void setProblemIdleTime (long t) throws SQLException {
        this.problemIdleTime = t;
        setProp(this.objid,PROB_IDLE_TIME,problemIdleTime);
    }

    public long getProblemIdleTime () {
        return this.problemIdleTime;
    }

    public void setIsVideoShown (boolean seenVideo) throws SQLException {
        this.videoShown = seenVideo;
        setProp(this.objid, VIDEO_SHOWN, seenVideo);
    }

    public boolean isVideoShown () {
        return this.videoShown;
    }


    public void setIsTextReaderUsed (boolean textReaderUsed) throws SQLException {
        this.textReaderUsed = textReaderUsed;
        setProp(this.objid, TEXT_READER_USED, textReaderUsed);
    }

    public boolean isTextReaderUsed () {
        return this.textReaderUsed;
    }

    public void setSolutionHintGiven(boolean solutionHintGiven) throws SQLException {
        this.solutionHintGiven = solutionHintGiven;
        setProp(this.objid, SOLUTION_HINT_GIVEN, solutionHintGiven);
    }

    public boolean isSolutionHintGiven() {
        return solutionHintGiven;
    }

    public void setCurIntervention(String curIntervention) throws SQLException {
        this.curIntervention = curIntervention;
        setProp(this.objid, CUR_INTERVENTION, curIntervention);
    }

    public String getCurIntervention() {
        return curIntervention;
    }

    public void setInterventionStartTime(long interventionStartTime) throws SQLException {
        this.interventionStartTime = interventionStartTime;
        setProp(this.objid, INTERVENTION_START_TIME, interventionStartTime);
    }

    public long getInterventionStartTime() {
        return interventionStartTime;
    }


    public void setInProblem(boolean inProblem) {
        this.inProblem = inProblem;
    }

    public boolean isInProblem() {
        return inProblem;
    }


    public void setProbExamplesShown(int num) throws SQLException {
        this.probExamplesShown = num;
        setProp(this.objid, PROB_EXAMPLES_SHOWN, num);

    }

    public int getProbExamplesShown () {
        return this.probExamplesShown;
    }

    public void setProblemBinding(String binding) throws SQLException {
        this.problemBinding = binding;
        setProp(this.objid, PROBLEM_BINDING, binding);
    }

    public String getProblemBinding()
    {
        return problemBinding;
    }

    public void setProblemAnswer(String problemAnswer) throws SQLException {
        this.problemAnswer = problemAnswer;
        setProp(this.objid, PROBLEM_ANSWER, problemAnswer);
    }

    public String getProblemAnswer() {
        return problemAnswer;
    }

    public List<String> getPossibleShortAnswers() {
        return possibleShortAnswers;
    }

    public void setPossibleShortAnswers(List<String> possibleShortAnswers) throws SQLException {
        this.possibleShortAnswers = possibleShortAnswers;
        setProp(this.objid,POSSIBLE_SHORT_ANSWERS,possibleShortAnswers);
    }
}
