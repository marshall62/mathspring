package edu.umass.ckc.wo.tutor.pedModel;

import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.exc.DeveloperException;
import edu.umass.ckc.wo.smgr.StudentState;
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

    private static Logger logger = Logger.getLogger(ProblemGrader.class);


    public enum difficulty {
        EASIER,
        SAME,
        HARDER
    }


    public ProblemScore gradePerformance(Connection conn, Problem lastProb, StudentState state) throws Exception {
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
    
    public difficulty getNextProblemDifficulty (ProblemScore score) {
            if (!score.isCorrect())
                return difficulty.EASIER;
            if (score.getMistakes() > score.getAvgMistakes()) {
                    logger.debug("Decreasing difficulty level... ");
                    return difficulty.EASIER;
            }
            else { // mistakes are LOW
                if (score.getHints() > score.getAvgHints()) {
                    if (score.getSolveTimeSecs() > score.getAvgSolveTime()) {
//                        logger.debug("Student is carefully seeing help and spending time, maintain level of difficulty (m<E_m, h>E_h, t>E_t)") ;
                        logger.debug("Maintaining same difficulty level... ");
                        return difficulty.SAME;

                    } else {
//                        logger.debug("Rushing through hints to get to the correct answer, decrease level of difficulty (m<E_m, h>E_h, t<E_t)") ;
                        logger.debug("Decreasing difficulty level... ");
                        return difficulty.EASIER;

                    }
                } else {
                    if (score.getSolveTimeSecs() > score.getAvgSolveTime()) {
//                        logger.debug("Correctly working on problem with effort but without help (m<E_m, h<E_h, t>E_t)") ;
//                        logger.debug("50% of the time increase level of difficulty. 50% of the time, maintain level of difficulty") ;
                        if (Math.random() > 0.5) {
                            logger.debug("Increasing difficulty level... ");
                            return difficulty.HARDER;
                        } else {
                            logger.debug("Maintaining same difficulty level... ");
                            return difficulty.SAME;
                        }
                    } else
                        ; // logger.debug("Correctly working the problem with no effort and few hints (m<E_m, h<E_h, t<E_t)") ;
                    logger.debug("Increasing the difficulty level...");
                    return difficulty.HARDER;
                }
            }

    }


}
