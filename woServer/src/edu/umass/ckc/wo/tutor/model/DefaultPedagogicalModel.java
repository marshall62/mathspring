package edu.umass.ckc.wo.tutor.model;

import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.pedModel.ProblemGrader;
import edu.umass.ckc.wo.tutor.probSel.ChallengeModeProblemSelector;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import edu.umass.ckc.wo.tutor.probSel.ReviewModeProblemSelector;
import edu.umass.ckc.wo.tutor.response.InternalEvent;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.*;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 3/13/15
 * Time: 11:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultPedagogicalModel extends PedagogicalModel {

    private static Logger logger = Logger.getLogger(DefaultPedagogicalModel.class);
    protected LessonModel lessonModel;
    //    protected TopicSelector topicSelector;
    ProblemGrader.difficulty nextDiff;
    List<PedagogicalMoveListener> pedagogicalMoveListeners;
    PedagogicalModelParameters params;

    public DefaultPedagogicalModel (SessionManager smgr, Pedagogy pedagogy) throws SQLException {
        pedagogicalMoveListeners = new ArrayList<PedagogicalMoveListener>();
        params = setParams(smgr.getPedagogicalModelParameters(),pedagogy.getParams());
        buildComponents(smgr,pedagogy);
    }

    private void buildComponents (SessionManager smgr, Pedagogy pedagogy) {
        try {

            // Use the params from the pedagogy and then overwrite any values with things that are set up for the class

//            topicSelector = new TopicSelectorImpl(smgr,params, this);
            lessonModel =  new LessonModel(smgr,params,pedagogy,this).buildModel();
            setStudentModel((StudentModel) Class.forName(pedagogy.getStudentModelClass()).getConstructor(SessionManager.class).newInstance(smgr));
            smgr.setStudentModel(getStudentModel());
            setProblemSelector((ProblemSelector) Class.forName(pedagogy.getProblemSelectorClass()).getConstructor(SessionManager.class, LessonModel.class, PedagogicalModelParameters.class).newInstance(smgr, lessonModel, params));
            setReviewModeProblemSelector((ReviewModeProblemSelector) Class.forName(pedagogy.getReviewModeProblemSelectorClass()).getConstructor(SessionManager.class, LessonModel.class, PedagogicalModelParameters.class).newInstance(smgr, lessonModel, params));
            setChallengeModeProblemSelector((ChallengeModeProblemSelector) Class.forName(pedagogy.getChallengeModeProblemSelectorClass()).getConstructor(SessionManager.class, LessonModel.class, PedagogicalModelParameters.class).newInstance(smgr, lessonModel, params));
            setHintSelector((HintSelector) Class.forName( pedagogy.getHintSelectorClass()).getConstructor().newInstance());
            if (pedagogy.getLearningCompanionClass() != null)
                setLearningCompanion((LearningCompanion) Class.forName( pedagogy.getLearningCompanionClass()).getConstructor(SessionManager.class).newInstance(smgr));
            if (pedagogy.getNextProblemInterventionSelector() != null)
                setNextProblemInterventionSelector(buildNextProblemIS(smgr, pedagogy));
            if (pedagogy.getAttemptInterventionSelector() != null)
                setAttemptInterventionSelector(buildAttemptIS(smgr, pedagogy));
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public PedagogicalModelParameters setParams(PedagogicalModelParameters classParams, PedagogicalModelParameters defaultParams) {
        defaultParams.overload(classParams);
        return defaultParams;
    }


    public void setStudentModel(StudentModel studentModel) {
        this.studentModel = studentModel;
    }

    public void setProblemSelector(ProblemSelector problemSelector) {
        this.problemSelector = problemSelector;
    }

    public void setHintSelector(HintSelector hintSelector) {
        this.hintSelector = hintSelector;
    }


    @Override
    public Response processAttempt(AttemptEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processBeginProblemEvent(BeginProblemEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processEndProblemEvent(EndProblemEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processResumeProblemEvent(ResumeProblemEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processHintRequest(HintEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processShowExampleRequest(ShowExampleEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processShowVideoRequest(ShowVideoEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processNextProblemRequest(NextProblemEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processBeginInterventionEvent(BeginInterventionEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processEndInterventionEvent(EndInterventionEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processBeginExampleEvent(BeginExampleEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processEndExampleEvent(EndExampleEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processBeginExternalActivityEvent(BeginExternalActivityEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processEndExternalActivityEvent(EndExternalActivityEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processClickCharacterEvent(ClickCharacterEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processMuteCharacterEvent(MuteCharacterEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processUnMuteCharacterEvent(UnMuteCharacterEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processEliminateCharacterEvent(EliminateCharacterEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processShowCharacterEvent(ShowCharacterEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processReadProblemEvent(ReadProblemEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processContinueAttemptInterventionEvent(ContinueAttemptInterventionEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processInputResponseAttemptInterventionEvent(InputResponseAttemptInterventionEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
