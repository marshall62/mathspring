package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.content.ExternalActivity;
import edu.umass.ckc.wo.db.DbExternalActivity;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.interventions.ExternalActivityAskIntervention;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.response.InterventionResponse;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.Intervention;
import edu.umass.ckc.wo.util.State;
import edu.umass.ckc.wo.util.WoProps;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 11/14/13
 * Time: 12:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExternalActivityIS extends NextProblemInterventionSelector {
    public static final String FORCE = "force";
    public static final String ASK = "ask";
    private double percentTimeToSelectXact = Settings.externalActivityPercentage;
    private String mode =  FORCE;
    private MyState state;



    public ExternalActivityIS(SessionManager smgr) {
        super(smgr);
    }

    @Override
    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel) throws SQLException {
        this.pedagogicalModel=pedagogicalModel;
        this.state = new MyState(smgr);
        configure();
    }

    private void configure() {
        // the percentage of times that it should choose an external act.
        String freqpct = getConfigParameter("frequencyPct");
        if (freqpct != null)
            percentTimeToSelectXact = Double.parseDouble(freqpct);
        String m = getConfigParameter("mode");
        // will be force or ask
        if (m != null)
            mode = m;
    }


    @Override
    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception {
        double r = new Random(System.currentTimeMillis()).nextDouble();
        int curTopicId = studentState.getCurTopic();
        double m = studentModel.getTopicMastery(curTopicId);
        int curProbId = smgr.getStudentState().getCurProblem();

        if (curProbId > 0 &&
                (r < ( percentTimeToSelectXact / 100.0)))  {
            ExternalActivity ea = (ExternalActivity) getExternalActivity(m);
            if (ea != null) {
                if (this.mode.equals(ASK)) {
                    String instr = ea.getInstructions();
                    if (instr == null || instr.equals(""))
                        instr = "Would you like to try a new type of activity?";
                    else
                        instr = "Below are some instructions for a new type of activity.<br><br>" + instr +
                                "<br><br>Would you like to try this activity?";
                    ea.setInstructions(instr);
                }
                ea.setDestinationIS(this.getClass().getName());
                ea.setAskMode(mode);
                state.setExternalActivityId(ea.getId());
            }
            return ea;
        }
        else
            return null;
    }




    public ExternalActivity selectExternalActivity() throws SQLException {
        // using the current topic find an external activity that applies to this topic and has not been
        // shown to this student
        int topicId = smgr.getStudentState().getCurTopic();
        int studId = smgr.getStudentId();
        double topicMastery = smgr.getStudentModel().getTopicMastery(topicId);
        // Get all the external activities about this topic.
        List<ExternalActivity> acts =
                DbExternalActivity.getActivitiesForStudent(smgr.getConnection(), topicId, studId);
        int s = acts.size();
        // The current heuristic for external activity selection is to not repeat one that has been previously
        // been given.   Until we've put meta-information on the activities this is the best we can do.
        List<String> shownIds = smgr.getStudentState().getExternalActivitiesGiven();

        // select one that hasn't been shown to this student
        for (ExternalActivity a: acts) {
            int id = a.getId();
            if (shownIds.size() == 0) {
                a.setTopicId(smgr.getStudentState().getCurTopic());
                return a;
            }
            boolean shown = false;
            for (String xid : shownIds) {
                if (id == Integer.parseInt(xid))   {
                    shown=true;
                    break;
                }
            }
            if (!shown){
                a.setTopicId(smgr.getStudentState().getCurTopic());
                return a;
            }

        }

        return null;
    }


    private NextProblemIntervention getExternalActivity (double topicMastery) throws SQLException {
        ExternalActivity ea= selectExternalActivity();
        if (ea != null)
            studentState.addExternalActivityGiven(ea.getId());
        return ea;

    }

    @Override
    public Intervention processInterventionTimeoutEvent(InterventionTimeoutEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    /**  The external activity instructions have been presented
     *   If the mode is ASK, then we check the user input to see if they answered YES about seeing the ext act.
     *   If the mode is FORCE, we just return it.
     */
    public Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception {
        String input = e.getUserInput();
        if ( (this.mode.equals(ASK) && input != null && input.equalsIgnoreCase("yes")) || this.mode.equals(FORCE) ) {
            int xactId = state.getExternalActivityId();
            ExternalActivity ea = DbExternalActivity.getExternalActivity(smgr.getConnection(),xactId);
            // Sets userInput XML in the intervention so that the logger can put it into the userInput field of the eventlog
            if (input.equalsIgnoreCase("yes"))
                setUserInput(this, "<userResponse><![CDATA[yes]]></userResponse>", e);
            else
                setUserInput(this, "<userResponse><![CDATA[ok]]></userResponse>", e);
            ea.setInstructions(null);  // this will indicate to the client code that it should show the activity rather than instructions.
            return new InterventionResponse(ea); }
        else  {
            setUserInput(this, "<userResponse><![CDATA[no]]></userResponse>", e);
            return null;
        }
    }




    private class MyState extends State {
        private final String XACTID =  ExternalActivityIS.this.getClass().getSimpleName() + ".currentExternalActivityId";

        int externalActivityId; // the id of the xact that we are giving

        MyState (SessionManager smgr) throws SQLException {

            this.conn=smgr.getConnection();
            this.objid = smgr.getStudentId();
            WoProps props = smgr.getStudentProperties();
            Map m = props.getMap();
            externalActivityId =  mapGetPropInt(m, XACTID, -1);
//            if (timeOfLastIntervention ==0)
//                setTimeOfLastIntervention(System.currentTimeMillis());

        }

        private int getExternalActivityId() {
            return externalActivityId;
        }

        private void setExternalActivityId(int xactId) throws SQLException {
            this.externalActivityId = xactId;
            setProp(this.objid,XACTID,xactId);
        }

    }

}
