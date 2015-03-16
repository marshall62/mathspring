package edu.umass.ckc.wo.interventions;

/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 3/12/15
 * Time: 11:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollaborationOriginatorIntervention extends InformationIntervention implements NextProblemIntervention{

    public String getType () {
        return "CollaborationOriginatorIntervention";
    }

    public String getDialogHTML () {
        String str = "<div><p>\"Please wait while we find a partner for you... <br/>" +
                "You will discuss the problem together." +
                "YOUR job is to use the mouse and keyboard.<br/>";

        str+="</div>";
        return str;
    }

    @Override
    public boolean isBuildProblem() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }}
