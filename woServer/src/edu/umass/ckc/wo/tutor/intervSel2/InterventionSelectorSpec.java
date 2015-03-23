package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorParam;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import org.jdom.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 2/10/14
 * Time: 11:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class InterventionSelectorSpec implements Comparable<InterventionSelectorSpec> {
    private String onEvent;
    private String className;
    private List<InterventionSelectorParam> params;
    private Element configXML;
    private int weight;

    public InterventionSelectorSpec() {
    }

    public InterventionSelectorSpec(String className, List<InterventionSelectorParam> paramSpecs, Element config) {
        this.className = className;
        this.params = paramSpecs;
        this.configXML=config;
    }

    public InterventionSelectorSpec (Element intervSel) {
        this.onEvent = intervSel.getAttributeValue("onEvent");
        this.className = intervSel.getAttributeValue("class");
        String w = intervSel.getAttributeValue("weight");
        if (w != null)
            this.weight = Integer.parseInt(w);
        else this.weight = 1;
        this.configXML = intervSel.getChild("config");
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


    public String getFullyQualifiedClassname () {
        String className =   Pedagogy.getFullyQualifiedClassname(Pedagogy.defaultClasspath + ".intervSel2", this.getClassName());
        return className;
    }

    public Element getConfigXML() {
        return configXML;
    }

    public String getOnEvent() {
        return onEvent;
    }

    public int getWeight() {
        return weight;
    }

    public InterventionSelector buildIS (SessionManager smgr) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        InterventionSelector sel= (InterventionSelector) Class.forName(this.getClassName()).getConstructor(SessionManager.class, PedagogicalModel.class).newInstance(smgr,smgr.getPedagogicalModel());
        sel.setParams(this.getParams());
        sel.setConfigXML(this.getConfigXML());
        sel.init(smgr,smgr.getPedagogicalModel());
        return sel;
    }


    @Override
    public int compareTo(InterventionSelectorSpec interventionSpec) {
        if (this.getWeight() < interventionSpec.getWeight())
            return -1;
        else if (this.getWeight() > interventionSpec.getWeight())
            return 1;
        else return 0;
    }
}
