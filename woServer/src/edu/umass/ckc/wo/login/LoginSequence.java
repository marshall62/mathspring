package edu.umass.ckc.wo.login;

import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.config.LoginXML;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbSession;
import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.login.interv.LoginIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelector;
import edu.umass.ckc.wo.tutor.model.InterventionGroup;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.woserver.ServletInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 4/13/15
 * Time: 3:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginSequence {
    private SessionManager smgr;
    private PedagogicalModel pedagogicalModel;
    private InterventionGroup interventionGroup;
    private ServletInfo servletInfo;
    private Connection conn;
    private int sessId;
    private int studId;


     // need to be able to use this class before there is a sessionId passed on the params

    public LoginSequence(ServletInfo servletInfo) throws Exception {
        this.servletInfo = servletInfo;
        this.conn = servletInfo.getConn();
        ServletParams params = servletInfo.getParams();
        this.sessId = params.getInt("sessionId");
        this.smgr = new SessionManager(servletInfo.getConn(),sessId,servletInfo.getHostPath(),servletInfo.getContextPath()).buildExistingSession();
        pedagogicalModel = smgr.getPedagogicalModel();
        Pedagogy ped = pedagogicalModel.getPedagogy();
        buildInterventions(ped);

    }

    private void buildInterventions (Pedagogy ped) throws Exception {
        LoginXML loginXML = ped.getLoginXML();
        interventionGroup = new InterventionGroup(loginXML.getInterventions());
        interventionGroup.buildInterventions(smgr,pedagogicalModel);
        for (InterventionSelector s : interventionGroup.getAllInterventions()) {
            LoginIntervention ls = (LoginIntervention) s;
            ls.init(servletInfo.getRequest());
        }
    }


    public void processAction (ServletParams params) throws Exception {
        interventionGroup.selectIntervention(smgr,new SessionEvent(params),"Login");
    }
}
