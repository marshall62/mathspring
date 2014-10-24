package edu.umass.ckc.wo.event.tutorhut;
import ckc.servlet.servbase.ServletParams;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 12/7/12
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReportErrorEvent extends TutorHutEvent {
    String message;

    public ReportErrorEvent(ServletParams p) throws Exception {
        super(p);
        setMessage(p.getString("message"));
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}