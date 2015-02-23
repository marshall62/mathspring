package edu.umass.ckc.wo.event;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 2/23/15
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class InternalTutorEvent {
    private SessionEvent userEvent;

    public InternalTutorEvent (SessionEvent e) {
        this.userEvent = e;

    }

    public InternalTutorEvent (){

    }
}
