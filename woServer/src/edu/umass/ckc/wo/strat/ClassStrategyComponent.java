package edu.umass.ckc.wo.strat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marshall on 6/16/17.
 */
public class ClassStrategyComponent {
    private int id;
    private String name;
    private String className;
    private List<SCParam> params;
    private List<ClassSCInterventionSelector> interventionSelectors;

    public ClassStrategyComponent(int scId, String name, String className) {
        this.id=scId;
        this.name=name;
        this.className=className;
        this.interventionSelectors = new ArrayList<ClassSCInterventionSelector>();
    }

    public void addInterventionSelector(ClassSCInterventionSelector isel) {
        this.interventionSelectors.add(isel);
    }

    public void setParams(List<SCParam> params) {
        this.params = params;
    }

    public List<SCParam> getParams() {
        return params;
    }

    public List<ClassSCInterventionSelector> getInterventionSelectors() {
        return interventionSelectors;
    }

    public String toString () {
        StringBuilder sb = new StringBuilder("StrategyComponent: " + id + " " + name + "\n");
        for (SCParam p: params) {
            sb.append("\t\t" + p.toString() + "\n");
        }
        for (ClassSCInterventionSelector sel: this.interventionSelectors)
            sb.append("\t\t" + sel.toString() + "\n");
        return sb.toString();

    }
}
