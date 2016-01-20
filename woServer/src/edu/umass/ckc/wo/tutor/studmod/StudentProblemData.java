package edu.umass.ckc.wo.tutor.studmod;

import edu.umass.ckc.wo.content.Problem;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: 6/13/12
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class StudentProblemData {
    private int id;
    private int probId;
    private int topicId;
    private int sessId;
    private long problemBeginTime;
    private long problemEndTime;
    private long timeInSession;
    private long timeInTutor;
    private long timeToFirstAttempt;
    private long timeToFirstHint;
    private long timeToSolve;
    private int numHints;
    private int numHintsBeforeCorrect;
    private int numHelpAids;
    private int numMistakes;
    private int numAttemptsToSolve;
    private boolean givenAnswerHint=false;
    private String mode;
    private double mastery;
    private String emotion;
    private int emotionLevel;
    private String effort;
    private boolean isSolved=false;
    private boolean seenVideo=false;
    private boolean seenExample=false;
    private boolean usedTextReader=false;

    public StudentProblemData () {}

    public StudentProblemData(int probId, int topicId, int sessId, long problemBeginTime, long timeInSession, long timeInTutor, String mode) {
        this.probId = probId;
        this.topicId = topicId;
        this.sessId = sessId;
        this.problemBeginTime = problemBeginTime;
        this.timeInSession = timeInSession;
        this.timeInTutor = timeInTutor;
        this.mode = mode;
    }

    public StudentProblemData(int probId, int topicId, int sessId, long problemBeginTime, long problemEndTime, long timeInSession, long timeInTutor,
                              long timeToFirstAttempt, long timeToFirstHint, long timeToSolve, int numHintsBeforeCorrect, int numHelpAids, int numMistakes,
                              int numAttemptsToSolve, boolean givenAnswerHint, String mode, double mastery, String emotion, int emotionLevel, String effort,
                              boolean seenVideo, boolean seenExample, boolean usedTextReader) {
        this.probId = probId;
        this.topicId = topicId;
        this.sessId = sessId;
        this.problemBeginTime = problemBeginTime;
        this.problemEndTime = problemEndTime;
        this.timeInSession = timeInSession;
        this.timeInTutor = timeInTutor;
        this.timeToFirstAttempt = timeToFirstAttempt;
        this.timeToFirstHint = timeToFirstHint;
        this.timeToSolve = timeToSolve;
        this.numHintsBeforeCorrect = numHintsBeforeCorrect;
        this.numHelpAids = numHelpAids;
        this.numMistakes = numMistakes;
        this.numAttemptsToSolve = numAttemptsToSolve;
        this.givenAnswerHint = givenAnswerHint;
        this.mode = mode;
        this.mastery = mastery;
        this.emotion = emotion;
        this.emotionLevel = emotionLevel;
        this.effort = effort;
        this.seenVideo=seenVideo;
        this.seenExample = seenExample;
        this.usedTextReader=usedTextReader;

    }

    public void setEffort(String effort) {
        this.effort = effort;
    }

    public void setEmotionLevel(int emotionLevel) {
        this.emotionLevel = emotionLevel;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public void setMastery(double mastery) {
        this.mastery = mastery;
    }

    public void setGivenAnswerHint(boolean givenAnswerHint) {
        this.givenAnswerHint = givenAnswerHint;
    }

    public void setNumAttemptsToSolve(int numAttemptsToSolve) {
        this.numAttemptsToSolve = numAttemptsToSolve;
    }

    public void setNumMistakes(int numMistakes) {
        this.numMistakes = numMistakes;
    }

    public void setNumHelpAids(int numHelpAids) {
        this.numHelpAids = numHelpAids;
    }

    public void setNumHintsBeforeCorrect(int numHintsBeforeCorrect) {
        this.numHintsBeforeCorrect = numHintsBeforeCorrect;
    }

    public void setTimeToSolve(long timeToSolve) {
        this.timeToSolve = timeToSolve;
    }

    public void setTimeToFirstHint(long timeToFirstHint) {
        this.timeToFirstHint = timeToFirstHint;
    }

    public void setTimeToFirstAttempt(long timeToFirstAttempt) {
        this.timeToFirstAttempt = timeToFirstAttempt;
    }

    public void setProblemEndTime(long problemEndTime) {
        this.problemEndTime = problemEndTime;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

    public int getProbId() {
        return probId;
    }

    public int getTopicId() {
        return topicId;
    }

    public int getSessId() {
        return sessId;
    }

    public long getProblemBeginTime() {
        return problemBeginTime;
    }

    public long getProblemEndTime() {
        return problemEndTime;
    }

    public long getTimeInSession() {
        return timeInSession;
    }

    public long getTimeInTutor() {
        return timeInTutor;
    }

    public long getTimeToFirstAttempt() {
        return timeToFirstAttempt;
    }

    public long getTimeToFirstHint() {
        return timeToFirstHint;
    }

    public long getTimeToSolve() {
        return timeToSolve;
    }

    public int getNumHintsBeforeCorrect() {
        return numHintsBeforeCorrect;
    }

    public int getNumHelpAids() {
        return numHelpAids;
    }

    public int getNumMistakes() {
        return numMistakes;
    }

    public int getNumAttemptsToSolve() {
        return numAttemptsToSolve;
    }

    public boolean getGivenAnswerHint() {
        return givenAnswerHint;
    }

    public String getMode() {
        return mode;
    }

    public double getMastery() {
        return mastery;
    }

    public String getEmotion() {
        return emotion;
    }

    public int getEmotionLevel() {
        return emotionLevel;
    }

    public String getEffort() {
        return effort;
    }

    public void setSeenVideo(boolean seenVideo) {
        this.seenVideo = seenVideo;
    }

    public void setSeenExample(boolean seenExample) {
        this.seenExample = seenExample;
    }

    public void setUsedTextReader(boolean usedTextReader) {
        this.usedTextReader = usedTextReader;
    }

    public void setProbId(int probId) {
        this.probId = probId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public void setSessId(int sessId) {
        this.sessId = sessId;
    }

    public void setProblemBeginTime(long problemBeginTime) {
        this.problemBeginTime = problemBeginTime;
    }

    public void setTimeInSession(long timeInSession) {
        this.timeInSession = timeInSession;
    }

    public void setTimeInTutor(long timeInTutor) {
        this.timeInTutor = timeInTutor;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNumHints(int numHints) {
        this.numHints = numHints;
    }

    public int getNumHints () {
        return this.numHints;
    }

    public boolean isPracticeProblem() {
        return this.mode != null && this.mode.equals(Problem.PRACTICE) ;
    }

    public boolean isTopicIntro () {
        return this.mode.equals(Problem.TOPIC_INTRO);
    }

    public boolean isDemo () {
        return this.mode.equals(Problem.DEMO);
    }

    public boolean isExample () {
        return this.mode.equals(Problem.EXAMPLE);
    }

    public long getTimeInProblemSeconds () {
        return Math.max(0,this.getProblemEndTime() - this.getProblemBeginTime()) / 1000;
    }
}
