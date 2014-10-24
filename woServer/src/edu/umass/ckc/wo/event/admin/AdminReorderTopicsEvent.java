package edu.umass.ckc.wo.event.admin;

import ckc.servlet.servbase.ServletParams;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 17, 2008
 * Time: 9:53:51 AM
 */
public class AdminReorderTopicsEvent extends AdminEditTopicsEvent {

    private int topicId;
    private String direction;
    


    public AdminReorderTopicsEvent (ServletParams p) throws Exception {
        super(p);
        topicId = p.getInt("topicId");
        direction = p.getString("direction");
    }

    public int getTopicId() {
        return topicId;
    }

    public String getDirection() {
        return direction;
    }
}
