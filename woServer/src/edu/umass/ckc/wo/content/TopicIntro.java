package edu.umass.ckc.wo.content;

import net.sf.json.JSONObject;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 21, 2009
 * Time: 10:59:21 AM
 */
public class TopicIntro extends Problem {




    public TopicIntro(String resource, String type, String topic) {
        this.id = 999;
        this.resource = resource;
        this.type = type;
        this.topicName = topic;
        setMode(Problem.TOPIC_INTRO);
        this.activityType = Problem.TOPIC_INTRO_PROB_TYPE;
    }






}
