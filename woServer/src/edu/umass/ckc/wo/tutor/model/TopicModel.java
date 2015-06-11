package edu.umass.ckc.wo.tutor.model;



import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.content.TopicIntro;

import edu.umass.ckc.wo.db.DbTopics;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.interventions.DemoProblemIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;

import edu.umass.ckc.wo.smgr.TopicState;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorSpec;
import edu.umass.ckc.wo.tutor.intervSel2.NextProblemInterventionSelector;
import edu.umass.ckc.wo.tutor.pedModel.*;
import edu.umass.ckc.wo.tutor.probSel.LessonModelParameters;
import edu.umass.ckc.wo.tutor.probSel.TopicModelParameters;
import edu.umass.ckc.wo.tutor.response.*;
import edu.umass.ckc.wo.tutormeta.HintSelector;
import edu.umass.ckc.wo.tutormeta.Intervention;
import edu.umass.ckc.wo.tutormeta.PedagogicalMoveListener;
import edu.umass.ckc.wo.tutormeta.TopicSelector;
import org.apache.log4j.Logger;


import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 2/27/15
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TopicModel extends LessonModel {
    private static Logger logger = Logger.getLogger(TopicModel.class);

    protected TopicSelector topicSelector;
    protected difficulty nextDiff;
    protected EndOfTopicInfo reasonsForEndOfTopic;
    protected HintSelector hintSelector;
    protected EndOfTopicInfo eotInfo;
    protected TopicModelParameters tmParams;


    /** This constructor is called initially just to get a pointer to the object.   Later we call a build method
     * to send it objects it needs to do its work.
     * @param smgr
     */
    public TopicModel (SessionManager smgr) {
        super(smgr);
    }


    public void init (SessionManager smgr, LessonModelParameters lmParams,Pedagogy pedagogy,
                      PedagogicalModel pedagogicalModel, PedagogicalMoveListener pedagogicalMoveListener) throws Exception {

        // The superclass constructor calls readLessonControl which takes the control parameters out of the pedagogy and
        // builds the intervention handling for the model
        super.init(smgr, lmParams, pedagogy, pedagogicalModel, pedagogicalMoveListener);
        this.tmParams = (TopicModelParameters) lmParams;
        hintSelector = pedagogicalModel.getHintSelector();
        topicSelector = new TopicSelectorImpl(smgr,tmParams);
        eotInfo = null;
    }


    private EndOfTopicInfo checkForEOT(InTopicEvent e) throws Exception {
        // No interventions, so grade the last problem and if EOT, send an internal event for that
        ProblemGrader grader = pedagogicalModel.getProblemGrader();
        Problem lastProb = ProblemMgr.getProblem(smgr.getStudentState().getCurProblem());

        ProblemScore score;
        difficulty nextDiff;
        if (lastProb != null) {
            score = grader.gradePerformance(lastProb);
            nextDiff = getNextProblemDifficulty(score);
        }
        else nextDiff = difficulty.SAME;
        smgr.getStudentState().setNextProblemDesiredDifficulty(nextDiff.name());
        // Have to convert the SessionEvent to IntraProblemEvent because we're in the middle of a problem in a topic and need the probElapsed to grade it.
        IntraProblemEvent ipe = (IntraProblemEvent) e.getSessionEvent();
        // now we need to use the score to find the desired difficulty of the next problem

        //we save this info so that interventions (TopicSwitchAskIS) can get it to determine if they are applicable
        eotInfo = isEndOfTopic(ipe.getProbElapsedTime(),nextDiff);
        return eotInfo;
    }

    public EndOfTopicInfo getEndOfTopicInfo () {
        return eotInfo;
    }

    // Ugly.  The TopicSwitchAskIS needs a way to stop being applicable after its played.  Its processContinue method
    // calls this model back and has it remove the endOfTopic info that it relies on to be applicable.
    public void clearEndOfTopicInfo () {
        eotInfo = null;
    }


    /**
     * If IN_TOPIC:  If there is an intervention return it, else see if the topic is done and switch to END_TOPIC.  If topic
     * is not done, return null.
     * If BEGINNING_OF_TOPIC:  If there is an intervention, return it, else switch to IN-TOPIC
     * If END_OF_TOPIC:  If there is an intervention, return it, else pick the next topic and switch to BEGIN_TOPIC
     * @param e
     * @return
     * @throws Exception
     */
    public Response processInternalEvent (InternalEvent e) throws Exception {


        // This switches to the next topic
        if (e instanceof BeginningOfTopicEvent)
            return processBeginTopic((BeginningOfTopicEvent) e);

        else if (e instanceof EndOfTopicEvent) {
            return processEndOfTopic((EndOfTopicEvent) e);
        }
        else if (e instanceof InTopicEvent) {
            return processInTopic((InTopicEvent) e);
        }
        return null;
    }

    /**
     *
     * The event will contain the next topic (selected while processing EndOfTopic and then passed to this method through the event).
     * So if there is a change of topics happening, make the state changes so that we are in the next topic
     * Then see if any interventions apply, and return one if there is
     * If no interventions, then switch to IN_TOPIC and process it.
     * @param e
     * @return
     * @throws Exception
     */
    private Response processBeginTopic (BeginningOfTopicEvent e) throws Exception {
        studentState.setTopicInternalState(TopicState.BEGINNING_OF_TOPIC);
        int curTopic = studentState.getCurTopic();
        int nextTopic = e.getTopicId();
        if (curTopic == -1)  {

            curTopic =  topicSelector.getNextTopicWithAvailableProblems(smgr.getConnection(), curTopic, smgr.getStudentState());
            curTopic = switchTopics(curTopic);

        }
        // curTopic and nextTopic will be different if we're coming from EndOfTopic processing where we've just passed in the next topic
        // to work on
        else if (nextTopic != -1 && curTopic != nextTopic) {
            curTopic = switchTopics(nextTopic);
        }
        // if there is no current topic we must be at the beginning of the session so get a new topic and switch to it.
        Response r;
        //// See if there are interventions applicable for BeginningOfTopic
        r = super.processInternalEvent(e); // gets intervention that is highest ranked for this InternalEvent

        if (r != null) {
            return r;
        }
        // If we couldn't switch to a new topic and no interventions were applicable, return no-more-problems
        if (curTopic == -1)
            return ProblemResponse.NO_MORE_PROBLEMS;
        return processInternalEvent(new InTopicEvent(e.getSessionEvent(),studentState.getCurTopic()));

    }


    /**
     * If IN_TOPIC:  see if there are interventions and return one if so,
     * o/w see if we have reached the end of the current topic and if so, switch to END_OF_TOPIC
     * and process that event
     * If none of the above, return null to indicate that this model has nothing it wants to do.
     * @param e
     * @return
     * @throws Exception
     */
    private Response processInTopic (InTopicEvent e) throws Exception {
        Response r;
        studentState.setTopicInternalState(TopicState.IN_TOPIC);

        //// See if there are interventions applicable for InTopic
        r = super.processInternalEvent(e); // gets intervention that is highest ranked for this InternalEvent
        if (r != null) {
            return r;
        }
        EndOfTopicInfo eot = checkForEOT(e);

        if (eot.isTopicDone())   {
            // So we need send ourselves an EndOfTopicEvent
            return this.processInternalEvent(new EndOfTopicEvent(e.getSessionEvent(), studentState.getCurTopic()));
        }
        else return null;
    }


    /**
     * See if we have any interventions that apply to EndOfTopic.   Return it if so
     * If no interventions, select the next topic and then switch to BeginningOFTopic and process that by
     * passing the next topic that was selected.
     * @param e
     * @return
     * @throws Exception
     */
    private Response processEndOfTopic(EndOfTopicEvent e) throws Exception {
        studentState.setTopicInternalState(TopicState.END_OF_TOPIC);
        // Find an intervention that applies to EndOfTopic
        Response r = super.processInternalEvent(e);
        // r == null means we have no interventions about end of topic and we move on to BeginTopic
        if (r == null) {
            // At this point we are done doing anything for EndOfTopic and will now move to BeginningOfTopic.
            // We get the next Topic ID and send it to the processBeginTopic method so that it can see that it was passed a different
            // topic than what is still the current topic.
            int curTopic=studentState.getCurTopic();
            int nextTopic =  topicSelector.getNextTopicWithAvailableProblems(smgr.getConnection(), curTopic, smgr.getStudentState());
            if (nextTopic == -1)
                return ProblemResponse.NO_MORE_PROBLEMS;
//            studentState.setCurTopic(nextTopic);
            return processInternalEvent(new BeginningOfTopicEvent(e.getSessionEvent(),nextTopic));
        }
        return r;

    }

    /**
     * Checks to see what internal state the topic is in and processes according to that.
     * If IN_TOPIC:  see if there are interventions and return one if so, o/w return null
     * If BEGINNING_OF_TOPIC:  see if there are interventions and return it if so, o/w switch to IN_TOPIC and process it
     * If END_OF_TOPIC: find an intervention, if not get the next topic, switch to BEGINNING_OF_TOPIC and process it
     * @param e
     * @return
     * @throws Exception
     */
    private Response processNextProblemEvent (NextProblemEvent e) throws Exception {
        Response r;
        // a students first session will not have a topic in the student state so grab the first one
        if (studentState.getCurTopic() == -1) {
            int nextTopic =  topicSelector.getNextTopicWithAvailableProblems(smgr.getConnection(), -1, smgr.getStudentState());
            switchTopics(nextTopic);
        }
        // If the current state is BeginningOfTopic, then send an BOT internal event to get interventions for that if any
        if (studentState.getTopicInternalState().equals(TopicState.BEGINNING_OF_TOPIC))  {
            return processInternalEvent(new BeginningOfTopicEvent(e,studentState.getCurTopic()));
        }
        // If the current state is EndOfTopic, send EOT internal event to get interventions for that if any
        else if (studentState.getTopicInternalState().equals(TopicState.END_OF_TOPIC))  {
            return processInternalEvent(new EndOfTopicEvent(e, studentState.getCurTopic()));
        }
        else if (studentState.getTopicInternalState().equals(TopicState.IN_TOPIC)) {
            return  processInternalEvent(new InTopicEvent(e,studentState.getCurTopic()));
        }
        return null;

    }

    @Override
    public Response processUserEvent(TutorHutEvent e) throws Exception {
        // Called at the beginning of processing a NextProblem Event.  If the topic is done for any reason,
        // this returns the EndOfTopic event.
        if (e instanceof NextProblemEvent) {
            return processNextProblemEvent( (NextProblemEvent) e);

        }

        // If this model didn't generate the intervention, return null so the pedagogical model can process it.
        // If this model did generate it, process it and if that results in an internal event, process the internal event
        // If it results in nothing, then let this model process this event as if it were a nextProblemEvent
        else if (e instanceof ContinueNextProblemInterventionEvent) {
            String lastInterventionClass = smgr.getStudentState().getLastIntervention();
            InterventionSelectorSpec spec= interventionGroup.getInterventionSelectorSpec(lastInterventionClass);
            // If the last intervention is not found in this model's list of interventions, it isn't relevant to this model
            // so we return null
            if (spec == null)
                return null;
            NextProblemInterventionSelector intSel = (NextProblemInterventionSelector) spec.buildIS(smgr);
            intSel.init(smgr,pedagogicalModel);
            // N.B. Assumption is that we no longer get Interventions back
            Response r = intSel.processContinueNextProblemInterventionEvent((ContinueNextProblemInterventionEvent) e);
            // no state change (InternalState returned) means process it just like a NextProblemEvent
            if (r == null) {
                // N.B. This can return null if no interventions and internal state = IN_TOPIC
                r = processNextProblemEvent(new NextProblemEvent(e.getServletParams()));
                // If this model processed the event, but there is nothing it wants to return, we need to return an indicator
                // that the intervention was processed here so the pedagogical model won't try to .
                if (r == null)
                    return new InterventionInputProcessed(e);
                else return r;
            }
            else if (r instanceof InternalEvent)
                return this.processInternalEvent((InternalEvent) r);
        }
        // If this model didn't generate the intervention, return null so the pedagogical model can process it.
        // If this model did generate it, process it and if that results in an internal event, process the internal event
        // If it results in nothing, then let this model process this event as if it were a nextProblemEvent
        else if (e instanceof InputResponseNextProblemInterventionEvent) {
            String lastInterventionClass = smgr.getStudentState().getLastIntervention();
            InterventionSelectorSpec spec = interventionGroup.getInterventionSelectorSpec(lastInterventionClass);
            // If the last intervention is not found in this model's list of interventions, it isn't relevant to this model
            // so we return null
            if (spec == null)
                return null;
            NextProblemInterventionSelector intSel = (NextProblemInterventionSelector) spec.buildIS(smgr);
//            NextProblemInterventionSelector intSel = (NextProblemInterventionSelector) new TutorModelUtils().getLastInterventionSelector(smgr);
            ((InputResponseNextProblemInterventionEvent) e).setUserInput(intSel.getUserInputXML());

            intSel.init(smgr,pedagogicalModel);

            Response r = intSel.processInputResponseNextProblemInterventionEvent((InputResponseNextProblemInterventionEvent) e);
            // No state change, so process this just like a NextProblem event now
            if (r == null)  {
                // N.B. This can return null if no interventions and internal state = IN_TOPIC
                r = processNextProblemEvent(new NextProblemEvent(e.getServletParams()));
                // If this model processed the event, but there is nothing it wants to return, we need to return an indicator
                // that the intervention was processed here so the pedagogical model won't try to .
                if (r == null)
                    return new InterventionInputProcessed(e);
                else return r;
            }
            // The intervention selector returned an internal event so process that.
            else if (r instanceof InternalEvent)
                return this.processInternalEvent((InternalEvent) r);
        }


        return null;

    }



    public difficulty getNextProblemDifficulty (ProblemScore score) {
        // New sessions won't have a previous problem score so we just return same difficulty
        if (score == null)
            return TopicModel.difficulty.SAME;
        if (!score.isCorrect())
            return TopicModel.difficulty.EASIER;
        if (score.getMistakes() > score.getAvgMistakes()) {
            logger.debug("Decreasing difficulty level... ");
            return TopicModel.difficulty.EASIER;
        }
        else { // mistakes are LOW
            if (score.getHints() > score.getAvgHints()) {
                if (score.getSolveTimeSecs() > score.getAvgSolveTime()) {
//                        logger.debug("Student is carefully seeing help and spending time, maintain level of difficulty (m<E_m, h>E_h, t>E_t)") ;
                    logger.debug("Maintaining same difficulty level... ");
                    return TopicModel.difficulty.SAME;

                } else {
//                        logger.debug("Rushing through hints to get to the correct answer, decrease level of difficulty (m<E_m, h>E_h, t<E_t)") ;
                    logger.debug("Decreasing difficulty level... ");
                    return TopicModel.difficulty.EASIER;

                }
            } else {
                if (score.getSolveTimeSecs() > score.getAvgSolveTime()) {
//                        logger.debug("Correctly working on problem with effort but without help (m<E_m, h<E_h, t>E_t)") ;
//                        logger.debug("50% of the time increase level of difficulty. 50% of the time, maintain level of difficulty") ;
                    if (Math.random() > 0.5) {
                        logger.debug("Increasing difficulty level... ");
                        return TopicModel.difficulty.HARDER;
                    } else {
                        logger.debug("Maintaining same difficulty level... ");
                        return TopicModel.difficulty.SAME;
                    }
                } else
                    ; // logger.debug("Correctly working the problem with no effort and few hints (m<E_m, h<E_h, t<E_t)") ;
                logger.debug("Increasing the difficulty level...");
                return TopicModel.difficulty.HARDER;
            }
        }

    }


    /**
     * Once we are truly ready to begin the nextTopic switchTopics initializes the state variables associated with the current topic.
     * @param nextTopic
     * @return
     * @throws Exception
     */
    protected int switchTopics (int nextTopic) throws Exception {
        smgr.getStudentState().newTopic();
        if (nextTopic != -1)
            pedagogicalMoveListener.newTopic(ProblemMgr.getTopic(nextTopic)); // inform pedagogical move listeners of topic switch
        topicSelector.initializeTopic(nextTopic, smgr.getStudentState());
        smgr.getStudentState().setCurTopic(nextTopic);
        return nextTopic;
    }





    public EndOfTopicInfo isEndOfTopic(long probElapsedTime, difficulty difficulty) throws Exception {
        return topicSelector.isEndOfTopic(probElapsedTime,difficulty);
    }





    public Problem getTopicDemo(int curTopic) throws Exception {
        Problem problem = null;
        TopicModelParameters.frequency exampleFreq= this.tmParams.getTopicExampleFrequency();
        if (!smgr.getStudentState().isExampleShown()) {
            if (exampleFreq == TopicModelParameters.frequency.always) {
                if (!smgr.getStudentState().isExampleSeen(curTopic))
                    smgr.getStudentState().addExampleSeen(curTopic);
                problem = topicSelector.getDemoProblem(curTopic);
                if (problem == null)
                    return null;
                smgr.getStudentState().setIsExampleShown(true);
                new TutorModelUtils().setupDemoProblem(problem,smgr,hintSelector);
                return problem;
            }
            else if (exampleFreq == TopicModelParameters.frequency.oncePerSession &&
                    !smgr.getStudentState().isExampleSeen(curTopic)) {
                smgr.getStudentState().addExampleSeen(curTopic);
                problem = topicSelector.getDemoProblem(curTopic);
                if (problem == null) return null;
                smgr.getStudentState().setIsExampleShown(true);
                new TutorModelUtils().setupDemoProblem(problem,smgr,hintSelector);
                return problem;
            }
        }
        return null;
    }


    public TopicIntro getTopicIntro(int curTopic) throws Exception {
        TopicModelParameters.frequency topicIntroFreq= this.tmParams.getTopicIntroFrequency();
        // if it should always be shown,  show it.
        if (topicIntroFreq == TopicModelParameters.frequency.always ) {
            // if it hasn't been seen in this session, store that it has.
            if (!smgr.getStudentState().isTopicIntroSeen(curTopic) )  {
                smgr.getStudentState().addTopicIntrosSeen(curTopic);
            }
            studentState.setTopicIntroShown(true);  // sets the flag that its been seen during this lesson/topic
            TopicIntro intro = DbTopics.getTopicIntro(smgr.getConnection(), curTopic);
            this.pedagogicalMoveListener.lessonIntroGiven(intro); // inform pedagogical move listeners that an intervention is given
            return intro;

        }
        else if (topicIntroFreq == TopicModelParameters.frequency.oncePerSession &&
                !smgr.getStudentState().isTopicIntroSeen(curTopic)) {
            smgr.getStudentState().addTopicIntrosSeen(curTopic);
            studentState.setTopicIntroShown(true);
            TopicIntro intro = DbTopics.getTopicIntro(smgr.getConnection(), curTopic);
            this.pedagogicalMoveListener.lessonIntroGiven(intro); // inform pedagogical move listeners that an intervention is given
            return intro;
        }

        return null;
    }




    public ProblemResponse getProblemInTopicSelectedByStudent (NextProblemEvent e) throws Exception {
        int nextTopic = e.getTopicToForce();
        smgr.getStudentState().newTopic();   // this completely resets the lesson state
        pedagogicalMoveListener.newTopic(ProblemMgr.getTopic(nextTopic)); // inform pedagogical move listeners of topic switch
        topicSelector.initializeTopic(nextTopic, smgr.getStudentState());
        smgr.getStudentState().setCurTopic(nextTopic);
        // TODO No need for returning TopicIntro or Demo.  That was handled by processInternalEvent.  This just needs to return a Problem
        // Remember:  the student has only selected a topic and not  problem.  So we need to get a problem from the topic
        ProblemResponse r = this.pedagogicalModel.getNextProblem(e);
        Problem p = r.getProblem();
        smgr.getStudentState().setCurProblem(p.getId());
        // If current problem is parametrized, then choose a binding for it and stick it in the ProblemResponse and ProblemState.
//        // This line of code needs to be duplicated because ONR calls this function directly instead of processNextProblemRequest.
//        if (p != null && p.getType().equals(Problem.HTML_PROB_TYPE)) {
//            r.shuffleAnswers(smgr.getStudentState());
//        }

        return r;
    }




    public boolean hasReadyContent(int lessonId) throws Exception {
        return topicSelector.hasReadyContent(lessonId); // lessonId is a topic ID
    }

    public List<Integer> getUnsolvedProblems(int theTopicId, int classId, boolean includeTestProblems) throws Exception {
        return topicSelector.getUnsolvedProblems(theTopicId, classId, includeTestProblems);
    }

    public List<Integer> getClassTopicProblems(int topicId, int classId, boolean includeTestProblems) throws Exception {
        return topicSelector.getClassTopicProblems(topicId, classId, includeTestProblems);
    }


    public TopicSelector getTopicSelector() {
        return topicSelector;
    }

    @Override
    protected Response buildInterventionResponse (Intervention interv) throws Exception {
        if (interv == null)
            return null;

        else if (interv instanceof DemoProblemIntervention)   {
            DemoResponse r= new DemoResponse(((DemoProblemIntervention) interv).getProblem());
//                    r.setProblemBindings(smgr);
            pedagogicalMoveListener.exampleGiven(r.getProblem());
            return r;
        }
        else return new InterventionResponse(interv);
    }

    public TopicModelParameters getTmParams() {
        return tmParams;
    }

    public HintSelector getHintSelector() {
        return hintSelector;
    }

    public enum difficulty {
        EASIER,
        SAME,
        HARDER
    }
}
