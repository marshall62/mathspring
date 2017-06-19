package edu.umass.ckc.wo.strat;

/**
 * Created by marshall on 6/16/17.
 */
public class ISParam {
    private int id;
    private String name;
    private String value;

    public ISParam(int paramId, String n, String v) {
        this.id=paramId;
        this.name=n;
        this.value=v;
    }

    public String toString () {
        return "\t\t\tParam: " + id + " " + name + "=" + value;
    }
}
