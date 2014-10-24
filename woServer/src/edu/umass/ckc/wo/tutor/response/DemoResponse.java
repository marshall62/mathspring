package edu.umass.ckc.wo.tutor.response;

import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.tutor.Settings;

/**
 * <p> Created by IntelliJ IDEA.
 * User: Ivon Arroyo
 * Date: Dec 29, 2008
 * Time: 10:59:25 AM
 */
public class DemoResponse extends ProblemResponse {

    public DemoResponse(Problem p) throws Exception {
        super(p);
        prob = p ;

        if ( prob!=null)
            prob.setMode(Problem.DEMO);
        buildJSON();
    }




    public String logEventName() {
        if ( prob != null )
           return "demo " + this.prob.getId();

        return null ;
    }


}