package edu.umass.ckc.wo.tutor.intervSel2;

import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.event.tutorhut.ContinueNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.InputResponseNextProblemInterventionEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.interventions.TopicSwitchAskIntervention;
import edu.umass.ckc.wo.interventions.TopicSwitchIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.pedModel.EndOfTopicInfo;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutormeta.Intervention;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 12/2/13
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class TopicSwitchAskIS extends NextProblemInterventionSelector {

    private static Logger logger = Logger.getLogger(TopicSwitchAskIS.class);

    public TopicSwitchAskIS(SessionManager smgr, PedagogicalModel pedagogicalModel) {
        super(smgr, pedagogicalModel);
    }

    @Override
    public void init(SessionManager smgr, PedagogicalModel pedagogicalModel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    /**
     * We will pop up a dialog asking student if they want to move to new topic of stay in current one only
     * if the reasons for leaving are maxTime or maxProblems.   This is because we can reset counters/timers so that
     * student can stay in topic longer.   Other reasons for leaving topic (e.g. mastery, content failures) we cannot
     * do anything about so student is just given a message that we are moving to next topic.
     */
    public NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception {
        if (smgr.getStudentState().getCurTopic() < 1) {
            return null;
        }
        boolean topicContinues = pedagogicalModel.isLessonContentAvailable(smgr.getStudentState().getCurTopic()) ;
        NextProblemIntervention intervention = null;
        if (!topicContinues) {
            EndOfTopicInfo reasons = pedagogicalModel.getReasonsForEndOfTopic();
            if (reasons.isTopicDone()) {
                String expl = reasons.getExplanation();
                String ask = this.getParameter("ask",this.getParams());
                boolean isAsk=false;
                if (ask != null)
                    isAsk = Boolean.parseBoolean(ask);
                // If configured to ask about staying in the topic and not a content failure, then pop up dialog asking if stay or switch topics.
                // Can only stay in the current topic if we have more content (i.e. maxProbs = true or maxTime has been reached)
                if (isAsk && !reasons.isContentFailure())
                    intervention = new TopicSwitchAskIntervention(expl,smgr.getSessionNum());
                // just inform that we are moving to next topic
                else intervention = new TopicSwitchIntervention(expl);
            }
            rememberInterventionSelector(this);
            return intervention;
        }
        else return null;
    }


    @Override
    public Intervention processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // inherits selectIntervention which does a rememberIntervention.   I think this should remember the name of this class
    // and not the super class but maybe not.

    @Override
    /**
     * We've told the user that we want to switch topics and given him the option of staying in the current topic or moving to the
     * next one.   The parameters will be inside the event and will need to be retrieved.  Value of wantSwitch will either be
     *
     */
    public Intervention processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception {
        ServletParams params = e.getServletParams();
        String wantSwitch = params.getString(TopicSwitchAskIntervention.WANT_TO_SWITCH);
        // if they want to stay in the topic for either reason,  then we reset these counters/timers so they
        // can be in until content failure or mastery
        if (wantSwitch != null && wantSwitch.equals(TopicSwitchAskIntervention.STAY)) {
            smgr.getStudentState().setTimeInTopic(0);
            smgr.getStudentState().setTopicNumProbsSeen(1);  // set to one so that it won't play an example
            logger.debug("Topic Switch: Student elects to STAY in topic.  Turning off topicSwitch flag");
            smgr.getStudentState().setTopicSwitch(false);
            setUserInput(this,"<topicSwitch wantSwitch=\"" + wantSwitch + "\"/>",e);
        }
        else  logger.debug("Topic Switch: Student elects to SWITCH to new topic.");
        return null;  // no more interventions to return.

    }

}
