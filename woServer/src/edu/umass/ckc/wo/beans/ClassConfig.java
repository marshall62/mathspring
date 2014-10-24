package edu.umass.ckc.wo.beans;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Apr 6, 2009
 * Time: 6:00:13 PM
 */
public class ClassConfig {
    private int pretest;
    private int posttest;
    private int fantasy;
    private int mfr;
    private int spatial;
    private int tutoring;
    private boolean useDefaults;

    public static final int OFF=0;
    public static final int ON=1;
    public static final int ON_AFTER_PRETEST=2;
    public static final int ON_AFTER_POSTTEST=3;

    public static ClassConfig getDefaultConfig () {
        return new ClassConfig(ON,ON_AFTER_PRETEST,ON_AFTER_POSTTEST,ON_AFTER_POSTTEST,ON_AFTER_POSTTEST,ON_AFTER_PRETEST,true);
    }

    public ClassConfig(int pretest, int posttest, int fantasy, int mfr, int spatial, int tutoring, boolean useDefaults) {
        this.pretest = pretest;
        this.posttest = posttest;
        this.fantasy = fantasy;
        this.mfr = mfr;
        this.spatial = spatial;
        this.tutoring = tutoring;
        this.useDefaults = useDefaults;
    }

    public int getPretest() {
        return pretest;
    }

    public int getPosttest() {
        return posttest;
    }

    public int getFantasy() {
        return fantasy;
    }

    public int getMfr() {
        return mfr;
    }

    public int getSpatial() {
        return spatial;
    }

    public int getTutoring() {
        return tutoring;
    }

    public boolean getUseDefaults () {
        return useDefaults;
    }



    public String toString () {
        return String.format("&pretest=%d&posttest=%d&fantasy=%d&mfr=%d&spatial=%d&tutoring=%d",
                pretest,posttest,fantasy,mfr,spatial,tutoring);
    }
}
