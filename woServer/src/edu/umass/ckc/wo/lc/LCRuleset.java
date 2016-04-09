package edu.umass.ckc.wo.lc;

import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.smgr.SessionManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 1/25/16
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class LCRuleset {
    private static Logger logger = Logger.getLogger(LCRuleset.class);
    List<LCRule> rules;

    public LCRuleset() {
        rules = new ArrayList<LCRule>();
    }

    public List<LCRule> getRules() {
        return rules;
    }

    public void addRule (LCRule r) {
        rules.add(r);
    }

    public List<LCRule> getRulesForEvent (String onEvent) {
        List<LCRule> rules = new ArrayList<LCRule>();
        for (LCRule r : rules) {
            if (r.getOnEvent().equals(onEvent))
                rules.add(r);
        }
        return rules;
    }

    public LCAction runRulesForEvent(SessionManager smgr, SessionEvent event, String onEvent) throws Exception {
        List<LCRule> rules = getRulesForEvent(onEvent);
        Collections.sort(rules); // sorts rules according to their priority
        for (LCRule r : rules) {
            r.setup(smgr,event);
            boolean res = r.test();
            // if the result of testing the rule is true, then we stop testing rules and apply this rule's action
            if (res) {
                logger.debug("Rule" + r.getName() + " is true");
                LCAction act = r.apply();
                return act;
            }
        }
        return null;
    }
}
