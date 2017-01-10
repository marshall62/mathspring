package edu.umass.ckc.wo.interventions;

import edu.umass.ckc.wo.tutor.intervSel2.AskEmotionIS;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 2/14/14
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class AskEmotionRadioIntervention extends InputResponseIntervention implements NextProblemIntervention {
    private AskEmotionIS.Emotion emotion;
    protected boolean buildProblem=false;
    private boolean askWhy=false;
    // the default question this thing asks has an emotion plugged into the string
    private String question = "Based on the last few problems tell us about your level of %s in solving math problems.";
    private String questionHeader = "Please tell us how you are feeling.";

    public static final String LEVEL = "level" ;
    public static final String EMOTION = "emotion" ;
    public static final String REASON = "reason" ;

    public AskEmotionRadioIntervention(AskEmotionIS.Emotion emotionToQuery, boolean askWhy) {
        this.emotion = emotionToQuery;
        this.askWhy=askWhy;
    }

    @Override
    public String logEventName() {
        return "AskEmotionIntervention-" + emotion.getName();

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
                                 getFormOpen() + " <p>" + this.questionHeader + "<br>" +
                String.format(question,emotion.getName());

        str += "<br><input type=\"hidden\" name=\"" + EMOTION + "\" value=\"" + emotion.getName() + "\"><br>";
        for (int i =0;i<emotion.getLabels().size();i++)
            str += "<input name=\"" + LEVEL + "\" type=\"radio\" value=\"" + emotion.getVals().get(i) + "\">" + emotion.getLabels().get(i) + "</input><br>";
        str += "<br><br>";
        if (askWhy) {
            str += "Why is that?<br>";
            str += "<textarea name=\"" + REASON + "\" rows=\"4\" cols=\"40\"/>";
        }
        str+= "</p>";

        str+="</form></div>";

        return str;
    }

    public String getType () {
        return "AskEmotionIntervention";
    }
}
