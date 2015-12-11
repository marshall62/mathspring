package edu.umass.ckc.wo.tutormeta;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 9/19/13
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class PedagogyParams {

    private int studId;
    private boolean showIntro;
    private long maxTime;
    private int maxProbs;
    private float topicMastery;
    private String mode;
    private boolean singleTopicMode;
    private String ccss;
    private int topicId;
    private int cuId;


    public PedagogyParams(int studId, boolean showIntro, int maxTimeMinutes, int maxProbs, String mode, boolean singleTopicMode, String ccss, int topicId, float topicMastery, int cuId) {
        this.studId = studId;
        this.showIntro = showIntro;
        this.maxTime = maxTimeMinutes * 60 * 1000;  // max time is given in minutes.   We convert to milliseconds here
        this.maxProbs = maxProbs;
        this.mode = mode;
        this.singleTopicMode = singleTopicMode;
        this.ccss = ccss;
        this.topicId=topicId;
        this.topicMastery = topicMastery;
        this.cuId= cuId;
    }

    public int getStudId() {
        return studId;
    }

    public boolean isShowIntro() {
        return showIntro;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public int getMaxProbs() {
        return maxProbs;
    }

    public String getMode() {
        return mode;
    }

    public boolean isSingleTopicMode() {
        return singleTopicMode;
    }

    public String getCcss(){
        return ccss;
    }

    public int getTopicId() {
        return topicId;
    }

    public float getTopicMastery() {
        return topicMastery;
    }

    public void setTopicMastery(float topicMastery) {
        this.topicMastery = topicMastery;
    }

    public int getCuId() {
        return cuId;
    }
}
