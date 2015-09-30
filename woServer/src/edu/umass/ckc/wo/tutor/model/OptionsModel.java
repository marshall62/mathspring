package edu.umass.ckc.wo.tutor.model;

import edu.umass.ckc.wo.event.tutorhut.TutorHutEvent;
import edu.umass.ckc.wo.log.TutorLogger;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.DynamicPedagogy;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorSpec;
import edu.umass.ckc.wo.tutor.pedModel.BasePedagogicalModel;
import edu.umass.ckc.wo.tutor.pedModel.DynamicPedagogicalModel;
import edu.umass.ckc.wo.tutor.response.InternalEvent;
import edu.umass.ckc.wo.tutor.response.Response;

import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 9/18/15
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class OptionsModel{

    SessionManager smgr;
    Pedagogy pedagogy;
    InterventionGroup intervs;

    public OptionsModel(SessionManager smgr, Pedagogy pedagogy, InterventionGroup intervs){
        this.smgr = smgr;
        this.pedagogy = pedagogy;
        this.intervs = intervs;
    }

    public Response processChanges(TutorHutEvent e) throws Exception {
        if(smgr.getTimeInSession() - smgr.getStudentState().getTimeLastChange() > 60000 && pedagogy instanceof DynamicPedagogy){
            smgr.getStudentState().setTimeLastChange(smgr.getTimeInSession());
            String change = changeRandElt(e);
            new TutorLogger(smgr).logDynamicChange(e, change);
        }
        return null;
    }

    private String changeRandElt(TutorHutEvent e) throws Exception {
        DynamicPedagogy dynPedagogy = (DynamicPedagogy) pedagogy;
        Random rand = new Random();
        String elementSwitched = null;
        String eltChanged = "None";
        while(elementSwitched == null){
            //TODO Compatibility checking: ask for list of dependencies and send them as parameters to the change methods
            switch (rand.nextInt(7)){
                case 1:
                    elementSwitched = dynPedagogy.changeChallengeModeProblemSelectorClass();
                    eltChanged = "ChallengeModeSelector";
                    break;
                case 2:
                    elementSwitched = dynPedagogy.changeHintSelectorClass();
                    eltChanged = "HintSelector";
                    break;
                case 3:
                    elementSwitched = dynPedagogy.changeLearningCompanionClass();
                    eltChanged = "LearningCompanion";
                    break;
                case 4:
                    elementSwitched = dynPedagogy.changeReviewModeProblemSelectorClass();
                    eltChanged = "ReviewModeProblemSelector";
                    break;
                case 5:
                    elementSwitched = dynPedagogy.changeProblemSelectorClass();
                    eltChanged = "ProblemSelector";
                    break;
                case 6:
                    elementSwitched = dynPedagogy.changeStudentModelClass();
                    eltChanged = "StudentModel";
                    break;
                default:
                    if (intervs != null){
                        List<InterventionSelectorSpec> specs = intervs.getInterventionsSpecs();
                        int toSwitch = rand.nextInt(specs.size());
                        InterventionSelectorSpec candidate = specs.get(toSwitch);
                        if(candidate.getTurnedOn()){
                            candidate.setTurnedOn(false);
                        }
                        else{
                            candidate.setTurnedOn(true);
                        }
                        elementSwitched = candidate.getClassName();
                        eltChanged = "Intervention:"+ candidate.getClassName()+" originally, "+ !candidate.getTurnedOn();
                    }
                    else{
                        elementSwitched = null;
                    }
                    break;
            }

        }
      //  System.out.println(eltChanged + " was changed to: " + elementSwitched);
        return eltChanged + " was changed to: " + elementSwitched;
    }
}
