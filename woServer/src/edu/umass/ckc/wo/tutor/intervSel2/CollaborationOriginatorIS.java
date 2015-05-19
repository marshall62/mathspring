package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.PartnerManager;
import edu.umass.ckc.wo.db.DbCollaborationLogging;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.event.tutorhut.ContinueNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.event.tutorhut.TimedInterventionEvent;
import edu.umass.ckc.wo.interventions.CollaborationConfirmationIntervention;
import edu.umass.ckc.wo.interventions.CollaborationOriginatorIntervention;
import edu.umass.ckc.wo.interventions.CollaborationTimeoutIntervention;
import edu.umass.ckc.wo.interventions.CollaborationOptionIntervention;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.User;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
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
    int minIntervalBetweenCollabInterventions = 60 * 1000;

    public CollaborationOriginatorIS(SessionManager smgr, PedagogicalModel pedagogicalModel) throws SQLException {
        super(smgr, pedagogicalModel);
    }

    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel){}

    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception{
        long now = System.currentTimeMillis();
        long lastInterventionForCollab = smgr.getStudentState().getLastInterventionTime();
        long timeSinceLastCollab = now - lastInterventionForCollab;
        if(PartnerManager.requestExists(smgr.getStudentId()) && PartnerManager.getRequestedPartner(smgr.getStudentId()) != null){
            PartnerManager.removeRequest(smgr.getStudentId());
            return null;
        }
        else{

            //Random is used to make sure the collaboration is not triggered every time.
            double random = Math.random();
            if(timeSinceLastCollab > minIntervalBetweenCollabInterventions && random < .1) {
                rememberInterventionSelector(this);
                smgr.getStudentState().setLastInterventionTime(now);
                PartnerManager.addRequest(smgr.getConnection(), smgr.getStudentId(), new ArrayList<String>());
                DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "CollaborationOptionIntervention");
                return new CollaborationOptionIntervention();
            }
            else{
                return null;
            }
        }
    }


    //  Send one of these when the thing closes
    //  Select 2nd intervention that does wait
    public Intervention processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception{
        Integer partner = PartnerManager.getRequestedPartner(smgr.getStudentId());
        if(partner == null){
            rememberInterventionSelector(this);
            //Do not log the intervention this time or the database will become cluttered
            return new CollaborationOriginatorIntervention();
        }
        else{
            rememberInterventionSelector(this);
            User u = DbUser.getStudent(smgr.getConnection(),partner);
            String name = (u.getFname() != null && !u.getFname().equals("")) ? u.getFname() : u.getUname();
            DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), partner, null, "CollaborationConfirmationIntervention");
            return new CollaborationConfirmationIntervention(name);
        }
    }

    public Intervention processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception{
        String option = e.getServletParams().getString(CollaborationTimeoutIntervention.OPTION);
        if(option != null && option.equals("Yes")){
            rememberInterventionSelector(this);
            DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, option, "CollaborationOriginatorIntervention");
            return new CollaborationOriginatorIntervention();
        }
        else if(option != null && (option.equals("No") || option.equals("No_alone") || option.equals("No_decline"))){
            PartnerManager.decline(smgr.getStudentId());
            rememberInterventionSelector(this);
            DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, option, "CollaborationConfirmationIntervention");
            return null;
        }
        else{
            return null;
        }
    }

    public Intervention processTimedInterventionEvent(TimedInterventionEvent e) throws Exception {
        rememberInterventionSelector(this);
        DbCollaborationLogging.saveEvent(conn, smgr.getStudentId(), 0, null, "CollaborationTimeoutIntervention");
        return new CollaborationTimeoutIntervention();
    }
}
