package edu.umass.ckc.wo.interventions.lc;

import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.smgr.SessionManager;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 3/10/16
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class LCRuleComponent {

    protected SessionManager smgr;
    protected SessionEvent event;

    public void setup (SessionManager smgr, SessionEvent event) {
        this.smgr = smgr;
        this.event = event;
    }
}


