package edu.umass.ckc.wo.lc;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 3/10/16
 * Time: 10:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class LCAction extends LCRuleComponent {
    private int id;
    private String msgText;
    private String name;

    public LCAction(int id, String msgText, String name) {
        this.id = id;
        this.msgText = msgText;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getMsgText() {
        return msgText;
    }

    public String getName() {
        return name;
    }

}
