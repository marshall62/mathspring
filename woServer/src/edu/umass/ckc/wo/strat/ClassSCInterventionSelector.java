package edu.umass.ckc.wo.strat;

import java.util.List;

/**
 * Created by marshall on 6/16/17.
 */
public class ClassSCInterventionSelector {
    private int id;
    private String config;
    private String name;
    private String className;
    private String onEvent;

    private List<ISParam> params;

    public void setConfig(String config) {
        this.config = config;
    }

    public String getConfig() {
        return config;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setOnEvent(String onEvent) {
        this.onEvent = onEvent;
    }

    public List<ISParam> getParams() {
        return params;
    }

    public void setParams(List<ISParam> params) {
        this.params = params;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString () {
        StringBuilder sb = new StringBuilder("InterventionSelector " + id + " " + name + "\n");
        for (ISParam p : this.params) {
            sb.append("\t\t\t" + p.toString() + "\n");
        }
        return sb.toString();
    }
}
