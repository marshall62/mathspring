package edu.umass.ckc.wo.tutor.pedModel;

import edu.umass.ckc.wo.PartnerManager;
import edu.umass.ckc.wo.db.DbCollaborationLogging;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.interventions.FinishCollaborationIntervention;
import edu.umass.ckc.wo.log.TutorLogger;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorSpec;
import edu.umass.ckc.wo.tutor.intervSel2.NextProblemInterventionSelector;
import edu.umass.ckc.wo.tutor.response.*;
import edu.umass.ckc.wo.tutormeta.Intervention;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/29/15
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollabPedagogicalModel extends BasePedagogicalModel {

    public CollabPedagogicalModel(){
        super();
    }

    public CollabPedagogicalModel(SessionManager smgr, Pedagogy pedagogy) throws SQLException {
        super(smgr, pedagogy);
    }

    public boolean isCollaborating (int studId) {
        return PartnerManager.requestExists(studId) && PartnerManager.getRequestedPartner(studId) != null;
    }

    public Response processNextProblemRequest(NextProblemEvent e) throws Exception {
        int studId = smgr.getStudentId();
        if(isCollaborating(studId)){
            PartnerManager.removeRequest(studId);
            DbCollaborationLogging.saveEvent(smgr.getConnection(), smgr.getStudentId(), 0, null, "CollaborationFinishedAlert_Originator");
            Intervention interv = new FinishCollaborationIntervention("Originator");
            smgr.getStudentState().setLastIntervention("edu.umass.ckc.wo.tutor.intervSel2.CollaborationIS");
            return new InterventionResponse(interv);
        }
        return super.processNextProblemRequest(e);
    }


    public Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception {
        smgr.getStudentState().setProblemIdleTime(0);
        Response r = lessonModel.processUserEvent(e) ; // give lesson model a chance to weigh in.
        // If the lesson model did not handled the event, this will find the last intervention and have it process the event.
        // We then see if there is an intervention that applies after that is done (or process an internal event
        if ( r == null) {
            String lastInterventionClass = smgr.getStudentState().getLastIntervention();
            InterventionSelectorSpec spec= interventionGroup.getInterventionSelectorSpec(lastInterventionClass);
            if (spec != null) {
                NextProblemInterventionSelector intSel = (NextProblemInterventionSelector) spec.buildIS(smgr);
                intSel.init(smgr,this);
                // N.B. Assumption is that we no longer get Interventions back
                r = intSel.processInputResponseNextProblemInterventionEvent( e);
                // XML created to represent user input is retrieved from the IS and put in the event for logger to stick in eventlog.userinput
                e.setUserInput(intSel.getUserInputXML());
                // The last intervention selector will either return an InternalEvent or null
                // if an internal state is returned, then process it
                if (r instanceof InternalEvent)
                    // this should not happen because pedagogical models don't have internal events.
                    r = processInternalEvent((InternalEvent) r);
                    // if null comes back, see if the pedagogical model has an intervention only if this user is not collaborating
                else if (r==null && !isCollaborating(smgr.getStudentId()))
                    r =  getNextProblemIntervention(new NextProblemEvent(e.getServletParams()));
                // otherwise its an InterventionResponse which will be logged and returned.
            }
        }
        // the lesson model processed the intervention input but didn't want to select a response after that
        // so see if this model wants to intervene.
        else if (r instanceof InterventionInputProcessed) {
            r = getNextProblemIntervention(new NextProblemEvent(e.getServletParams()));
        }

        // If we don't pick an intervention or if the lesson model didn't, grade the last problem and try to give a new problem
        if (r == null) {
            StudentState state = smgr.getStudentState();
            // have to regrade last problem so that we can select a problem
            gradeLastProblem();

            r = getNextProblem(null);
            studentModel.newProblem(state, ((ProblemResponse) r).getProblem());
        }

        new TutorLogger(smgr).logInputResponseNextProblemIntervention( e, r);
        if (learningCompanion != null )
            learningCompanion.processUncategorizedEvent(e,r);
        return r;
    }

}
