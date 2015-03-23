package edu.umass.ckc.wo.tutor.model;



import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.event.tutorhut.TutorHutEvent;
import edu.umass.ckc.wo.smgr.SessionManager;

import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelector;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorSpec;
import edu.umass.ckc.wo.tutor.pedModel.EndOfTopicInfo;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import edu.umass.ckc.wo.tutor.response.InternalEvent;
import edu.umass.ckc.wo.tutor.response.InterventionResponse;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.Intervention;
import edu.umass.ckc.wo.tutormeta.PedagogicalMoveListener;
import org.jdom.Element;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 2/23/15
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class LessonModel implements TutorEventProcessor {

    protected SessionManager smgr;
    protected PedagogicalModelParameters  pmParams;
    protected Element lessonControlXML;
    protected Pedagogy pedagogy;
    protected PedagogicalMoveListener pedagogicalMoveListener;
    protected PedagogicalModel pedagogicalModel;
    protected StudentState studentState;



    protected InterventionGroup interventionGroup;


    public LessonModel(SessionManager smgr) {
        this.smgr = smgr;
    }

    /**
     * Given the definition in the LessonModel, extract what's relevant to building a Lesson Model so that this can run
     * based on the definition.  This includes rules that say what should happen on BeginLesson, EndLesson, and BeginTopic
     * and EndTopic (handled in the TopicModel subclass of this which relies on this for some inheritance of model behavior)
     * @param smgr
     * @param pmParams
     */
    public LessonModel(SessionManager smgr, PedagogicalModelParameters pmParams,Pedagogy pedagogy, PedagogicalModel pedagogicalModel, PedagogicalMoveListener pedagogicalMoveListener) {
        this.smgr = smgr;
        this.pmParams=pmParams;
        this.pedagogy = pedagogy;
        this.pedagogicalModel = pedagogicalModel;
        this.pedagogicalMoveListener = pedagogicalMoveListener;
        this.studentState = smgr.getStudentState();
    }


    /**
     * Called by the PedagogicalModel to build this model (or the TopicModel subclass) from the parameters and the lesson control element.
     * @return
     * @throws SQLException
     */
    public LessonModel buildModel() throws SQLException {
        LessonModel lm;
        if (this.pmParams.isTopicLessonStyle())
            lm= new TopicModel(this.smgr,this.pmParams,this.pedagogy, this.pedagogicalModel,this.pedagogicalMoveListener);
        else lm= new LessonModel(this.smgr,this.pmParams,this.pedagogy,this.pedagogicalModel,this.pedagogicalMoveListener);
        lm.readLessonControl(this.pedagogy.getLessonControlElement());
        return lm;
    }

    /*     Take apart XML like:
        <lessonControl>
            <interventions>
                <interventionSelector onEvent="EndOfTopic" weight="1" class="TopicSwitchAskIS">
                    <config>
                        <ask val="false"></ask>
                    </config>
                </interventionSelector>
                .
                .
            </interventions>
        </lessonControl>
     */
    protected void readLessonControl(Element lessonControlElement) {
        List<Element> intervSels = lessonControlElement.getChild("interventions").getChildren("interventionSelector");
        this.interventionGroup = new InterventionGroup();
        for (Element intervSel : intervSels) {
            InterventionSelectorSpec spec = new InterventionSelectorSpec(intervSel);
            this.interventionGroup.add(spec);
        }

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
        List<InterventionSelectorSpec> candidates;

        // select interventions that apply to the onEvent (e.g. EndOfTopic)
        candidates= getCandidateInterventionForEvent(e, e.getOnEventName());  // get back a list of all the interventions with onEvent == EndOfTopic
        return selectBestCandidate(candidates, e); // overrides methods take care of the selection


    }

    public boolean hasReadyContent(int lessonId) throws Exception {
        return true;
    }

    // TODO this method needs renaming and a new signature to make sense for lessons but it is a start in
    // the  right direction for getting this test out of the BasePedMod
    public EndOfTopicInfo isEndOfTopic(long probElapsedTime, TopicModel.difficulty difficulty) throws Exception {
        return null;
    }

    // Go through the intervention selectors in priority order and take the first one that returns an intervention.  This
    // means each selector is responsible for shutting itself off after it runs.
    protected Response selectBestCandidate(List<InterventionSelectorSpec> candidates, InternalEvent e) throws Exception {
        Collections.sort(candidates);   // sort into ascending order by weight
        for (InterventionSelectorSpec spec: candidates) {
            String className = spec.getFullyQualifiedClassname();
            spec.setClassName(className);
            InterventionSelector isel = spec.buildIS(smgr);
            // will check to see if the selector wants to run an intervention
            Intervention interv= isel.selectIntervention(e.getSessionEvent());
            if (interv != null)
                return buildInterventionResponse(interv);
        }
        return null;
    }

    protected Response buildInterventionResponse (Intervention interv) throws Exception {
        return new InterventionResponse(interv);
    }




    private List<InterventionSelectorSpec> getCandidateInterventionForEvent (InternalEvent e, String onEvent) {
        List<InterventionSelectorSpec> specs = new ArrayList<InterventionSelectorSpec>();
        for (InterventionSelectorSpec i: this.interventionGroup.getInterventionsSpecs()) {
            if (i.getOnEvent().equals(onEvent))
                specs.add(i);
        }
        return specs;
    }
}
