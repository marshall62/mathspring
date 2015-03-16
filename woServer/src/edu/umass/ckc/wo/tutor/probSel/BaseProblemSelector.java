package edu.umass.ckc.wo.tutor.probSel;

import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.exc.DeveloperException;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.model.LessonModel;
import edu.umass.ckc.wo.tutor.model.TopicModel;
import edu.umass.ckc.wo.tutor.pedModel.ProblemGrader;
import edu.umass.ckc.wo.tutormeta.ProblemSelector;
import edu.umass.ckc.wo.tutormeta.TopicSelector;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 1/30/14
 * Time: 4:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class BaseProblemSelector implements ProblemSelector {

    protected PedagogicalModelParameters parameters;
    protected TopicModel topicModel;
    protected SessionManager smgr;

    public BaseProblemSelector(SessionManager smgr, LessonModel lessonModel, PedagogicalModelParameters params) {
        this.smgr = smgr;
        this.topicModel = (TopicModel) lessonModel;
        this.parameters=params;
    }






    @Override
    /**
     * precondition:  This method is only called if we know the topic has no upcoming content failure and all other conditions for continuing in a topic
     * are met.    In theory,  there should be no fencepost errors based on this.
     */
    public Problem selectProblem(SessionManager smgr, NextProblemEvent e, ProblemGrader.difficulty nextProblemDesiredDifficulty) throws Exception {
        StudentState state = smgr.getStudentState();
        List<Integer> topicProbIds = topicModel.getUnsolvedProblems(state.getCurTopic(),smgr.getClassID(), smgr.isTestUser());
//        List<Problem> topicProblems = xx;
        int lastIx = state.getCurProblemIndexInTopic();
        int nextIx=-1;
        // lastIx is -1 when the topic is new.
        if (lastIx == -1)
            nextIx = (topicProbIds.size()-1) / parameters.getDifficultyRate();

        if (nextIx == -1 && nextProblemDesiredDifficulty == ProblemGrader.difficulty.EASIER) {
            if (lastIx <= 0)
                throw new DeveloperException("Last problem index=0 and want easier problem.   Content failure NOT PREDICTED by TopicSelector");
            nextIx = lastIx / parameters.getDifficultyRate();
        }
        else if (nextIx == -1 && nextProblemDesiredDifficulty == ProblemGrader.difficulty.HARDER) {
            if (lastIx >= topicProbIds.size())
                throw new DeveloperException("Last problem >= number of problems in topic.   Content failure NOT PREDICTED by TopicSelector");
            nextIx = lastIx + ((topicProbIds.size()-1 - lastIx) / parameters.getDifficultyRate());

        }
        else if (nextIx == -1 && nextProblemDesiredDifficulty == ProblemGrader.difficulty.SAME) {
            nextIx = Math.min(lastIx, topicProbIds.size()-1);
        }
        int nextProbId = topicProbIds.get(nextIx);
        state.setCurProblemIndexInTopic(nextIx);
        state.setCurTopicHasEasierProblem(nextIx > 0);
        state.setCurTopicHasHarderProblem(nextIx < topicProbIds.size() - 1);
        Problem p = ProblemMgr.getProblem(nextProbId);
        return p;
    }



    @Override
    public void init(SessionManager smgr) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setParameters(PedagogicalModelParameters params) {
        this.parameters = params;
    }
}
