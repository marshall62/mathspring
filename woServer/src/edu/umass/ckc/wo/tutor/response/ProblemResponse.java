package edu.umass.ckc.wo.tutor.response;

import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.content.TopicIntro;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutormeta.Intervention;
import edu.umass.ckc.wo.tutormeta.TopicMastery;
import net.sf.json.JSONObject;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Dec 29, 2008
 * Time: 10:59:25 AM
 */
public class ProblemResponse extends Response {
    public static final ProblemResponse NO_MORE_PROBLEMS = new ProblemResponse(true,false,false);
    public static final ProblemResponse NO_MORE_REVIEW_PROBLEMS = new ProblemResponse(false,true,false);
    public static final ProblemResponse NO_MORE_CHALLENGE_PROBLEMS = new ProblemResponse(false,false,true);
    protected Problem prob;
    private String endPage = null;
    private Intervention intervention = null;
    private String params = null; // Specific bindings for student, if this problem is parametrized

    private boolean noMoreProblems=false;
    private boolean noMoreReviewProblems=false;
    private boolean noMoreChallengeProblems=false;


    public ProblemResponse(Problem p, List<TopicMastery> topicMasteryLevels, int curTopic) {
        this.prob = p;
        this.topicMasteryLevels = topicMasteryLevels;
        this.curTopic=curTopic;
        buildJSON();
    }

    //new constructor to allow for finishing test
    public ProblemResponse(Problem p) {
        this.prob = p;
        buildJSON();
    }

    public ProblemResponse (boolean noMoreProblems, boolean noMoreReviewProblems, boolean noMoreChallengeProblems ) {
        this.noMoreProblems=noMoreProblems;
        this.noMoreReviewProblems=noMoreReviewProblems;
        this.noMoreChallengeProblems=noMoreChallengeProblems;
        buildJSON();
    }

    public ProblemResponse () {
        buildJSON();

    }

    public Intervention getIntervention() {
        return intervention;
    }

    public void setIntervention(Intervention intervention) {
        if (intervention != null)      {
            JSONObject interventionJSON = new JSONObject();
            intervention.buildJSON(interventionJSON);
            jsonObject.element("intervention", interventionJSON.toString());
        }
    }

    public String logEventName() {
        if (prob == null)
            return "NoProblem";
        else if (prob instanceof TopicIntro)
            return Problem.TOPIC_INTRO_PROB_TYPE;
        else if (prob.isPractice())
           return "PracticeProblem";
        else return "ExampleProblem";
    }

    public JSONObject buildJSON() {
        jsonObject = new JSONObject();
        if (prob == null || noMoreProblems || noMoreChallengeProblems || noMoreReviewProblems) {
            String label =  "noMoreProblems";
            if (noMoreChallengeProblems)
                label = "noMoreChallengeProblems";
            else if (noMoreReviewProblems)
                label = "noMoreReviewProblems";
            jsonObject.element("activityType", label);
            jsonObject.element("endPage",endPage);
            // Some interventions are designed to be shown while a problem is being shown
            // These interventions append their JSon to the end of a Problem's JSon

            return jsonObject;
        }

        else {
            prob.buildJSON(jsonObject);  //  problem generates its own json object so we add in character stuff

            return jsonObject;
        }

    }

    public Problem getProblem() {
        return prob;
    }

    // In multiple choice problems, we would like the answer slot to be chosen randomly, so that the answer
    // for a given problem is not always, for instance, c.
    // Not all problems have an e answer, so e is not a candidate for switching.
    // Note that we only have the capability to shuffle answers in HTML5 problems.
    private String chooseAnswerPosition(String oldAnswer) {
        if (!(oldAnswer.equals("a") || oldAnswer.equals("b") || oldAnswer.equals("c") || oldAnswer.equals("d") || oldAnswer.equals("e"))) {
            return "";
        }
        String newAns = "";
        Random randomGenerator = new Random();
        int randomIndex = randomGenerator.nextInt(4);
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
        String oldAnswer = getProblem().getAnswer();
        String newAnswer = chooseAnswerPosition(oldAnswer);
        state.setProblemAnswer(newAnswer);
        jsonObject.element("oldAnswer", oldAnswer);
        jsonObject.element("newAnswer", newAnswer);
    }

    public void setEndPage(String endPage) {
        this.endPage = endPage;
        jsonObject.element("endPage",endPage);

    }

    public String getEndPage() {
        return endPage;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getParams() {
        return params;
    }
}