package edu.umass.ckc.wo.lc;

import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.event.tutorhut.EventType;
import edu.umass.ckc.wo.event.tutorhut.TutorHutEvent;
import edu.umass.ckc.wo.smgr.SessionManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/24/16
 * Time: 8:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class InferenceEngine {
    private static Logger logger = Logger.getLogger(InferenceEngine.class);
    private SessionManager smgr;

    public InferenceEngine (SessionManager smgr) {
        this.smgr=smgr;
    }

    /**
     * When an event comes in from the user, we run the rules to find a possible response from the
     * learning companion to include with the data returned to the client.
     *
     * Steps:
     * Gather all rulesets for user
     * For each ruleset:
     *     for each rule that applies to the given event:
     *         if rule satisfies the rulesets meta-rules
     *             add to candidates list
     * sort candidates list by priority
     * for each rule in candidates list:
     *    if rule applies, fire it.
     * @param event
     */
    public void runLCRules (SessionEvent event) {

    }

    /**
     * Given a bunch of rulesets and an event, return the rule that applies.
     * @param event
     * @param rulesets
     * @return
     * @throws Exception
     */
    public LCRule runRulesForEvent(TutorHutEvent event, List<LCRuleset> rulesets) throws Exception {
        List<LCRule> rulesThatApply = new ArrayList<LCRule>();
        for (LCRuleset rs: rulesets) {
            List<LCRule> eventRules = rs.getRulesForEvent(event); // get the rules that apply to the event
            // add rules to rulesThatApply - add the ones that satisfy the meta ruels.
            rulesThatSatisfyMetaRules(rs, eventRules, event, rulesThatApply);

        }
        Collections.sort(rulesThatApply); // sorts rules according to their priority
        for (LCRule r : rulesThatApply) {
            r.setup(smgr,event);
            boolean res = r.test();
            // if the result of testing the rule is true, then we stop testing rules and return it
            if (res) {
                logger.debug("Rule" + r.getName() + " is true");
                LCAction act = r.getAction();
                logger.debug("Action: " + act.getMsgText());
                RuleHistoryCache.getInstance().addRuleInstantiation(smgr,r);
                return r;
            }
        }

        return null;
    }

    /**
     * From a list of candidates we go through the rulesets meta-rules and return the list of rules
     * that satisfy all the meta ruels.
     * @param ruleset
     * @param candidates
     * @param event
     * @return
     */
    private void rulesThatSatisfyMetaRules(LCRuleset ruleset, List<LCRule> candidates, TutorHutEvent event, List<LCRule> rulesThatApply) {
        List<LCMetaRule> metaRules = ruleset.getMetaRules();
        long now = System.currentTimeMillis();
        StudentRuleHistory hist = RuleHistoryCache.getInstance().getStudentHistory(smgr);
        if (hist != null) {
            for (LCMetaRule mr : metaRules) {
                // this will add in the rules that satisfy the meta rule.
                rulesThatApply.addAll(rulesThatSatisfyMetaRule(mr, hist, candidates, now));
            }
        }
        else rulesThatApply.addAll(candidates);
    }

    /**
     * Given the rule history for a student and a meta-rule and the rules that apply so far, return a list of
     * those rules that satisfy the meta-rule.
     * @param mr
     * @param hist
     * @param candidates
     * @return
     */
    private List<LCRule> rulesThatSatisfyMetaRule(LCMetaRule mr, StudentRuleHistory hist, List<LCRule> candidates, long now) {
        // TODO not sure what the interface should be so that a meta-rule can be applied
        // It seems like a meta-rule should be given a set of candidate LCrules and the students rule history and it
        // should be able to return a list of candidate rules that satisfy the meta-rule.
        candidates = mr.apply(hist,candidates, now); // will get a new (reduced) list of candidates that satisfy it
        return candidates;
    }


}
