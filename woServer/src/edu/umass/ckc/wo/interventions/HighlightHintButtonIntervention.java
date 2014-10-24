package edu.umass.ckc.wo.interventions;

import net.sf.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 11/21/13
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class HighlightHintButtonIntervention extends SimpleBaseIntervention implements AttemptIntervention {


    @Override
    public String logEventName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isShowGrade() {
        return true;     // note that Flash problems grade themselves, so this will only work for HTML problems.
    }

    public JSONObject buildJSON(JSONObject jo) {
        jo.element("interventionType","HighlightHintButton");
        return jo;
    }


}
