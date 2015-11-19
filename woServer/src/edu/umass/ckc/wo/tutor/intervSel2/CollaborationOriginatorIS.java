package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.PartnerManager;
import edu.umass.ckc.wo.db.DbCollaborationLogging;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.event.tutorhut.ContinueNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.event.tutorhut.InterventionTimeoutEvent;
import edu.umass.ckc.wo.interventions.*;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.response.InterventionResponse;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.Intervention;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 3/13/15
 * Time: 2:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class CollaborationOriginatorIS extends NextProblemInterventionSelector {

    private static Logger logger = Logger.getLogger(CollaborationIS.class);
    int minIntervalBetweenCollabInterventions = 60 * 1000 * 5; // wait 5 minutes between collab offers.

    public CollaborationOriginatorIS(SessionManager smgr) throws SQLException {
        super(smgr);
    }

    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel){
        this.pedagogicalModel = pedagogicalModel;
    }

    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception{
        long now = System.currentTimeMillis();
        long lastInterventionForCollab = smgr.getStudentState().getLastInterventionTime();
        long timeSinceLastCollab = now - lastInterventionForCollab;

        // This condition is true right after the originator is given his final intervention before starting the problem.   It tells them
        // how to work with their partner and they click OK.  That response is processed by  processInputResponseNextProblemInterventionEvent
        // below and it returns null so that the pedagogical model then attempts to find an intervention (which results in this being called)
        // None should be available (actually nothing guarantees this - it would be awful if some intervention (like look at the MPP) were
        // to suddenly come up; to prevent this every intervention selector would need to check that a partnership doesn't exist.
         // SOLUTION:  CollabPedagogicalModel will have to behave differently than BasePedagogicalModel when processing InputResponses
        if(PartnerManager.requestExists(smgr.getStudentId()) && PartnerManager.getRequestedPartner(smgr.getStudentId()) != null){
//            PartnerManager.removeRequest(smgr.getStudentId());
            return null;
        }
        else{


            //Random is used to make sure the collaboration is not triggered every time.
            double random = Math.random();
            // Based on nothing other than time a student is offered help from a neighbor.  This intervention has
            // inputs so that the student may accept for decline.
            if(timeSinceLastCollab > minIntervalBetweenCollabInterventions && random < 0.15) {
//                rememberInterventionSelector(this);   // can't do it this way in the new intervention model (must have interventions provide class name of IS so that this class is used)
                smgr.getStudentState().setLastInterventionTime(now);
                PartnerManager.addRequest(smgr.getConnection(), smgr.getStudentId(), new ArrayList<String>());
                DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "CollaborationOfferedBySystem_Originator");
                return new CollaborationOptionIntervention();
            }
            else{
                return null;
            }
        }
    }

    public Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception{
        String option = e.getServletParams().getString(CollaborationTimedoutIntervention.OPTION);
        // Do you want to work with someone OR do you want to continue waiting  Answer of YES handled here:  keeps the user waiting
        if(option != null && option.equals("Yes")){
//            rememberInterventionSelector(this);
            DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, option, "CollaborationAccepted_Originator");
            Intervention interv =  new CollaborationOriginatorIntervention();
            return new InterventionResponse(interv); // DM had modify to return an InterventionResponse rather than intervention
        }
        // Do you want to work with someone =NO OR do you want to continue waiting =NO handled here:
        // logs it and then selects next problem
        else if(option != null && (option.equals("No") || option.equals("No_alone") || option.equals("No_decline"))){
            PartnerManager.decline(smgr.getStudentId());
//            rememberInterventionSelector(this);
            DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, option, "CollaborationDeclined_Originator");
            return null;
        }
        // When originator clicks the OK button to start working together with the partner this returns null so that next problem
        // is selected.  Or when the originator clicks the OK button after the collaboration has ended.
        else{
            //To begin
           if(PartnerManager.requestExists(smgr.getStudentId())){
                DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "CollaborationConfirmationToBeginClickedOK_Originator");
            }
           //After ended
            else{
                DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "CollaborationFinishedClickedOK_Originator");
            }
            return null;
        }
    }

    // asks the originator if they want to continue to wait for the partner (helper).   This happens every 60 seconds and is triggered
    // by code in intervhandlers.js processCollaborationOriginatorIntervention
    public Intervention processInterventionTimeoutEvent(InterventionTimeoutEvent e) throws Exception {
        Integer partner = PartnerManager.getRequestedPartner(smgr.getStudentId());

        // returns message to originator saying that they are waiting for partner
        if(partner == null){
            if(e.getTimeWaiting()>= 60000){
                DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "CollaborationAskContinueWaiting_Originator");
                return new CollaborationTimedoutIntervention();
            }
            else{
                return new SameIntervention();
            }
        }
        else{
            User u = DbUser.getStudent(smgr.getConnection(),partner);
            String name = (u.getFname() != null && !u.getFname().equals("")) ? u.getFname() : u.getUname();
            DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), partner, null, "CollaborationConfirmationToBeginAlert_Originator");
            Intervention interv = new CollaborationConfirmationIntervention(name);
            return interv;
        }


//        rememberInterventionSelector(this);
    }
}
