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
                                 getFormOpen() + " <p>We will ask these questions a <b>few</b> times, so its <b>OK</b> to change your mind.  <br>" +
                "Please be as <b>honest</b> as possible in answering these questions. <br><br>";
        str += "1.  How do you feel about your work in Mathspring?<br>";
        str += "<textarea name=\"" + FEELING + "\" rows=\"4\" cols=\"40\"/>" ;
        str += "<br><br>2. What led you to this feeling?<br>";
        str += "<textarea name=\"" + REASON + "\" rows=\"4\" cols=\"40\"/>" ;
        str += "<br><br>3. Do you wish to continue working in Mathspring?  Why or why not?<br>";
        str += "<textarea name=\"" + CONTINUE + "\" rows=\"4\" cols=\"40\"/>" ;
        str+= "</p>";

        str+="</form></div>";

        return str;
    }

    public String getType () {
        return "AskEmotionIntervention";
    }
}
