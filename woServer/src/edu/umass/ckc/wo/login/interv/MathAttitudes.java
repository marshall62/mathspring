package edu.umass.ckc.wo.login.interv;

import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.db.DbUserProfile;
import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.login.LoginParams;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutormeta.Intervention;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 4/14/15
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class MathAttitudes extends LoginInterventionSelector {
    private static final String JSP = "mathAttitudes.jsp";

    public MathAttitudes(SessionManager smgr) throws SQLException {
        super(smgr);
    }

    public Intervention selectIntervention (SessionEvent e) throws SQLException {
        long shownTime = this.interventionState.getTimeOfLastIntervention();
        boolean isFirstLogin = DbUser.isFirstLogin(smgr.getConnection(),smgr.getStudentId());
        // isFirstLogin is checked so that we only show this the first time the student logs in.
        // shownTime is checked to make sure we don't show this more than once in that first login
        if (!isFirstLogin || shownTime > 0)
            return null;
        else {
            super.selectIntervention(e);
            return new LoginIntervention(JSP);
        }
    }



    public void processInput (ServletParams params) throws SQLException {


        int confidence = params.getInt(LoginParams.CONFIDENCE,0);
        int excitement = params.getInt(LoginParams.EXCITEMENT,0);
        int frustration = params.getInt(LoginParams.FRUSTRATION,0);
        int interest = params.getInt(LoginParams.INTEREST,0);
        // TODO might want to put in a correct servlet path rather than ""

        DbUserProfile.deleteProfile(servletInfo.getConn(), smgr.getStudentId())  ;
        DbUserProfile.setValues(conn, smgr.getStudentId(), confidence, excitement, interest, frustration);

    }



    public String f (SessionManager smgr) {
        return JSP;
    }
}
