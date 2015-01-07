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

    public Response processNextProblemRequest(SessionManager smgr, NextProblemEvent e, Response r) throws Exception {

        AffectStudentModel sm;
        try {
            sm = (AffectStudentModel) smgr.getStudentModel();
        } catch (Exception ex) {
            throw new DeveloperException("You must use an AffectStudentModel when learning companions are part of the pedagogy");
        }
        List<String> l = selectEmotions(sm,r,smgr);
        addCharacterControl(r);
        return r;

    }




        public List<String> selectEmotions(AffectStudentModel m, Response r, SessionManager smgr) throws Exception {
            if (r instanceof TopicIntroResponse || r instanceof InterventionResponse) {
                clips.add("idle");
                return clips;
            }
            // 12/22/14 DM:  This block of code is repeated in FullEmpathicLC, SemiEmpathicLC, NoEmpathicLC.   This was
            // done this way because of a request and the desire to make the simplest change necessary to achieve the behavior.

            else if (r instanceof ProblemResponse)
                return super.selectEmotions(m,r,smgr);

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
