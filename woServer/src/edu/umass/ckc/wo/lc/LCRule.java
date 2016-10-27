package edu.umass.ckc.wo.lc;

import java.util.ArrayList;
import java.util.List;

/**
 *   A single rule that is composed of several conditions that can be tested.  The rule is evaluated by evaluating all its conditions.
 *   If they are all true, the rule's action is executed.
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 1/25/16
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class LCRule extends LCRuleComponent implements Comparable<LCRule>{

    List<LCCondition> conditions;
    LCAction action;
    private int id;
    private String name;
    private String descr;
    private String onEvent;
    private double priority;
    private String notes;
    private String interventionPointName;

    public LCRule() {
        conditions = new ArrayList<LCCondition>();
    }

    public LCRule(int id, String name, String descr, String onEvent, double priority) {
        this();
        this.id=id;
        this.name=name;
        this.descr=descr;
        this.onEvent=onEvent;
        this.priority=priority;
    }

    public List<LCCondition> getConditions() {
        return conditions;
    }

    public void addCondition(LCCondition c) {
        conditions.add(c);
    }

    /**
     * Test to see if a rule has all conditions true
     * @return true if all the rule conditions are true
     * @throws Exception
     */
    public boolean test () throws Exception {
        try {
            for (LCCondition cond : conditions) {
                cond.setup(smgr, event);
                boolean res = cond.eval();
                if (!res)
                    return false;
            }
            action.setup(smgr, event);
            return true;    // if all conditions are true, return true
        } catch (Exception ee) {
            System.out.println("Failed to evaluate rule: " + this.getName());
            throw ee;
        }
    }

    /**
     *
     */
    public LCAction getAction () {
       return this.action;
    }

    public String getOnEvent() {
        return onEvent;
    }

    public String getName() {
        return name;
    }

    public String getDescr() {
        return descr;
    }

    public double getPriority() {
        return priority;
    }

    public int getId() {
        return id;
    }

    public void setAction(LCAction action) {
        this.action = action;
    }

    public boolean eval () throws Exception {
        boolean fail = false;
        for (LCCondition c : conditions) {
            c.setup(smgr,event);
            if (!c.eval()) {
                fail = true;
                break;
            }
        }
        return !fail;

    }

    @Override
    public int compareTo(LCRule lcRule) {
        if (this.priority < lcRule.getPriority())
            return -1;
        else if (this.priority > lcRule.getPriority())
            return 1;
        else return 0;
    }

    public String getNotes() {
        return notes;
    }


    public String getInterventionPointName() {
        return interventionPointName;
    }


    public void setId(int id) {
        this.id = id;
    }

    public String toString () {
        StringBuilder sb = new StringBuilder(this.getName() + ": IF ");
        for (LCCondition c : this.getConditions())
            sb.append(c.toString()).append(" AND ");
        sb.delete(sb.lastIndexOf("AND"),sb.length());
        sb.append(" THEN " );
        sb.append(this.getAction().getName() + " ("+this.getAction().getMsgText()+")");
        return sb.toString();
    }
}
