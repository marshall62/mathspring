package edu.umass.ckc.wo.tutor.intervSel2;

import ckc.servlet.servbase.UserException;
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
    private String runFreq;
    private InterventionSelector selector;

    public static final String ALWAYS="always";
    public static final String ONCE="once";
    public static final String ONCE_PER_SESSION="oncepersession";
    public static final String ONCE_PER_TOPIC="oncepertopic";

    public InterventionSelectorSpec() {
    }

    public InterventionSelectorSpec(String className, List<InterventionSelectorParam> paramSpecs, Element config) {
        this.className = className;
        this.params = paramSpecs;
        this.configXML=config;
    }

    private boolean checkValidFreq (String inputFreq) {
        inputFreq = inputFreq.toLowerCase();
        return inputFreq.equals(ONCE) || inputFreq.equals(ONCE_PER_SESSION) || inputFreq.equals(ONCE_PER_TOPIC);
    }

    public InterventionSelectorSpec (Element intervSel) throws UserException {
        this.onEvent = intervSel.getAttributeValue("onEvent");
        this.className = intervSel.getAttributeValue("class");
        String w = intervSel.getAttributeValue("weight");

        if (w != null)
            this.weight = Integer.parseInt(w);
        else this.weight = 1;
        String freq = intervSel.getAttributeValue("runFreq");  // usually null
        if (freq == null)
            this.runFreq=ALWAYS;
        else if (checkValidFreq(freq))
            this.runFreq = freq.toLowerCase();
        else throw new UserException("runFrequency is not a valid value:" + freq);
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

    public String getRunFreq() {
        return runFreq;
    }

    public InterventionSelector buildIS (SessionManager smgr) throws Exception {

        InterventionSelector sel= (InterventionSelector) Class.forName(this.getFullyQualifiedClassname()).getConstructor(SessionManager.class).newInstance(smgr);
        sel.setParams(this.getParams());
        sel.setConfigXML(this.getConfigXML());
        //sel.init(smgr,smgr.getPedagogicalModel());  // Want to put off the call to init til just before we call .selectIntervention()
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

    public void setSelector(InterventionSelector selector) {
        this.selector = selector;
    }

    public InterventionSelector getSelector() {
        return selector;
    }
}
