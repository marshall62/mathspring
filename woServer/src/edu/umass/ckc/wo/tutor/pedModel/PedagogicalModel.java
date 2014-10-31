package edu.umass.ckc.wo.tutor.pedModel;

import ckc.servlet.servbase.BaseServlet;
import edu.umass.ckc.email.Emailer;
import edu.umass.ckc.wo.assistments.AssistmentsHandler;
import edu.umass.ckc.wo.assistments.AssistmentsUser;
import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Hint;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbAssistmentsUsers;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.event.tutorhut.EnterTutorEvent;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.interventions.SelectHintSpecs;
import edu.umass.ckc.wo.log.TutorLogger;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.intervSel2.AttemptInterventionSelector;
import edu.umass.ckc.wo.tutor.intervSel2.MyProgressPageIS;
import edu.umass.ckc.wo.tutor.intervSel2.NextProblemInterventionSelector;
import edu.umass.ckc.wo.tutor.probSel.ChallengeModeProblemSelector;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import edu.umass.ckc.wo.tutor.probSel.ReviewModeProblemSelector;
import edu.umass.ckc.wo.tutor.response.AttemptResponse;
import edu.umass.ckc.wo.tutor.response.HintResponse;
import edu.umass.ckc.wo.tutor.response.ProblemResponse;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.*;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;


/**
 * Abstract class that defines the basic event processing of a pedagogical model used in the TutorHut
 *
 * User: marshall
 * Date: Dec 3, 2008
 * Time: 9:56:34 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PedagogicalModel { // extends PedagogicalModelOld {

    private static Logger logger = Logger.getLogger(PedagogicalModel.class);

    public static final String CHALLENGE_MODE = "challenge";
    public static final String REVIEW_MODE = "review";

    protected PedagogicalModelParameters params;
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
    protected EndOfTopicInfo reasonsForEndOfTopic;

    public PedagogicalModelParameters setParams(PedagogicalModelParameters classParams, PedagogicalModelParameters defaultParams) {
        defaultParams.overload(classParams);
        return defaultParams;
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


    /**
     * Handle a TutorHutEvent and dispatch to the abstract method which handles it.  Each PedagogicalModel will
     * will have these processing methods plus potentially two others when an intervention selector is part of the pedagogy.
     * When an intervention selector is part of the pedagogy, the InterventionPedagogicalModel overrides this to handle
     * the intervention events and also calls this method to handle the basic processing of the events below.
     * @param e
     * @return
     * @throws Exception
     */
    public Response processEvent (TutorHutEvent e) throws Exception {
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
            r = processNextProblemRequest((NextProblemEvent) e);
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
        }
        else if (e instanceof ContinueNextProblemInterventionEvent) {
            r = processContinueNextProblemInterventionEvent((ContinueNextProblemInterventionEvent) e);
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



    public abstract ProblemResponse getProblemSelectedByStudent(NextProblemEvent e) throws Exception;
    public abstract ProblemResponse getProblemInTopicSelectedByStudent(NextProblemEvent e) throws Exception;
    public abstract ProblemResponse getChallengingProblem (NextProblemEvent e) throws Exception;
    public abstract ProblemResponse getReviewProblem (NextProblemEvent e) throws Exception;





    // results:  AttemptResponse | InterventionResponse
    public abstract Response processAttempt (AttemptEvent e) throws Exception;

    public abstract Response processBeginProblemEvent (BeginProblemEvent e) throws Exception;
    public abstract Response processEndProblemEvent (EndProblemEvent e) throws Exception;

    // results: HintResponse | InterventionResponse
    public abstract Response processHintRequest (HintEvent e) throws Exception;
    public abstract Response processShowExampleRequest (ShowExampleEvent e) throws Exception;
    public abstract Response processShowVideoRequest (ShowVideoEvent e) throws Exception;

    // results: ProblemResponse | InterventionResponse
    public abstract Response processNextProblemRequest (NextProblemEvent e) throws Exception;
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



    public abstract Response processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception;
    public abstract Response processContinueAttemptInterventionEvent(ContinueAttemptInterventionEvent e) throws Exception;
    public abstract Response processInputResponseAttemptInterventionEvent(InputResponseAttemptInterventionEvent e) throws Exception;
    public abstract Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception;


;

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
    protected abstract Problem doSelectChallengeProblem(NextProblemEvent e) throws Exception;
    protected abstract Problem doSelectReviewProblem (NextProblemEvent e) throws Exception;

    public abstract boolean isTopicContentAvailable (int topicId) throws Exception;


    public boolean isAttemptCorrect (int probId, String userInput) throws SQLException {
        //Problem p = new DbProblem().getProblem(smgr.getConnection(),probId);
        Problem p = ProblemMgr.getProblem(probId);
        if (p.isShortAnswer())
        {
            List<String> answers = p.getAnswerVals();
            for (String a: answers) {
                if (a.equalsIgnoreCase(userInput))
                    return true;
            }
            return false;
        }
        else if (p != null) {
            return p.getAnswer().equalsIgnoreCase(userInput.trim());
        }
        return false;
    }


    public LearningCompanion getLearningCompanion() {
        return learningCompanion;
    }

    public void setLearningCompanion(LearningCompanion learningCompanion) {
        this.learningCompanion = learningCompanion;
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

    public EndOfTopicInfo getReasonsForEndOfTopic () {
        return this.reasonsForEndOfTopic;
    }

    public abstract void newSession (int sessionId) throws SQLException;


    /**
     * The ped model should not show the MPP if there is a nextprob intervention selector that determines when MPP shows.
     * If no intervention selector of this sort, then show MPP is determined by a flag in the pedagogy.
     * @return
     */
    public boolean isShowMPP() {
        if (this.nextProblemInterventionSelector != null) {
            List<NextProblemInterventionSelector> l = this.nextProblemInterventionSelector.getSubSelectorList();
            for (NextProblemInterventionSelector s: l) {
                if (s instanceof MyProgressPageIS)
                    return false;
            }
        }
        return this.getParams().isShowMPP();
    }
}
