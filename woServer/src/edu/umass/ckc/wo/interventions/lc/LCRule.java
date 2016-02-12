package edu.umass.ckc.wo.interventions.lc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 1/25/16
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class LCRule {

    List<LCCondition> conditions;

    public LCRule() {
        conditions = new ArrayList<LCCondition>();
    }

    public List<LCCondition> getConditions() {
        return conditions;
    }

    public void addCondition(LCCondition c) {
        conditions.add(c);
    }
}
