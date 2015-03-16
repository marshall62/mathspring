package edu.umass.ckc.wo.interventions;

/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 3/13/15
 * Time: 12:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class CloseWindowIntervention extends InputResponseIntervention implements NextProblemIntervention{
    public String getType () {
        return "CloseWindowIntervention";
    }

    public String getDialogHTML () {
        return null;
    }

    @Override
    public boolean isBuildProblem() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
