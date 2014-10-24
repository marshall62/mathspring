package edu.umass.ckc.wo.event.tutorhut;

import ckc.servlet.servbase.ServletParams;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 7, 2011
 * Time: 2:03:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class FormalityBeginProblemEvent extends BeginProblemEvent {
    public static final String PROB_ID = "probId";
    private int probId;

    public FormalityBeginProblemEvent(ServletParams p) throws Exception {
        super(p);
        probId = p.getInt(PROB_ID,-1);
    }

    public int getProbId() {
        return probId;
    }
}
