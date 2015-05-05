package edu.umass.ckc.wo.config;

import edu.umass.ckc.wo.tutor.probSel.LessonModelParameters;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;
import edu.umass.ckc.wo.tutor.probSel.TopicModelParameters;
import org.jdom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 4/3/15
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class LessonXML extends ConfigXML{


    public LessonXML(Element interventions, Element control, String name, String style) {
        super(interventions, control, name, style);
    }

    public LessonModelParameters getLessonModelParams () {
        if (style.equalsIgnoreCase("topics"))
            return new TopicModelParameters(this.control);
        else return new LessonModelParameters(this.control);
    }


}
