package edu.umass.ckc.wo.interventions;

/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 3/13/15
 * Time: 12:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class FinishCollaborationIntervention extends InputResponseIntervention implements NextProblemIntervention{
    public String getType () {
        return "FinishCollaborationIntervention";
    }

    public String getDialogHTML () {
        String str = "<div><p>Finished working together.<br/>";

        str+="</div>";
        return str;
    }

    @Override
    public boolean isBuildProblem() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
