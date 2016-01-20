package edu.umass.ckc.wo.tutor.probSel;

import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.exc.DeveloperException;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.model.LessonModel;
import edu.umass.ckc.wo.tutor.model.TopicModel;
import edu.umass.ckc.wo.tutor.pedModel.ProblemScore;
import edu.umass.ckc.wo.tutormeta.ProblemSelector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    protected LessonModelParameters lessonModelParameters;
    protected SessionManager smgr;

    public BaseProblemSelector(SessionManager smgr, LessonModel lessonModel, PedagogicalModelParameters params) {
        this.smgr = smgr;
        this.topicModel = (TopicModel) lessonModel;
        this.parameters=params;
        this.lessonModelParameters = ((TopicModel) lessonModel).getTmParams(); // topics assumed when using this selector
    }




    @Override
    /**
     * precondition:  This method is only called if we know the topic has no upcoming content failure and all other conditions for continuing in a topic
     * are met.    In theory,  there should be no fencepost errors based on this.
     */
    public Problem selectProblem(SessionManager smgr, NextProblemEvent e, ProblemScore lastProblemScore) throws Exception {
        if (topicModel.isInInterleavedTopic()) {
            return selectInterleavedProblem(smgr.getConnection(),smgr.getStudentId());
        }
        TopicModel.difficulty nextDiff = topicModel.getNextProblemDifficulty(lastProblemScore);
        StudentState state = smgr.getStudentState();
        // Gets problems with testable problems included if the user is marked to receive testable stuff.
        List<Integer> topicProbIds = topicModel.getUnsolvedProblems(state.getCurTopic(),smgr.getClassID(), DbUser.isShowTestControls(smgr.getConnection(), smgr.getStudentId()));
//        List<Problem> topicProblems = xx;
        int lastIx = state.getCurProblemIndexInTopic();
        int nextIx=-1;
        // lastIx is -1 when the topic is new.
        if (lastIx == -1)
            nextIx = (int) Math.round((topicProbIds.size()-1) / parameters.getDifficultyRate());

        if (nextIx == -1 && nextDiff == TopicModel.difficulty.EASIER) {
            if (lastIx <= 0)
                throw new DeveloperException("Last problem index=0 and want easier problem.   Content failure NOT PREDICTED by TopicSelector");
            nextIx =(int) Math.round(lastIx / parameters.getDifficultyRate());
        }
        else if (nextIx == -1 && nextDiff == TopicModel.difficulty.HARDER) {
            if (lastIx >= topicProbIds.size())
                throw new DeveloperException("Last problem >= number of problems in topic.   Content failure NOT PREDICTED by TopicSelector");
            nextIx = lastIx + ((int) Math.round((topicProbIds.size()-1 - lastIx) / parameters.getDifficultyRate()));

        }
        else if (nextIx == -1 && nextDiff == TopicModel.difficulty.SAME) {
            nextIx = Math.min(lastIx, topicProbIds.size()-1);
        }
        int nextProbId = topicProbIds.get( nextIx);
        state.setCurProblemIndexInTopic( nextIx);
        state.setCurTopicHasEasierProblem(nextIx > 0);
        state.setCurTopicHasHarderProblem(nextIx < topicProbIds.size() - 1);
        Problem p = ProblemMgr.getProblem(nextProbId);
        return p;
    }

    public static boolean hasInterleavedProblem (Connection conn, int studId) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select count(*) from interleavedProblems where studid=? and shown=0";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,studId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int c= rs.getInt(1);
                return c > 0;
            }
            return false;
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }

    public static Problem selectInterleavedProblem(Connection conn, int studId) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select probId from interleavedProblems where studid=? and shown=0 order by position";
            stmt = conn.prepareStatement(q,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1,studId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int c= rs.getInt(1);
                rs.updateInt("shown",1);
                rs.updateRow();
                return ProblemMgr.getProblem(c);
            }
            else return null;
        }  finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }


    @Override
    public void setParameters(PedagogicalModelParameters params) {
        this.parameters = params;
    }
}
