package edu.umass.ckc.wo.interventions;

import edu.umass.ckc.wo.tutor.intervSel2.AskEmotionIS;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 2/14/14
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class AskEmotionFreeAnswerIntervention extends InputResponseIntervention implements NextProblemIntervention {



    public static final String REASON = "reason" ;
    public static final String FEELING = "feeling" ;
    public static final String CONTINUE = "continue" ;
    public static final String GOAL = "goal" ;
    public static final String RESULT = "result" ;

    public AskEmotionFreeAnswerIntervention() {


    }

    @Override
    public String logEventName() {
        return "AskEmotionFreeAnswerIntervention" ;

    }


    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isBuildProblem() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getResource() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDialogHTML() {
        String str = "<div>  " +
                                 getFormOpen() + " <p>We will ask these questions a <b>few</b> times, so its <b>OK</b> to change your mind.  " +
                "Please be as <b>honest</b> as possible in answering these questions. <br><br>";
        str += "1.  How would you describe your emotions right now" +
                " (as compared to the last time you were asked)?";
        str += "<textarea name=\"" + FEELING + "\" rows=\"3\" cols=\"40\"/>" ;
        str += "<br><br>2. Why do you feel that way?";
        str += "<textarea name=\"" + REASON + "\" rows=\"3\" cols=\"40\"/>" ;
        str += "<br><br>3. What are you trying to do?";
        str += "<textarea name=\"" + GOAL + "\" rows=\"3\" cols=\"40\"/>" ;
//        str += "<br><br>3. What do you want to happen?";
//        str += "<textarea name=\"" + RESULT + "\" rows=\"3\" cols=\"40\"/>" ;
        str+= "</p>";

        str+="</form></div>";

        return str;
    }

    public String getType () {
        return "AskEmotionIntervention";
    }
}
