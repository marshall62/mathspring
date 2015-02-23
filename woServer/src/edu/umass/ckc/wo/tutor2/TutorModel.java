package edu.umass.ckc.wo.tutor2;

import ckc.servlet.servbase.ActionEvent;
import edu.umass.ckc.wo.event.InternalTutorEvent;
import edu.umass.ckc.wo.event.LoginEvent;
import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.response.Response;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 2/23/15
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class TutorModel implements TutorModelInterface {
    private PedagogicalModel pedModel;
    private LessonModel lessonModel;
    private LearningCompanionModel lcModel;
    private SessionModel sessModel;

    /**
     * Given the specification of a tutor from definition files (pedagogy, learning companion, lesson, session) build the appropriate
     * components into this TutorModel
     */
    public void buildTutorModel () {

    }

    @Override
    public Response processInternalTutorEvent(InternalTutorEvent e) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processUserEvent(SessionEvent e) {
        return null;
    }


}
