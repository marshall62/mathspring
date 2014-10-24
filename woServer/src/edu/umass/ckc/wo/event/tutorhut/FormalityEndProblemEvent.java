package edu.umass.ckc.wo.event.tutorhut;

import ckc.servlet.servbase.ServletParams;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 7, 2011
 * Time: 2:03:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class FormalityEndProblemEvent extends FormalityBeginProblemEvent {
    private long probElapsedTime;

    public FormalityEndProblemEvent(ServletParams p) throws Exception {
        super(p);
        probElapsedTime = p.getLong("probElapsedTime");
    }

    public long getProbElapsedTime() {
        return probElapsedTime;
    }


}