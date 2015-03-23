package edu.umass.ckc.wo.tutor.model;

import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorSpec;

import java.util.ArrayList;
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

    public InterventionGroup() {
        this.interventionsSpecs = new ArrayList<InterventionSelectorSpec>();
    }

    public void add (InterventionSelectorSpec s) {
        this.interventionsSpecs.add(s);
    }

    public List<InterventionSelectorSpec> getInterventionsSpecs() {
        return interventionsSpecs;
    }
}
