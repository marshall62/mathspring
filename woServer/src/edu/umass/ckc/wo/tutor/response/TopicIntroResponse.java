package edu.umass.ckc.wo.tutor.response;

import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.content.TopicIntro;
import edu.umass.ckc.wo.tutormeta.TopicMastery;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Dec 29, 2008
 * Time: 10:59:25 AM
 */
public class TopicIntroResponse extends ProblemResponse {


    public TopicIntroResponse(TopicIntro intro, List<TopicMastery> topicMasteryLevels, int curTopic) {
        super(intro,topicMasteryLevels,curTopic);
    }

    public TopicIntroResponse(TopicIntro intro) {
        super(intro );
    }
}