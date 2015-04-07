package edu.umass.ckc.wo.tutor.probSel;


import edu.umass.ckc.wo.tutormeta.PedagogyParams;
import org.jdom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: Apr 18, 2011
 * Time: 9:42:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class LessonModelParameters {

    public LessonModelParameters () {

    }

    public LessonModelParameters (Element controlElt) {

    }

    // overload the params of this with those given for class.
    public LessonModelParameters overload(LessonModelParameters classParams) {

        return this;

    }

    public LessonModelParameters overload (PedagogyParams userParams) {

        return this;
    }


}