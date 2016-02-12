package edu.umass.ckc.wo.interventions.lc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 1/25/16
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class LCRuleset {
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
}
