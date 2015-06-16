package edu.umass.ckc.wo.event.tutorhut;

import ckc.servlet.servbase.ServletParams;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: May 21, 2015
 * Time: 9:59:59 AM
 * Sent by a timer that is part of an intervention that runs in the client.   Typically used to see if the intervention
 * should continue or be removed in favor of something else.
 */
public class InterventionTimeoutEvent extends IntraProblemEvent {
    private long timeSinceStart;

    public InterventionTimeoutEvent(ServletParams p) throws Exception {
        super(p);
        this.timeSinceStart = p.getLong("timeSinceStart",0);
    }

    public long getTimeSinceStart() {
        return timeSinceStart;
    }
}