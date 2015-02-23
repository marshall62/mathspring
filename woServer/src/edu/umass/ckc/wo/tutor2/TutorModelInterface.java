package edu.umass.ckc.wo.tutor2;

import edu.umass.ckc.wo.event.InternalTutorEvent;
import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.tutor.response.Response;
import edu.umass.ckc.wo.tutormeta.TutorEventHandler;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 2/23/15
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TutorModelInterface {

    public Response processInternalTutorEvent (InternalTutorEvent e) ;

    public Response processUserEvent (SessionEvent e) ;
}
