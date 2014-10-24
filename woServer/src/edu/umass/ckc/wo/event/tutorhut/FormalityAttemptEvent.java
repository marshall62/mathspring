package edu.umass.ckc.wo.event.tutorhut;

import ckc.servlet.servbase.ServletParams;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: 7/5/11
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class FormalityAttemptEvent extends AttemptEvent {
    private int probId;
    public static final String PROB_ID = "probId";

    public FormalityAttemptEvent(ServletParams p) throws Exception {
        super(p);
        setProbId(p.getInt(PROB_ID,-1));
    }

    public void setProbId(int probId) {
        this.probId = probId;
    }

    public int getProbId() {
        return probId;
    }
}
