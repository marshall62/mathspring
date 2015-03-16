package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.PartnerManager;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.event.tutorhut.ContinueNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
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

    public CollaborationIS(SessionManager smgr, PedagogicalModel pedagogicalModel) throws SQLException {
        super(smgr, pedagogicalModel);
    }

    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel){}

    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception{
        Integer partner = PartnerManager.checkForRequestingPartner(smgr.getStudentId());
        //TODO log partner here somehow
        if(partner != null){
              return (new CollaborationPartnerIS(smgr, pedagogicalModel)).selectInterventionWithId(e, partner);
        }
        //If eligible partners exist
        else if(PartnerManager.hasEligiblePartners(smgr.getConnection(), smgr.getStudentId(), new ArrayList<String>())){
            if(smgr.getStudentState().getLessonState().getNextProblemMode().equals(Problem.PRACTICE)){
              return (new CollaborationOriginatorIS(smgr, pedagogicalModel)).selectIntervention(e);
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

    // Some interventions ask if you want to do something
    //  No choices
    //  Use this one
    //  Look at TopicSwitchIntervention for reference
    //  Send one of these when the thing closes
    //  Select 2nd intervention that does wait
    public Intervention processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception{
        return ((NextProblemInterventionSelector) getInterventionSelectorThatGeneratedIntervention()).processContinueNextProblemInterventionEvent(e);
    }

    //  For when students are asked to help or when waiting for a helper (so when polling)
    public Intervention processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception{
        return ((NextProblemInterventionSelector) getInterventionSelectorThatGeneratedIntervention()).processInputResponseNextProblemInterventionEvent(e);
    }

}
