package edu.umass.ckc.wo.interventions;

import edu.umass.ckc.wo.content.Hint;
import edu.umass.ckc.wo.tutormeta.Intervention;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 11/7/13
 * Time: 4:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class TopicSwitchIntervention extends InformationIntervention implements NextProblemIntervention {
    protected String reasons;


    public TopicSwitchIntervention(String reasons) {
        this.reasons = reasons;
    }



    public String getType () {
        return "TopicSwitch";
    }


    public String getDialogHTML () {
        String str = "<div><p>You are about to switch topics because:<br>";
        str += reasons;

        str+="</p></div>";
        return str;
    }


    @Override
    public boolean isBuildProblem() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
