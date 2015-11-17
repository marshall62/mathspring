package edu.umass.ckc.wo.content;

import edu.umass.ckc.wo.db.DbStudentProblemHistory;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 11/11/15
 * Time: 4:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProblemBinding {

    private Problem problem;
    private Binding binding;
    private String oldAnswer;
    private String newAnswer;

    public ProblemBinding(Problem problem) {
        this.problem = problem;
    }

    // In multiple choice problems, we would like the answer slot to be chosen randomly, so that the answer
    // for a given problem is not always, for instance, c.
    // Not all problems have an e answer, so e is not a candidate for switching.
    // Note that we only have the capability to shuffle answers in HTML5 problems.
    private String chooseAnswerPosition(String oldAnswer) {
        if (!(oldAnswer.equals("a") || oldAnswer.equals("b") || oldAnswer.equals("c") || oldAnswer.equals("d") || oldAnswer.equals("e"))) {
            return null;
        }
        String newAns = null;
        Random randomGenerator = new Random();
        int randomIndex = randomGenerator.nextInt(problem.getAnswers().size());
        switch (randomIndex) {
            case(0):
                newAns = "a";
                break;
            case(1):
                newAns = "b";
                break;
            case(2):
                newAns = "c";
                break;
            case(3):
                newAns = "d";
                break;
        }
        return newAns;
    }



    public void shuffleAnswers(StudentState state) throws SQLException {
        oldAnswer = problem.getAnswer();
        newAnswer = chooseAnswerPosition( oldAnswer);
        // if we want no shuffling better make sure that oldAnswer gets put in the state (as below with newAnswer)
        if (newAnswer != null) {
            state.setProblemAnswer(newAnswer);
        }
    }

    public void setBindings(SessionManager smgr) throws SQLException {

        if (problem != null && problem.isParametrized()) {
            binding = problem.getParams().addBindings2(problem, smgr.getStudentId(), smgr.getConnection(), smgr.getStudentState());
            if (problem.isMultiChoice())
                this.shuffleAnswers(smgr.getStudentState());
            // parameterized short answer problems need to save the possible answers in the student state

        }
    }


    public JSONObject getJSON() {
        if (binding != null) {
            JSONObject jo = new JSONObject();
            binding.getJSON(jo);
            return jo;
        }
        else return null;
    }
}
