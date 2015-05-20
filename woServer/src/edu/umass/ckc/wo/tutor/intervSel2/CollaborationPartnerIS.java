package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.PartnerManager;
import edu.umass.ckc.wo.db.DbCollaborationLogging;
import edu.umass.ckc.wo.event.tutorhut.ContinueNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.interventions.*;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutormeta.Intervention;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 3/13/15
 * Time: 2:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class CollaborationPartnerIS extends NextProblemInterventionSelector {

    private static Logger logger = Logger.getLogger(CollaborationIS.class);
    private String partnerName = null;

    public CollaborationPartnerIS(SessionManager smgr, PedagogicalModel pedagogicalModel) throws SQLException {
        super(smgr, pedagogicalModel);
    }

    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel){}

    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception{
        rememberInterventionSelector(this);
        CollaborationPartnerIntervention interv = new CollaborationPartnerIntervention();
        interv.setPartner(partnerName);
        return interv;
    }

    public NextProblemIntervention selectInterventionWithId(NextProblemEvent e, int id) throws Exception{
        partnerName = PartnerManager.getPartnerName(smgr.getConnection(), id);
        DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), id, null, "CollaborationPartnerIntervention");
        return selectIntervention(e);
    }

    public Intervention processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception{
        if(PartnerManager.isPartner(smgr.getStudentId())){
            rememberInterventionSelector(this);
            CollaborationPartnerIntervention interv = new CollaborationPartnerIntervention();
            Integer partnerId = PartnerManager.getRequestingPartner(smgr.getStudentId());
            interv.setPartner(PartnerManager.getPartnerName(conn, partnerId));
            return interv;
        }
        rememberInterventionSelector(this);
        DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "CollaborationPartnerIntervention");
        return new FinishCollaborationIntervention();
    }

    //  For when students are asked to help or when waiting for a helper (so when polling)
    public Intervention processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception{
        return null;
    }

}
