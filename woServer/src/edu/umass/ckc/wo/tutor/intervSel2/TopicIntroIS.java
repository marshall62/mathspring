package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.content.TopicIntro;
import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.event.tutorhut.ContinueNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.interventions.TopicIntroIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.model.TopicModel;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import edu.umass.ckc.wo.tutor.probSel.TopicModelParameters;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.Intervention;
import org.jdom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 3/20/15
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class TopicIntroIS extends InterventionSelector {
    TopicModel topicModel;
    TopicModelParameters.frequency freq;

    public TopicIntroIS(SessionManager smgr) {
        super(smgr);

    }

    @Override
    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel) {
//        super.init(smgr,pedagogicalModel);
        this.pedagogicalModel=pedagogicalModel;
        topicModel = (TopicModel) pedagogicalModel.getLessonModel();
        configure();
    }

    private void configure () {
        Element config = this.getConfigXML();
        Element freqElt = config.getChild("topicIntroFrequency");
        String freqstr = freqElt.getTextTrim();
        this.freq = PedagogicalModelParameters.convertTopicIntroFrequency(freqstr);

    }

    @Override
    public Intervention selectIntervention(SessionEvent e) throws Exception {
        TopicIntro intro = getTopicIntro(studentState.getCurTopic());
        if (intro != null)  {
            TopicIntroIntervention tii = new TopicIntroIntervention(intro);
            return tii;
        }
        return null;
    }

    protected TopicIntro getTopicIntro (int curTopic) throws Exception {
        // all checking of conditions for display of intro is done in getTopicIntro
        return topicModel.getTopicIntro(curTopic);
    }





}
