package edu.umass.ckc.wo.tutor.model;

import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.event.tutorhut.TutorHutEvent;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.response.*;
import edu.umass.ckc.wo.tutormeta.Intervention;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 3/16/15
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class TutorModel implements TutorEventProcessor {
    private LessonModel lessonModel;
    private SessionManager smgr;

    public TutorModel(SessionManager smgr) {
        this.smgr = smgr;
    }

    @Override
    public Response processUserEvent(TutorHutEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processInternalEvent(InternalEvent e) throws Exception {
        Response r;
        StudentState state = smgr.getStudentState();
        int lastProbId =  state.getCurProblem();  // must do this before processing the event because it might clear curProb
        if (e instanceof BeginningOfTopicEvent)
            r = ((TopicModel) lessonModel).processInternalEvent(e);
        else r=  new Response();
        if (r instanceof ProblemResponse) {
            ProblemResponse pr = (ProblemResponse) r;
            Problem p = pr.getProblem();
            smgr.getStudentModel().newProblem(state,p);  // this does not set curProb = new prob id,
            smgr.getStudentState().setCurProblem(lastProbId);  // must make curProb be lastProb id so EndProblem event that comes in next has the id of last problem
            smgr.getStudentModel().save();
        }
        else if (r instanceof InterventionResponse) {
            Intervention i = ((InterventionResponse) r).getIntervention();
            smgr.getStudentModel().interventionGiven(state,i);
            smgr.getStudentModel().save();
        }
        return r;

    }




    public LessonModel getLessonModel() {
        return lessonModel;
    }

    public void setLessonModel(LessonModel lessonModel) {
        this.lessonModel = lessonModel;
    }
}
