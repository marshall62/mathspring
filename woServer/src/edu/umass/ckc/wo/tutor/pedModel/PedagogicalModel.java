package edu.umass.ckc.wo.tutor.pedModel;

import ckc.servlet.servbase.BaseServlet;
import edu.umass.ckc.email.Emailer;
import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.config.LessonXML;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.content.ProblemAnswer;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbUserPedagogyParams;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.interventions.SelectHintSpecs;
import edu.umass.ckc.wo.log.TutorLogger;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutor.intervSel2.*;
import edu.umass.ckc.wo.tutor.model.*;
import edu.umass.ckc.wo.tutor.probSel.ChallengeModeProblemSelector;
import edu.umass.ckc.wo.tutor.probSel.LessonModelParameters;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import edu.umass.ckc.wo.tutor.probSel.ReviewModeProblemSelector;
import edu.umass.ckc.wo.tutor.response.HintResponse;
import edu.umass.ckc.wo.tutor.response.InternalEvent;
import edu.umass.ckc.wo.tutor.response.ProblemResponse;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutor.studmod.StudentProblemData;
import edu.umass.ckc.wo.tutormeta.*;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Abstract class that defines the basic event processing of a pedagogical model used in the TutorHut
 *
 * User: marshall
 * Date: Dec 3, 2008
 * Time: 9:56:34 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PedagogicalModel implements TutorEventProcessor { // extends PedagogicalModelOld {

    private static Logger logger = Logger.getLogger(PedagogicalModel.class);

    public static final String CHALLENGE_MODE = "challenge";
    public static final String REVIEW_MODE = "review";
    protected Pedagogy pedagogy;
    protected LessonModel lessonModel;
    protected PedagogicalModelParameters params;
    protected LessonModelParameters lessonModelParameters;

    protected StudentModel studentModel;
    protected ProblemSelector problemSelector ;// problem selection is a pluggable strategy
    protected HintSelector hintSelector;  // hint selection is a pluggable strategy
    protected SessionManager smgr;  // object that contains state and session info and db connection
    protected LearningCompanion learningCompanion=null; // an optional LearningCompanion (null if none)
    protected AttemptInterventionSelector attemptInterventionSelector=null; // an optional intervention selector (can be null)
    protected NextProblemInterventionSelector nextProblemInterventionSelector=null; // an optional intervention selector (can be null)
    protected ExampleSelector exampleSelector=null; // an optional example selector (can be null)
    protected VideoSelector videoSelector=null; // an optional video selector (can be null)
    protected ChallengeModeProblemSelector challengeModeSelector;
    protected ReviewModeProblemSelector reviewModeSelector;
    protected ProblemGrader problemGrader;
    protected ProblemScore lastProblemScore;
    protected InterventionGroup interventionGroup;
    private TutorModel tutorModel; // temporarily here until we build the correct set of models

//    public PedagogicalModelParameters setParams(PedagogicalModelParameters classParams, PedagogicalModelParameters defaultParams) {
//        defaultParams.overload(classParams);
//        return defaultParams;
//    }

    public LessonModel getLessonModel () {
        return this.lessonModel;
    }

    public PedagogicalModelParameters getParams () {
        return this.params;
    }

    public void setParams(PedagogicalModelParameters params) {
        this.params = params;
    }

    public void setStudentModel(StudentModel studentModel) {
        this.studentModel = studentModel;
    }

    public void setProblemSelector(ProblemSelector problemSelector) {
        this.problemSelector = problemSelector;
    }

    public void setHintSelector(HintSelector hintSelector) {
        this.hintSelector = hintSelector;
    }

    public HintSelector getHintSelector() {
        return hintSelector;
    }

    public void setSmgr(SessionManager smgr) {
        this.smgr = smgr;
    }

    public void setAttemptInterventionSelector(AttemptInterventionSelector attemptInterventionSelector) {
        this.attemptInterventionSelector = attemptInterventionSelector;
    }

    public void setNextProblemInterventionSelector(NextProblemInterventionSelector nextProblemInterventionSelector) {
        this.nextProblemInterventionSelector = nextProblemInterventionSelector;
    }

    public void setExampleSelector(ExampleSelector exampleSelector) {
        this.exampleSelector = exampleSelector;
    }

    public void setVideoSelector(VideoSelector videoSelector) {
        this.videoSelector = videoSelector;
    }

    public void setChallengeModeSelector(ChallengeModeProblemSelector challengeModeSelector) {
        this.challengeModeSelector = challengeModeSelector;
    }

    public void setReviewModeSelector(ReviewModeProblemSelector reviewModeSelector) {
        this.reviewModeSelector = reviewModeSelector;
    }

    public StudentModel getStudentModel() {
        return studentModel;
    }

    public SessionManager getSessionMgr () {
        return smgr;
    }


    @Override
    public Response processInternalEvent(InternalEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Handle a TutorHutEvent and dispatch to the abstract method which handles it.  Each PedagogicalModel will
     * will have these processing methods plus potentially two others when an intervention selector is part of the pedagogy.
     * When an intervention selector is part of the pedagogy, the InterventionPedagogicalModel overrides this to handle
     * the intervention events and also calls this method to handle the basic processing of the events below.
     * @param e
     * @return
     * @throws Exception
     *
     */
    @Override
    public Response processUserEvent(TutorHutEvent e) throws Exception {
        Response r = null;
        StudentState state = smgr.getStudentState();
        // make sure probElapseTime is saved on each event containing one
        if (e instanceof IntraProblemEvent)
            smgr.getStudentState().setProbElapsedTime(((IntraProblemEvent) e).getProbElapsedTime());
        // Formality problem attempts handled separately from standard attempts


        if (e instanceof AttemptEvent) {
            r = processAttempt((AttemptEvent) e);
            studentModel.save();
            return r;
        }


        else if (e instanceof NextProblemEvent)  {
            NextProblemEvent ee = (NextProblemEvent)  e;
            //  I think the only way we arrive at this with forceProblem=true is from the tool for test-users that allows problem selection from dialog
            if (ee.isForceProblem())
                r = processStudentSelectsProblemRequest(ee);
            else if (ee.getMode().equalsIgnoreCase(CHALLENGE_MODE) || state.isInChallengeMode())
                r = processChallengeModeNextProblemRequest(ee);
            else if (ee.getMode().equalsIgnoreCase(REVIEW_MODE) || state.isInReviewMode())
                r = processReviewModeNextProblemRequest(ee);
            else r = processNextProblemRequest((NextProblemEvent) e);
            studentModel.save();
            return r;
        }



        else if (e instanceof HintEvent) {
            r =  processHintRequest((HintEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof ShowExampleEvent) {
            r = processShowExampleRequest((ShowExampleEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof ShowVideoEvent) {
            r = processShowVideoRequest((ShowVideoEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof ShowInstructionsEvent) {
            new TutorLogger(smgr).logIntraProblemEvent((IntraProblemEvent) e,"ShowInstructions", r);
            studentModel.save();
            return r;
        }
        else if (e instanceof ShowGlossaryEvent) {
            new TutorLogger(smgr).logIntraProblemEvent((IntraProblemEvent) e,"ShowGlossary", r);
            studentModel.save();
            return r;
        }
        else if (e instanceof ShowFormulasEvent) {
            new TutorLogger(smgr).logIntraProblemEvent((IntraProblemEvent) e,"ShowFormulas", r);
            studentModel.save();
            return r;
        }


        else if (e instanceof BeginProblemEvent) {
            r = processBeginProblemEvent((BeginProblemEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof ResumeProblemEvent) {
            r = processResumeProblemEvent((ResumeProblemEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof BeginExternalActivityEvent) {
            r = processBeginExternalActivityEvent((BeginExternalActivityEvent) e);
            studentModel.save();
            return r;
        }

        else if (e instanceof EndProblemEvent) {
            r = processEndProblemEvent((EndProblemEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof EndExternalActivityEvent) {
            r = processEndExternalActivityEvent((EndExternalActivityEvent) e);
            studentModel.save();
            return r;
        }

        else if (e instanceof ClickCharacterEvent) {
            r = processClickCharacterEvent((ClickCharacterEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof MuteCharacterEvent) {
            r = processMuteCharacterEvent((MuteCharacterEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof UnMuteCharacterEvent) {
            r = processUnMuteCharacterEvent((UnMuteCharacterEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof EliminateCharacterEvent) {
            r = processEliminateCharacterEvent((EliminateCharacterEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof ShowCharacterEvent) {
            r = processShowCharacterEvent((ShowCharacterEvent) e);
            studentModel.save();
            return r;
        }

        else if (e instanceof ReadProblemEvent) {
            r = processReadProblemEvent((ReadProblemEvent) e);
            studentModel.save();
            return r;
        }

        else if (e instanceof ReportErrorEvent) {
            r = new Response();
            new TutorLogger(smgr).logReportedError((ReportErrorEvent) e);
            int sessId = e.getSessionId();
            Emailer.sendErrorEmail(BaseServlet.adminEmail, BaseServlet.emailServer, "wayang error for session: " + sessId, ((ReportErrorEvent) e).getMessage(), null);
            studentModel.save();
            return r;
        }
        else if (e instanceof BeginInterventionEvent) {
            r = processBeginInterventionEvent((BeginInterventionEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof EndInterventionEvent) {
            r = processEndInterventionEvent((EndInterventionEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof BeginExampleEvent) {
            r = processBeginExampleEvent((BeginExampleEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof EndExampleEvent) {
            r = processEndExampleEvent((EndExampleEvent) e);
            studentModel.save();
            return r;
        }                                 /*
        else if (e instanceof ContinueNextProblemInterventionEvent) {
            r = processContinueNextProblemInterventionEvent((ContinueNextProblemInterventionEvent) e);
            studentModel.save();
            return r;
        }                               */
        else if (e instanceof InterventionTimeoutEvent){
            r = processInterventionTimeoutEvent((InterventionTimeoutEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof ContinueAttemptInterventionEvent) {
            r = processContinueAttemptInterventionEvent((ContinueAttemptInterventionEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof InputResponseNextProblemInterventionEvent) {
            r = processInputResponseNextProblemInterventionEvent((InputResponseNextProblemInterventionEvent) e);
            studentModel.save();
            return r;
        }
        else if (e instanceof InputResponseAttemptInterventionEvent) {
            r = processInputResponseAttemptInterventionEvent((InputResponseAttemptInterventionEvent) e);
            studentModel.save();
            return r;
        }

        else if (e instanceof ClearSessionLogEntriesEvent) {
            new TutorLogger(smgr).clearSessionLog(smgr.getSessionNum());
            studentModel.save();
            return new Response();
        }
        else if (e instanceof ClearUserPropertiesEvent) {
            smgr.clearUserProperties();
            studentModel.save();
            return new Response();
        }


        else return new Response("Unknown Event");

    }






    // results:  AttemptResponse | InterventionResponse
    public abstract Response processAttempt (AttemptEvent e) throws Exception;

    public abstract Response processBeginProblemEvent (BeginProblemEvent e) throws Exception;
    public abstract Response processEndProblemEvent (EndProblemEvent e) throws Exception;
    public abstract Response processResumeProblemEvent (ResumeProblemEvent e) throws Exception;

    // results: HintResponse | InterventionResponse
    public abstract Response processHintRequest (HintEvent e) throws Exception;
    public abstract Response processShowExampleRequest (ShowExampleEvent e) throws Exception;
    public abstract Response processShowVideoRequest (ShowVideoEvent e) throws Exception;

    // results: ProblemResponse | InterventionResponse
    public abstract Response processNextProblemRequest (NextProblemEvent e) throws Exception;
    public abstract Response processStudentSelectsProblemRequest (NextProblemEvent e) throws Exception;
    public abstract Response processMPPSelectProblemRequest (NextProblemEvent e) throws Exception;
    public abstract Response processChallengeModeNextProblemRequest (NextProblemEvent e) throws Exception;
    public abstract Response processReviewModeNextProblemRequest (NextProblemEvent e) throws Exception;

    public abstract ProblemResponse getNextProblem(NextProblemEvent e) throws Exception;

//    protected abstract Response startTutor(EnterTutorEvent e) throws Exception ;


    // The reason we have these two is because a continue or inputResponse may require
    // the pedagogical model to call its wrapped PMs to select a problem
//    public abstract Response processContinueRequestInternal(ContinueEvent e) throws Exception;
//    public abstract Response processInputResponseInternal(InputResponseEvent e) throws Exception;

    public abstract Response processBeginInterventionEvent (BeginInterventionEvent e)  throws Exception;
    public abstract Response processEndInterventionEvent (EndInterventionEvent e)  throws Exception;
    public abstract Response processBeginExampleEvent (BeginExampleEvent e)  throws Exception;
    public abstract Response processEndExampleEvent (EndExampleEvent e)  throws Exception;
    public abstract Response processBeginExternalActivityEvent (BeginExternalActivityEvent e) throws Exception;
    public abstract Response processEndExternalActivityEvent (EndExternalActivityEvent e) throws Exception;

    public abstract Response processClickCharacterEvent (ClickCharacterEvent e)  throws Exception;
    public abstract Response processMuteCharacterEvent (MuteCharacterEvent e)  throws Exception;
    public abstract Response processUnMuteCharacterEvent (UnMuteCharacterEvent e)  throws Exception;
    public abstract Response processEliminateCharacterEvent (EliminateCharacterEvent e)  throws Exception;
    public abstract Response processShowCharacterEvent (ShowCharacterEvent e)  throws Exception;

    public abstract Response processReadProblemEvent(ReadProblemEvent e) throws Exception;



   // public abstract Response processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception;
    public abstract Response processInterventionTimeoutEvent(InterventionTimeoutEvent e) throws Exception;
    public abstract Response processContinueAttemptInterventionEvent(ContinueAttemptInterventionEvent e) throws Exception;
    public abstract Response processInputResponseAttemptInterventionEvent(InputResponseAttemptInterventionEvent e) throws Exception;
    public abstract Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception;






    /** These two methods are called each time a pedagogical model makes a problem/hint selection as a result
     * of an intervention selector requesting that a problem/hint be given in response to an intervention.
     *  If the pedagogical model that owns the intervention selector does not have a problem selector/hint selector
     * it must pass the request to the decorated pedagogical model through one of these two methods.
     *
     * Note:  By default, the BasePedagogicalModel (subclass of this) provides the default behavior of calling the
     * decorated PedModel.
     *
     * @param selectionCriteria
     * @return
     * @throws Exception
     */
    public abstract HintResponse doSelectHint (SelectHintSpecs selectionCriteria) throws Exception;



    private boolean findAnswerMatch (List<ProblemAnswer> possible, String studentInput) {
        for (ProblemAnswer a: possible) {
            if (a.grade(studentInput))
                return true;
        }
        return false;
    }

    public boolean isAttemptCorrect (int probId, String userInput) throws SQLException {
        //Problem p = new DbProblem().getProblem(smgr.getConnection(),probId);
        Problem p = ProblemMgr.getProblem(probId);

        // Note:  Auth tool has two places to put answer for a short answer problem.  If only one answer is
        // put in, it can be fetched using p.getAnswer().   If there are multiple forms of the answer, then use
        // p.getAnswers().   Code below checks for which one was used and then uses it/them for grading the user input
        if (p.isShortAnswer() && !p.isParametrized()) {
            List<ProblemAnswer> possibleAnswers = null;
            if (p.getAnswers().size()== 0) {
                possibleAnswers = new ArrayList<ProblemAnswer>();
                possibleAnswers.add(new ProblemAnswer(p.getAnswer(),probId));
            }
            else possibleAnswers = p.getAnswers();
            return findAnswerMatch(possibleAnswers,userInput);
        }
        else if (p != null) {
            if (p.isParametrized()) {
                if (p.isMultiChoice())
                    return smgr.getStudentState().getProblemAnswer().equalsIgnoreCase(userInput.trim());
                else {
                    // Get the list and check if one element is equal to student input.  This list comes from the StudentState
                    // because it depends on the bindings selected for this problem and this student
                    List<String> possibleInputs = smgr.getStudentState().getPossibleShortAnswers();
                    List<ProblemAnswer> correctAnswers = new ArrayList<ProblemAnswer>();
                    // turn the String coming out of the student state into simple ProblemAnswer objects so we can use the grade method
                    for (String a: possibleInputs) {
                        correctAnswers.add(new ProblemAnswer(a.replaceAll("\\s+",""),probId));
                    }
                    return findAnswerMatch(correctAnswers,userInput);
                }
            }
            return p.getAnswer().equalsIgnoreCase(userInput.replaceAll("\\s+",""));
        }
        return false;
    }


    public LearningCompanion getLearningCompanion() {
        return learningCompanion;
    }

    public void setLearningCompanion(LearningCompanion learningCompanion) {
        this.learningCompanion = learningCompanion;
    }

    protected PedagogicalModelParameters getPedagogicalModelParametersForUser(Connection connection, Pedagogy ped, int classId, int studId) throws SQLException {

        // first we get the parameters out of the Pedagogy as defined in the XML pedagogies.xml
        PedagogicalModelParameters defaultParams = ped.getParams();
        // If this is a configurable pedagogy (meaning that it can be given some parameters to guide its behavior),  then
        // see if this user has a set of parameters and if so use them to configure the pedagogy.
        // these params come from settings in the WoAdmin tool for the class.
        PedagogicalModelParameters classParams = DbClass.getPedagogicalModelParameters(connection, classId);
        // overload the defaults with stuff defined for the class.
        defaultParams.overload(classParams);
//       if (this.pedagogicalModel instanceof ConfigurablePedagogy) {
        // these params are the ones that were passed in by Assistments and saved for the user
        PedagogyParams userParams = DbUserPedagogyParams.getPedagogyParams(connection, studId);
        // overload the params with anything provided for the user.
        defaultParams.overload(userParams);
        return defaultParams;
    }



    protected LessonModelParameters getLessonModelParametersForUser(Connection connection, Pedagogy ped, int classId, int studId) throws SQLException {

        String lessonName = ped.getLessonName();
        LessonXML lx =  Settings.lessonMap.get(lessonName);
        // first we get the parameters out of the Pedagogy's lesson as defined in the XML lessons.xml
        lessonModelParameters = lx.getLessonModelParams();

        // If this is a configurable pedagogy (meaning that it can be given some parameters to guide its behavior),  then
        // see if this user has a set of parameters and if so use them to configure the pedagogy.
        // these params come from settings in the WoAdmin tool for the class.
        LessonModelParameters classParams = DbClass.getLessonModelParameters(connection, classId);
        // overload the defaults with stuff defined for the class.
        lessonModelParameters.overload(classParams);
//       if (this.pedagogicalModel instanceof ConfigurablePedagogy) {
        // these params are the ones that were passed in by Assistments and saved for the user

        PedagogyParams userParams = DbUserPedagogyParams.getPedagogyParams(connection, studId);
        lessonModelParameters.overload(userParams);
        // overload the params with anything provided for the user.
//        defaultParams.overload(userParams);
        return lessonModelParameters;
    }

    /**
     * Get a list of problems the student has solved or seen as an example.   Works using the problemReuseInterval which is a number of
     * sessions or days.  We only select problems within the interval.  This is a way to control recency.   We want solved problems and examples to be eligible to
     * show again after a certain number of sessions or days (ideally this number might be determined on a per student basis but for now it lives in the pedagogy
     * definition)
     * @return
     * @throws Exception
     */
    public List<Integer> getRecentExamplesAndCorrectlySolvedProblems (List<StudentProblemData> probEncountersInTopic) throws Exception {
        // get the ones that are within the problemReuseInterval
        List<Integer> probs = new ArrayList<Integer>();
        int nSessionReuseInterval = this.params.getProblemReuseIntervalSessions();
        int nDayReuseInterval = this.params.getProblemReuseIntervalDays();
        int sess = smgr.getSessionNum();
        Date now = new Date(System.currentTimeMillis());
        int numSessions=0;
        for (StudentProblemData d: probEncountersInTopic) {
            Date probBeginTime = new Date(d.getProblemBeginTime());
            if (d.getSessId() != sess) {
                numSessions++;
                sess = d.getSessId();
            }
            int dayDiff = computeDayDiff(now,probBeginTime);
            // We stop when one of the intervals is reached
            if (numSessions == nSessionReuseInterval || dayDiff >= nDayReuseInterval)
                break;
            if (d.isSolved())
                probs.add(d.getProbId());
            else if (d.getMode().equals(Problem.DEMO))
                probs.add(d.getProbId());
        }
        return probs;
    }

    /**
     * Returns ids of problems that have been given to the student.  Problems considered "seen" must be within
     * the problem reuse interval specified for the pedagogy and class.
     * @param probEncountersInTopic
     * @return
     * @throws Exception
     */
    public List<Integer> getPracticeProblemsSeen (List<StudentProblemData> probEncountersInTopic) throws Exception {

        // get the ones that are within the problemReuseInterval
        List<Integer> probs = new ArrayList<Integer>();
        int nSessionReuseInterval = this.params.getProblemReuseIntervalSessions();
        int nDayReuseInterval = this.params.getProblemReuseIntervalDays();
        int sess = smgr.getSessionNum();
        Date now = new Date(System.currentTimeMillis());
        int numSessions=0;
        for (StudentProblemData d: probEncountersInTopic) {
            Date probBeginTime = new Date(d.getProblemBeginTime());
            if (d.getSessId() != sess) {
                numSessions++;
                sess = d.getSessId();
            }
            int dayDiff = computeDayDiff(now,probBeginTime);
            if (numSessions == nSessionReuseInterval || dayDiff >= nDayReuseInterval)
                break;
            if (d.isPracticeProblem())
                probs.add(d.getProbId());

        }
        return probs;

    }

    private int computeDayDiff(Date now, Date probBeginTime) {
        long msDif = now.getTime() - probBeginTime.getTime();
        long secs = msDif / 1000;
        long mins = secs / 60;
        long hrs = mins / 60;
        int days = (int) hrs / 24;
        return days;
    }







    public void setChallengeModeProblemSelector (ChallengeModeProblemSelector challengeModeProblemSelector)  {
        this.challengeModeSelector = challengeModeProblemSelector;
    }

    public void setReviewModeProblemSelector (ReviewModeProblemSelector reviewModeProblemSelector)  {
        this.reviewModeSelector = reviewModeProblemSelector;
    }

    public ChallengeModeProblemSelector getChallengeModeSelector () {
        return this.challengeModeSelector;
    }

    public ReviewModeProblemSelector getReviewModeSelector () {
        return this.reviewModeSelector;
    }

    public ProblemSelector getProblemSelector () {
        return this.problemSelector;
    }


    public abstract void newSession (int sessionId) throws SQLException;


    /**
     * The ped model should not show the MPP if there is a nextprob intervention selector that determines when MPP shows.
     * If no intervention selector of this sort, then show MPP is determined by a flag in the pedagogy.
     * @return
     */
    public boolean isShowMPP() {
        // note: this used to return false if there was an intervention in the pedagogical model that turned the MPP on and off.
        // That behavior is no longer requested, so we simply return true so that MPP always shows.

        return true;
    }

    public TutorModel getTutorModel() {
        return tutorModel;
    }

    public void setTutorModel(TutorModel tutorModel) {
        this.tutorModel = tutorModel;
    }

    public ProblemGrader getProblemGrader() {
        return problemGrader;
    }

    public ProblemScore getLastProblemScore() {
        return lastProblemScore;
    }

    public Pedagogy getPedagogy () {
        return this.pedagogy;
    }


    public abstract void addPedagogicalMoveListener(PedagogicalMoveListener pml);
}
