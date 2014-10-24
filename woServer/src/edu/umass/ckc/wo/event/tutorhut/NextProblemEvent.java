package edu.umass.ckc.wo.event.tutorhut;

import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.content.Lesson;
import edu.umass.ckc.wo.content.Problem;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: Nov 25, 2008
 * Time: 9:59:59 AM
 * Sent when user clicks the "next problem" button.
 * Event action: nextProblem 
 * Event params:  sessId , elapsedTime (both handled by inheritance)
 */
public class NextProblemEvent extends IntraProblemEvent {
    // for debugging
    public static final String FORCE_INTRO = "forceIntro";
    public static final String FORCE_INTERVENTION = "forceIntervention";
    public static final String DEBUG_INTERVENTION = "debugIntervention";
    public static final String PROB_NAME = "probName";
    public static final String PROB_ID = "probID";
    public static final String TOPIC = "topic";
    public static final String MODE = "mode";
    public static final String PROB_MODE = "probMode";
    public static final String EXTERNAL = "isExternal";

    private boolean intervene;
    private boolean forceIntro=false;
    private String debugInterv = null;
    private String probName=null;
    private String probId=null;
    private String mode=null;
    private String probMode=null;
    private boolean isExternal=true;
    private int topicToForce=-1 ;

    private Lesson lesson; // a way to force a Lesson

    public NextProblemEvent (ServletParams p) throws Exception {
        super(p);
        String interv = p.getString(FORCE_INTERVENTION,"false");
        String intro = p.getString(FORCE_INTRO,"false");
        debugInterv = p.getString(DEBUG_INTERVENTION,null);
        probName = p.getString(PROB_NAME,null);
        if (probName != null  && probName.equals(""))
            probName = null;
        mode = p.getString(MODE,null); // is practice, challenge, or review
        probMode = p.getString(PROB_MODE, null);

        probId = p.getString(PROB_ID,null);
        if (probId != null && ( probId.equalsIgnoreCase("undefined") || probId.equals("") || probId.equals("-1")))
            probId = null;
        topicToForce = p.getInt(TOPIC,-1);
        isExternal=p.getBoolean(EXTERNAL,false);   // if external not passed, problem will not be external

        setIntervene(Boolean.parseBoolean(interv));
        setForceIntro(Boolean.parseBoolean(intro));
    }

    public NextProblemEvent (long elapsedTime, long probElapsedTime) {
        this.probElapsedTime = probElapsedTime;
        this.elapsedTime = elapsedTime;
    }

    public NextProblemEvent (long elapsedTime, long probElapsedTime, Lesson l) {
        this.probElapsedTime = probElapsedTime;
        this.elapsedTime = elapsedTime;
        this.lesson = l;
    }

    public NextProblemEvent (long elapsedTime, long probElapsedTime, String probId) {
        this(elapsedTime,probElapsedTime);
        this.probId = probId;
    }

    public NextProblemEvent (long elapsedTime, long probElapsedTime, int topicToForce) {
        this(elapsedTime,probElapsedTime);
        this.topicToForce = topicToForce;
    }

    // THis is used for requests coming from student to show a particular problem.   From MPP, it will
    // want to show it in Practice mode.   From Assistments it may want practice OR example/demo
    public NextProblemEvent(long elapsedTime, long probElapsedTime, String probId, String probMode) {
        this(elapsedTime,probElapsedTime,probId);
        this.probMode = probMode;
    }

    public boolean isIntervene() {
        return intervene;
    }

    public void setIntervene(boolean intervene) {
        this.intervene = intervene;
    }

    public boolean checkDebugIntervention (String value) {
        return (debugInterv != null && debugInterv.equals(value));
    }

    public String getMode() {
        return mode;
    }

    public String getProbName() {
        return probName;
    }

    public String getProbId() {
        return probId;
    }

    public void setProbId(String probId) {
        this.probId = probId;
    }

    public void setForceIntro(boolean forceIntro) {
        this.forceIntro = forceIntro;
    }

    public boolean isForceIntro () {
        return this.forceIntro;
    }

    public int getTopicToForce()  {
        return topicToForce ;
    }

    public void clearTopicToForce () {
        topicToForce = -1;
    }

    public boolean isForceTopic () {
        return topicToForce != -1;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean external) {
        isExternal = external;
    }

    public boolean isTutorMode () {
        return mode == null || (!mode.equalsIgnoreCase("challenge") && !mode.equals("review"));
    }

    public boolean isForceProblem() {
        return this.probName != null || this.probId != null;
    }

    public boolean isChallenge() {
        return mode != null && mode.equalsIgnoreCase("challenge");
    }

    public boolean isReview() {
        return mode != null && mode.equalsIgnoreCase("review");
    }

    public String getProbMode() {
        return probMode;
    }

    public boolean isDemoMode() {
        return probMode.equals(Problem.DEMO);
    }


    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setTopicToForce(int topicToForce) {
        this.topicToForce = topicToForce;
    }

    public Lesson getLesson() {
        return lesson;
    }
}
