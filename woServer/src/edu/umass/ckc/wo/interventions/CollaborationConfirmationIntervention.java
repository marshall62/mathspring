package edu.umass.ckc.wo.interventions;

/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 3/12/15
 * Time: 8:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollaborationConfirmationIntervention extends InputResponseIntervention implements NextProblemIntervention{
    public String getType () {
        return "CollaborationConfirmationIntervention";
    }


    public String getDialogHTML () {
        String str = "<div><p>The next activity is a special one. <br/> " +
                 "You will be working with the student sitting to your left or right. "+
                 "He/She read the problem aloud, and your job is to use the " +
                 "mouse and keyboard and WAIT and NOT RUSH to answer.  WORK " +
                 "TOGETHER  ON PAPER to solve the problem, make a sketch, write an " +
                 "equation, whatever is necessary. <br/>";

        str+="</div>";
        return str;
    }


    @Override
    public boolean isBuildProblem() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
