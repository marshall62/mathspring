package edu.umass.ckc.wo.content;

import edu.umass.ckc.wo.interventions.NextProblemIntervention;
import edu.umass.ckc.wo.tutormeta.Intervention;
import net.sf.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: 8/22/12
 * Time: 2:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExternalActivity extends Problem implements NextProblemIntervention {
    private static final String EXTERNAL_ACTIVITY = "ExternalActivity";
    private int topicId;

    public ExternalActivity() {
    }

    public ExternalActivity(int id, String name, String descr, String url, String instructions, double difficultyRating) {
        //super(id,url,null,name,descr,false,null,instructions,null);
        super(id, url, null, name, descr, false, 0, null, null, instructions, null, "ready", null, null, QuestType.multiChoice);
        setType("ExternalActivity");
        this.setDiff_level(difficultyRating);
    }

    @Override
    public boolean isBuildProblem() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public JSONObject buildJSON(JSONObject jo) {
        jo.element("activityType",Intervention.INTERVENTION);
        jo.element("interventionType",ExternalActivity.EXTERNAL_ACTIVITY);
        jo.element("id",this.getId());
        jo.element("resource",this.getResource());
        jo.element("topicId",this.getTopicId());
        jo.element("instructions",this.getInstructions());
        jo.element("mode", PRACTICE);
        return jo;
    }

    public void setTopicId(int topic) {
        this.topicId = topic;
    }

    public int getTopicId() {
        return topicId;
    }
}
