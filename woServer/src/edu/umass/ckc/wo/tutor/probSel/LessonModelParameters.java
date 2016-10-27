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
    private int maxTimeMinutes = 15;  // default to 15 minute s
    private int maxProbs = 10;            // default to 10 problems
    private int minProbs = 1;            // default to 1 problem
    private double desiredMastery = 0.95;  // default to 95%
    private double difficultyRate = 2; // 2 gives the splitting denominator in the binary search using 1/2 as the fraction

    public LessonModelParameters () {

    }

    /*    Given something like from the lessonDefinition table
        <controlParameters>
            <minNumberProbs>3</minNumberProbs>
            <maxNumberProbs>10</maxNumberProbs>
            <maxTimeMinutes>15</maxTimeMinutes>
            <difficultyRate>3</difficultyRate>
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
            this.maxTimeMinutes =  Integer.parseInt(maxTimeElt.getTextTrim());
        Element diffElt = controlElt.getChild("difficultyRate");
        if (diffElt != null)
            this.difficultyRate =  Double.parseDouble(diffElt.getTextTrim());
    }

    // overload the params of this with those given for class.
    public LessonModelParameters overload(LessonModelParameters classParams) {

        return this;

    }

    public LessonModelParameters overload (PedagogyParams userParams) {
        this.ccss = userParams.getCcss();
        this.maxTimeMinutes = userParams.getMaxTimeMinutes() ;
        this.maxProbs = userParams.getMaxProbs() ;
        this.desiredMastery = userParams.getMastery(); // It's not a topic mastery in this case.  It will be a mastery for a standard
        return this;
    }

    public String getCcss() {
        return ccss;
    }

    public long getMaxTimeMinutes() {
        return maxTimeMinutes;
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
}