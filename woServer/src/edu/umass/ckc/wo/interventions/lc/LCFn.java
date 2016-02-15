package edu.umass.ckc.wo.interventions.lc;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 1/25/16
 * Time: 12:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class LCFn {
    private String fnName; // a method in the LCAccessors class
    private Class[] args;

    public LCFn(String fnName, Class[] args) {
        this.fnName = fnName;
        this.args = args;
    }

    public String getFnName() {
        return fnName;
    }

    public void setFnName(String fnName) {
        this.fnName = fnName;
    }

    public Class[] getArgs() {
        return args;
    }

    public void setArgs(Class[] args) {
        this.args = args;
    }
}
