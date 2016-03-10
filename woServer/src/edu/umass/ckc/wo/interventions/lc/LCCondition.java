package edu.umass.ckc.wo.interventions.lc;

import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.smgr.SessionManager;

import java.lang.reflect.InvocationTargetException;

/**
 * A condition is a boolean expression composed of expressions joined by boolean operators AND, OR, and NOT.
 * For now This is being kept simple and is limited to just a single expression and NOT.
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 1/25/16
 * Time: 12:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class LCCondition  extends LCRuleComponent {
    private LCExpr expr;
    boolean applyNot = false;


    public LCCondition (LCExpr expr, boolean applyNot) {
        this.applyNot = applyNot;
        this.expr = expr;
    }

    public void setup (SessionManager smgr, SessionEvent ev) {

    }

    public boolean eval ( ) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        boolean v = expr.eval();
        return applyNot ? !v : v;
    }
}
