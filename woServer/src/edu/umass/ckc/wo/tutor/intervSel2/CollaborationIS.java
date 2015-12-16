package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.PartnerManager;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.event.tutorhut.InterventionTimeoutEvent;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.Intervention;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 2/2/15
 * Time: 7:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollaborationIS extends NextProblemInterventionSelector {

    private static Logger logger = Logger.getLogger(CollaborationIS.class);

    public CollaborationIS(SessionManager smgr) throws SQLException {
        super(smgr);
    }

    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel){
        this.pedagogicalModel = pedagogicalModel;
    }

    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception{
        // See if this student has been requested as a partner for some other student who needs help
        Integer partner = PartnerManager.checkForRequestingPartner(smgr.getStudentId());
        // If a student (originator) is awaiting this student's help (the partner) then we give the partner some interventions
        if(partner != null){
            CollaborationPartnerIS ii= new CollaborationPartnerIS(smgr);
            ii.init(smgr,pedagogicalModel);
            // tells the helper to work with the person next to them (on the other computer)
            // and locks their screen until they complete the problem together.
            return ii.selectInterventionWithId(e, partner);
        }
        //If eligible partners exist for a student that (may) need help (the originator), we may put the originator into a collab situation
        else if(PartnerManager.hasEligiblePartners(smgr.getConnection(), smgr.getStudentId(), new ArrayList<String>())){
            //
            if (smgr.getStudentState().getLessonState().getNextProblemMode() == null)
                return null;
            if(smgr.getStudentState().getLessonState().getNextProblemMode().equals(Problem.PRACTICE)){
              CollaborationOriginatorIS ii = new CollaborationOriginatorIS(smgr);
               ii.init(smgr,pedagogicalModel);
               return ii.selectIntervention(e);
            }
            else{
                return null;
            }
        }
        //If no eligible partners exist
        else{
            return null;
        }
    }

    public Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception{
        // The new intervention model does not allow one intervention selector to delegate to another intervention selector as done by this
        // implementation (because constructing the intervention selector using the name of the last intervention selector needs an intervention spec
        // object which is only created if an intervention selector is placed in pedagogies.xml.   Since these delegates don't have entries in pedagogies.xml
        // they cannot be constructed because no InterventionSpec exists).   So I make all interventions pass back the intervention selector to which
        // the input response should go.  e.g.  destination=edu.umass.ckc.wo.tutor.intervSel2.CollaborationOriginatorIS
        String dest = e.getServletParams().getString("destination");
        Class c = Class.forName(dest);
        NextProblemInterventionSelector is =  (NextProblemInterventionSelector) c.getConstructor(SessionManager.class).newInstance(smgr);
        is.init(smgr,pedagogicalModel);
        return is.processInputResponseNextProblemInterventionEvent(e);
//        return ((NextProblemInterventionSelector) getInterventionSelectorThatGeneratedIntervention()).processInputResponseNextProblemInterventionEvent(e);
    }

    @Override
    public Intervention processInterventionTimeoutEvent(InterventionTimeoutEvent e) throws Exception {
        String dest = e.getServletParams().getString("destination");
        Class c = Class.forName(dest);
        NextProblemInterventionSelector is =  (NextProblemInterventionSelector) c.getConstructor(SessionManager.class).newInstance(smgr);
        is.init(smgr,pedagogicalModel);
        return is.processInterventionTimeoutEvent(e);
//        return ((NextProblemInterventionSelector) getInterventionSelectorThatGeneratedIntervention()).processTimedInterventionEvent(e);
    }

}

