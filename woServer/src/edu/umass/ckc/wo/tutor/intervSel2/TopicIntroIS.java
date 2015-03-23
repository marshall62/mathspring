package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.content.TopicIntro;
import edu.umass.ckc.wo.event.tutorhut.ContinueNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.model.TopicModel;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutor.response.TopicIntroResponse;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 3/20/15
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class TopicIntroIS extends NextProblemInterventionSelector {
    TopicModel topicModel;

    public TopicIntroIS(SessionManager smgr, PedagogicalModel pedagogicalModel) {
        super(smgr, pedagogicalModel);
        topicModel = (TopicModel) pedagogicalModel.getLessonModel();
    }
    @Override
    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception {
        TopicIntro intro = getTopicIntro(studentState.getCurTopic());
        TopicIntroResponse tir = new TopicIntroResponse(intro);

        return tir;
    }

    protected TopicIntro getTopicIntro (int curTopic) throws Exception {
        // all checking of conditions for display of intro is done in getTopicIntro
        return topicModel.getTopicIntro(curTopic);
    }



    @Override
    public Response processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
