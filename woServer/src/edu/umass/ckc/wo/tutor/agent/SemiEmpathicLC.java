package edu.umass.ckc.wo.tutor.agent;

import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.event.tutorhut.AttemptEvent;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.exc.DeveloperException;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.response.*;
import edu.umass.ckc.wo.tutor.studmod.AffectStudentModel;
import edu.umass.ckc.wo.tutormeta.StudentModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 3/13/14
 * Time: 12:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemiEmpathicLC extends EmotionalLC {

    public Response processNextProblemRequest(SessionManager smgr, NextProblemEvent e, Response r) throws DeveloperException {

        AffectStudentModel sm;
        try {
            sm = (AffectStudentModel) smgr.getStudentModel();
        } catch (Exception ex) {
            throw new DeveloperException("You must use an AffectStudentModel when learning companions are part of the pedagogy");
        }
        List<String> l = selectEmotions(sm,r);
        addCharacterControl(r);
        return r;

    }



        // TODO this code duplicates a method below that should be eliminated
        public List<String> selectEmotions(AffectStudentModel m, Response r) {
            if (r instanceof TopicIntroResponse || r instanceof InterventionResponse) {
                clips.add("idle");
                return clips;
            }
            else if (r instanceof ProblemResponse) {
                Problem p = ((ProblemResponse) r).getProblem();
                if (p != null && p.isExample())  {
                    clips.add("interestHigh");
                    return clips;
                }
            }
            //Every 5 problems, it trains attributions
            if (java.lang.Math.random() < 0.10) {
                List genAttrList = Arrays.asList(generalAttribution);
                Collections.shuffle(genAttrList);

                clips.add((String) genAttrList.get(0));
            }

            if (clips.size() == 0) {
                clips.add("idle");
                return clips;
            }

            return clips;
        }


    private boolean isLow(int emotionValue) {
        if ( emotionValue < 3 && emotionValue > 0 )
            return true ;

        return false ;
    }

    private boolean isHigh(int emotionValue) {
        if ( emotionValue > 3 )
            return true ;

        return false ;
    }
}
