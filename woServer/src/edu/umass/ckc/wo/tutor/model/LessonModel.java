package edu.umass.ckc.wo.tutor.model;



import edu.umass.ckc.wo.config.LessonXML;
import edu.umass.ckc.wo.event.tutorhut.TutorHutEvent;
import edu.umass.ckc.wo.smgr.SessionManager;

import edu.umass.ckc.wo.state.StudentState;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.pedModel.EndOfTopicInfo;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.probSel.LessonModelParameters;
import edu.umass.ckc.wo.tutor.probSel.TopicModelParameters;
import edu.umass.ckc.wo.tutor.response.InternalEvent;
import edu.umass.ckc.wo.tutor.response.InterventionResponse;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.Intervention;
import edu.umass.ckc.wo.tutormeta.PedagogicalMoveListener;
import edu.umass.ckc.wo.tutormeta.StudentModel;
import org.jdom.Element;

import java.sql.SQLException;


/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 2/23/15
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class LessonModel implements TutorEventProcessor {

    protected SessionManager smgr;
    protected LessonModelParameters  lmParams;
    protected Element lessonControlXML;
    protected Pedagogy pedagogy;
    protected PedagogicalMoveListener pedagogicalMoveListener;
    protected PedagogicalModel pedagogicalModel;
    protected StudentState studentState;
    protected StudentModel studentModel;
    protected InterventionGroup interventionGroup;


    public LessonModel(SessionManager smgr) {
        this.smgr = smgr;
    }

    /**
     * Given the definition in the LessonModel, extract what's relevant to building a Lesson Model so that this can run
     * based on the definition.  This includes rules that say what should happen on BeginLesson, EndLesson, and BeginTopic
     * and EndTopic (handled in the TopicModel subclass of this which relies on this for some inheritance of model behavior)
     * @param smgr
     * @param lmParams
     */
    public void init (SessionManager smgr, LessonModelParameters lmParams,Pedagogy pedagogy,
                      PedagogicalModel pedagogicalModel, PedagogicalMoveListener pedagogicalMoveListener) throws Exception {
        this.smgr = smgr;
        this.lmParams=lmParams;
        this.pedagogy = pedagogy;
        this.pedagogicalModel = pedagogicalModel;
        this.pedagogicalMoveListener = pedagogicalMoveListener;
        this.studentState = smgr.getStudentState();
        this.studentModel= smgr.getStudentModel();
        LessonXML x = pedagogy.getLessonXML();
        interventionGroup = new InterventionGroup(x.getInterventions());
        interventionGroup.buildInterventions(smgr,pedagogicalModel);
    }


    /**
     * Called by the PedagogicalModel to build this model (or the TopicModel subclass) from the parameters and the lesson control element.
     * @return
     * @throws SQLException
     */
    public static LessonModel buildModel(SessionManager smgr, LessonModelParameters params) throws SQLException {
        LessonModel lm;
        if (params instanceof TopicModelParameters)
            lm= new TopicModel(smgr);
        else lm= new LessonModel(smgr);

        return lm;
    }




    @Override
    public Response processUserEvent(TutorHutEvent e) throws Exception {
        return null;
    }

    @Override
    /**
     * Gets the interventions that are defined for this model and finds ones that apply to the situation using
     * the onEventName tag of the intervention.   It selects the best candidate using a default algorithm that
     * takes uses the weight of each intervention to put it in order.
     */
    public Response processInternalEvent(InternalEvent e) throws Exception {
        Response r;
        Intervention interv = interventionGroup.selectIntervention(smgr,e.getSessionEvent(),e.getOnEventName());
        return buildInterventionResponse(interv);
    }

    public boolean hasReadyContent(int lessonId) throws Exception {
        return true;
    }

    // TODO this method needs renaming and a new signature to make sense for lessons but it is a start in
    // the  right direction for getting this test out of the BasePedMod
    public EndOfTopicInfo isEndOfTopic(long probElapsedTime, TopicModel.difficulty difficulty) throws Exception {
        return null;
    }



//    protected InterventionSelectorSpec getInterventionSelectorSpec(String lastInterventionClass) {
//        for (InterventionSelectorSpec s : this.interventionGroup.getInterventionsSpecs()) {
//            if (s.getFullyQualifiedClassname().equals(lastInterventionClass))
//                return s;
//        }
//        return null;
//    }

    protected Response buildInterventionResponse (Intervention interv) throws Exception {
        if (interv == null)
            return null;
        else return new InterventionResponse(interv);
    }

    public PedagogicalMoveListener getPedagogicalMoveListener() {
        return pedagogicalMoveListener;
    }


}
