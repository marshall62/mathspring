package edu.umass.ckc.wo.smgr;

import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Hint;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.event.tutorhut.BeginProblemEvent;
import edu.umass.ckc.wo.tutor.intervSel.InterventionState;
import edu.umass.ckc.wo.tutormeta.Intervention;
import edu.umass.ckc.wo.tutormeta.TutorEventHandler;
import edu.umass.ckc.wo.util.State;
import edu.umass.ckc.wo.util.WoProps;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Aug 18, 2005
 * Time: 9:12:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class StudentState extends State implements TutorEventHandler {

     private static Logger logger = Logger.getLogger(StudentState.class);





    public static final String HINT_EVENT = "hint";
    public static final String ATTEMPT_EVENT = "attempt";
    public static final String NO_EVENT = null;
    private static final String END_PROBLEM_EVENT = "endProblem";
    public static final String CORRECT_ATTEMPT_EVENT = "correct attempt";
    public static final String INCORRECT_ATTEMPT_EVENT = "incorrect attempt";

   // variables that are only needed for the duration of the student's session


    private ProblemState problemState;
    private TopicState topicState;
    private SessionState sessionState;
    private WorkspaceState workspaceState;
    private PrePostState ppState;
    private SessionManager smgr;
    private int curProblemIndexInTopic;
    private boolean curProblemIsTopicIntro;

    public StudentState(Connection conn, SessionManager smgr) {
        this.smgr = smgr;
        this.conn = conn;
        this.problemState = new ProblemState(conn);
        this.topicState = new TopicState(conn);
        this.sessionState = new SessionState(conn);
        this.workspaceState = new WorkspaceState(conn);
        this.ppState = new PrePostState(conn);
    }




    public void extractProps(WoProps props) throws SQLException {
        problemState.extractProps(props);
        topicState.extractProps(props);
        sessionState.extractProps(props);
        workspaceState.extractProps(props);
        ppState.extractProps(props);

    }


    public void setObjid(int objid) {
        super.setObjid(objid);
        problemState.setObjid(objid);
        topicState.setObjid(objid);
        sessionState.setObjid(objid);
        workspaceState.setObjid(objid);
        ppState.setObjid(objid);
    }

   //////////////////   Beginning of Topic State methods

    /* Methods that are dealing with state during teaching a topic */

    public void setCurProblem(int curProblem) throws SQLException {
        topicState.setCurProblem(curProblem);
    }
    public int getCurProblem() {
        return topicState.getCurProblem();
    }

    public void setLastProblem (int lastProblem) throws SQLException {
       topicState.setLastProblem(lastProblem);
    }

    public int getLastProblem () {
        return topicState.getLastProblem();
    }

    public void setNextProblem (int nextProblem) throws SQLException {
        topicState.setNextProblem(nextProblem);
    }

    public int getNextProblem () {
        return topicState.getNextProblem();
    }

    public void setCurProblemMode(String curProblemMode) throws SQLException {
        topicState.setCurProblemMode(curProblemMode);
    }

    public String getCurProblemMode() {
        return topicState.getCurProblemMode();
    }

    public void setLastProblemMode(String mode) throws SQLException {
        topicState.setLastProblemMode(mode);
    }

    public String getLastProblemMode() {
        return topicState.getLastProblemMode();
    }


    public void setCurProbType(String type) throws SQLException {
        topicState.setCurProbType(type);
    }

    public String getCurProbType() {
        return topicState.getCurProbType();
    }

    public boolean isTopicIntroShown() {
        return topicState.isTopicIntroShown();
    }

    public void setTopicIntroShown(boolean b) throws SQLException {
        topicState.setTopicIntroShown(b);
    }


    public void setSecondHardestSolvedWell(boolean b) throws SQLException {
        topicState.setSecondHardestSolvedWell(b);
    }

    public boolean isSecondHardestSolvedWell() {
        return topicState.isSecondHardestSolvedWell();

    }


    public void setSecondEasiestProblemFailedSolve(boolean x) throws SQLException {
        topicState.setSecondEasiestProblemFailedSolve(x);
    }

    public boolean isSecondEasiestProblemFailedSolve() {
        return topicState.isSecondEasiestProblemFailedSolve();
    }

    public void setSecondHardestShown(boolean b) throws SQLException {
        topicState.setSecondHardestShown(b);
    }

    public boolean isSecondHardestShown() {
        return topicState.isSecondHardestShown();
    }

    public void setHardestSolvedWell(boolean b) throws SQLException {
        topicState.setSecondHardestSolvedWell(b);
    }

    public boolean isHardestSolvedWell() {
        return topicState.isHardestSolvedWell();
    }

    public void setHardestShown(boolean b) throws SQLException {
        topicState.setHardestShown(b);

    }

    public boolean isHardestShown() {
        return topicState.isHardestShown();
    }


    public boolean isEasiestShown() {
        return topicState.isEasiestShown();
    }

    public void setEasiestShown(boolean x) throws SQLException {
       topicState.setEasiestShown(x);
    }

    public boolean isHardestProblemSolvedWell() {
        return topicState.isHardestProblemSolvedWell();
    }

    public void setHardestProblemSolvedWell(boolean hardestProblemSolvedWell) throws SQLException {
        topicState.setHardestProblemSolvedWell(hardestProblemSolvedWell);
    }

    public boolean isEasiestProblemFailedSolve() {
        return topicState.isEasiestProblemFailedSolve() ;
    }

    public void setEasiestProblemFailedSolve(boolean easiestProblemFailedSolve) throws SQLException {
        topicState.setEasiestProblemFailedSolve(easiestProblemFailedSolve);
    }

    public boolean isSecondEasiestShown() {
        return topicState.isSecondEasiestShown();
    }


    public void setSecondEasiestShown(boolean x) throws SQLException {
        topicState.setSecondEasiestShown(x);
    }

    /**
     * Each time a problem is selected by the AdaptiveProblemGroup selector this records if the selector wanted a harder,easier, or
     * same difficulty problem.   Puts them all into a list stored in the state
     * @param criterion
     */
    public void addProblemSelectionCriterion(String criterion) throws SQLException {
        topicState.addProblemSelectionCriterion(criterion);
    }

    public String getLastIntervention() {
        return topicState.getLastIntervention();
    }

    public void setLastIntervention(String lastIntervention) throws SQLException {
        topicState.setLastIntervention(lastIntervention);
    }

    public void setLastAnswer(String lastAnswer) throws SQLException {
        topicState.setLastAnswer(lastAnswer);
    }


    public boolean getInBtwProbIntervention () {
        return topicState.getInBtwProbIntervention();
    }

    public void setInBtwProbIntervention (boolean b) throws SQLException {
        topicState.setInBtwProbIntervention(b);
    }

    public void setTopicNumProbsSeen(int n) throws SQLException {
       topicState.setTopicNumProbsSeen(n);
    }

    public int getTopicNumProbsSeen () {
        return topicState.getTopicNumProbsSeen();
    }

    public void setTopicNumPracticeProbsSeen(int n) throws SQLException {
        topicState.setTopicNumPracticeProbsSeen(n);
    }

    public int getTopicNumPracticeProbsSeen () {
        return topicState.getTopicNumPracticeProbsSeen();
    }


    public int getStudentSelectedTopic() {
        return topicState.getStudentSelectedTopic();
    }

    public void setStudentSelectedTopic(int studentSelectedTopic) throws SQLException {
        topicState.setStudentSelectedTopic(studentSelectedTopic);
    }


    public int getSidelinedTopic() {
        return topicState.getSidelinedTopic();
    }

    public void setSidelinedTopic(int topicId) throws SQLException {
        topicState.setSidelinedTopic(topicId);
    }

    public boolean isInReviewMode() {
        return topicState.isInReviewMode();
    }


    public boolean isInChallengeMode() {
        return topicState.isInChallengeMode();
    }




    public boolean isTopicSwitch() {
        return topicState.isTopicSwitch();
    }

    public void setTopicSwitch(boolean topicSwitch) throws SQLException {
        topicState.setTopicSwitch(topicSwitch);
    }

    public boolean isContentFailureTopicSwitch() {
        return topicState.isContentFailureTopicSwitch();
    }

    public void setContentFailureTopicSwitch(boolean contentFailureTopicSwitch) throws SQLException {
        topicState.setContentFailureTopicSwitch(contentFailureTopicSwitch);
    }


    public void setTeachTopicMode(boolean b) throws SQLException {
        topicState.setTeachTopicMode(b);
    }

    public boolean inTeachTopicMode () {
        return topicState.inTeachTopicMode();
    }


    public void setInReviewMode(boolean inReviewMode) throws SQLException {
        topicState.setInReviewMode(inReviewMode);
    }


    public void setInChallengeMode(boolean inChallengeMode) throws SQLException {
        topicState.setInChallengeMode(inChallengeMode);
    }

    public void setInPracticeMode(boolean inPracticeMode) throws SQLException {
        topicState.setInPracticeMode(inPracticeMode);
    }

    public void setIsExampleShown(boolean b) throws SQLException {
        topicState.setIsExampleShown(b);
    }

    public boolean isExampleShown () {
        return topicState.isExampleShown();
    }


    public void setCurProblemIndexInTopic(int curProblemIndexInTopic) throws SQLException {
        topicState.setCurProblemIndexInTopic(curProblemIndexInTopic);
    }

    public int getCurProblemIndexInTopic() {
        return topicState.getCurProblemIndexInTopic();
    }

    private void setNextProblemMode(String mode) throws SQLException {
        topicState.setNextProblemMode(mode);
    }

    private String getNextProblemMode () {
        return topicState.getNextProblemMode();
    }


    public long getTimeInTopic() {
        return topicState.getTimeInTopic();
    }

    public void setTimeInTopic(long t) throws SQLException {
        topicState.setTimeInTopic(t);
    }

    public int getContentFailureCounter() {
        return topicState.getContentFailureCounter();
    }

    public void setContentFailureCounter(int contentFailureCounter) throws SQLException {
        topicState.setContentFailureCounter(contentFailureCounter);
    }

    public String getTopicInternalState () {
        return topicState.getInternalState();
    }

    public void setTopicInternalState (String st) throws SQLException {
        topicState.setInternalState(st);
    }

    public int getTopicProblemsSolved () {
        return topicState.getTopicProblemsSolved();
    }

    public void setTopicProblemsSolved (int n) throws SQLException {
        topicState.setTopicProblemsSolved(n);
    }


    /////////////////  End of Topic State methods

    //////////////////   Beginning of Session State methods.

    public List<String> getClips () {
        return sessionState.getClips();
    }

    public List<String> getClipCounters () {
        return sessionState.getClipCounters();
    }

    public int getLcClipCount (String clipName) {
        return sessionState.getLcClipCount(clipName);
    }

    public void setLcClipCount (String clipName, int count) throws SQLException {
        sessionState.setLcClipCount(clipName, count);
    }

    public void setLastInterventionTime(long time) throws SQLException  {
        sessionState.setLastInterventionTime(time);
    }

    public long getLastInterventionTime() {
        return sessionState.getLastInterventionTime();
    }


    // Given a 1-based clip-number, update the woproperty at this position number (clipNumber) to
    // be count.
    // Then update the instance variable (0 based) that holds the list of clip counters corresponding
    // with whats in woproperties.
    public void incrClipCount (int clipNumber, int count) throws SQLException {
        sessionState.incrClipCount(clipNumber, count);
    }

    public void setClipCount (String clip, int count ) throws SQLException {

        sessionState.setClipCount(clip, count);
    }





    /* end of lesson state  methods */

/* Methods that are dealing with state during a problem..   THese just forward to the problemState object but allow convenience for accessing through StudentState */


    public int getProbExamplesShown() {
        return problemState.getProbExamplesShown();
    }

    public long getProbElapsedTime() {
        return problemState.getProbElapsedTime();
    }


    public void setProbElapsedTime(long probElapsed) throws SQLException {
        problemState.setProbElapsedTime(probElapsed);
    }

    public void setStrategicHintShown(boolean b) throws SQLException {
        problemState.setStrategicHintShown(b);
    }

    public void setCurIntervention(String s) throws SQLException {
        problemState.setCurIntervention(s);
    }

    public void setIsTextReaderUsed(boolean b) throws SQLException {
        problemState.setIsTextReaderUsed(b);
    }



    public void setIsVideoShown(boolean b) throws SQLException {
        problemState.setIsVideoShown(b);
    }

    public void setProblemIdleTime(int t) throws SQLException {
        problemState.setProblemIdleTime(t);
    }


    public int getCurHintId () {
        return problemState.getCurHintId();
    }

    public String getCurHint () {
        return problemState.getCurHint();
    }

    public long getProbStartTime() {
        return problemState.getProbStartTime();
    }

    public long getHintStartTime() {
        return problemState.getHintStartTime();
    }

    public long getAttemptStartTime() {
        return problemState.getAttemptStartTime();
    }

    public double getCurProblemAvgTimeBetweenAttempts() {
        return problemState.getCurProblemAvgTimeBetweenAttempts();
    }

    public long getInterventionStartTime() {
        return problemState.getInterventionStartTime();
    }

    public int getNumAttemptsOnCurProblem() {
        return problemState.getNumAttemptsOnCurProblem();
    }

    public int getNumMistakesOnCurProblem() {
        return problemState.getNumMistakesOnCurProblem();
    }


    public int getNumHintsGivenOnCurProblem() {
        return problemState.getNumHintsGivenOnCurProblem();
    }


    public int getNumHelpAidsGivenOnCurProblem() {
        return problemState.getNumHelpAidsGivenOnCurProblem();
    }

    // Returns true only if they solved it correctly
    public boolean isProblemSolved() {
        return problemState.isProblemSolved();
    }


    public long getTimeToSolve() {
        return problemState.getTimeToSolve();
    }


    public long getTimeToFirstEvent() {
        return problemState.getTimeToFirstEvent();
    }


    public long getTimeToFirstHint() {
        return problemState.getTimeToFirstHint();
    }


    public long getTimeInHintsBeforeCorrect() {
        return problemState.getTimeInHintsBeforeCorrect();
    }

    public long getTimeToFirstAttempt() {
        return problemState.getTimeToFirstAttempt();
    }

    public int getNumHintsBeforeCorrect() {
        return problemState.getNumHintsBeforeCorrect();
    }

    public int getNumHelpAidsBeforeCorrect() {
        return problemState.getNumHelpAidsBeforeCorrect();
    }

    public String getFirstEvent() {
        return problemState.getFirstEvent();
    }

    public String getLastEvent() {
        return problemState.getLastEvent();
    }


    public boolean isStrategicHintShown () {
        return problemState.isStrategicHintShown();
    }


    public boolean isVideoShown () {
        return problemState.isVideoShown();
    }



    public boolean isTextReaderUsed () {
        return problemState.isTextReaderUsed();
    }

    public boolean isSolutionHintGiven() {
        return problemState.isSolutionHintGiven();
    }

    public String getCurIntervention () {
        return problemState.getCurIntervention();
    }


    public void setInProblem(boolean inProblem) {
        problemState.setInProblem(inProblem);
    }

    public boolean isInProblem() {
        return problemState.isInProblem();
    }

    public boolean isLastEvent(String ev) {
        return (problemState.getLastEvent() != NO_EVENT && problemState.getLastEvent().equals(ev));
    }


    public void interventionGiven(StudentState state, Intervention intervention) throws SQLException {
        problemState.setInterventionStartTime(getTime());
    }

    public void setProblemBinding(String binding) throws SQLException {
        problemState.setProblemBinding(binding);
    }

    public String getProblemBinding()
    {
        return problemState.getProblemBinding();
    }

    public void setProblemAnswer(String ans) throws SQLException {
        problemState.setProblemAnswer(ans);
    }

    public String getProblemAnswer() {
        return problemState.getProblemAnswer();
    }


    public List<String> getPossibleShortAnswers() {
        return problemState.getPossibleShortAnswers();
    }

    public void setPossibleShortAnswers(List<String> possibleShortAnswers) throws SQLException {
        problemState.setPossibleShortAnswers(possibleShortAnswers);
    }


    /* end of problem state methods */




/*The following methods have to do with the session state*/

    public String getCurLocation() {
        return sessionState.getCurLocation();
    }

    public  void setCurLocation(String curLocation) throws SQLException {
        sessionState.setCurLocation(curLocation);
    }

    public long getTime() {
        return sessionState.getTime();
    }

    public void setTime(long time) throws SQLException {
        sessionState.setTime(time);
    }

    public int getNumProblemsThisTutorSession() {
        return sessionState.getNumProblemsThisTutorSession();
    }

    public void setNumProblemsThisTutorSession(int n) throws SQLException {
        sessionState.setNumProblemsThisTutorSession(n);
    }

    public int getNumRealProblemsThisTutorSession() {
        return sessionState.getNumRealProblemsThisTutorSession();
    }

    public void setNumRealProblemsThisTutorSession(int n) throws SQLException {
        sessionState.setNumRealProblemsThisTutorSession(n);
    }


    public long getTutorEntryTime () {
        return sessionState.getTutorEntryTime();
    }

    public void setTutorEntryTime(long time) throws SQLException {
        sessionState.setTutorEntryTime(time);
    }

    public List<String> getExternalActivitiesGiven () {
        return sessionState.getExternalActivitiesGiven();
    }

    public void addExternalActivityGiven (int id) throws SQLException {
        sessionState.addExternalActivityGiven(id);
    }

    public List<String> getExampleProblemsGiven() {
        return sessionState.getExampleProblemsGiven();
    }

    public void addExampleProblemGiven(int probId) throws SQLException {
        sessionState.addExampleProblemGiven(probId);
    }

    public int getTempProblemIndex() {
        return sessionState.getTempProblemIndex();
    }

    public void setTempProblemIndex(int tempProblemIndex) throws SQLException {
        sessionState.setTempProblemIndex(tempProblemIndex);
    }

    public void addTopicIntrosSeen(int topicId) throws SQLException {
        sessionState.addTopicIntrosSeen(topicId);
    }

    public boolean isTopicIntroSeen (int topicId) {
       return sessionState.isTopicIntroSeen(topicId);
    }

    public void addExampleSeen(int topicId) throws SQLException {
        sessionState.addTopicExampleSeen(topicId);
    }

    public boolean isExampleSeen (int topicId) {
        return sessionState.isTopicExampleSeen(topicId);
    }



    /* end of session state methods */

    /* The following methods have to do with the workspace state (state that is preserved after the student logs out) */



    public int getCurLesson (){
        return workspaceState.getCurLesson();
    }

    public void setCurLesson (int lessonId) throws SQLException {
        workspaceState.setCurLesson(lessonId);
    }

    public int getCurCU () {
        return workspaceState.getCurCU();
    }

    public void setCurCU (int cu) throws SQLException {
        workspaceState.setCurCU(cu);
    }

    public int getCurCluster () {
        return workspaceState.getCurCluster();
    }

    public void setCurCluster (int clust) throws SQLException {
        workspaceState.setCurCluster(clust);
    }

    public String getCurStd () {
        return workspaceState.getCurStd();
    }

    public void setCurStd (String std) throws SQLException {
        workspaceState.setCurStd(std);
    }

    public String getPrereqStd () {
        return workspaceState.getPrereqStd();
    }

    public void setPrereqStd (String std) throws SQLException {
        workspaceState.setPrereqStd(std);
    }

    public List<String> getPrereqStdStack () {
        return workspaceState.getPrereqStdStack();
    }

    public void addPrereqStdStack (String std) throws SQLException {
        workspaceState.addPrereqStdStack(std);
    }




    public int getCurTopic() {
        return workspaceState.getCurTopic();
    }

    public void setCurTopic(int pgroupID) throws SQLException {
        workspaceState.setCurTopic(pgroupID);
    }

    /*end of workspace state methods */

    /* Methods for pre / post tests */

    public boolean getPretestCompleted() {
        return ppState.getPretestCompleted();
    }

    public boolean getSatHutCompleted() {
        return ppState.getSatHutCompleted();
    }

    public boolean getPosttestCompleted() {
        return ppState.getPosttestCompleted();
    }

    public int getPretestProblemSet() {
        return ppState.getPretestProblemSet();
    }

    public int getPosttestProblemSet() {
        return ppState.getPosttestProblemSet();
    }

    public List getPretestProblemsGiven() {
        return ppState.getPretestProblemsGiven();
    }

    public List getPosttestProblemsGiven() {
        return ppState.getPosttestProblemsGiven();
    }


    public boolean isPretestCompleted() {
        return ppState.isPretestCompleted();
    }

    public boolean isPosttestCompleted() {
        return ppState.isPosttestCompleted();
    }

    public boolean isSatHutCompleted(){
        return false;
    }
    public void setPretestProblemSet (int p) throws SQLException {
        ppState.setPretestProblemSet(p);
    }

    public void addPretestProblem(int id) throws SQLException {
        ppState.addPretestProblem(id);
    }

    public void addPosttestProblem(int id) throws SQLException {
        ppState.addPosttestProblem(id);
    }

    public void setCurPreProblem(int id) throws SQLException {
        ppState.setCurPreProblem(id);
    }

    public void setCurPostProblem(int id) throws SQLException {
        ppState.setCurPostProblem(id);
    }

    public void setPretestCompleted(boolean pretestCompleted) throws SQLException {
        ppState.setPretestCompleted(pretestCompleted);
    }

    public void setPosttestCompleted(boolean b) throws SQLException {
        ppState.setPosttestCompleted(b);
    }

    public void setPosttestProblemSet(int problemSet) throws SQLException {
        ppState.setPosttestProblemSet(problemSet);
    }

        /* End of Methods for pre / post tests */




    public void newTopic () throws SQLException {
        topicState.initializeState();
    }

    public void newSession (SessionManager smgr) throws SQLException {
        InterventionState.clearState(conn, smgr.getStudentId());  // on new session interventions need to clear their states.
        sessionState.initializeState();
        topicState.initializeState();
        problemState.initializeProblemState();


    }



    private void initializeProblemState (Problem p) throws SQLException {
        this.problemState.initializeProblemState();
    }

    // At the time NextProblem button is clicked, the tutor selects the next prob (or intervention).   If
    // it selects a next problem, this method gets called but the new problem doesn't really start
    // playing until the beginProblem event comes in (after an endProblem event terminating the previous
    //  problem).    Note no previous endProblem event can be expected for the first problem in a session.
    //
    // So all state variables associated with the newly
    // selected problem (which used to be initialized here) are now initialized in beginProblem
    public void newProblem(StudentState state, Problem p) throws SQLException {
        if (p == null) {
            setCurProblem(-1);
        } else {
            this.setNextProblem(p.getId());  // Why next?
            this.setNextProblemMode(p.getMode());

//            this.setLastProblemMode(lessonState.getCurProblemMode());
//            this.setCurProblemMode(p.getMode()); // We set the mode now because we have the actual Problem object and its mode.
            // When the beginProblem event happens, we don't know the mode anymore

            // When the student select a problem + topic, we need to set a state variable to hold the topicId.
            // This topicId will be logged with events associated with this problem only.   When the endProblem event
            // comes in,   we remove this topic from the state so that the tutor can revert to logging with the topicId
            // held by the smgr.
            this.setStudentSelectedTopic(p.getInTopicId());

        }
        // moved initializeProblemState to beginProblem so that the endProblem event has the correct
        // problem settings at the time it is called (after this method and before beginProblem)
    }



    // This is called when a problem is put on-screen in Flash.
    public void beginProblem(SessionManager smgr, BeginProblemEvent e) throws SQLException {
        this.setInProblem(true);
        problemState.beginProblem(smgr, e);
        this.setLastProblem(topicState.getCurProblem());
        this.setCurProblem(topicState.getNextProblem());


        this.setCurProblemMode(topicState.getNextProblemMode());
        Problem prob = ProblemMgr.getProblem(topicState.getNextProblem());
        //  a TOpicIntro won't be found here.
        if (prob != null) {
            this.setCurProbType(prob.getType());
        }
        else if (this.getCurProblemMode().equals(Problem.TOPIC_INTRO))
            this.setCurProbType(Problem.TOPIC_INTRO_PROB_TYPE);
        initializeProblemState(new DbProblem().getProblem(conn, topicState.getCurProblem()));

    }


    public void endProblem(SessionManager smgr, int studId,long probElapsedTime, long elapsedTime) throws SQLException {
        // At the end of each problem the timeInTopic is increased by the time spent in the problem.
        this.setTimeInTopic(this.getTimeInTopic()+ probElapsedTime);
        // check if the previous event was a hint and problem not solved yet.  If so, then update hint_time
        if ((this.isLastEvent(HINT_EVENT)) && (! isProblemSolved())) {
            long curr_hint_time = this.getTimeInHintsBeforeCorrect();
            long extra_hint_time = this.getProbElapsedTime() - this.getHintStartTime();
            problemState.setTimeInHintsBeforeCorrect(curr_hint_time + extra_hint_time);
        }
        problemState.setLastEvent(END_PROBLEM_EVENT);
        topicState.setLastProblemMode(topicState.getCurProblemMode());
        // We save the current problem into the workspace state only when it is completed.   This means that the next time a user logs in (who is using
        // common core pedagogy) they will use the current problem to find a location in the lesson structure.
        if (this.getCurProblemMode().equals(Problem.PRACTICE))
            workspaceState.setCurProb(this.getCurProblem());
        else workspaceState.setCurProb(-1); // If its not practice remove the probId from workspace state.
        this.setInProblem(false);  // DM 1/25/10
        // NOTE:  It is critical that the client end every problem because a problem that was started with a forced probID + topicId
        // is temporarily saving the topicId in the student state.   While the state variable is set to a value (other than -1)
        // the system will log all events with this topic ID.   When the problem ends we reset the this state var to -1
        this.setStudentSelectedTopic(-1);  // if the student has forced a problem+topic for this problem,  remove the topic
    }






    public void helpAidGiven(StudentState s) throws SQLException {
        problemState.setNumHelpAidsGivenOnCurProblem(problemState.getNumHelpAidsGivenOnCurProblem() + 1);
        if (this.getTimeToFirstEvent() < 0) {
            problemState.setTimeToFirstEvent(this.getProbElapsedTime());
        }
        if ( ! isProblemSolved() )
            problemState.setNumHelpAidsBeforeCorrect(this.getNumHelpAidsBeforeCorrect() + 1) ;

    }

    public void videoGiven (StudentState s) throws SQLException {
        problemState.setNumHelpAidsGivenOnCurProblem(problemState.getNumHelpAidsGivenOnCurProblem() + 1);
        smgr.getStudentState().setIsVideoShown(true);
        if (this.getTimeToFirstEvent() < 0) {
            problemState.setTimeToFirstEvent(this.getProbElapsedTime());
        }
        if ( ! isProblemSolved() )
            problemState.setNumHelpAidsBeforeCorrect(this.getNumHelpAidsBeforeCorrect() + 1) ;
    }

    public void exampleGiven(StudentState s, int exampleId) throws SQLException {
        problemState.setNumHelpAidsGivenOnCurProblem(problemState.getNumHelpAidsGivenOnCurProblem() + 1);
        setIsExampleShown(true);   // this says whether an example is seen within the topic.
        problemState.setProbExamplesShown(problemState.getProbExamplesShown() + 1); // counts how many examples given in the cur problem
        addExampleProblemGiven(exampleId);
        if (this.getTimeToFirstEvent() < 0) {
            problemState.setTimeToFirstEvent(this.getProbElapsedTime());
        }
        if ( ! isProblemSolved() )
            problemState.setNumHelpAidsBeforeCorrect(this.getNumHelpAidsBeforeCorrect() + 1) ;
    }


    public void hintGiven(StudentState state, Hint hint) throws SQLException {

        // check if the previous event was a hint and problem not solved yet.  If so, then update hint_time
        if ((this.isLastEvent(HINT_EVENT)) && (! isProblemSolved())) {
            long curr_hint_time = this.getTimeInHintsBeforeCorrect();
            long extra_hint_time = this.getProbElapsedTime() - this.getHintStartTime();
            problemState.setTimeInHintsBeforeCorrect(curr_hint_time + extra_hint_time);
        }
        // now update things
        problemState.setLastEvent(HINT_EVENT);
        if (hint == null) return;
        problemState.setProblemIdleTime(0);
        if (!isProblemSolved())
            problemState.setNumHintsGivenOnCurProblem(problemState.getNumHintsGivenOnCurProblem() + 1);
        problemState.setHintStartTime(this.getProbElapsedTime());
        boolean atEnd = hint.getLabel().equals(problemState.getCurHint());
        problemState.setCurHint(hint.getLabel());
        problemState.setCurHintId(hint.getId());
        if (!isProblemSolved() && hint.getGivesAnswer())
            problemState.setSolutionHintGiven(true);
        if (!isProblemSolved() && getNumHintsGivenOnCurProblem() == 1) {
            problemState.setTimeToFirstHint(this.getProbElapsedTime());
        }
        if (this.getTimeToFirstEvent() < 0) {
            problemState.setTimeToFirstEvent(this.getProbElapsedTime());
        }
        // for jeff
        if (this.getFirstEvent() == null) {
            problemState.setFirstEvent(HINT_EVENT);
            problemState.setTimeToFirstEvent(this.getProbElapsedTime());
        }
        // don't tamper with the problem history if this hint is requested after the problem has been solved or all hints are given
        if (!isProblemSolved() && !atEnd)
            smgr.getStudentModel().getStudentProblemHistory().hint(smgr, this.getProbElapsedTime(), hint.getGivesAnswer());

    }

    /**
     * Any action (e.g. drawing) that indicates the student is doing something
     * can result in an update of the state so that the idle counter is reset.
     * @throws java.sql.SQLException
     */
    public void studentNonIdleEvent () throws SQLException {
        problemState.setProblemIdleTime(0);
    }


    // the only reason a StudentState is a parameter is because this implements an interface requiring it
    public void studentAttempt(StudentState state, String answer, boolean isCorrect, long probElapsed) throws SQLException {
        // there are certain counters and stats that shouldn't be touched if this problem has already been solved and the student is
        // just clicking on other answers.
        boolean previouslySolved = isProblemSolved();
        if (!previouslySolved && isCorrect)
            topicState.setTopicProblemsSolved(topicState.getTopicProblemsSolved()+1);
        // I have no idea what this now time is coming from.   Its always 0 and
        // causes bad calculations
//        long now = this.getTime();
        long now = this.getProbElapsedTime();
        // check if the previous event was a hint and problem not solved yet.  If so, then update hint_time
        if ((this.isLastEvent(HINT_EVENT)) && (! isProblemSolved())) {
            long curr_hint_time = this.getTimeInHintsBeforeCorrect();
            long extra_hint_time = probElapsed - this.getHintStartTime();
            problemState.setTimeInHintsBeforeCorrect(curr_hint_time + extra_hint_time);
        }
        this.setLastAnswer(answer);
        if ( ! isProblemSolved())
            problemState.setNumAttemptsOnCurProblem(problemState.getNumAttemptsOnCurProblem() + 1);
        long lastAttemptTime = this.getAttemptStartTime();
        problemState.setAttemptStartTime(this.getTime());
        problemState.setProblemIdleTime(0);
        problemState.setHintStartTime(-1); // once an attempt is given, we need to reset the last hint
//        this.setCurHint(null);
//        this.setCurHintId(-1);
        problemState.setLastEvent(ATTEMPT_EVENT);
        if (this.getTimeToFirstAttempt() == -1) {
            problemState.setTimeToFirstAttempt(probElapsed);
        }
        if (this.getTimeToFirstEvent() < 0) {
            problemState.setTimeToFirstEvent(probElapsed);
        }
        // for jeff
        if (this.getFirstEvent() == null) {
            if (isCorrect)
                problemState.setFirstEvent(CORRECT_ATTEMPT_EVENT);
            else
                problemState.setFirstEvent(INCORRECT_ATTEMPT_EVENT);
            problemState.setTimeToFirstEvent(probElapsed);
        }
        if (isCorrect) {
            // for jeff  - when correct, note the number of hints prior
            problemState.setNumHintsBeforeCorrect(this.getNumHintsGivenOnCurProblem());
            problemState.setProblemSolved(true);
            problemState.setTimeToSolve(this.getProbElapsedTime());

        }
        else if (! isProblemSolved()) {
            problemState.setNumMistakesOnCurProblem(problemState.getNumMistakesOnCurProblem() + 1);
        }
        if (lastAttemptTime == -1 && !previouslySolved ) {
            problemState.setAttemptStartTime(now);
            problemState.setCurProblemAvgTimeBetweenAttempts(probElapsed);
        } else if (!previouslySolved) {
            long diff = now - lastAttemptTime;
            double x = updateRunningAverage(problemState.getCurProblemAvgTimeBetweenAttempts(), diff);
            problemState.setCurProblemAvgTimeBetweenAttempts(x);
        }
        if (!previouslySolved)
            smgr.getStudentModel().getStudentProblemHistory().attempt(smgr,isCorrect,probElapsed);

    }

    private double updateRunningAverage(double runningAvg, double curVal) {
        if (problemState.getNumAttemptsOnCurProblem() == 0) {
            // when a variable's curVal has an initialization value of -1 , we want to return 0 for the avg.
            if (curVal < 0)
                return 0.0;
            else
                return curVal;
        } else if (problemState.getNumAttemptsOnCurProblem() == 1)
            return curVal;
        else
            return (runningAvg * problemState.getNumAttemptsOnCurProblem() + curVal) / (problemState.getNumAttemptsOnCurProblem() + 1);

    }






































    /**
     * A utility that clears student state so that the tutor hut can be re-entered as if the student
     * has never used the tutor hut.    The WoAdmin tools allow teachers/admins to make this happen.
     * @throws java.sql.SQLException
     */

    public void clearTutorHutState() throws SQLException {
        ProblemState.clearState(conn, objid);
        TopicState.clearState(conn, objid);
        SessionState.clearState(conn, objid);
        InterventionState.clearState(conn,objid);
        WorkspaceState.clearState(conn,objid);
    }

    public WorkspaceState getWorkspaceState () {
        return this.workspaceState;
    }


    public TopicState getTopicState() {
        return topicState;
    }

    public SessionState getSessionState () {
        return sessionState;
    }

    public ProblemState getProblemState () {
        return this.problemState;
    }

    public PrePostState getPrePostState() {
        return ppState;
    }


    public boolean curTopicHasEasierProblem() {
        return topicState.curTopicHasEasierProblem();
    }

    public boolean curTopicHasHarderProblem() {
        return topicState.curTopicHasHarderProblem();
    }


    public void setCurTopicHasEasierProblem(boolean b) throws SQLException {
        topicState.setCurTopicHasEasierProblem(b);
    }

    public void setCurTopicHasHarderProblem(boolean b) throws SQLException {
        topicState.setCurTopicHasHarderProblem(b);
    }

    public String getNextProblemDesiredDifficulty() {
       return sessionState.getNextProblemDesiredDifficulty();
    }

    public void setNextProblemDesiredDifficulty(String diff) throws SQLException {
        sessionState.setNextProblemDesiredDifficulty(diff);
    }

    public int getNumProbsSinceLastIntervention() {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    public void setNumProbsSinceLastIntervention(int i) {
        //To change body of created methods use File | Settings | File Templates.
    }


}
