package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutormeta.Intervention;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 11/14/13
 * Time: 12:39 PM
 * Combines other NextProblemInterventionSelectors by putting them in a List and using the first one in the list
 * to request an intervention as the winner.
 */
public class AttemptListCombinerIS extends AttemptInterventionSelector {

    public AttemptListCombinerIS(SessionManager smgr, PedagogicalModel pedagogicalModel) {
        super(smgr, pedagogicalModel);
    }

    @Override
    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Intervention selectIntervention(AttemptEvent e) throws Exception {
        for (AttemptInterventionSelector sel: this.subSelectors) {
            Intervention intervention = sel.selectIntervention(e);
            if (intervention != null) {
                rememberInterventionSelector(sel);
                return intervention;
            }
        }
        return null;
    }

    @Override
    public Intervention processContinueAttemptInterventionEvent(ContinueAttemptInterventionEvent e) throws Exception {
        return ((AttemptInterventionSelector) getInterventionSelectorThatGeneratedIntervention()).processContinueAttemptInterventionEvent(e);

    }

    @Override
    public Intervention processInputResponseAttemptInterventionEvent(InputResponseAttemptInterventionEvent e) throws Exception {
        if (this.subSelectors != null) {
            String classname = smgr.getStudentState().getLastIntervention();
            for (AttemptInterventionSelector helper: this.subSelectors) {
                if (helper.getClass().getName().equals(classname))
                    return helper.processInputResponseAttemptInterventionEvent(e);
            }
        }
        return ((AttemptInterventionSelector) getInterventionSelectorThatGeneratedIntervention()).processInputResponseAttemptInterventionEvent(e);
    }
}
