package edu.umass.ckc.wo.tutor.pedModel;

import edu.umass.ckc.wo.assistments.AssistmentsHandler;
import edu.umass.ckc.wo.assistments.AssistmentsUser;
import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Hint;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.content.TopicIntro;
import edu.umass.ckc.wo.content.Video;
import edu.umass.ckc.wo.db.DbAssistmentsUsers;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.interventions.SelectHintSpecs;
import edu.umass.ckc.wo.interventions.SelectProblemSpecs;
import edu.umass.ckc.wo.log.TutorLogger;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorSpec;
import edu.umass.ckc.wo.tutor.intervSel2.AttemptInterventionSelector;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelector;
import edu.umass.ckc.wo.tutor.intervSel2.NextProblemInterventionSelector;
import edu.umass.ckc.wo.tutor.probSel.*;
import edu.umass.ckc.wo.tutor.response.*;
import edu.umass.ckc.wo.tutor.vid.BaseVideoSelector;
import edu.umass.ckc.wo.tutormeta.*;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 11/14/13
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasePedagogicalModel extends PedagogicalModel implements PedagogicalMoveListener {

    private static Logger logger = Logger.getLogger(BasePedagogicalModel.class);
    protected TopicSelector topicSelector;
    ProblemGrader.difficulty nextDiff;
    List<PedagogicalMoveListener> pedagogicalMoveListeners;


    public BasePedagogicalModel() {
    }

    private BasePedagogicalModel (SessionManager smgr) {
        setSmgr(smgr);
        setExampleSelector(new BaseExampleSelector());
        setVideoSelector(new BaseVideoSelector());
    }



    public BasePedagogicalModel (SessionManager smgr, Pedagogy pedagogy) {
        this(smgr);
        try {
            pedagogicalMoveListeners = new ArrayList<PedagogicalMoveListener>();
            // Use the params from the pedagogy and then overwrite any values with things that are set up for the class
            params = setParams(smgr.getPedagogicalModelParameters(),pedagogy.getParams());
            topicSelector = new TopicSelectorImpl(smgr,params, this);
            setStudentModel((StudentModel) Class.forName(pedagogy.getStudentModelClass()).getConstructor(SessionManager.class).newInstance(smgr));
            smgr.setStudentModel(getStudentModel());
            setProblemSelector((ProblemSelector) Class.forName(pedagogy.getProblemSelectorClass()).getConstructor(SessionManager.class, TopicSelector.class, PedagogicalModelParameters.class).newInstance(smgr, topicSelector, params));
            setReviewModeProblemSelector((ReviewModeProblemSelector) Class.forName(pedagogy.getReviewModeProblemSelectorClass()).getConstructor(SessionManager.class, TopicSelectorImpl.class, PedagogicalModelParameters.class).newInstance(smgr, topicSelector, params));
            setChallengeModeProblemSelector((ChallengeModeProblemSelector) Class.forName(pedagogy.getChallengeModeProblemSelectorClass()).getConstructor(SessionManager.class, TopicSelector.class, PedagogicalModelParameters.class).newInstance(smgr, topicSelector, params));
            setHintSelector((HintSelector) Class.forName( pedagogy.getHintSelectorClass()).getConstructor().newInstance());
            if (pedagogy.getLearningCompanionClass() != null)
                setLearningCompanion((LearningCompanion) Class.forName( pedagogy.getLearningCompanionClass()).getConstructor(SessionManager.class).newInstance(smgr));
            if (pedagogy.getNextProblemInterventionSelector() != null)
                setNextProblemInterventionSelector(buildNextProblemIS(smgr, pedagogy));
            if (pedagogy.getAttemptInterventionSelector() != null)
                setAttemptInterventionSelector(buildAttemptIS(smgr, pedagogy));
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void addPedagogicalMoveListener (PedagogicalMoveListener listener) {
        this.pedagogicalMoveListeners.add(listener);
    }

    private InterventionSelector newInstance (InterventionSelectorSpec interventionSelectorSpec, SessionManager smgr) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        InterventionSelector sel= (InterventionSelector) Class.forName(interventionSelectorSpec.getClassName()).getConstructor(SessionManager.class, PedagogicalModel.class).newInstance(smgr,this);
        sel.setParams(interventionSelectorSpec.getParams());
        sel.setConfigXML(interventionSelectorSpec.getConfigXML());
        sel.init(smgr,this);
        return sel;
    }

    private NextProblemInterventionSelector buildNextProblemIS(SessionManager smgr, Pedagogy pedagogy) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        InterventionSelectorSpec npSpec = pedagogy.getNextProblemInterventionSelector();
        List<InterventionSelectorSpec> subs = pedagogy.getSubNextProblemInterventionSelectors();
        NextProblemInterventionSelector sel = (NextProblemInterventionSelector) newInstance(npSpec,smgr);
        this.addPedagogicalMoveListener(sel);

        if (subs != null) {
            List<NextProblemInterventionSelector> subSels = new ArrayList<NextProblemInterventionSelector>();
            for (InterventionSelectorSpec sub : subs) {
                NextProblemInterventionSelector ss = (NextProblemInterventionSelector) newInstance(sub,smgr);
                this.addPedagogicalMoveListener(ss);
                subSels.add(ss);
            }
            sel.setSubSelectors(subSels);
        }
        return sel;
    }

    private AttemptInterventionSelector buildAttemptIS(SessionManager smgr, Pedagogy pedagogy) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        InterventionSelectorSpec atSpec = pedagogy.getAttemptInterventionSelector();
        List<InterventionSelectorSpec> subs = pedagogy.getSubAttemptInterventionSelectors();
        AttemptInterventionSelector sel = (AttemptInterventionSelector) newInstance(atSpec,smgr);
        this.addPedagogicalMoveListener(sel);
        if (subs != null) {
            List<AttemptInterventionSelector> subSels = new ArrayList<AttemptInterventionSelector>();
            for (InterventionSelectorSpec sub : subs) {
                AttemptInterventionSelector ss = (AttemptInterventionSelector) newInstance(sub,smgr);
                this.addPedagogicalMoveListener(ss);
                subSels.add(ss);
            }
            sel.setSubSelectors(subSels);
        }
        return sel;
    }

    @Override
    /**
     * If there is an attempt intervention selected by the attempt intervention selector, run that intervention.
     * Otherwise, simply grade the problem, update the student model and return.
     */
    public Response processAttempt(AttemptEvent e) throws Exception {
        boolean isCorrect = isAttemptCorrect(smgr.getStudentState().getCurProblem(),e.getUserInput());
        e.setCorrect(isCorrect);
        Intervention intervention=null;

        // first update the student model so that intervention selectors have access to latest stats based on this attempt
        studentModel.studentAttempt(smgr.getStudentState(), e.getUserInput(), isCorrect, e.getProbElapsedTime());
        if (attemptInterventionSelector != null)
            intervention = attemptInterventionSelector.selectIntervention(e);
        AttemptResponse r;
        // No more interventions
        if (intervention == null) {
            interventionGiven(intervention); // inform pedagogical move listeners that an intervention is given
//            studentModel.studentAttempt(smgr.getStudentState(), e.getUserInput(), isCorrect, e.getProbElapsedTime());
            r = new AttemptResponse(true,isCorrect, studentModel.getTopicMasteries(),smgr.getStudentState().getCurTopic());
        }
        else {
            // record this attempt.  We will need to send back information about its correctness
            // once the interventions are done.
            attemptGraded(isCorrect); // inform pedagogical move listeners that an intervention is given
            r = new AttemptResponse(false,isCorrect,intervention, studentModel.getTopicMasteries(),
                    smgr.getStudentState().getCurTopic()) ;
        }

        if (learningCompanion != null )
            learningCompanion.processAttempt(smgr,e,r);
        new TutorLogger(smgr).logAttempt(e, r);
        return r;
    }

    @Override
    public Response processBeginProblemEvent(BeginProblemEvent e) throws Exception {
        Response r = new Response();
        this.studentModel.beginProblem(smgr, e); // this sets state.curProb to the new probid
        new TutorLogger(smgr).logBeginProblem(e, r);  // this relies on the above being done first
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processResumeProblemEvent(ResumeProblemEvent e) throws Exception {
        Response r = new Response();
        new TutorLogger(smgr).logResumeProblem(e,r);  // this relies on the above being done first
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processEndProblemEvent(EndProblemEvent e) throws Exception {
        Response r = new Response();
        // at the end of a problem the emotional state of the student model is updated
//        this.studentModel.updateEmotionalState(this.smgr,e.getProbElapsedTime(),e.getElapsedTime());
        this.studentModel.endProblem(smgr, smgr.getStudentId(),e.getProbElapsedTime(),e.getElapsedTime());
        r.setEffort(this.studentModel.getEffort());
        new TutorLogger(smgr).logEndProblem(e, r);
        if (Settings.usingAssistments)
        {
            AssistmentsUser assu = DbAssistmentsUsers.getUserFromWayangStudId(smgr.getConnection(), smgr.getStudentId());
            if (assu != null) {
                smgr.setAssistmentsUser(true);
                AssistmentsHandler.logToAssistmentsProblemEnd(smgr, (EndProblemEvent) e);
            }
        }
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    public HintResponse doSelectHint (SelectHintSpecs selectionCriteria) throws Exception {
        Hint h;
        if (selectionCriteria == null)
            h = hintSelector.selectHint(smgr);
        else
            h = hintSelector.selectHint(smgr,selectionCriteria);
        this.studentModel.hintGiven(smgr.getStudentState(), h);
        // write the student model back to the database
        this.studentModel.save();
        return new HintResponse(h);
    }


    public HintResponse processHintRequest (HintEvent e) throws Exception {
        Hint h= hintSelector.selectHint(smgr);
        this.studentModel.hintGiven(smgr.getStudentState(), h);
        hintGiven(h); // inform pedagogical move listeners that an intervention is given
        HintResponse r = new HintResponse(h);
        if (learningCompanion != null )
            learningCompanion.processHintRequest(smgr,e,r);
        new TutorLogger(smgr).logHintRequest(e, r);
        return r;
    }

    @Override
    public Response processShowExampleRequest(ShowExampleEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Problem p = ProblemMgr.getProblem(smgr.getStudentState().getCurProblem());
        int exId = p.getExample();
        Response r;
        if (exId >= 0)
        {
            Problem ex = ProblemMgr.getProblem(exId);
            List<Hint> solution=null;
            if (ex != null)  {
                hintSelector.init(smgr);
                solution = hintSelector.selectFullHintPath(smgr,ex.getId());
                // No updating of the student model/state is happening with regard to how many hints are seen
                // even though the example is comprised of a problem and all its hints.
                ex.setSolution(solution);
                r= new ExampleResponse(ex);
            }
            else r= new ExampleResponse(null);
        }
        else r= new ExampleResponse(null);
        studentModel.exampleGiven(smgr.getStudentState(), exId);
        new TutorLogger(smgr).logShowExample((ShowExampleEvent) e, (ExampleResponse) r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processShowVideoRequest(ShowVideoEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Problem p = ProblemMgr.getProblem(smgr.getStudentState().getCurProblem());
        String vid = p.getVideo();

        Response r = new Video(vid);
        new TutorLogger(smgr).logShowVideoTransaction((ShowVideoEvent) e, r);
        studentModel.videoGiven(smgr.getStudentState());
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);

        return r;
    }

    protected TopicIntro getTopicIntro (int curTopic) throws Exception {
        PedagogicalModelParameters.frequency topicIntroFreq, exampleFreq;
        topicIntroFreq= this.params.getTopicIntroFrequency();
        // if the topic intro sho
        if (topicIntroFreq == PedagogicalModelParameters.frequency.always) {
            if (!smgr.getStudentState().isTopicIntroSeen(curTopic))
                smgr.getStudentState().addTopicIntrosSeen(curTopic);
            TopicIntro intro =topicSelector.getIntro(curTopic);
            lessonIntroGiven(intro ); // inform pedagogical move listeners that an intervention is given
            return intro;
        }
        else if (topicIntroFreq == PedagogicalModelParameters.frequency.oncePerSession &&
                !smgr.getStudentState().isTopicIntroSeen(curTopic)) {
            smgr.getStudentState().addTopicIntrosSeen(curTopic);
            TopicIntro intro =topicSelector.getIntro(curTopic);
            lessonIntroGiven(intro); // inform pedagogical move listeners that an intervention is given
            return intro;
        }

        return null;
    }

    public void initiateDemoProblem(Problem problem) throws Exception {
        smgr.getStudentState().setIsExampleShown(true);
        hintSelector.init(smgr);
        // need to put in the solution since its an example
        List<Hint> soln = hintSelector.selectFullHintPath(smgr, problem.getId());
        problem.setSolution(soln);
        problem.setMode(Problem.DEMO);
    }

    protected Problem getTopicExample (int curTopic) throws Exception {
        Problem problem = null;
        PedagogicalModelParameters.frequency exampleFreq= this.params.getTopicExampleFrequency();
        if (!smgr.getStudentState().isExampleShown()) {
            if (exampleFreq == PedagogicalModelParameters.frequency.always) {
                if (!smgr.getStudentState().isExampleSeen(curTopic))
                    smgr.getStudentState().addExampleSeen(curTopic);
                problem = topicSelector.getExample(curTopic, this.hintSelector);
                initiateDemoProblem(problem);
                return problem;
            }
            else if (exampleFreq == PedagogicalModelParameters.frequency.oncePerSession &&
                    !smgr.getStudentState().isExampleSeen(curTopic)) {
                smgr.getStudentState().addExampleSeen(curTopic);
                problem = topicSelector.getExample(curTopic, this.hintSelector);
                initiateDemoProblem(problem);
                return problem;
            }
        }
        return null;
    }

    protected InterventionResponse getNextProblemIntervention (NextProblemEvent e) throws Exception {
       NextProblemIntervention intervention = null;
        NextProblemInterventionResponse r=null;
        // If student is in challenge or review mode, we do not want interventions
        if (nextProblemInterventionSelector != null && !smgr.getStudentState().isInChallengeMode() && !smgr.getStudentState().isInReviewMode() &&
                e.isTutorMode())
        {
//            nextProblemInterventionSelector.init(smgr, this);
            intervention= nextProblemInterventionSelector.selectIntervention(e);
        }
        if (intervention != null) {
            // When we generate a NextProb intervention we need to save the grading of the problem that was just done (i.e. whether we want
            // an easier,harder/same problem) because after intervention responses are processed we go back to showing problems and we need
            // that grade for making calls to the TopicSelector.
            smgr.getStudentState().setNextProblemDesiredDifficulty(nextDiff.name());
            interventionGiven(intervention); // tell pedagogical move listeners that an intervention is given
//            smgr.getStudentState().setInBtwProbIntervention(true);
            r = new NextProblemInterventionResponse(intervention);
            // A hack because we need to determine if the Intervention returned should also build a Problem.   So we have to see if
            // the intervention is a NextProblemIntervention and then ask if it wants a problem built.   The only case of this currently
            // is the intervention which turns on/off MPP which at same time should show a new problem
            r.setBuildProblem(intervention.isBuildProblem());
        }
        return r;
    }

    protected boolean getForcedTopic (NextProblemEvent e) throws Exception {
        if (e.isForceTopic()) {
            smgr.getStudentState().setInReviewMode(false);
            smgr.getStudentState().setInChallengeMode(false);
            smgr.getStudentState().setInPracticeMode(true);
            smgr.getStudentState().newTopic();
            topicSelector.initializeTopic(e.getTopicToForce(), smgr.getStudentState());
            smgr.getStudentState().setCurTopic(e.getTopicToForce());
            return true;
        }
        return false;
    }

    /**
     * Return the problem selected by the student (in the requested mode if that is given)
     * @param e
     * @return
     */
    public ProblemResponse getProblemSelectedByStudent(NextProblemEvent e) throws Exception {

        if (! e.isForceProblem())
           return null;

        ProblemResponse r = null;
        // If student has selected a particular problem, then they must have left challenge/review mode and are back in practice mode.
        smgr.getStudentState().setInReviewMode(false);
        smgr.getStudentState().setInChallengeMode(false);
        smgr.getStudentState().setInPracticeMode(true);

        Problem p = ProblemMgr.getProblem(Integer.parseInt(e.getProbId()));
        p.setMode(Problem.PRACTICE);
        // The student may have selected a problem using the MPP.   This means they had to open up a topic in order to make
        // the selection.   This topic is passed through MPPTryProblemEvent and we stick it in the NextProblemEvent passed
        // to this as the topicToForce EVEN THOUGH WE ARE NOT REALLY FORCING THE TOPIC.   It is merely placed there so that
        // we can then add the topic name to the activity JSON to refresh the GUI so that it shows the topic of this problem
        int topicId = e.getTopicToForce();
        setProblemTopic(p, topicId);
        boolean showAsDemo = false;
        if (e.getProbMode() == null) {
            if (!smgr.getStudentState().isExampleShown() && params.getTopicExampleFrequency() != PedagogicalModelParameters.frequency.never) {
                showAsDemo = true;
            }
        }
        else if (e.getProbMode().equals(Problem.DEMO)) {
            showAsDemo = true;
        }

        // If the user asks for the problem to be given as a DEMO/EXAMPLE (this would only come from a TeachTopicEvent through Assistments) OR
        // the student is at a point in a new topic where an example has not been shown yet, then set the mode to DEMO
        if (e.isForceProblem() )     {
            if (showAsDemo) {
                initiateDemoProblem(p);
            }
            else smgr.getStudentState().setTopicNumPracticeProbsSeen(smgr.getStudentState().getTopicNumPracticeProbsSeen() + 1);
        if (p != null)
            problemGiven(p);

        r = new ProblemResponse(p);
        }
        if (p != null && p.getType().equals(Problem.HTML_PROB_TYPE)) {
            r.shuffleAnswers(smgr.getStudentState());
        }
        if (p != null && p.isParametrized()) {
            p.getParams().addBindings(r, smgr.getStudentId(), smgr.getConnection(), smgr.getStudentState());

        }
        return r;

    }

    private void setProblemTopic(Problem p, int topicId) {
        if (topicId > 0) {
            p.setInTopicId(topicId);
            Topic t = ProblemMgr.getTopic(topicId);
            if (t != null)
                p.setInTopicName(ProblemMgr.getTopic(topicId).getName());
            else p.setInTopicName("");
        }
    }

    public ProblemResponse getChallengingProblem (NextProblemEvent e) throws Exception {
        StudentState state = smgr.getStudentState();
        Problem p = null;
        ProblemResponse r=null;
        if (e.getMode() !=null && e.getMode().equalsIgnoreCase(CHALLENGE_MODE) && !state.isInChallengeMode()) {
            smgr.getStudentState().setInChallengeMode(true);
            smgr.getStudentState().setCurProblemIndexInTopic(-1);
        }
        if (smgr.getStudentState().isInChallengeMode()) {
            // when entering the mode, sideline the current topic
            if (e.isForceTopic() && e.getTopicToForce() != smgr.getStudentState().getCurTopic()) {
                smgr.getStudentState().setSidelinedTopic(smgr.getStudentState().getCurTopic());
            }
            p = doSelectChallengeProblem(e);
            if (p == null) {
                r = ProblemResponse.NO_MORE_CHALLENGE_PROBLEMS;
                ((ProblemResponse) r).setEndPage(ChallengeModeProblemSelector.END_PAGE);
                return r;
            }
        }
        if (p ==null && (smgr.getStudentState().isInChallengeMode())) {
            smgr.getStudentState().setInReviewMode(false);
            smgr.getStudentState().setInChallengeMode(false);
            e.clearTopicToForce(); // A topic is on the NextProb event because it is for review/challenge mode.  We clear it so the regular
        }
        if (p != null)  {
            r = new ProblemResponse(p) ;
            problemGiven(p); // tell all the pedagogical move listeners that a problem is being given.
        }
        return r;
    }

    public ProblemResponse getReviewProblem (NextProblemEvent e) throws Exception {
        StudentState state = smgr.getStudentState();
        Problem p = null;
        ProblemResponse r = null;
        if (e.getMode() !=null && e.getMode().equalsIgnoreCase(REVIEW_MODE) && !state.isInReviewMode()) {
            smgr.getStudentState().setInReviewMode(true);
            smgr.getStudentState().setCurProblemIndexInTopic(-1);
        }
        if (smgr.getStudentState().isInReviewMode()) {
            // when entering the mode, sideline the current topic
            if (e.isForceTopic() && e.getTopicToForce() != smgr.getStudentState().getCurTopic()) {
                smgr.getStudentState().setSidelinedTopic(smgr.getStudentState().getCurTopic());
            }
            p = doSelectReviewProblem(e);
            if (p == null)  {
                r= ProblemResponse.NO_MORE_REVIEW_PROBLEMS;
                ((ProblemResponse) r).setEndPage(ReviewModeProblemSelector.END_PAGE);
                return r;
            }
        }
        if (p ==null &&  smgr.getStudentState().isInReviewMode()) {
            smgr.getStudentState().setInReviewMode(false);
            smgr.getStudentState().setInChallengeMode(false);
            e.clearTopicToForce(); // A topic is on the NextProb event because it is for review/challenge mode.  We clear it so the regular
        }
        if (p != null)  {
            r = new ProblemResponse(p) ;
            problemGiven(p); // tell all the pedagogical move listeners that a problem is being given.
        }
        return r;

    }


//    public ProblemResponse getNonPracticeModeProblem(NextProblemEvent e) throws Exception {
//        StudentState state = smgr.getStudentState();
//        Problem p = null;
//        boolean topicForce = getForcedTopic(e);
//        ProblemResponse r = getProblemSelectedByStudent(e);
//        if (r != null) return r;
//        if (e.getMode() !=null && e.getMode().equalsIgnoreCase("challenge") && !state.isInChallengeMode()) {
//            smgr.getStudentState().setInChallengeMode(true);
//            smgr.getStudentState().setCurProblemIndexInTopic(-1);
//        }
//        else if (e.getMode() !=null && e.getMode().equalsIgnoreCase("review") && !state.isInReviewMode()) {
//            smgr.getStudentState().setInReviewMode(true);
//            smgr.getStudentState().setCurProblemIndexInTopic(-1);
//        }
//
//        if (smgr.getStudentState().isInChallengeMode()) {
//            // when entering the mode, sideline the current topic
//            if (e.isForceTopic() && e.getTopicToForce() != smgr.getStudentState().getCurTopic()) {
//                smgr.getStudentState().setSidelinedTopic(smgr.getStudentState().getCurTopic());
//            }
//            p = doSelectChallengeProblem(e);
//            if (p == null) {
//                r = ProblemResponse.NO_MORE_CHALLENGE_PROBLEMS;
//                ((ProblemResponse) r).setEndPage(ChallengeModeProblemSelector.END_PAGE);
//                return r;
//            }
//        }
//
//        if (smgr.getStudentState().isInReviewMode()) {
//            // when entering the mode, sideline the current topic
//            if (e.isForceTopic() && e.getTopicToForce() != smgr.getStudentState().getCurTopic()) {
//                smgr.getStudentState().setSidelinedTopic(smgr.getStudentState().getCurTopic());
//            }
//            p = doSelectReviewProblem(e);
//            if (p == null)  {
//                r= ProblemResponse.NO_MORE_REVIEW_PROBLEMS;
//                ((ProblemResponse) r).setEndPage(ReviewModeProblemSelector.END_PAGE);
//                return r;
//            }
//        }
//
//        if (p ==null && (smgr.getStudentState().isInChallengeMode() || smgr.getStudentState().isInReviewMode())) {
//            smgr.getStudentState().setInReviewMode(false);
//            smgr.getStudentState().setInChallengeMode(false);
//            e.clearTopicToForce(); // A topic is on the NextProb event because it is for review/challenge mode.  We clear it so the regular
//        }
////        Response r = callToGetForcedProb();
//        //Response r = null;
//        // sometimes the client forces the server to select a problem in a given topic.   Here we need to log that
//        // problem within that topic (indicated by problem.inTopicId having a topicId != -1)
//        if (p != null)  {
//            r = new ProblemResponse(p) ;
//            problemGiven(p); // tell all the pedagogical move listeners that a problem is being given.
//        }
//        return r;
//    }



    protected ProblemResponse getProblem(NextProblemEvent e, ProblemGrader.difficulty nextProbDesiredDiff) throws Exception {
        Problem p = problemSelector.selectProblem(smgr, e,nextProbDesiredDiff);
        ProblemResponse r=null;
        if (p != null) {
            p.setMode(Problem.PRACTICE);
            setProblemTopic(p, smgr.getStudentState().getCurTopic());
            smgr.getStudentState().setTopicNumPracticeProbsSeen(smgr.getStudentState().getTopicNumPracticeProbsSeen() + 1);
            problemGiven(p); // inform pedagogical move listeners that a problem is given.
            r = new ProblemResponse(p);
        }
        else r = ProblemResponse.NO_MORE_PROBLEMS;
        return r;
    }

    protected int switchTopics (int curTopic) throws Exception {
        int nextTopic = topicSelector.getNextTopicWithAvailableProblems(smgr.getConnection(), curTopic, smgr.getStudentState());
        smgr.getStudentState().newTopic();
        if (nextTopic != -1)
            newTopic(ProblemMgr.getTopic(nextTopic)); // inform pedagogical move listeners of topic switch
        topicSelector.initializeTopic(nextTopic, smgr.getStudentState());
        smgr.getStudentState().setCurTopic(nextTopic);
        return nextTopic;
    }




    public EndOfTopicInfo getReasonsForEndOfTopic () {
        return  reasonsForEndOfTopic;
    }

    public boolean isTopicContentAvailable (int topicId) throws Exception {
        //   reasonsForEndOfTopic will be bound if this method is called during processing of a NextProblem event because the first thing it
        // does is to score the student which binds this variable.   Other contexts such as the MPP want to know if a topic can play but
        // we haven't got a basis for scoring a student (to see max time, max problems, or content failure) so we just return if
        // there are any available problems in the topic.
        if (reasonsForEndOfTopic == null)
            return topicSelector.hasReadyContent(topicId);
        else return !reasonsForEndOfTopic.isTopicDone();
    }

    public EndOfTopicInfo getReasonsForTopicEnd () {
        return reasonsForEndOfTopic;
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


    /**
     * Process a request for a next problem
     * @param  e  NextProblemEvent asks for the next problem
     * @return
     * @throws Exception
     */
    public Response processNextProblemRequest(NextProblemEvent e) throws Exception {

        // We have a fixed sequence which prefers forced problems, followed by topic intros, examples, interventions, regular problems.
        // If we ever want something more customized (e.g. from a XML pedagogy defn),  this would have to operate based on that defn

        Response r;
        StudentState state = smgr.getStudentState();
        Problem curProb=null;

        r = getProblemSelectedByStudent(e);
        if (r == null) r = getChallengingProblem(e);
        if (r == null) r = getReviewProblem(e);
//        r = getNonPracticeModeProblem(e);
        // only grade the problem if we aren't trying to force a topic or problem
        if (r == null)
            gradeProblem(e.getProbElapsedTime());

        int curTopic = smgr.getStudentState().getCurTopic();
        this.reasonsForEndOfTopic=  topicSelector.isEndOfTopic(e.getProbElapsedTime(), nextDiff);
        boolean topicDone = curTopic == -1 || reasonsForEndOfTopic.isTopicDone();
        // second we try to find an intervention
        if (r == null) r = getNextProblemIntervention(e);
        // Some interventions are designed to be shown while a problem is being shown
        // These interventions are a property of a problem
        if (r == null || (r instanceof NextProblemInterventionResponse && ((NextProblemInterventionResponse) r).isBuildProblem())) {
            Intervention intervention = null;
            if (r != null)
                intervention = ((NextProblemInterventionResponse)r).getIntervention();
            r = getTopicIntroDemoOrProblem(e, state, curTopic, topicDone);
            // give the problem its intervention property
            if (r instanceof ProblemResponse)
                ((ProblemResponse)r).setIntervention(intervention);
            if (r instanceof TopicIntroResponse)
                curProb = ((TopicIntroResponse) r).getProblem() ;
            else if (r instanceof DemoResponse)
                curProb = ((DemoResponse) r).getProblem() ;
        }
        if (r != null && r instanceof ProblemResponse) {
            curProb = ((ProblemResponse) r).getProblem();

            if (curProb != null && curProb.getType().equals(Problem.HTML_PROB_TYPE)) {
                ((ProblemResponse)r).shuffleAnswers(smgr.getStudentState());
            }
            // If current problem is parametrized, then choose a binding for it and stick it in the ProblemResponse and ProblemState.
            if (curProb != null && curProb.isParametrized()) curProb.getParams().addBindings((ProblemResponse) r, smgr.getStudentId(), smgr.getConnection(), smgr.getStudentState());
        }
        if (learningCompanion != null )
            learningCompanion.processNextProblemRequest(smgr,e,r);
        if (curProb != null)
            smgr.getStudentModel().newProblem(state,curProb);
        if (r instanceof InterventionResponse)
            new TutorLogger(smgr).logNextProblemIntervention(e,(InterventionResponse) r);
        else new TutorLogger(smgr).logNextProblem(e, r.getCharacterControl());
        StudentEffort eff = studentModel.getEffort();
        r.setEffort(eff);
        return r;
    }

    public ProblemResponse getProblemInTopicSelectedByStudent (NextProblemEvent e) throws Exception {
        int nextTopic = e.getTopicToForce();
        smgr.getStudentState().newTopic();   // this completely resets the lesson state
        newTopic(ProblemMgr.getTopic(nextTopic)); // inform pedagogical move listeners of topic switch
        topicSelector.initializeTopic(nextTopic, smgr.getStudentState());
        smgr.getStudentState().setCurTopic(nextTopic);
        ProblemResponse r = getTopicIntroDemoOrProblem(e,smgr.getStudentState(),nextTopic,false);
        Problem p = r.getProblem();
        smgr.getStudentState().setCurProblem(p.getId());
        // If current problem is parametrized, then choose a binding for it and stick it in the ProblemResponse and ProblemState.
        // This line of code needs to be duplicated because ONR calls this function directly instead of processNextProblemRequest.
        if (p != null && p.getType().equals(Problem.HTML_PROB_TYPE)) {
            r.shuffleAnswers(smgr.getStudentState());
        }
        if (p != null && p.isParametrized()) {
            p.getParams().addBindings((ProblemResponse) r, smgr.getStudentId(), smgr.getConnection(), smgr.getStudentState());

        }
        return r;
    }

    private ProblemResponse getTopicIntroDemoOrProblem(NextProblemEvent e, StudentState state, int curTopic, boolean topicDone) throws Exception {
        ProblemResponse r=null;
        // If nothing is being forced, first see if topic switch is necessary.   If so,   maybe we need to show a topicIntro
        if ( topicDone ) {
            curTopic = switchTopics(curTopic);  // sets student state curTopic
            if (curTopic == -1)
                return ProblemResponse.NO_MORE_PROBLEMS;
            TopicIntro ti = getTopicIntro(curTopic);
            if (ti != null) {
                r = new TopicIntroResponse(ti);

            }
        }

        // new code. We might want to return a topic intro if this is the first problem we have shown in this session.
        if (r == null && state.getCurProblem() == -1) {
            TopicIntro ti = getTopicIntro(curTopic);
            if (ti != null) {
                r = new TopicIntroResponse(ti);
            }
        }
        // maybe we need to show an example
        if (r == null) {
            Problem ex = getTopicExample(curTopic);
            if (ex != null) {
                r=  new DemoResponse(ex);
                exampleGiven(ex);  // inform pedagogical move listeners of example being given
            }
        }

        // we have to return a problem
        if (r == null)   {
            r = getProblem(e, nextDiff);
        }
        return r;
    }




    @Override
    public Response processBeginInterventionEvent(BeginInterventionEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r = new Response();
        new TutorLogger(smgr).logShowIntervention(e, r, smgr.getStudentState().getLastIntervention());
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processEndInterventionEvent(EndInterventionEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r = new Response();
        new TutorLogger(smgr).logEndIntervention(e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processBeginExampleEvent(BeginExampleEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r = new Response();
        new TutorLogger(smgr).logBeginExample(e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processEndExampleEvent(EndExampleEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r = new Response();
        new TutorLogger(smgr).logEndExample( e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processBeginExternalActivityEvent(BeginExternalActivityEvent e) throws Exception {
        Response r = new Response();
        new TutorLogger(smgr).logBeginExternalActivity(e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processEndExternalActivityEvent(EndExternalActivityEvent e) throws Exception {
        Response r = new Response();
        new TutorLogger(smgr).logEndExternalActivity(e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processClickCharacterEvent(ClickCharacterEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r = new Response();
        new TutorLogger(smgr).logClickCharacter(e, r);
        return learningCompanion.processClickCharacterEvent(smgr,e);
    }

    @Override
    public Response processMuteCharacterEvent(MuteCharacterEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r = new Response();
        new TutorLogger(smgr).logMuteCharacter(e, r);
        if (learningCompanion != null)
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processUnMuteCharacterEvent(UnMuteCharacterEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r = new Response();
        new TutorLogger(smgr).logUnMuteCharacter(e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processEliminateCharacterEvent(EliminateCharacterEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r = new Response();
        new TutorLogger(smgr).logEliminateCharacter(e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processShowCharacterEvent(ShowCharacterEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r = new Response();
        new TutorLogger(smgr).logShowCharacter(e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processReadProblemEvent(ReadProblemEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        smgr.getStudentState().setIsTextReaderUsed(true);
        Response r = new Response();
        new TutorLogger(smgr).logReadProblem(e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    public InterventionSelector getLastInterventionSelector () throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String lastInterventionClass = smgr.getStudentState().getLastIntervention();
        Class interventionSelectorClass = Class.forName(lastInterventionClass);
        Constructor constructor = interventionSelectorClass.getConstructor(SessionManager.class, PedagogicalModel.class);
        InterventionSelector isel = (InterventionSelector) constructor.newInstance(smgr,this);
        return isel;
    }

    @Override
    public Response processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception {
        Response r;
        smgr.getStudentState().setProblemIdleTime(0);
        NextProblemInterventionSelector isel = (NextProblemInterventionSelector) getLastInterventionSelector();
        Intervention intervention = isel.processContinueNextProblemInterventionEvent(e);

        if (intervention != null) {
            r= new InterventionResponse(intervention);
        }
        // this does not want to generate another intervention.  So select a new prob
        else {
            StudentState state = smgr.getStudentState();
            boolean isTopicDone = gradeProblem(e.getProbElapsedTime());
            r = getTopicIntroDemoOrProblem(new NextProblemEvent(e.getElapsedTime(), e.getProbElapsedTime()), state, state.getCurTopic(), isTopicDone);
            studentModel.newProblem(state, ((ProblemResponse) r).getProblem());
        }
        new TutorLogger(smgr).logContinueNextProblemIntervention(e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processContinueAttemptInterventionEvent(ContinueAttemptInterventionEvent e) throws Exception {
        Response r;
        smgr.getStudentState().setProblemIdleTime(0);
        Intervention intervention = attemptInterventionSelector.processContinueAttemptInterventionEvent(e);

        if (intervention != null) {
            r= new InterventionResponse(intervention);
        }
        else {
            // TODO make sure we don't need to do the below and that we have an analog for it
            // we are done with post-attempt interventions.  Its now time to grade the problem.
            if (smgr.getStudentState().isProblemSolved())
                r= new Response("grade=true&isCorrect=true");
            else
                r= new Response("grade=true&isCorrect=false");
        }
        new TutorLogger(smgr).logContinueAttemptIntervention(e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r;
        Intervention intervention = nextProblemInterventionSelector.processInputResponseNextProblemInterventionEvent(e);
        e.setUserInput(nextProblemInterventionSelector.getUserInputXML());
//        intervention.setUserInput()
        // We need to know what grade the student got on the last problem they solved so we can tell if the topic is about to end
        String diff = smgr.getStudentState().getNextProblemDesiredDifficulty();
        nextDiff = ProblemGrader.difficulty.valueOf(diff);
        EndOfTopicInfo info = topicSelector.isEndOfTopic(e.getProbElapsedTime(), nextDiff);
        if (intervention != null && intervention instanceof SelectProblemSpecs) {
            nextDiff = ((SelectProblemSpecs) intervention).getDesiredDifficulty();
            info = topicSelector.isEndOfTopic(e.getProbElapsedTime(),nextDiff);
            if (info.isTopicDone()) {
                return  ProblemResponse.NO_MORE_PROBLEMS;
            }
            else r = getProblem(new NextProblemEvent(e.getElapsedTime(),e.getProbElapsedTime()),nextDiff);
            studentModel.newProblem(smgr.getStudentState(), ((ProblemResponse) r).getProblem());
        }
        else if (intervention != null) {
            r= new InterventionResponse(intervention);
        }
        else {
//            smgr.getStudentState().setInBtwProbIntervention(false);
            r= getTopicIntroDemoOrProblem(new NextProblemEvent(e.getElapsedTime(), e.getProbElapsedTime()),
                    smgr.getStudentState(), smgr.getStudentState().getCurTopic(), info.isTopicDone());
            studentModel.newProblem(smgr.getStudentState(), ((ProblemResponse) r).getProblem());
        }
        new TutorLogger(smgr).logInputResponseNextProblemIntervention( e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

    @Override
    public Response processInputResponseAttemptInterventionEvent(InputResponseAttemptInterventionEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r;
        Intervention intervention = attemptInterventionSelector.processInputResponseAttemptInterventionEvent(e);
        e.setUserInput(attemptInterventionSelector.getUserInputXML());
        if (intervention != null && intervention instanceof SelectHintSpecs) {
            r= doSelectHint((SelectHintSpecs) intervention);
        }
        else if (intervention != null) {
            r= new InterventionResponse(intervention);
        }
        else {
            // we are done with post-attempt interventions.  Its now time to grade the problem.
            if (smgr.getStudentState().isProblemSolved())
                r= new Response("&grade=true&isCorrect=true");
            else
                r= new Response("&grade=true&isCorrect=false");
        }
        new TutorLogger(smgr).logInputResponseAttemptIntervention( e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }


    public Problem doSelectChallengeProblem (NextProblemEvent e) throws Exception {
        StudentState state =smgr.getStudentState();
        int lastIx= state.getCurProblemIndexInTopic();
        lastIx++;
        List<Integer> topicProbIds = topicSelector.getClassTopicProblems(state.getCurTopic(), smgr.getClassID(), smgr.isTestUser());
        if (lastIx >= topicProbIds.size())    {
            state.setInChallengeMode(false);
            return null;
        }
        else {
            Problem p;
            if (e.getTopicToForce() > 0)
                smgr.getStudentState().setCurTopic(e.getTopicToForce());
            challengeModeSelector.init(smgr);
            p= challengeModeSelector.selectProblem(smgr, e, ProblemGrader.difficulty.HARDER);
            return p;
        }
    }

    public Problem doSelectReviewProblem (NextProblemEvent e) throws Exception {
        StudentState state =smgr.getStudentState();
        int lastIx= state.getCurProblemIndexInTopic();
        lastIx++;
        List<Integer> topicProbIds = topicSelector.getClassTopicProblems(state.getCurTopic(), smgr.getClassID(), smgr.isTestUser());
        if (lastIx >= topicProbIds.size())    {
            state.setInReviewMode(false);
            return null;
        }
        else {
            Problem p;
            if (e.getTopicToForce() > 0)
                smgr.getStudentState().setCurTopic(e.getTopicToForce());
            reviewModeSelector.init(smgr);
            p= reviewModeSelector.selectProblem(smgr, e, ProblemGrader.difficulty.HARDER);
            return p;
        }
    }


    @Override
    public void problemGiven(Problem p) throws SQLException {
        StudentState st = smgr.getStudentState();
        st.setNumRealProblemsThisTutorSession(st.getNumRealProblemsThisTutorSession()+1);
        st.setNumProblemsThisTutorSession(st.getNumProblemsThisTutorSession()+1);
        for (PedagogicalMoveListener l : this.pedagogicalMoveListeners)
            l.problemGiven(p);
    }

    @Override
    public void exampleGiven(Problem ex) throws SQLException {
        StudentState st = smgr.getStudentState();
        st.setNumProblemsThisTutorSession(st.getNumProblemsThisTutorSession()+1);
        for (PedagogicalMoveListener l : this.pedagogicalMoveListeners)
            l.exampleGiven(ex);
    }

    @Override
    public void lessonIntroGiven(TopicIntro intro) throws SQLException {
        for (PedagogicalMoveListener l : this.pedagogicalMoveListeners)
            l.lessonIntroGiven(intro);
    }

    @Override
    public void attemptGraded(boolean isCorrect) {
        for (PedagogicalMoveListener l : this.pedagogicalMoveListeners)
            l.attemptGraded(isCorrect);
    }

    @Override
    public void hintGiven( Hint hint) throws SQLException {
        for (PedagogicalMoveListener l : this.pedagogicalMoveListeners)
            l.hintGiven(hint);
    }



    @Override
    public void interventionGiven(Intervention intervention) throws SQLException {
        for (PedagogicalMoveListener l : this.pedagogicalMoveListeners)
            l.interventionGiven(intervention);
    }

    @Override
    public void newTopic(Topic t) {
        for (PedagogicalMoveListener l : this.pedagogicalMoveListeners)
            l.newTopic(t);
    }

    @Override
    public void newSession(int sessId) throws SQLException {
        new TutorLogger(smgr).newSession(smgr.getStudentId(),sessId,0);
        for (PedagogicalMoveListener l : this.pedagogicalMoveListeners)
            l.newSession(sessId);
    }


}
