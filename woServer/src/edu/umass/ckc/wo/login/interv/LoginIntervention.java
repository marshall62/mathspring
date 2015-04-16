package edu.umass.ckc.wo.login.interv;

import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelector;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 4/14/15
 * Time: 4:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginIntervention  extends InterventionSelector {
    protected HttpServletRequest request;

    public LoginIntervention(SessionManager smgr) {
        super(smgr);
    }

    public void init (HttpServletRequest req) {
        this.request = req;
    }

    @Override
    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
