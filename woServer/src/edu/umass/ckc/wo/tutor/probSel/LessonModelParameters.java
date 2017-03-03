package edu.umass.ckc.wo.tutor.probSel;


import edu.umass.ckc.wo.tutormeta.PedagogyParams;
import org.jdom.Element;

/**
 * Handles lessons that are not the standard Topic based teaching.   We use this for a Common Core approach (when the lessondefinition has a style=ccss
 * User: marshall
 * Date: Apr 18, 2011
 * Time: 9:42:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class LessonModelParameters {

    private String ccss;
    protected long maxTimeMs = 15 * 60 * 1000;
    protected long minTimeMs = 3 * 60 * 1000;
//    protected int maxTimeMinutes = 15;  // default to 15 minute s
//    protected int minTimeMinutes = 3;  // default to 3 minute s
    protected int maxProbs = 10;            // default to 10 problems
    protected int minProbs = 1;            // default to 1 problem
    protected double desiredMastery = 0.95;  // default to 95%
    protected double difficultyRate = 2; // 2 gives the splitting denominator in the binary search using 1/2 as the fraction
    protected int contentFailureThreshold ;

    public LessonModelParameters () {

    }

    /*    Given something like from the lessonDefinition table
        <controlParameters>
            <minNumberProbs>3</minNumberProbs>
            <maxNumberProbs>10</maxNumberProbs>
            <maxTimeMinutes>15</maxTimeMinutes>
            <minTimeMinutes>3</minTimeMinutes>
            <difficultyRate>3</difficultyRate>
            <contentFailureThreshold>2</contentFailureThreshold>
        </controlParameters>
     */
    public LessonModelParameters (Element controlElt) {
        Element maxProbsElt = controlElt.getChild("maxNumberProbs");
        if (maxProbsElt != null)
            this.maxProbs =  Integer.parseInt(maxProbsElt.getTextTrim());
        Element minProbsElt = controlElt.getChild("minNumberProbs");
        if (maxProbsElt != null)
            this.minProbs =  Integer.parseInt(minProbsElt.getTextTrim());
        Element maxTimeElt = controlElt.getChild("maxTimeMinutes");
        if (maxTimeElt != null)
            setMaxTimeMinutes(Integer.parseInt(maxTimeElt.getTextTrim()));
        Element minTimeElt = controlElt.getChild("minTimeMinutes");
        if (minTimeElt != null)
            setMinTimeMinutes(Integer.parseInt(minTimeElt.getTextTrim()));
        Element diffElt = controlElt.getChild("difficultyRate");
        if (diffElt != null)
            this.difficultyRate =  Double.parseDouble(diffElt.getTextTrim());
        Element threshElt = controlElt.getChild("contentFailureThreshold");
        if (threshElt != null)
            this.contentFailureThreshold =  Integer.parseInt(threshElt.getTextTrim());
    }

    // overload the params of this with those given for class.
    public LessonModelParameters overload(LessonModelParameters classParams) {
        if (classParams.getMaxProbs() > 0)
            this.maxProbs =  classParams.getMaxProbs();
        if (classParams.getMinProbs() > 0)
            this.minProbs =  classParams.getMinProbs();
        if (classParams.getMaxTimeMinutes() > 0)
            this.maxTimeMs =  classParams.getMaxTimeMs();
        if (classParams.getMinTimeMinutes() > 0)
            this.minTimeMs =  classParams.getMinTimeMs();
        if (classParams.getDesiredMastery() > 0)
            this.desiredMastery = classParams.getDesiredMastery();
        if (classParams.getDifficultyRate() > 0)
            this.difficultyRate = classParams.getDifficultyRate();
        if (classParams.getContentFailureThreshold() > 0)
            this.contentFailureThreshold = classParams.getContentFailureThreshold();
        return this;
    }

    public LessonModelParameters overload (PedagogyParams userParams) {
        this.ccss = userParams.getCcss();
        setMaxTimeMinutes(userParams.getMaxTimeMinutes()) ;
        this.maxProbs = userParams.getMaxProbs() ;
        this.desiredMastery = userParams.getMastery(); // It's not a topic mastery in this case.  It will be a mastery for a standard
        return this;
    }

    public String getCcss() {
        return ccss;
    }

    public long getMaxTimeMs() {
        return maxTimeMs;
    }

    public long getMinTimeMs() {
        return minTimeMs;
    }

    public int getMaxTimeMinutes() {
        return (int) (this.maxTimeMs / (1000*60));
    }

    public long getMinTimeMinutes() {
        return (int) (this.minTimeMs / (1000*60));
    }

    public void setMinTimeMinutes (int minutes) {
        this.minTimeMs = minutes * 60 * 1000;
    }

    public void setMaxTimeMinutes (int minutes) {
        this.maxTimeMs = minutes * 60 * 1000;
    }

    public int getMaxProbs() {
        return maxProbs;
    }

    public double getDesiredMastery() {
        return desiredMastery;
    }

    public int getMinProbs() {
        return minProbs;
    }

    public double getDifficultyRate() {
        return difficultyRate;
    }

    public int getContentFailureThreshold() {
        return contentFailureThreshold;
    }

    public void setContentFailureThreshold(int contentFailureThreshold) {
        this.contentFailureThreshold = contentFailureThreshold;
    }

    public void setMaxTimeMs(long maxTimeMs) {
        this.maxTimeMs = maxTimeMs;
    }

    public void setMinTimeMs(long minTimeMs) {
        this.minTimeMs = minTimeMs;
    }

    public void setMaxProbs(int maxProbs) {
        this.maxProbs = maxProbs;
    }

    public void setMinProbs(int minProbs) {
        this.minProbs = minProbs;
    }

    public void setDesiredMastery(double desiredMastery) {
        this.desiredMastery = desiredMastery;
    }

    public void setDifficultyRate(double difficultyRate) {
        this.difficultyRate = difficultyRate;
    }
}