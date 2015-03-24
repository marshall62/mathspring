package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.content.ExternalActivity;
import edu.umass.ckc.wo.db.DbExternalActivity;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.response.Response;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 11/14/13
 * Time: 12:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExternalActivityIS extends NextProblemInterventionSelector {
    private double PERCENT_TIME_TO_SELECT_EXTERNAL_ACT = Settings.externalActivityPercentage;

    public ExternalActivityIS(SessionManager smgr) {
        super(smgr);
    }

    @Override
    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel) {
        this.pedagogicalModel=pedagogicalModel;
    }


    @Override
    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception {
        double r = new Random(System.currentTimeMillis()).nextDouble();
        int curTopicId = studentState.getCurTopic();
        double m = studentModel.getTopicMastery(curTopicId);
        int curProbId = smgr.getStudentState().getCurProblem();
        // a probId in the event means that the client is trying to force the server to pick a certain problem or external activity
        // If isExternal is true, then return that given external activity.   If it is false, then return null, so the problem selector can return the requested problem.
        if (e.getProbId() != null && !e.isExternal())
            return null;
        else if (e.getProbId() != null && e.isExternal())
            return DbExternalActivity.getExternalActivity(conn, Integer.parseInt(e.getProbId()));
        if (e instanceof NextProblemEvent &&
                ((NextProblemEvent) e).isIntervene())
            return getExternalActivity(m);
            // randomly get an external activity 10% of the time.
        else if (curProbId > 0 &&
                (r < ( PERCENT_TIME_TO_SELECT_EXTERNAL_ACT / 100.0)))  {
            return getExternalActivity(m);
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
            for (String xid : shownIds) {
                if (id != Integer.parseInt(xid))    {
                    a.setTopicId(smgr.getStudentState().getCurTopic());
                    return  a;
                }
            }

        }

        return null;
    }


    private NextProblemIntervention getExternalActivity (double topicMastery) throws SQLException {
        return selectExternalActivity();

    }

    @Override
    public Response processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
