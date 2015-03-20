package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.Intervention;

import java.lang.reflect.InvocationTargetException;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 11/14/13
 * Time: 12:39 PM
 * Combines other NextProblemInterventionSelectors by putting them in a List and using the first one in the list
 * to request an intervention as the winner.
 */
public class NextProblemListCombinerIS extends NextProblemInterventionSelector {


    public NextProblemListCombinerIS(SessionManager smgr, PedagogicalModel pedagogicalModel) {
        super(smgr, pedagogicalModel);
    }

    @Override
    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception {
        for (NextProblemInterventionSelector sel: this.subSelectorList) {
            NextProblemIntervention intervention = sel.selectIntervention(e);
            if (intervention != null) {
                // TODO :   Must record in student state the intervention selector that is returning the intervention
                //
                rememberInterventionSelector(sel);
                return intervention;
            }
        }
        return null;
    }

    @Override
    public Response processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception {
        return ((NextProblemInterventionSelector) getInterventionSelectorThatGeneratedIntervention()).processContinueNextProblemInterventionEvent(e);

    }


    public Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception {
        if (this.subSelectorList != null) {
            String classname = smgr.getStudentState().getLastIntervention();
            for (NextProblemInterventionSelector helper: this.subSelectorList) {
                if (helper.getClass().getName().equals(classname))
                    return helper.processInputResponseNextProblemInterventionEvent(e);
            }
        }
        return ((NextProblemInterventionSelector) getInterventionSelectorThatGeneratedIntervention()).processInputResponseNextProblemInterventionEvent(e);
    }


}
