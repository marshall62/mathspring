package edu.umass.ckc.wo.tutor.model;

import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionState;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelector;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorSpec;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutormeta.Intervention;
import edu.umass.ckc.wo.tutormeta.PedagogicalMoveListener;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 3/11/15
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class InterventionGroup {
    private List<InterventionSelectorSpec> interventionsSpecs;
    private Element interventionsElt;

    public InterventionGroup() {
        this.interventionsSpecs = new ArrayList<InterventionSelectorSpec>();

    }

    public InterventionGroup (Element interventionsElt) {
        this();
        this.interventionsElt = interventionsElt;


    }

    /**
     * Parses the XML of an interventions Element
     *  <interventions>
     <interventionSelector onEvent="EndOfTopic" weight="1" class="TopicSwitchAskIS">
     <config>
     <ask val="false"></ask>
     </config>
     </interventionSelector>
     .
     .
     </interventions>
     * @param smgr
     * @param pedMod
     * @throws Exception
     */
    public void buildInterventions (SessionManager smgr, PedagogicalModel pedMod) throws Exception {
        List<Element> intervSels = interventionsElt.getChildren("interventionSelector");
        for (Element intervSel : intervSels) {
            InterventionSelectorSpec spec = new InterventionSelectorSpec(intervSel);

            this.add(spec);
            // we build the selector and stick it inside the spec.   These need to be built so that they can listen to pedaogogical moves sent to them
            // through the ped model.
            InterventionSelector sel = spec.buildIS(smgr);
            spec.setSelector(sel);
            pedMod.addPedagogicalMoveListener( (PedagogicalMoveListener) sel);
        }
    }

    public void add (InterventionSelectorSpec s) {
        this.interventionsSpecs.add(s);
    }


    public InterventionSelectorSpec getInterventionSelectorSpec(String lastInterventionClass) {
        for (InterventionSelectorSpec s : this.getInterventionsSpecs()) {
            if (s.getFullyQualifiedClassname().equals(lastInterventionClass))
                return s;
        }
        return null;
    }


    public List<InterventionSelectorSpec> getInterventionsSpecs() {
        return interventionsSpecs;
    }

    public List<InterventionSelectorSpec> getCandidateInterventionForEvent(String onEvent) {
        List<InterventionSelectorSpec> specs = new ArrayList<InterventionSelectorSpec>();
        for (InterventionSelectorSpec i: this.getInterventionsSpecs()) {
            if (i.getOnEvent().equals(onEvent))
                specs.add(i);
        }
        return specs;
    }

    // Go through the intervention selectors in priority order and take the first one that returns an intervention.  This
    // means each selector is responsible for shutting itself off after it runs.
    public Intervention selectIntervention (SessionManager smgr, SessionEvent e, String onEvent) throws Exception {
        List<InterventionSelectorSpec> candidates = getCandidateInterventionForEvent(onEvent);
        Collections.sort(candidates);   // sort into ascending order by weight
        for (InterventionSelectorSpec spec: candidates) {
            // skip it if its a run-once intervention that has already been run
            if (!(spec.isRunOnce() && InterventionState.hasRun(smgr.getConnection(), smgr.getStudentId(), spec.getClassName()))) {
                InterventionSelector isel = spec.getSelector();
                isel.init(smgr,smgr.getPedagogicalModel());
                // will check to see if the selector wants to run an intervention
                Intervention interv= isel.selectIntervention(e);
                if (interv != null) {
                    if (spec.isRunOnce()) {
                        boolean success = InterventionState.setRun(smgr.getConnection(),smgr.getStudentId(),spec.getClassName());
                        // if for some reason it fails to set the status of this intervention selector as having run (e.g. primary
                        // key violation) this will abort using that intervention a second time.
                        if (!success)
                            continue;
                    }
                    // This is the only place where we need to remember the intervention selector
                    smgr.getStudentState().setLastIntervention(spec.getFullyQualifiedClassname());
                    return interv;
                }
            }
        }
        return null;
    }

    public List<InterventionSelector> getAllInterventions() {
        List<InterventionSelector> selectors = new ArrayList<InterventionSelector>();
        for (InterventionSelectorSpec spec: this.interventionsSpecs) {
            selectors.add(spec.getSelector());
        }
        return selectors;
    }
}
