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


    private frequency topicIntroFrequency;    //  These frequencies are no longer set in the lesson
    private frequency topicExampleFrequency;  // They are now set in the Interventions that are part of the lesson
    private String ccss;
    private String lessonStyle;
    private boolean singleTopicMode;
    private InterleavedProblemSetParams interleaveParams=null;  // If interleaved problem sets are part of the lesson, this will be non-null

    // overload the params of this with those given for class.
    public LessonModelParameters overload(LessonModelParameters theClassParams) {
        TopicModelParameters classParams = (TopicModelParameters) theClassParams;
        if (classParams == null) return this;
        if (classParams.getDifficultyRate() > 0)
            this.difficultyRate =classParams.getDifficultyRate();
        if (classParams.getMaxProbs() > 0)
            this.maxProbs =classParams.getMaxProbs();
        if (classParams.getMinProbs() > 0)
            this.minProbs =classParams.getMinProbs();
        // class params has maxTime given in MS.
        if (classParams.getMaxTimeMs() > 0)
            this.maxTimeMs =classParams.getMaxTimeMs();
        // class params has minTime in MS
        if (classParams.getMinTimeMs() > 0)
            this.minTimeMs = classParams.getMinTimeMs();
        if (classParams.getContentFailureThreshold() > 0)
            this.contentFailureThreshold =classParams.getContentFailureThreshold();
        if (classParams.getDesiredMastery() > 0)
            this.desiredMastery =classParams.getDesiredMastery();
        if (classParams.getLessonStyle() != null )
            this.lessonStyle = classParams.getLessonStyle();
        this.singleTopicMode = classParams.isSingleTopicMode();
        
        // frequency is no longer relevant since they are set in the lessons interventions rather than lesson config
        if (classParams.getTopicIntroFrequency() != null)
            this.topicIntroFrequency =classParams.getTopicIntroFrequency();
        if (classParams.getTopicExampleFrequency() != null)
            this.topicExampleFrequency =classParams.getTopicExampleFrequency();

        return this;

    }

    // only used for ASssistments users who have params saved on a per user basis
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
        setMaxTimeMinutes((int) userParams.getMaxTime());  // userPedagogyParameters table has maxTime in minutes, we want MS
        this.maxProbs = userParams.getMaxProbs();
        this.desiredMastery = userParams.getMastery();
        this.minProbs = 1;
        this.singleTopicMode = userParams.isSingleTopicMode();
        // If we get passed no topic from Assistments, then this translates into setting the maxtime in the topic to 0
        // so we'll show the one forced problem and out.
        if (userParams.getTopicId() == -1)
            this.setMaxTimeMs(0);
        return this;
    }



    // Called with parameters read from TeacherAdmin's class config
    public TopicModelParameters(long maxTimeInTopic, int contentFailureThreshold, double topicMastery, int minNumberProbs,
                                long minTimeInTopic, double difficultyRate,  int maxNumberProbs,
                                frequency topicIntroFreq, frequency exampleFreq,
                                String lessonStyle) {
        this.maxProbs = maxNumberProbs;
        this.maxTimeMs = maxTimeInTopic;
        this.contentFailureThreshold = contentFailureThreshold;
        this.desiredMastery = topicMastery;
        this.minProbs= minNumberProbs;
        this.minTimeMs= minTimeInTopic;
        this.difficultyRate= difficultyRate;
        this.topicIntroFrequency = topicIntroFreq;
        this.topicExampleFrequency = exampleFreq;
        this.lessonStyle = lessonStyle;
    }

    public TopicModelParameters(boolean setDefaultValues) {
        if (setDefaultValues) {
            this.maxProbs = MAX_NUM_PROBS_PER_TOPIC;
            this.maxTimeMs = MAX_TIME_IN_TOPIC;
            this.contentFailureThreshold = CONTENT_FAILURE_THRESHOLD;
            this.desiredMastery = TOPIC_MASTERY;
            this.minProbs = MIN_NUM_PROBS_PER_TOPIC;
            this.minTimeMs = MIN_TIME_IN_TOPIC;
            this.difficultyRate = DIFFICULTY_RATE;
            this.topicIntroFrequency = DEFAULT_TOPIC_INTRO_FREQ;
            this.topicExampleFrequency = DEFAULT_EXAMPLE_FREQ;
        }
        else {
            this.maxProbs = -1;
            this.maxTimeMs = -1;
            this.contentFailureThreshold = -1;
            this.desiredMastery = -1;
            this.minProbs = -1;
            this.minTimeMs = -1;
            this.difficultyRate = -1;
            this.topicIntroFrequency = null;
            this.topicExampleFrequency = null;
        }
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
            this.setDesiredMastery(topicMastery);
        }

        c = p.getChild("minNumberProbs");
        if (c != null) {
            s = c.getValue();
            int minNumberProbs = Integer.parseInt(s);
            this.setMinProbs(minNumberProbs);
        }

        c = p.getChild("maxNumberProbs");
        if (c != null) {
            s = c.getValue();
            int maxNumberProbs = Integer.parseInt(s);
            this.setMaxProbs(maxNumberProbs);
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
            double difficultyRate = Double.parseDouble(s);
            this.setDifficultyRate(difficultyRate);
        }


        // Setting frequency here no longer has an effect since frequency is set in the TopicIntroIS rather than lesson config
        c = p.getChild("topicIntroFrequency");
        if (c != null) {
            s = c.getValue();
            this.setTopicIntroFrequency(s);
        }

        // Setting frequency here no longer has an effect since frequency is set in the ShowDemoIS rather than lesson config
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
        int numTopicsToReview=3;
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
        c = p.getChild("numTopicsToReview");
        if (c != null) {
            s = c.getValue();
            numTopicsToReview = Integer.parseInt(s);

        }

        InterleavedProblemSetParams iParams = new InterleavedProblemSetParams();
        iParams.setNumTopicsToWait(everyNTopics);
        iParams.setExploredProblemNum(exploredTopicProbNum);
        iParams.setExploredMinTime(exploredTopicMinTime);
        iParams.setNumProbsPerTopic(numProbsPerTopic);
        iParams.setNumTopicsToReview(numTopicsToReview);
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


    public boolean isSingleTopicMode() {
        return singleTopicMode;
    }


    public String getCcss(){
        return ccss;
    }

    public void setMaxTimeInTopicSecs(int maxTimeInTopicSecs) {
        this.maxTimeMs = maxTimeInTopicSecs * 1000;
    }



    public void setMinTimeInTopicSecs(int minTimeInTopicSecs) {

        this.minTimeMs = minTimeInTopicSecs * 1000;
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