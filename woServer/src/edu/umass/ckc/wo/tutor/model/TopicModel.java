package edu.umass.ckc.wo.tutor.model;



import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.content.TopicIntro;

import edu.umass.ckc.wo.event.tutorhut.ContinueNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.event.tutorhut.TutorHutEvent;
import edu.umass.ckc.wo.interventions.TopicSwitchAskIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;

import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelector;
import edu.umass.ckc.wo.tutor.intervSel2.NextProblemInterventionSelector;
import edu.umass.ckc.wo.tutor.pedModel.*;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import edu.umass.ckc.wo.tutor.response.*;
import edu.umass.ckc.wo.tutormeta.HintSelector;
import edu.umass.ckc.wo.tutormeta.PedagogicalMoveListener;
import edu.umass.ckc.wo.tutormeta.TopicSelector;
import org.apache.log4j.Logger;
import org.jdom.Element;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
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








    public TopicModel(SessionManager smgr, PedagogicalModelParameters pmParams, Pedagogy pedagogy, PedagogicalModel pedagogicalModel,
                      PedagogicalMoveListener pedagogicalMoveListener) throws SQLException {
        // The superclass constructor calls readLessonControl which takes the control parameters out of the pedagogy and
        // builds the intervention handling for the model
        super(smgr, pmParams,pedagogy,pedagogicalModel,pedagogicalMoveListener);
        hintSelector = pedagogicalModel.getHintSelector();
        topicSelector = new TopicSelectorImpl(smgr,pmParams);
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

        // first we switch to the next topic
        int nextTopic = switchTopics(studentState.getCurTopic());

        Response r;
        //// See if there are interventions applicable for BeginningOfTopic
        r = super.processInternalEvent(e); // gets intervention that is highest ranked for this InternalEvent
        if (r != null) return r; // return the intervention

        // If we couldn't switch to a new topic and no interventions were applicable, return no-more-problems
        if (nextTopic == -1)
            return ProblemResponse.NO_MORE_PROBLEMS;
        // We did get a new topic
        pedagogicalMoveListener.newTopic(ProblemMgr.getTopic(nextTopic)); // inform pedagogical move listeners of topic switch
        topicSelector.initializeTopic(nextTopic, smgr.getStudentState());
        smgr.getStudentState().setCurTopic(nextTopic);
        // TopicIntro and Example are now interventions that take place on BeginningOfTopic Event.
//        // See if we should play the TopicIntro. Return it if so.
//        TopicIntro ti = getTopicIntro(nextTopic);
//        if (ti != null)   {
//            return new TopicIntroResponse(ti);
//        }
//        // See if we should play a Demo problem.   Return it if so.
//        Problem ex = getTopicExample(nextTopic);
//        if (ex != null) {
//            r=  new DemoResponse(ex);
//            ((DemoResponse) r).setProblemBindings(smgr);
//            pedagogicalMoveListener.exampleGiven(ex);  // inform pedagogical move listeners of example being given
//        }
        // There's nothing special to do so let the Pedagogical Model start running the topic
        return null;
    }

    /**
     * Processing an internal event may return an intervention, or another InternalEvent.   In some cases (BeginningOfTopic)
     * it can return responses like TopicIntro or DemoProblem.
     * @param e
     * @return
     * @throws Exception
     */
    public Response processInternalEvent (InternalEvent e) throws Exception {
        Response r=null;
        int nextTopic=-1;
        StudentState state = smgr.getStudentState();
        /// Run some code prior to seeing if there are interventions applicable.

        // This switches to the next topic
        if (e instanceof BeginningOfTopicEvent)
            return processBeginTopic((BeginningOfTopicEvent) e);

        else if (e instanceof EndOfTopicEvent) {
            return processEndOfTopic((EndOfTopicEvent) e);
        }

        return r;
    }

    /**
     * See if we have any interventions that apply to EndOfTopic
     * @param e
     * @return
     * @throws Exception
     */
    private Response processEndOfTopic(EndOfTopicEvent e) throws Exception {
        // Find an intervention that applies to EndOfTopic
        Response r = super.processInternalEvent(e);
        // r == null means we have no interventions about end of topic and we move on to BeginTopic
        if (r == null)
            return processInternalEvent(new BeginningOfTopicEvent(e.getSessionEvent(),studentState.getCurTopic()));
        return r;

    }

    @Override
    public Response processUserEvent(TutorHutEvent e) throws Exception {
        // Called at the beginning of processing a NextProblem Event.  If the topic is done for any reason,
        // this returns the EndOfTopic event.
        if (e instanceof NextProblemEvent) {
            ProblemGrader grader = pedagogicalModel.getProblemGrader();
            Problem lastProb = ProblemMgr.getProblem(smgr.getStudentState().getCurProblem());
            ProblemScore score = grader.gradePerformance(lastProb);
            difficulty nextDiff = getNextProblemDifficulty(score);
            // When we generate a NextProb intervention we need to save the grading of the problem that was just done (i.e. whether we want
            // an easier,harder/same problem) because after intervention responses are processed we go back to showing problems and we need
            // that grade for making calls to the TopicSelector.
            // We save the desired next problem difficulty in the student state because its possible that we return a bunch
            // of interventions and then need to select a problem.   This will be pulled out and used to guide problem selection.
            // TODO this is awful.  We should remember the last problem and just regrade it based on the history record rather
            // tucking this away.
            smgr.getStudentState().setNextProblemDesiredDifficulty(nextDiff.name());
            // now we need to use the score to find the desired difficulty of the next problem
            EndOfTopicInfo eot = isEndOfTopic(((NextProblemEvent) e).getProbElapsedTime(),nextDiff);
            if (eot.isTopicDone())   {
                // So we need send ourselves an EndOfTopicEvent
                return this.processInternalEvent(new EndOfTopicEvent(e,eot,studentState.getCurTopic()));
            }
            else return null;
        }
        // If we get a Continue from an intervention.   See if the intervention is relevant to topic begin/end
        else if (e instanceof ContinueNextProblemInterventionEvent) {
            NextProblemInterventionSelector intSel = (NextProblemInterventionSelector) new TutorModelUtils().getLastInterventionSelector(smgr);
            // TODO need to determine if this intervention / response is relevant to this topic model.
            boolean isRelevant = true;
            if (isRelevant)  {
                // N.B. Assumption is that we no longer get Interventions back
                Response r = intSel.processContinueNextProblemInterventionEvent((ContinueNextProblemInterventionEvent) e);
                if (r == null)
                    return new InterventionInputProcessed(e);  // indicator that the intervention processed the input but has no state change
                // has the effect of running the rules for the internal state returned by the intervention.  The state may stay the
                // same and thus we get the effect of possibly returning more than one intervention for an internal state
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





    protected int switchTopics (int curTopic) throws Exception {
        int nextTopic = topicSelector.getNextTopicWithAvailableProblems(smgr.getConnection(), curTopic, smgr.getStudentState());
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


    /**
     *  This is only method necessary for selecting interventions.   The rest is handled by the superclass.  This takes care of
     *  selecting an InterventionSpec from the list of candidates that apply to the event.   Even this behavior looks like a good default
     *  and could be moved to superclass but we'll leave it here until we are sure that both behave the same way.
     * @param candidates
     * @param e
     * @return
     * @override
     */
    protected InterventionSpec selectBestCandidate(List<InterventionSpec> candidates, InternalEvent e) {
        if (candidates.size() == 1)
            return candidates.get(0);
        else {
            // sort them by weight and return the first
            sortCandidates(candidates);
            return candidates.get(0);
        }
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
        return topicSelector.getUnsolvedProblems(theTopicId,classId,includeTestProblems);
    }

    public List<Integer> getClassTopicProblems(int topicId, int classId, boolean includeTestProblems) throws Exception {
        return topicSelector.getClassTopicProblems(topicId,classId,includeTestProblems);
    }


    public TopicSelector getTopicSelector() {
        return topicSelector;
    }



    public enum difficulty {
        EASIER,
        SAME,
        HARDER
    }
}
