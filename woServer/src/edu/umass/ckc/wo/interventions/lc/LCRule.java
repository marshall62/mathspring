package edu.umass.ckc.wo.interventions.lc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 *   A single rule that is composed of several conditions that can be tested.  The rule is evaluated by evaluating all its conditions.
 *   If they are all true, the rule's action is executed.
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 1/25/16
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class LCRule extends LCRuleComponent {

    List<LCCondition> conditions;
    LCAction action;

    public LCRule() {
        conditions = new ArrayList<LCCondition>();
    }

    public List<LCCondition> getConditions() {
        return conditions;
    }

    public void addCondition(LCCondition c) {
        conditions.add(c);
    }

    public void apply () {
        action.setup(smgr,event);
        action.eval();
    }


    public boolean eval () throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        boolean fail = false;
        for (LCCondition c : conditions) {
            c.setup(smgr,event);
            if (!c.eval()) {
                fail = true;
                break;
            }
        }
        return !fail;

    }
}
