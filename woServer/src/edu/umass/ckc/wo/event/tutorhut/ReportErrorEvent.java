package edu.umass.ckc.wo.event.tutorhut;
import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.content.Problem;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 12/7/12
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReportErrorEvent extends TutorHutEvent {
    public static final String PROB_ELAPSED_TIME = "probElapsedTime";
    private String message;
    private String probId;
    private long probElapsedTime;
    private String mode;  // practice, challenge, review
    private boolean isProbBroken=true;


    public ReportErrorEvent(ServletParams p) throws Exception {
        super(p);
        this.probId = p.getString("probId", null);
        this.probElapsedTime = p.getLong(PROB_ELAPSED_TIME, -1);
        this.isProbBroken = p.getBoolean("isBroken",true);
        this.mode = p.getString("mode", Problem.PRACTICE);
        setMessage(p.getString("message"));
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProbId() {
        return probId;
    }

    public long getProbElapsedTime() {
        return probElapsedTime;
    }

    public String getMode() {
        return mode;
    }

    public boolean isProbBroken() {
        return isProbBroken;
    }
}