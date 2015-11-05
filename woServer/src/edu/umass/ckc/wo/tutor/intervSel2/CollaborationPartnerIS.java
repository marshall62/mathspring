package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.PartnerManager;
import edu.umass.ckc.wo.db.DbCollaborationLogging;
import edu.umass.ckc.wo.event.tutorhut.ContinueNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.InterventionTimeoutEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.interventions.*;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.response.InterventionResponse;
import edu.umass.ckc.wo.tutor.response.Response;
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

    public CollaborationPartnerIS(SessionManager smgr) throws SQLException {
        super(smgr);
    }

    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel){
        this.pedagogicalModel=pedagogicalModel;
    }

    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception{
//        rememberInterventionSelector(this);
        // The helper is given an intervention that tells
        // them to help the person who is next to them (partnerName).
        //  This intervention plays in the client along with a loop that runs and sends an event every second.
        //   This event is handled below in  processContinueNextProblemInterventionEvent
        CollaborationPartnerIntervention interv = new CollaborationPartnerIntervention();
        interv.setPartner(partnerName);
        return interv;
    }

    // Tell the helper that they are about to work with a partner to give them some help.
    //
    public NextProblemIntervention selectInterventionWithId(NextProblemEvent e, int id) throws Exception{
        // the person who is waiting for help is the given id.   This is the partner.
        partnerName = PartnerManager.getPartnerName(smgr.getConnection(), id); // get the partner's name
        // update DB with collab event indicating the two students are starting to collaborate
        DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), id, null, "CollaborationPartnerIntervention");
        return selectIntervention(e);
    }

    //  THis handles the input from the last intervention saying they are done and then clicking OK
    public Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception{
        DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "CollaborationFinished");
        return null;
    }

    @Override
    public Intervention processInterventionTimeoutEvent(InterventionTimeoutEvent e) throws Exception {
        // while the helper is working with the originator this is true and this returns the intervention that tells the helper
        // he must work with the originator.  Happens every second.    This partnership ends when they click the nextproblem button
        // and is cleaned out by the CollaborationOrignatorIS
        if(PartnerManager.isPartner(smgr.getStudentId())){
            SameIntervention interv = new SameIntervention();
        //    CollaborationPartnerIntervention interv = new CollaborationPartnerIntervention();
        //    Integer partnerId = PartnerManager.getRequestingPartner(smgr.getStudentId());
            // this breaks the originator out of their wait loop
          //  interv.setPartner(PartnerManager.getPartnerName(conn, partnerId));
            return interv;
        }
        // this happens when the collaboration is done.  THe reason they are not partners anymore (above condition of if)
        // is because the originator clicks NextProblem button which then sends an event to CollaborationIS which removes the partnership.
        //  So we tell the helper he is done.
        DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "FinishCollaborationIntervention");
        Intervention interv= new FinishCollaborationIntervention();
        return interv;
    }

}
