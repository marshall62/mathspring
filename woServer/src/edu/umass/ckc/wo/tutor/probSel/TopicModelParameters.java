package edu.umass.ckc.wo.tutor.probSel;


import edu.umass.ckc.wo.tutormeta.PedagogyParams;
import org.jdom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: Apr 18, 2011
 * Time: 9:42:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class TopicModelParameters extends LessonModelParameters {
    public static final int MAX_NUM_PROBS_PER_TOPIC = 8;
    public static final int MIN_NUM_PROBS_PER_TOPIC = 3;
    public static final int MAX_TIME_IN_TOPIC = 10 * 60 * 1000;
    public static final int MIN_TIME_IN_TOPIC = 30 * 1000;
    public static final int CONTENT_FAILURE_THRESHOLD = 1;
    public static final double TOPIC_MASTERY = 0.85;
    public static final int DIFFICULTY_RATE = 2;
    public static final frequency DEFAULT_TOPIC_INTRO_FREQ = frequency.always;
    public static final frequency DEFAULT_EXAMPLE_FREQ = frequency.always;




    public enum frequency {
        never,
        oncePerSession,
        always
    }


    private frequency topicIntroFrequency;
    private frequency topicExampleFrequency;
    private String ccss;
    private double difficultyRate ; // this is the divisor that the problem selector uses to find increase/decrease its index into the
    private int maxNumberProbs ;   // list of available problems
    private int minNumberProbs ;
    private long maxTimeInTopic;   // this is in milliseconds
    private long minTimeInTopic;    // in milliseconds
    private int contentFailureThreshold ; // the number of times it will select a problem within this topic when it can't meet
    // the easier/harder/same criteria.   Once exceeded, jump topics
    private double topicMastery;
    private String lessonStyle;
    private boolean singleTopicMode;
    private InterleavedProblemSetParams interleaveParams=null;  // If interleaved problem sets are part of the lesson, this will be non-null

    // overload the params of this with those given for class.
    public LessonModelParameters overload(LessonModelParameters theClassParams) {
        TopicModelParameters classParams = (TopicModelParameters) theClassParams;
        if (classParams == null) return this;
        if (classParams.getDifficultyRate() > 0)
            this.difficultyRate =classParams.getDifficultyRate();
        if (classParams.getMaxNumberProbs() > 0)
            this.maxNumberProbs =classParams.getMaxNumberProbs();
        if (classParams.getMinNumberProbs() > 0)
            this.minNumberProbs =classParams.getMinNumberProbs();
        // class params has maxTime given in MS.
        if (classParams.getMaxTimeInTopic() > 0)
            this.maxTimeInTopic =classParams.getMaxTimeInTopic();
        // class params has minTime in MS
        if (classParams.getMinTimeInTopic() > 0)
            this.minTimeInTopic =classParams.getMinTimeInTopic();
        if (classParams.getContentFailureThreshold() > 0)
            this.contentFailureThreshold =classParams.getContentFailureThreshold();
        if (classParams.getTopicMastery() > 0)
            this.topicMastery =classParams.getTopicMastery();

        if (classParams.getTopicIntroFrequency() != null)
            this.topicIntroFrequency =classParams.getTopicIntroFrequency();
        if (classParams.getTopicExampleFrequency() != null)
            this.topicExampleFrequency =classParams.getTopicExampleFrequency();
        if (classParams.getLessonStyle() != null )
            this.lessonStyle = classParams.getLessonStyle();
        this.singleTopicMode = classParams.isSingleTopicMode();
        return this;

    }

    public TopicModelParameters overload (PedagogyParams userParams) {
        if (userParams == null)
            return this;
        if (userParams.isShowIntro())
            this.topicIntroFrequency= frequency.oncePerSession;
        if (userParams.getMode().equalsIgnoreCase("Example"))  {
            topicExampleFrequency = frequency.always;
        }
        else if (userParams.getMode().equalsIgnoreCase("Practice")) {
            topicExampleFrequency = frequency.never;
        }
        else {
            topicExampleFrequency = frequency.oncePerSession;
        }
        topicIntroFrequency = userParams.isShowIntro() ? frequency.oncePerSession : frequency.never;
        int minutes= (int) userParams.getMaxTime();  // userPedagogyParameters table has maxTime in minutes, we want MS
        maxTimeInTopic =minutes;
        minTimeInTopic = 0;
        maxNumberProbs = userParams.getMaxProbs();
        topicMastery = userParams.getTopicMastery();
        minNumberProbs = 1;
        this.singleTopicMode = userParams.isSingleTopicMode();
        // If we get passed no topic from Assistments, then this translates into setting the maxtime in the topic to 0
        // so we'll show the one forced problem and out.
        if (userParams.getTopicId() == -1)
            this.setMaxTimeInTopic(0);
        return this;
    }


    public TopicModelParameters(long maxTimeInTopic, int contentFailureThreshold, double topicMastery, int minNumberProbs,
                                long minTimeInTopic, int difficultyRate,  int maxNumberProblem
                               ) {
        this.maxNumberProbs = maxNumberProbs;
        this.maxTimeInTopic = maxTimeInTopic;
        this.contentFailureThreshold = contentFailureThreshold;
        this.topicMastery = topicMastery;
        this.minNumberProbs= minNumberProbs;
        this.minTimeInTopic= minTimeInTopic;
        this.difficultyRate= difficultyRate;
        this.topicIntroFrequency = DEFAULT_TOPIC_INTRO_FREQ;
        this.topicExampleFrequency = DEFAULT_EXAMPLE_FREQ;
    }

    // Called with parameters read from TeacherAdmin's class config
    public TopicModelParameters(long maxTimeInTopic, int contentFailureThreshold, double topicMastery, int minNumberProbs,
                                long minTimeInTopic, double difficultyRate,  int maxNumberProbs,
                                frequency topicIntroFreq, frequency exampleFreq,
                                String lessonStyle) {
        this.maxNumberProbs = maxNumberProbs;
        this.maxTimeInTopic = maxTimeInTopic;
        this.contentFailureThreshold = contentFailureThreshold;
        this.topicMastery = topicMastery;
        this.minNumberProbs= minNumberProbs;
        this.minTimeInTopic= minTimeInTopic;
        this.difficultyRate= difficultyRate;
        this.topicIntroFrequency = topicIntroFreq;
        this.topicExampleFrequency = exampleFreq;
        this.lessonStyle = lessonStyle;
    }

    public TopicModelParameters() {
        this.maxNumberProbs = MAX_NUM_PROBS_PER_TOPIC;
        this.maxTimeInTopic = MAX_TIME_IN_TOPIC;
        this.contentFailureThreshold = CONTENT_FAILURE_THRESHOLD;
        this.topicMastery = TOPIC_MASTERY;
        this.minNumberProbs=MIN_NUM_PROBS_PER_TOPIC;
        this.minTimeInTopic=MIN_TIME_IN_TOPIC;
        this.difficultyRate=DIFFICULTY_RATE;
        this.topicIntroFrequency = DEFAULT_TOPIC_INTRO_FREQ;
        this.topicExampleFrequency = DEFAULT_EXAMPLE_FREQ;
    }

    /**
     * This constructor is the one that is used to parse the XML in a the lesson definition.
     * @param controlElt
     */
    public TopicModelParameters (Element controlElt) {
        readControlParams(controlElt);
    }

    // Given a set of pm params that have default settings this reads the XML config and
    // overwrites any that are provided.
    private void readControlParams(Element p) {

        Element c;
        String s;


        c = p.getChild("maxTimeInTopicSecs")  ;
        if (c != null) {
            s = c.getValue();
            int maxTimeSecs = Integer.parseInt(s);
            this.setMaxTimeInTopicSecs(maxTimeSecs);
        }



        c = p.getChild("contentFailureThreshold");
        if (c != null) {
            s = c.getValue();
            int contentFailureThreshold = Integer.parseInt(s);
            this.setContentFailureThreshold(contentFailureThreshold);
        }

        c = p.getChild("topicMastery");
        if (c != null) {
            s = c.getValue();
            double topicMastery = Double.parseDouble(s);
            this.setTopicMastery(topicMastery);
        }

        c = p.getChild("minNumberProbs");
        if (c != null) {
            s = c.getValue();
            int minNumberProbs = Integer.parseInt(s);
            this.setMinNumberProbs(minNumberProbs);
        }

        c = p.getChild("maxNumberProbs");
        if (c != null) {
            s = c.getValue();
            int maxNumberProbs = Integer.parseInt(s);
            this.setMaxNumberProbs(maxNumberProbs);
        }

        c = p.getChild("minTimeInTopicSecs");
        if (c != null) {
            s = c.getValue();
            int minTimeInTopicSecs = Integer.parseInt(s);
            this.setMinTimeInTopicSecs(minTimeInTopicSecs);
        }

        c = p.getChild("difficultyRate");
        if (c != null) {
            s = c.getValue();
            int difficultyRate = Integer.parseInt(s);
            this.setDifficultyRate(difficultyRate);
        }


        // this will replace showTopicIntro
        c = p.getChild("topicIntroFrequency");
        if (c != null) {
            s = c.getValue();
            this.setTopicIntroFrequency(s);
        }

        // this will replace showExampleFirst
        c = p.getChild("topicExampleFrequency");
        if (c != null) {
            s = c.getValue();
            this.setTopicExampleFrequency(s);
        }

        // this will replace showExampleFirst
        c = p.getChild("interleavedProblemSets");
        if (c != null) {
            readInterleavedProblemSetConfig(c);
        }


    }

    /**
     * Given an element like
     * <interleavedProblemSet> <everyNTopics>N</everyNTopics> <numProbsInExploredTopic>8</numProbsInExploredTopic>
     *  <minTimeInExploredTopic>10</minTimeInExploredTopic> <numProbsPerTopic>2</numProbsPerTopic></interleavedProblemSet>
     * parse it into an object that holds the definition and place it inside the topicModelParams
     * @param p
     */
    private void readInterleavedProblemSetConfig(Element p) {
        Element c = p.getChild("everyNTopics");
        String s;
        int everyNTopics = -1;
        int exploredTopicMinTime=10;  // given in minutes
        int exploredTopicProbNum=10;
        int numProbsPerTopic=3;
        if (c != null) {
            s = c.getValue();
            everyNTopics = Integer.parseInt(s);
        }
        c = p.getChild("numProbsInExploredTopic");
        if (c != null) {
            s = c.getValue();
            exploredTopicProbNum = Integer.parseInt(s);

        }
        c = p.getChild("minTimeInExploredTopic");
        if (c != null) {
            s = c.getValue();
            exploredTopicMinTime = Integer.parseInt(s);

        }
        c = p.getChild("numProbsPerTopic");
        if (c != null) {
            s = c.getValue();
            numProbsPerTopic = Integer.parseInt(s);

        }

        InterleavedProblemSetParams iParams = new InterleavedProblemSetParams();
        iParams.setNumTopicsToWait(everyNTopics);
        iParams.setExploredProblemNum(exploredTopicProbNum);
        iParams.setExploredMinTime(exploredTopicMinTime);
        iParams.setNumProbsPerTopic(numProbsPerTopic);
        this.interleaveParams = iParams;

    }


    // gets the given TopicIntro frequency from a string
    public static frequency convertTopicIntroFrequency (String s) {
        if (s != null && !s.trim().equalsIgnoreCase(""))
            return frequency.valueOf(s);
        else return DEFAULT_TOPIC_INTRO_FREQ;
    }

    // gets the given TopicIntro frequency from a string
    public static frequency convertExampleFrequency (String s) {
        if (s != null && !s.trim().equalsIgnoreCase(""))
            return frequency.valueOf(s);
        else return DEFAULT_EXAMPLE_FREQ;
    }




    public int getMaxNumberProbs() {
        return maxNumberProbs;
    }

    public void setMaxNumberProbs(int maxNumberProbs) {
        this.maxNumberProbs = maxNumberProbs;
    }

    public long getMaxTimeInTopic() {
        return maxTimeInTopic;
    }

    public void setMaxTimeInTopic(long maxTimeInTopic) {
        this.maxTimeInTopic = maxTimeInTopic;
    }

    public int getMaxTimeInTopicMinutes () {
        return (int) (maxTimeInTopic / 60000);
    }


    public void setMaxTimeInTopicMinutes (double minutes) {
        maxTimeInTopic = (long) (minutes * 1000 * 60);
    }

    public void setMinTimeInTopicMinutes (double minutes) {
        minTimeInTopic = (long) (minutes * 1000 * 60);
    }

    public int getContentFailureThreshold() {
        return contentFailureThreshold;
    }

    public void setContentFailureThreshold(int contentFailureThreshold) {
        this.contentFailureThreshold = contentFailureThreshold;
    }

    public double getTopicMastery() {
        return topicMastery;
    }

    public void setTopicMastery(double topicMastery) {
        this.topicMastery = topicMastery;
    }

    public int getMinNumberProbs() {
        return minNumberProbs;
    }

    public long getMinTimeInTopic() {
        return minTimeInTopic;
    }

    public long getMinTimeInTopicMinutes() {
        return minTimeInTopic/60000;
    }

    public double getDifficultyRate() {
        return difficultyRate;
    }

    public void setDifficultyRate(int difficultyRate) {
        this.difficultyRate = difficultyRate;
    }





    public boolean isSingleTopicMode() {
        return singleTopicMode;
    }


    public String getCcss(){
        return ccss;
    }

    public void setMaxTimeInTopicSecs(int maxTimeInTopicSecs) {
        this.maxTimeInTopic = maxTimeInTopicSecs * 1000;
    }

    public void setMinNumberProbs(int minNumberProbs) {
        this.minNumberProbs = minNumberProbs;
    }



    public void setMinTimeInTopicSecs(int minTimeInTopicSecs) {
        this.minTimeInTopic = minTimeInTopicSecs * 1000;
    }

    public void setTopicIntroFrequency(String topicIntroFrequency) {
        this.topicIntroFrequency = TopicModelParameters.frequency.valueOf(topicIntroFrequency);
    }

    public frequency getTopicIntroFrequency() {
        return topicIntroFrequency;
    }

    public void setTopicExampleFrequency(String topicExampleFrequency) {
        this.topicExampleFrequency = TopicModelParameters.frequency.valueOf(topicExampleFrequency);
    }

    public frequency getTopicExampleFrequency() {
        return topicExampleFrequency;
    }


    public boolean isTopicLessonStyle() {
        return lessonStyle.equals("topics");
    }


    public void setLessonStyle(String lessonStyle) {
        this.lessonStyle = lessonStyle;
    }

    public String getLessonStyle() {
        return lessonStyle;
    }

    public boolean showInterleavedProblemSets () {
        return this.getInterleaveParams() != null;
    }

    public InterleavedProblemSetParams getInterleaveParams() {
        return interleaveParams;
    }
}