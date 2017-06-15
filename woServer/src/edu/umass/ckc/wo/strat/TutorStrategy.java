package edu.umass.ckc.wo.strat;

import edu.umass.ckc.wo.tutor.Pedagogy;

/**
 * Created by marshall on 6/14/17.
 * Making it be a subclass of Pedagogy so that all places where Pedagogy is used can accept this class.
 */
public class TutorStrategy extends Pedagogy {

    private int id;

    public int getStratId () {
        return this.id;
    }

}
