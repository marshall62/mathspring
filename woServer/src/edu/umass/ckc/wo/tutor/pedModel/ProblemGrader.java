package edu.umass.ckc.wo.tutor.pedModel;

import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.exc.DeveloperException;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.model.TopicModel;
import org.apache.log4j.Logger;

import java.sql.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 1/30/14
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProblemGrader {
    private SessionManager smgr;

    private static Logger logger = Logger.getLogger(ProblemGrader.class);


    public ProblemGrader(SessionManager smgr) {
        this.smgr = smgr;
    }

    public ProblemScore gradePerformance(Problem lastProb) throws Exception {
        Connection conn = smgr.getConnection();
        StudentState state = smgr.getStudentState();
        double time = state.getTimeToSolve() / 1000.0;  // convert from ms to seconds
        int mistakes = state.getNumMistakesOnCurProblem();
        int hints = state.getNumHintsBeforeCorrect();
        boolean isCorrect = state.isProblemSolved();
        ProblemScore score = new ProblemScore();
        score.setMistakes(mistakes);
        score.setHints(hints);
        score.setCorrect(isCorrect);
        score.setSolveTimeSecs(time);
        if (lastProb == null)
            throw new DeveloperException("Last problem is null in ProblemGrader.gradePerformance");
        float[] expected = DbProblem.getExpectedBehavior(conn, lastProb.getId());
        if (expected != null) {
            float expMistakes = expected[1];
            float expHints = expected[0];
            float expTime = expected[2];
            score.setAvgMistakes(expMistakes);
            score.setAvgHints(expHints);
            score.setAvgSolveTime(expTime); 
        }

        return score;
    }
    



}
