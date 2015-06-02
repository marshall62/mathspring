package edu.umass.ckc.wo.interventions;

/**
 * TimeoutInterventions are returned to the client and can put up a dialog with a message just like other interventions, but there
 * is no continue or submit button.   Instead the intervention sits on screen and starts a wait loop in the browser.   It sends
 * a TimeoutEvent to the server after its wait period.  The TimeoutEvent gets sent back to the InterventionSelector that generated this
 * intervention and it can return an indication to the client to keep spinning its wait loop, a new intervention, or a new problem.
 * User: david
 * Date: 5/21/15
 * Time: 2:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeoutIntervention {

    public long waitTime;




}
