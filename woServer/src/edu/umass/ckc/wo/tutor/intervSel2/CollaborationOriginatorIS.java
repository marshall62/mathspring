package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.collab.CollaborationManager;
import edu.umass.ckc.wo.collab.CollaborationState;
import edu.umass.ckc.wo.db.DbCollaborationLogging;
import edu.umass.ckc.wo.db.DbUser;
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

    //Stores state data, such as point in collaboration, time since collaboration
    private CollaborationState state;

    private static Logger logger = Logger.getLogger(CollaborationIS.class);

    public CollaborationOriginatorIS(SessionManager smgr) throws SQLException {
        super(smgr);
        state = CollaborationManager.getCollaborationState(smgr);
        //rezecib debug
        System.out.println("CollaborationOriginatorIS constructor");
    }

    //CollaborationOriginatorIS works a little differently, in that it gets called several times without being init'd,
    // via the SameIntervention path?
    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel){
        this.pedagogicalModel = pedagogicalModel;
    }

    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception{
        // This condition is true right after the originator is given his final intervention before starting the problem.   It tells them
        // how to work with their partner and they click OK.  That response is processed by  processInputResponseNextProblemInterventionEvent
        // below and it returns null so that the pedagogical model then attempts to find an intervention (which results in this being called)
        // None should be available (actually nothing guarantees this - it would be awful if some intervention (like look at the MPP) were
        // to suddenly come up; to prevent this every intervention selector would need to check that a partnership doesn't exist.
         // SOLUTION:  CollabPedagogicalModel will have to behave differently than BasePedagogicalModel when processing InputResponses
        //TODO: Better way to handle this transition?
        if(CollaborationManager.requestExists(smgr.getStudentId()) && CollaborationManager.getRequestedPartner(smgr.getStudentId()) != null){
//            CollaborationManager.removeRequest(smgr.getStudentId());
            return null;
        }
        else{
            // This intervention has inputs so that the student may accept or decline.
            //TODO: Make request addition dependent on the student's response (so partner doesn't get notified before decision)
            CollaborationManager.addRequest(smgr.getConnection(), smgr.getStudentId());
            DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "CollaborationOfferedBySystem_Originator");
            return new CollaborationOptionIntervention();
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
            CollaborationManager.decline(smgr.getStudentId());
//            rememberInterventionSelector(this);
            DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, option, "CollaborationDeclined_Originator");
            return null;
        }
        // When originator clicks the OK button to start working together with the partner this returns null so that next problem
        // is selected.  Or when the originator clicks the OK button after the collaboration has ended.
        else{
            //To begin
           if(CollaborationManager.requestExists(smgr.getStudentId())){
                DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "CollaborationConfirmationToBeginClickedOK_Originator");
               return null;
            }
           //After ended
            else{
               DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "CollaborationFinishedClickedOK_Originator");
               Response r=  smgr.getPedagogicalModel().processNextProblemRequest(new NextProblemEvent(e.getElapsedTime(),e.getProbElapsedTime()));
               return r;
           }
        }
    }

    // asks the originator if they want to continue to wait for the partner (helper).   This happens every 60 seconds and is triggered
    // by code in intervhandlers.js processCollaborationOriginatorIntervention
    public Intervention processInterventionTimeoutEvent(InterventionTimeoutEvent e) throws Exception {
        Integer partner = CollaborationManager.getRequestedPartner(smgr.getStudentId());

        // returns message to originator saying that they are waiting for partner
        if(partner == null){
            //rezecib debug
            System.out.println("CollaborationOriginator Time Waiting: " + e.getTimeWaiting()
                    + " (max wait: " + state.getMaxPartnerWaitPeriod() + ")");
            if(e.getTimeWaiting() >= state.getMaxPartnerWaitPeriod()){
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
