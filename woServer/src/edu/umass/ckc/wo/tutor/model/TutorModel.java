package edu.umass.ckc.wo.tutor.model;

import edu.umass.ckc.wo.event.tutorhut.TutorHutEvent;
import edu.umass.ckc.wo.tutor.response.BeginningOfTopicEvent;
import edu.umass.ckc.wo.tutor.response.InternalEvent;
import edu.umass.ckc.wo.tutor.response.Response;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 3/16/15
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class TutorModel extends TutorEventProcessor {
    private LessonModel lessonModel;



    @Override
    public Response processUserEvent(TutorHutEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processInternalEvent(InternalEvent e) throws Exception {
        if (e instanceof BeginningOfTopicEvent)
            return ((TopicModel) lessonModel).processInternalEvent(e);
        else return new Response();

    }

    public LessonModel getLessonModel() {
        return lessonModel;
    }

    public void setLessonModel(LessonModel lessonModel) {
        this.lessonModel = lessonModel;
    }
}
