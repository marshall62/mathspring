package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.event.tutorhut.ContinueNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.interventions.DemoProblemIntervention;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.model.TopicModel;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.Intervention;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 3/20/15
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class DemoProblemIS extends InterventionSelector {
    TopicModel topicModel;

    public DemoProblemIS(SessionManager smgr) {
        super(smgr);

    }

    @Override
    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel) {
        this.pedagogicalModel=pedagogicalModel;
        topicModel = (TopicModel) pedagogicalModel.getLessonModel();
    }

    @Override
    public Intervention selectIntervention(SessionEvent e) throws Exception {
        Problem demo = getDemoProblem(studentState.getCurTopic());
        if (demo != null) {
            DemoProblemIntervention dpi = new DemoProblemIntervention(demo);
            return dpi;
        }
        return null;
    }

    protected Problem getDemoProblem (int curTopic) throws Exception {
        // all checking of conditions for display of intro is done in getTopicIntro
        return topicModel.getTopicExample(curTopic);
    }






}
