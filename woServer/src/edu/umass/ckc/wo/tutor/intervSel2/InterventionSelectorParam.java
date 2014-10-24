package edu.umass.ckc.wo.tutor.intervSel2;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 2/10/14
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class InterventionSelectorParam {
    private String name;
    private String value;

    public InterventionSelectorParam() {
    }

    public InterventionSelectorParam(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
