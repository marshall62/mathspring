package edu.umass.ckc.wo.tutor.intervSel2;

import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.content.Hint;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.content.TopicIntro;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.Intervention;
import edu.umass.ckc.wo.tutormeta.PedagogicalMoveListener;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 11/14/13
 * Time: 12:31 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class NextProblemInterventionSelector extends InterventionSelector implements PedagogicalMoveListener {

    protected List<NextProblemInterventionSelector> subSelectorList;
    private boolean buildProblem=false;

    public NextProblemInterventionSelector(SessionManager smgr) {
        super(smgr);
    }

    public void setSubSelectors (List<NextProblemInterventionSelector> subSelectors) {
        this.subSelectorList = subSelectors;
    }

    public List<NextProblemInterventionSelector> getSubSelectorList() {
        return subSelectorList;
    }

    /**
     * Subclasses that select interventions at the time of nextProblem event must override this
     *
     * @param e
     * @return
     * @throws Exception
     *
     */
    public abstract NextProblemIntervention selectIntervention(NextProblemEvent e) throws Exception;

    public abstract Response processContinueNextProblemInterventionEvent(ContinueNextProblemInterventionEvent e) throws Exception;
    public abstract Response processInputResponseNextProblemInterventionEvent(InputResponseNextProblemInterventionEvent e) throws Exception;


    @Override
    public void problemGiven(Problem p) throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exampleGiven(Problem ex) throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void lessonIntroGiven(TopicIntro intro) throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void attemptGraded(boolean isCorrect) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void hintGiven( Hint hint) throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }



    @Override
    public void interventionGiven(Intervention intervention) throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void newTopic(Topic t) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void newSession(int sessId) throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isBuildProblem() {
        return buildProblem;
    }

    public void setBuildProblem(boolean buildProblem) {
        this.buildProblem = buildProblem;
    }

    public String getUserInputXML() throws Exception {
        if (subSelectorList == null)
            return this.userInputXML;
        else {
            for (NextProblemInterventionSelector sel: this.subSelectorList) {
                String x = sel.getUserInputXML();
                if (x != null)
                    return x;

            }
            return null;
        }

    }
}
