package edu.umass.ckc.wo.tutor.agent;

import edu.umass.ckc.wo.content.Hint;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.lc.LCAction;
import edu.umass.ckc.wo.lc.LCRuleset;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.TransactionLogger;
import edu.umass.ckc.wo.tutor.hintSel.StrategicHintSelector;
import edu.umass.ckc.wo.tutor.response.*;
import edu.umass.ckc.wo.tutor.studmod.AffectStudentModel;
import edu.umass.ckc.wo.tutormeta.LearningCompanion;
import org.dom4j.rule.RuleSet;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 12/12/13
 * Time: 8:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class RuleDrivenLearningCompanion extends LearningCompanion {

    private LCRuleset ruleSet;


    public RuleDrivenLearningCompanion() {

    }

    public void setRuleSet (LCRuleset ruleSet) {
        this.ruleSet = ruleSet;
    }


    @Override
    public String getGender() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getEthnicity() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getCharactersName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // Default implementation of methods that must be overidden by learning companions that are subclasses.
    //
    // Learning Companions must contribute to handling requests by adding movie clips to control the on-screen character.
    // The learning companion is passed the session manager, the request event, and the response that has been built so far.
    // The response has already been built by the pedagogical model's helpers (hint selector, problem selector, intervention selector).
    // The learning companion is passed the already built response (r) and it can do whatever processing it needs to do
    // (accessing the student model and student state through the session manager) so that it selects some movie clips to control
    // character.   The movie clip names should be added to the response and the response should be returned.
    // r.addCharacterControl(String[] clipNames)

    public Response processShowSolveProblemRequest (SessionManager smgr, ShowSolveProblemEvent e, HintSequenceResponse r) throws Exception {
        addLearningCompanionToResponse(r);
        return r;
    }


    public Response processNextProblemRequest (SessionManager smgr, NextProblemEvent e, Response r) throws Exception {
        addEmotion("idle");
        addLearningCompanionToResponse(r);
        return r;
    }


//    public Response processNextProblemRequest (SessionManager smgr, NextProblemEvent e, Response r) throws Exception {
//        StudentState state = smgr.getStudentState();
//        if (r instanceof ProblemResponse) {
//            Problem p = ((ProblemResponse) r).getProblem();
//            // when content runs out,  p will be null
//            if (p == null)
//                return r;
//            if (p instanceof TopicIntro)
//                return r;
//            // if this is the first non-topicIntro add the solve-together
//            if  (state.getTopicNumProbsSeen() == 1)
//                pushEmotion("solve together");
//
//            if (p.isPractice() && p.hasStrategicHint())
//                addEmotion("idea");
//        }
//        addLearningCompanionToResponse(r);
//        return r;
//    }

    public Response processHintRequest (SessionManager smgr, HintEvent e, HintResponse r) throws Exception {
        addLearningCompanionToResponse(r);
        return r;
    }
    public Response processAttempt (SessionManager smgr, AttemptEvent e, AttemptResponse r) throws Exception {
        LCAction act = this.ruleSet.runRulesForEvent(smgr, e, "Attempt");
        if (act != null)    {
            clips.add(act.getName()); // The LCAction has the name of the html lc action and this is all we really use
            addLearningCompanionToResponse(r);
        }
        return r;
    }
    public Response processInputResponse (SessionManager smgr, InputResponseEvent e, Response r) throws Exception {
        addLearningCompanionToResponse(r);
        return r;
    }
    public Response processContinueRequest (SessionManager smgr, ContinueEvent e, Response r) throws Exception {
        addLearningCompanionToResponse(r);
        return r;
    }

    // The basic response to a click on the character is to return the strategic hint if it hasn't already been played or
    // some indication that it has played or that there isn't one.  The BasicHintSelector is responsible for getting the hint.
    public Response processClickCharacterEvent(SessionManager smgr, ClickCharacterEvent e) throws Exception {
        // if the strategic hint has already been shown, return  strategicAlreadyPlayed
        if (smgr.getStudentState().isStrategicHintShown()) {
            Hint h = Hint.STRATEGIC_HINT_PLAYED;
            h.setProblemId(smgr.getStudentState().getCurProblem());
            ClickCharacterResponse r = new ClickCharacterResponse(h.getLabel(),h.getId());
            TransactionLogger.logClickCharacter(smgr.getConnection(), e, r, smgr.getStudentState().getCurProblem(), smgr.getStudentId(), smgr.getSessionNum());
            return r;

        }
        else {
            StrategicHintSelector hs= new StrategicHintSelector();
            hs.init(smgr);
            Hint h = hs.selectStrategicHint(smgr,smgr.getStudentState().getCurProblem());
            // possibly alter the label of the strategic hint to be customized based on the learning
            // companion being used (e.g. jakeStrategicHint, janeStrategicHint)
            setStrategicHintLabel(h);
            // return the hint (its either noStrategicHint or strategic_hint)
            ClickCharacterResponse r = new ClickCharacterResponse(h.getLabel(),h.getId());
            TransactionLogger.logClickCharacter(smgr.getConnection(), e, r, smgr.getStudentState().getCurProblem(), smgr.getStudentId(),smgr.getSessionNum());
            return r;
        }
    }

    // All learning companions need to say they are solving the problems with the student and to show interest in the
    // examples.  Otherwise, by default, idle.   Sub-classes will override this.
    protected List<String> selectEmotions(AffectStudentModel m, Response r, SessionManager smgr) throws Exception {
        if (r instanceof ProblemResponse) {
            Problem p = ((ProblemResponse) r).getProblem();
            // By request of Ivon 6/14
            if (smgr.getStudentState().getNumRealProblemsThisTutorSession() == 1)
            {
                clips.add("solveTogether");
                return clips;
            }
            if (p != null && p.isExample())  {
                clips.add("interestHigh");
                return clips;
            }
        }
        clips.add("idle");

        return clips;
    }

}
