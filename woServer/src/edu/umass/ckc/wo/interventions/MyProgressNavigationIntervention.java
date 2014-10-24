package edu.umass.ckc.wo.interventions;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 9/3/14
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class MyProgressNavigationIntervention extends InformationIntervention implements NextProblemIntervention {
    public String getType () {
        return "MyProgressNavigation";
    }


    public String getDialogHTML () {
        String str = "<div><p>Let's see how much progress we are making!<br>";

        str+="</div>";
        return str;
    }


    @Override
    public boolean isBuildProblem() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}


