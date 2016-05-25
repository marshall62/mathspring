package edu.umass.ckc.wo.lc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/21/16
 * Time: 10:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class LCMetaRule {
    private String name;
    private String units;
    private String value;
    private int vali;

    public LCMetaRule(String name, String units, String value) {
        this.name = name;
        this.units = units;
        this.value = value;
        if (units.equalsIgnoreCase("milliseconds"))
            this.vali = Integer.parseInt(value);
        else if (units.equalsIgnoreCase("seconds"))
            this.vali = Integer.parseInt(value) * 1000;
        else if (units.equalsIgnoreCase("minutes"))
            this.vali = Integer.parseInt(value) * 60 * 1000;
    }

    public String getName() {
        return name;
    }

    public String getUnits() {
        return units;
    }

    public String getValue() {
        return value;
    }



    public List<LCRule> apply(StudentRuleHistory hist, List<LCRule> candidates, long now) {
        List<LCRule> keep = new ArrayList<LCRule>();
        for (LCRule r : candidates) {
            if (testInstantiations(hist, r, now))
                keep.add(r);
        }
        return keep;
    }

    /**
     * See if this meta-rule is true by testing the given rule against the student history and the current time.
     * @param hist
     * @param r
     * @param now
     * @return
     */
    private boolean testInstantiations(StudentRuleHistory hist, LCRule r, long now) {
        for (StudentRuleHistory.LCRuleInstantiation inst : hist.getHistory()) {
            // fireMinimumFrequency means that we should return false if this rule fails any the test against
            //  any rule instantiation
            if (this.name.equalsIgnoreCase("ruleFireMinimumFrequency") && !testInstantiation(inst,r,now))
                 return false;
        }
        return true;

    }

    /**
     * Test a given rule instantiation against the candidate rule and the current time.
     * @param inst
     * @param r
     * @param now
     * @return
     */
    private boolean testInstantiation(StudentRuleHistory.LCRuleInstantiation inst, LCRule r, long now) {
        // If the instantiation is for the given rule r, make sure it was fired longer ago than
        // the meta rules value.
        if (this.name.equalsIgnoreCase("ruleFireMinimumFrequency") && inst.getR()==r)  {
            long diff = now - inst.getTime();
            return diff > this.vali;
        }
        return true;

    }
}
