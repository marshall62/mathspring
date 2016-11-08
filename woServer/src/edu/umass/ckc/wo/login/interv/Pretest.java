package edu.umass.ckc.wo.login.interv;

import ckc.servlet.servbase.ServletParams;
import edu.umass.ckc.wo.content.PrePostProblemDefn;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.db.DbPrePost;
import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.login.LoginSequence;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.pedModel.PedagogicalModel;
import edu.umass.ckc.wo.tutormeta.Intervention;
import org.jdom.Element;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 4/14/15
 * Time: 3:38 PM
 * This will show a pretest if there is one and the student needs to complete it.   Here are the guidelines
 *
 * It is marked as a run-once-per-session intervention so it will get checked on every login.
 * A student is only considered to have completed the test if they have answered ALL the questions (TODO make this some kind
 * of defineable behavior because I can guarantee that people will change their minds about this)
 * If all problems have not been completed we use the number completed as an index into the next problem to show.
 *
 * So the only way we can have a student or class have a repeat of a pretest is to blow away their entries in the
 * preposttestdata table.
 *
 * To make sure that a class receives a pretest:
 *   1.  Look up the pedagogy that the class uses and check the name of the loginBehavior associated with that pedagogy
 *   2.   Look at the loginBehavior table associated with this and edit the XML that defines the interventions for that login behavior
 *   3.   One of the interventions must be defined similar to this (where the Pretest is included along with optional config)
 *
 *   <login name="SurveyLogin">
                <controlParameters></controlParameters>

            <interventions>
                 <interventionSelector onEvent="Login" weight="2" runFreq="once" class="edu.umass.ckc.wo.login.interv.StudentName">
 <config>
 </config>
 </interventionSelector>

 <interventionSelector onEvent="Login" weight="1" runFreq="oncepersession" class="edu.umass.ckc.wo.login.interv.Pretest">
 <config>
 <terminationTest>completeAllProblems</terminationTest>
 <startMessage><![CDATA[This message is given at the beginning of the test with question 1.]]></![CDATA[This></startMessage>
 </config>
 </interventionSelector>


 </interventions>
 </login>

      4.   Make sure the classConfig lists a pretest ID in its pretest field
      5.  Make sure that pretest exists in the preposttest table
      6.  Define prepostproblems in that table and associate them with the preposttest thru the prepostproblemtestmap table which
           puts them in sequence using its position field (1-based)
      7.  Make sure images are placed in resources/images/pretest/nameOftest/  dir and that these are correctly placed as the
          url of a problem with something like /images/pretest/nameOfTest/problem1.jpg
      8.   Make sure the pretest data has been cleared for students from the preposttestdata table.
 */
public class Pretest extends LoginInterventionSelector {
    private static final String ANSWER = "answer";
    private static final String PROBID = "probId";
    private static final String QUESTION = "question";
    private static final String JSP = "pretestQuestion.jsp";
    public static final String TERMINATION_TEST = "terminationTest";
    public static final String COMPLETE_ALL_PROBLEMS = "completeAllProblems";
    public static final String MESSAGE = "message";
    public static final String START_MESSAGE = "startMessage";
    public static final String ELAPSED_TIME = "elapsedTime";
    public static final String NUM_PROBS_IN_TEST = "numProbsInTest";
    public static final String NUM_PROBS_COMPLETED = "numProbsCompleted";

    protected int testId;
    protected int classId;
    protected int numProbsInTest;
    protected int numTestProbsCompleted;
    private String startMessage;
    protected String testType;
    private String terminationPredicate;


    public Pretest(SessionManager smgr) throws SQLException {
        super(smgr);
        classId = smgr.getClassID();
        testType = DbPrePost.PRETEST;
    }

    protected void setProperties (String testType) throws SQLException {
        this.testId = DbClass.getClassPrePostTest(smgr.getConnection(),classId,testType);
        this.numProbsInTest = DbPrePost.getPrePostTestNumProblems(smgr.getConnection(),this.testId);
        this.numTestProbsCompleted = DbPrePost.getStudentCompletedNumProblems(smgr.getConnection(),this.testId, smgr.getStudentId(),testType);

    }

    public void init (SessionManager smgr, PedagogicalModel pm) throws Exception {
        setProperties(testType);
        if (configXML != null) {
            Element eotElt = configXML.getChild(TERMINATION_TEST);
            if (eotElt != null)
                terminationPredicate = eotElt.getTextTrim();
            else
                terminationPredicate = COMPLETE_ALL_PROBLEMS;
            Element startMsgElt = configXML.getChild(START_MESSAGE);
            if (startMsgElt != null)
                startMessage = startMsgElt.getTextTrim();
        }
        else terminationPredicate = COMPLETE_ALL_PROBLEMS;
    }



    // The config XML defines how the pretest termination is tested.   For now we have only defined one predicate which
    // completeAllProblems (this is in the loginBehavior table inside the XML that defines the Pretest login intervention)
    // as <config> <terminationTest>completeAllProblems</terminationTest> </config>
    // If no config, we do this by default.
    public boolean isTestComplete () {
        if (terminationPredicate == null || terminationPredicate.equals(COMPLETE_ALL_PROBLEMS))
            return this.numTestProbsCompleted >= this.numProbsInTest;
        else return false;
    }

    /**
     * This is a simple test that overriden by Posttest subclass with more complexity.
     * @return
     */
    public boolean isTestOn () throws SQLException {
        return true;
    }

    public Intervention selectIntervention (SessionEvent e) throws Exception {
        // The pretest is set to run once-per-session which means that this will be called each time a student logs in.
        // We want to insure that each student completes the entire pretest.  For now we are hard-wired to
        // test that the number of answers == the number of questions (later we may make this test vary based on some config)

        boolean runTest = isTestOn();
        boolean pretestComplete = isTestComplete();
        boolean replayTest = false; // TODO To override the above we'd need some flag on the class or on the student
                                    // If that flag is set, we'd have to blow away the students previous answers so that things play right.

        if (this.testId == -1)
            return null;
        // if the test is deactiveated, we have to abort showing it
        else if (!isTestActive(smgr.getConnection(), this.testId))
            return null;
        if ( pretestComplete && !replayTest)
            return null;
        // only run the test if it is ready to run (pretests are always ready, posttests have to be turned on with a switch in classconfig table)
        else if (runTest) {
            HttpServletRequest req = this.servletInfo.getRequest();
            super.selectIntervention(e);
            // Get the next pre-test problem (using the numCompleted + 1 as the position index)
            PrePostProblemDefn p = getPrePostProblemN(smgr.getConnection(),this.testId,this.numTestProbsCompleted +1);
            req.setAttribute(MESSAGE,this.startMessage);
            req.setAttribute(QUESTION,p);
            req.setAttribute(NUM_PROBS_IN_TEST, this.numProbsInTest);
            req.setAttribute(NUM_PROBS_COMPLETED, this.numTestProbsCompleted);
            req.setAttribute(LoginSequence.SESSION_ID,smgr.getSessionNum());
            // TODO The JSP will conditionally generate the write kind of HTML depending on whether its a multiple-choice or short-answer question.
            return new LoginIntervention(JSP);
        }
        else return null;
    }

    /*
    Take the students input to the current question and store it in the db.  Then
     generate a pretest page that is the next question in the pretest.

      This must return null when the pretest is over.
     */

    public LoginIntervention processInput (ServletParams params) throws Exception {
        String userAnswer = params.getString(ANSWER);
        int probId = params.getInt(PROBID);
        int timeOnProb = params.getInt(ELAPSED_TIME) / 1000; // convert to seconds
        PrePostProblemDefn thisProb = DbPrePost.getPrePostProblem(conn,probId);
        boolean isCorrect = true;
        if (thisProb != null) {
            isCorrect = gradeProb(thisProb,userAnswer);
        }
        // Store the student answer to this question (need studId, probId, and answer)
        DbPrePost.storeStudentAnswer(conn,smgr.getSessionNum(),smgr.getStudentId(),probId,userAnswer,testType, timeOnProb, isCorrect,this.numTestProbsCompleted++);
        PrePostProblemDefn p = getNextPretestQuestion(smgr);
        if (p == null)
            return null;
        else {
            HttpServletRequest req = this.servletInfo.getRequest();
            req.setAttribute(QUESTION,p);
            req.setAttribute(NUM_PROBS_IN_TEST, this.numProbsInTest);
            req.setAttribute(NUM_PROBS_COMPLETED, this.numTestProbsCompleted);
            req.setAttribute(LoginInterventionSelector.INTERVENTION_CLASS,getClass().getName());
            //  The JSP will conditionally generate the write kind of HTML depending on whether its a multiple-choice or short-answer question.
            return new LoginIntervention(JSP);
        }
    }

    // If a problem has an expected answer (e.g. the answer field is non-null) we grade it.
    // pretest problems that are multi-choice do not rely on a,b,c as the userAnswer.  What comes back as the userInput from the HTML form is
    // something like "2 - I don't like this".   So to check correctness of a multi-choice question, the correct answer needs
    // to be stored in the database as "2 - I don't like this" rather than "b".  We then do a string match.   Short answer questions
    // just use a string match and remove spaces and upper case.
    private boolean gradeProb(PrePostProblemDefn thisProb, String userAnswer) {

        // For short-answer questions we do a string match.  The string match algorithm does nothing more than remove trailing spaces and upper case.
        if (thisProb.isMultiChoice() && thisProb.getAnswer() != null) {
            return thisProb.getAnswer().toLowerCase().trim().equals(userAnswer.toLowerCase().trim());
        }
        // if its a short-answer problem with an expected answer, check it
        else if (thisProb.getAnswer() != null) {
            return thisProb.getAnswer().toLowerCase().trim().equals(userAnswer.toLowerCase().trim());
        }
        // its not a question with an expected answer, so just mark it as correct
        else
            return true;
    }

    private boolean isTestActive (Connection conn, int testId) throws SQLException {
        return DbPrePost.isTestActive(conn,testId);

    }


    // position given is 1-based,  datbase stores them as zero based, so reduce it
    private PrePostProblemDefn getPrePostProblemN(Connection connection, int pretestId, int position) throws SQLException {
        PrePostProblemDefn p = DbPrePost.getPrePosttestProblem(connection,pretestId,position-1);  // convert to 0-based.
        return p;
    }

    private PrePostProblemDefn getNextPretestQuestion(SessionManager smgr) throws SQLException {
        // get the next problem using the numCompleted + 1 as an index
        PrePostProblemDefn p = getPrePostProblemN(smgr.getConnection(),this.testId,this.numTestProbsCompleted +1);
        return p;
    }

}
