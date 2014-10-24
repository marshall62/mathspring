package edu.umass.ckc.wo.tutor.studmod;

import edu.umass.ckc.wo.smgr.StudentState;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Dec 19, 2011
 * Time: 5:18:04 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MasteryHeuristic {
    double computeTopicMastery(long timeToFirstAttempt, double topicMastery, int probID, int topicID,
                               int numHints, boolean isCorrect, int numMistakes, int numProbsInTopic, String problemMode) throws SQLException;


}
