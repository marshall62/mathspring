package edu.umass.ckc.wo.lc;

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
    private int condId;
    private LCExpr expr;
    boolean applyNot = false;

    public LCCondition (int id, String fnName, String relop, String param, String paramType, boolean applyNot) {
        this.condId = id;
        if (paramType.equals("String"))
            expr = new LCExpr(fnName,null,relop,param);
        else if (paramType.equals("Integer"))
            expr = new LCExpr(fnName,null,relop,Integer.parseInt(param));
        else if (paramType.equals("Double"))
            expr = new LCExpr(fnName,null,relop,Double.parseDouble(param));
        this.applyNot = applyNot;
    }


    public LCCondition (LCExpr expr, boolean applyNot) {
        this.applyNot = applyNot;
        this.expr = expr;
    }

    public void setup (SessionManager smgr, SessionEvent ev) {

    }

    public boolean eval ( ) throws Exception {
        boolean v = expr.eval();
        return applyNot ? !v : v;
    }
}
