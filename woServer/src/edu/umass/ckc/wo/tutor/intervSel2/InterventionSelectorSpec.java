package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorParam;
import org.jdom.Element;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 2/10/14
 * Time: 11:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class InterventionSelectorSpec {

    private String className;
    private List<InterventionSelectorParam> params;
    private Element configXML;

    public InterventionSelectorSpec() {
    }

    public InterventionSelectorSpec(String className, List<InterventionSelectorParam> paramSpecs, Element config) {
        this.className = className;
        this.params = paramSpecs;
        this.configXML=config;
    }

    public String getClassName() {
        return className;
    }

    public List<InterventionSelectorParam> getParams() {
        return params;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Element getConfigXML() {
        return configXML;
    }


}
