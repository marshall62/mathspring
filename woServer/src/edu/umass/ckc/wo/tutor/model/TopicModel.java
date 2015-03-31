package edu.umass.ckc.wo.tutor.model;



import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.content.TopicIntro;

import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.interventions.DemoProblemIntervention;
import edu.umass.ckc.wo.interventions.TopicIntroIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;

import edu.umass.ckc.wo.smgr.TopicState;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelector;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorSpec;
import edu.umass.ckc.wo.tutor.intervSel2.NextProblemInterventionSelector;
import edu.umass.ckc.wo.tutor.pedModel.*;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import edu.umass.ckc.wo.tutor.response.*;
import edu.umass.ckc.wo.tutormeta.HintSelector;
import edu.umass.ckc.wo.tutormeta.Intervention;
import edu.umass.ckc.wo.tutormeta.PedagogicalMoveListener;
import edu.umass.ckc.wo.tutormeta.TopicSelector;
import org.apache.log4j.Logger;
import org.jdom.Element;


import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Collections;
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


    /** This constructor is called initially just to get a pointer to the object.   Later we call a build method
     * to send it objects it needs to do its work.
     * @param smgr
     */
    public TopicModel (SessionManager smgr) {
        super(smgr);
    }




    public void init (SessionManager smgr, PedagogicalModelParameters pmParams, Pedagogy pedagogy, PedagogicalModel pedagogicalModel,
                      PedagogicalMoveListener pedagogicalMoveListener) throws SQLException {
        // The superclass constructor calls readLessonControl which takes the control parameters out of the pedagogy and
        // builds the intervention handling for the model
        super.init(smgr, pmParams, pedagogy, pedagogicalModel, pedagogicalMoveListener);
        hintSelector = pedagogicalModel.getHintSelector();
        topicSelector = new TopicSelectorImpl(smgr,pmParams);
    }


    /**
     * Processing an internal event may return an intervention, or another InternalEvent.   In some cases (BeginningOfTopic)
     * it can return responses like TopicIntro or DemoProblem.
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
     * Beginning a topic means selecting the new topic.   We can then return an intervention if there are any that apply.
     * If no interventions apply, we can show the topic intro, demo, and finally return nothing to indicate
     * to the caller that it should select a problem.
     * @param e
     * @return
     * @throws Exception
     */
    private Response processBeginTopic (BeginningOfTopicEvent e) throws Exception {
        studentState.setTopicInternalState(TopicState.BEGINNING_OF_TOPIC);
        int curTopic = studentState.getCurTopic();
        // We assume that we switched topics right after EndOfTopic was done.

        if (curTopic == -1)  {

            curTopic =  topicSelector.getNextTopicWithAvailableProblems(smgr.getConnection(), curTopic, smgr.getStudentState());
            switchTopics(curTopic);
        }




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


    private Response processInTopic (InTopicEvent e) throws Exception {
        Response r;
        studentState.setTopicInternalState(TopicState.IN_TOPIC);

        //// See if there are interventions applicable for InTopic
        r = super.processInternalEvent(e); // gets intervention that is highest ranked for this InternalEvent
        if (r != null) {
            return r;
        }
        // No interventions, so grade the last problem and if EOT, send an internal event for that
        ProblemGrader grader = pedagogicalModel.getProblemGrader();
        Problem lastProb = ProblemMgr.getProblem(smgr.getStudentState().getCurProblem());
        ProblemScore score;
        difficulty nextDiff;
        score = grader.gradePerformance(lastProb);
        nextDiff = getNextProblemDifficulty(score);
        smgr.getStudentState().setNextProblemDesiredDifficulty(nextDiff.name());
        // Have to convert the SessionEvent to IntraProblemEvent because we're in the middle of a problem in a topic and need the probElapsed to grade it.
        IntraProblemEvent ipe = (IntraProblemEvent) e.getSessionEvent();
        // now we need to use the score to find the desired difficulty of the next problem
        EndOfTopicInfo eot = isEndOfTopic(ipe.getProbElapsedTime(),nextDiff);
        if (eot.isTopicDone())   {
            // So we need send ourselves an EndOfTopicEvent
            return this.processInternalEvent(new EndOfTopicEvent(e.getSessionEvent(), studentState.getCurTopic()));
        }
        else return null;
    }

    /**
     * See if we have any interventions that apply to EndOfTopic
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
            int curTopic=studentState.getCurTopic();
            int nextTopic =  topicSelector.getNextTopicWithAvailableProblems(smgr.getConnection(), curTopic, smgr.getStudentState());
            if (nextTopic == -1)
                return ProblemResponse.NO_MORE_PROBLEMS;
            studentState.setCurTopic(nextTopic);
            return processInternalEvent(new BeginningOfTopicEvent(e.getSessionEvent(),studentState.getCurTopic()));
        }
        return r;

    }

    private Response processNextProblemEvent (NextProblemEvent e) throws Exception {
        Response r;
        // a students first session will not have a topic in the student state so grab the first one
        if (studentState.getCurTopic() == -1) {
            int nextTopic =  topicSelector.getNextTopicWithAvailableProblems(smgr.getConnection(), -1, smgr.getStudentState());
            switchTopics(nextTopic);
        }
        // If the current state is BeginningOfTopic, then send an BOT internal event to get interventions for that if any
        if (studentState.getTopicInternalState().equals(TopicState.BEGINNING_OF_TOPIC))  {
            return processInternalEvent(new BeginningOfTopicEvent(e,-1));
        }
        // If the current state is EndOfTopic, send EOT internal event to get interventions for that if any
        if (studentState.getTopicInternalState().equals(TopicState.END_OF_TOPIC))  {
            return processInternalEvent(new EndOfTopicEvent(e, studentState.getCurTopic()));
        }
        if (studentState.getTopicInternalState().equals(TopicState.IN_TOPIC)) {
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
        // If we get a Continue from an intervention.   See if the intervention is relevant to topic begin/end
        else if (e instanceof ContinueNextProblemInterventionEvent) {
            NextProblemInterventionSelector intSel = (NextProblemInterventionSelector) new TutorModelUtils().getLastInterventionSelector(smgr);
            // TODO need to determine if this intervention / response is relevant to this topic model.
            boolean isRelevant = true;
            if (isRelevant)  {
                intSel.init(smgr,pedagogicalModel);
                // N.B. Assumption is that we no longer get Interventions back
                Response r = intSel.processContinueNextProblemInterventionEvent((ContinueNextProblemInterventionEvent) e);
                // no state change (InternalState returned) means process it just like a NextProblemEvent
                if (r == null)
                    return processNextProblemEvent(new NextProblemEvent(e.getServletParams()));
                else if (r instanceof InternalEvent)
                    return this.processInternalEvent((InternalEvent) r);
            }
            // if its not relevant, the pedagogical model will ask the intervention selector to process the input
            else return null;
        }
        else if (e instanceof InputResponseNextProblemInterventionEvent) {
            NextProblemInterventionSelector intSel = (NextProblemInterventionSelector) new TutorModelUtils().getLastInterventionSelector(smgr);
            ((InputResponseNextProblemInterventionEvent) e).setUserInput(intSel.getUserInputXML());
            // TODO need to determine if this intervention / response is relevant to this topic model.
            boolean isRelevant = true;
            if (isRelevant)  {
                intSel.init(smgr,pedagogicalModel);
                // this either returns an InternalState which keeps processing going within this model. o/w the intervention is done
                Response r = intSel.processInputResponseNextProblemInterventionEvent((InputResponseNextProblemInterventionEvent) e);
                if (r == null)
                    return new InterventionInputProcessed( e);  // indicator that no more processing is necesssary
                else if (r instanceof InternalEvent)
                    return this.processInternalEvent((InternalEvent) r);
            }
            // if its not relevant, the pedagogical model will ask the intervention selector to process the input
            else return null;
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

    /**
     *
     * Called by the superclass constructor to read the XML control for this model.   It puts together a list of
     * intervention selectors.
     * @param lessonControlElement
     * @override
     */
    protected void readLessonControl(Element lessonControlElement) {
        super.readLessonControl(lessonControlElement);  // for now the superclass does everything we need
    }




    public EndOfTopicInfo isEndOfTopic(long probElapsedTime, difficulty difficulty) throws Exception {
        return topicSelector.isEndOfTopic(probElapsedTime,difficulty);
    }





    public Problem getTopicExample (int curTopic) throws Exception {
        Problem problem = null;
        PedagogicalModelParameters.frequency exampleFreq= this.pmParams.getTopicExampleFrequency();
        if (!smgr.getStudentState().isExampleShown()) {
            if (exampleFreq == PedagogicalModelParameters.frequency.always) {
                if (!smgr.getStudentState().isExampleSeen(curTopic))
                    smgr.getStudentState().addExampleSeen(curTopic);
                problem = topicSelector.getExample(curTopic, this.hintSelector);
                if (problem == null)
                    return null;
                smgr.getStudentState().setIsExampleShown(true);
                new TutorModelUtils().setupDemoProblem(problem,smgr,hintSelector);
                return problem;
            }
            else if (exampleFreq == PedagogicalModelParameters.frequency.oncePerSession &&
                    !smgr.getStudentState().isExampleSeen(curTopic)) {
                smgr.getStudentState().addExampleSeen(curTopic);
                problem = topicSelector.getExample(curTopic, this.hintSelector);
                if (problem == null) return null;
                smgr.getStudentState().setIsExampleShown(true);
                new TutorModelUtils().setupDemoProblem(problem,smgr,hintSelector);
                return problem;
            }
        }
        return null;
    }


    public TopicIntro getTopicIntro (int curTopic, PedagogicalModelParameters.frequency topicIntroFreq) throws Exception {
        topicIntroFreq= this.pmParams.getTopicIntroFrequency();
        // if the topic intro sho
        if (topicIntroFreq == PedagogicalModelParameters.frequency.always && !studentState.isTopicIntroShown()) {
            if (!smgr.getStudentState().isTopicIntroSeen(curTopic) )  {
                smgr.getStudentState().addTopicIntrosSeen(curTopic);
                studentState.setTopicIntroShown(true);
            }
            TopicIntro intro =topicSelector.getIntro(curTopic);
            this.pedagogicalMoveListener.lessonIntroGiven(intro); // inform pedagogical move listeners that an intervention is given
            return intro;

        }
        else if (topicIntroFreq == PedagogicalModelParameters.frequency.oncePerSession &&
                !smgr.getStudentState().isTopicIntroSeen(curTopic)) {
            smgr.getStudentState().addTopicIntrosSeen(curTopic);
            TopicIntro intro =topicSelector.getIntro(curTopic);
            this.pedagogicalMoveListener.lessonIntroGiven(intro); // inform pedagogical move listeners that an intervention is given
            return intro;
        }

        return null;
    }

    public TopicIntro getTopicIntro (int curTopic) throws Exception {
        PedagogicalModelParameters.frequency topicIntroFreq, exampleFreq;
        topicIntroFreq= this.pmParams.getTopicIntroFrequency();
        // if the topic intro sho
        if (topicIntroFreq == PedagogicalModelParameters.frequency.always) {
            if (!smgr.getStudentState().isTopicIntroSeen(curTopic))
                smgr.getStudentState().addTopicIntrosSeen(curTopic);
            TopicIntro intro =topicSelector.getIntro(curTopic);
            this.pedagogicalMoveListener.lessonIntroGiven(intro); // inform pedagogical move listeners that an intervention is given
            return intro;
        }
        else if (topicIntroFreq == PedagogicalModelParameters.frequency.oncePerSession &&
                !smgr.getStudentState().isTopicIntroSeen(curTopic)) {
            smgr.getStudentState().addTopicIntrosSeen(curTopic);
            TopicIntro intro =topicSelector.getIntro(curTopic);
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
        if (interv instanceof TopicIntroIntervention)
            return new TopicIntroResponse(((TopicIntroIntervention) interv).getTopicIntro());
        else if (interv instanceof DemoProblemIntervention)   {
            DemoResponse r= new DemoResponse(((DemoProblemIntervention) interv).getProblem());
//                    r.setProblemBindings(smgr);
            pedagogicalMoveListener.exampleGiven(r.getProblem());
            return r;
        }
        else return new InterventionResponse(interv);
    }



    public enum difficulty {
        EASIER,
        SAME,
        HARDER
    }
}
