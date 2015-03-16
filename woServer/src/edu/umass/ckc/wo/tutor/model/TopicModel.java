package edu.umass.ckc.wo.tutor.model;



import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Hint;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.content.TopicIntro;
import edu.umass.ckc.wo.event.SessionEvent;

import edu.umass.ckc.wo.event.tutorhut.IntraProblemEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;

import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.pedModel.*;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import edu.umass.ckc.wo.tutor.response.*;
import edu.umass.ckc.wo.tutormeta.HintSelector;
import edu.umass.ckc.wo.tutormeta.PedagogicalMoveListener;
import edu.umass.ckc.wo.tutormeta.TopicSelector;
import org.jdom.Element;


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

    protected TopicSelector topicSelector;
    protected ProblemGrader.difficulty nextDiff;
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
     * This may return another internal event or a response
     * @param e
     * @return
     * @throws Exception
     */
    public Response processInternalEvent (InternalEvent e) throws Exception {
        Response r=null;
        StudentState state = smgr.getStudentState();
        r = super.processInternalEvent(e); // finds an applicable intervention.  Response = null if none.
        // When we are out of interventions occuring because of End of Topic, we select the next topic
        // and return an internal event indicating the beginning of the next Topic
        if (r == null && e instanceof EndOfTopicEvent) {
            int nextTopic = switchTopics(state.getCurTopic());
            if (nextTopic == -1)
                return ProblemResponse.NO_MORE_PROBLEMS;
            return new BeginningOfTopicEvent(e.getSessionEvent(), nextTopic);
        }
        // When beginning a topic we need to see if we should show an intro and/or a demo problem
        else if (r == null && e instanceof BeginningOfTopicEvent) {
            smgr.getStudentState().newTopic();   // this completely resets the lesson state
            int nextTopic = state.getCurTopic();
            pedagogicalMoveListener.newTopic(ProblemMgr.getTopic(nextTopic)); // inform pedagogical move listeners of topic switch
            topicSelector.initializeTopic(nextTopic, smgr.getStudentState());
            smgr.getStudentState().setCurTopic(nextTopic);
            TopicIntro ti = getTopicIntro(nextTopic);
            if (ti != null)   {
                return new TopicIntroResponse(ti);
            }
            Problem ex = getTopicExample(nextTopic);
            if (ex != null) {
                r=  new DemoResponse(ex);
                ((DemoResponse) r).setProblemBindings(smgr);
                pedagogicalMoveListener.exampleGiven(ex);  // inform pedagogical move listeners of example being given
            }


        }
        else if (state.getCurProblem() == -1)  {
            TopicIntro ti = getTopicIntro(state.getCurTopic());
            if (ti != null)   {
                return new TopicIntroResponse(ti);
            }
        }

        return r;
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

    public EndOfTopicInfo isEndOfTopic(long probElapsedTime, ProblemGrader.difficulty difficulty) throws Exception {
        return topicSelector.isEndOfTopic(probElapsedTime,difficulty);
    }


    protected boolean gradeProblem (long probElapsedTime) throws Exception {
        StudentState state = smgr.getStudentState();
        ProblemGrader grader = new ProblemGrader();
        Problem lastProb = ProblemMgr.getProblem(state.getCurProblem());
        String lastProbMode = state.getCurProblemMode();
        Problem curProb=null;
        ProblemScore score=null;

        if (lastProb != null  && lastProbMode.equals(Problem.PRACTICE))     {
            score = grader.gradePerformance(smgr.getConnection(),lastProb,smgr.getStudentState());
            nextDiff = grader.getNextProblemDifficulty(score);
        }
        else nextDiff = ProblemGrader.difficulty.SAME;
        this.reasonsForEndOfTopic=  topicSelector.isEndOfTopic(probElapsedTime, nextDiff);
        boolean topicDone = reasonsForEndOfTopic.isTopicDone();
        return topicDone;
    }



    protected Problem getTopicExample (int curTopic) throws Exception {
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



    protected TopicIntro getTopicIntro (int curTopic) throws Exception {
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
        ProblemResponse r = this.pedagogicalModel.getProblem(e, ProblemGrader.difficulty.SAME);
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

}
