package edu.umass.ckc.wo.interventions;

/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 3/13/15
 * Time: 3:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class CollaborationPartnerIntervention extends InformationIntervention implements NextProblemIntervention{

    private String name = null;

    public String getType () {
        return "CollaborationPartnerIntervention";
    }

    public String getDialogHTML () {
        //TODO find a better way to indicate the partner
        String str = "<div><p>In this next problem, you will work with " + name + " who should be" +
                " sitting next to you.   You will work on ONE math problem, and then you will come back to your screen again. <br/> <br/>" +
                name +" will click on answers and hints and use the" +
                " keyboard, and your job is to READ the math problem aloud.<br/><br/>" +
                " Discuss the math problem. Work together on how to solve it. USE PAPER" +
                " and PENCIL so that you sketch out a solution.<br/>";
        str+="</div>";
        return str;
    }

    @Override
    public boolean isBuildProblem() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setPartner(String name){
        this.name = name;
    }
}
