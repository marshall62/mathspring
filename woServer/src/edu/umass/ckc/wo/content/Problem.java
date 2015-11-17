package edu.umass.ckc.wo.content;


import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutormeta.Activity;
import edu.umass.ckc.wo.util.JSONUtil;
import edu.umass.ckc.wo.util.ProbPlayer;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


/**
 * A ProblemImpl Object represents the information out of the Problem table in the database.
 * This object is used to represent both SAT and Adventure problems.
 */

public class Problem implements Activity {

    public static QuestType parseType(String t) {
        if (t.equals("shortanswer"))
            return QuestType.shortAnswer;
        return QuestType.multiChoice;
    }




    public enum QuestType {
        multiChoice ,
        shortAnswer
    }
    public static final String DEMO = "demo";
    public static final String EXAMPLE = "example";
    public static final String PRACTICE = "practice";
    public static final String TOPIC_INTRO = "topicIntro";
    public static final String TESTABLE_STATUS = "testable";
    public static final String QUICK_AUTH="quickAuth";
    protected int id;
    protected String resource;
    private String answer;   // the letter of the multi-choice answer (a-e)
    private List<ProblemAnswer> answers;  // for short answer questions we have all possible answers  and multiple choice will be here too for quickAuth probs
    private QuestType questType; // multichoice or shortanswer
    private double diff_level ;
    private String form;
    private String name;
    private String nickname;
    private String instructions ;   //Instructions to the problem that go only with External URL Problems
    private String statementHTML;
    private String questionAudio;
    private String imageURL;
    private List<Hint> allHints;
    private int numHints=0;   // only one client of this class uses this field - AdaptiveProblemGroupProblemSelector
    private boolean hasStrategicHint=false;
    private String mode="practice";  // practice mode is the typical tutor hut mode - can also be "example"
    private String status="ready";  // most problems are ready
    private int exampleID; // an example problem that is used to prepare for this one
    private int[] topicIds; // ids of the problem groups this problems is in
    private List<Topic> topics;

    public static final String SAT_PROBLEM="satProblem";
    public static final String ADV_PROBLEM="advProblem";

    private boolean isExternalActivity=false;
    private int inTopicId =-1; // a special variable that says what topic this problem is being played in.   Only used when
                        // client forces a prob + topic.   This is then used to make sure logging shows the given topic

    private List<CCStandard> standards;
    protected String type = FLASH_PROB_TYPE;
    protected String topicName = "";
    protected String activityType;

    public static String FLASH_PROB_TYPE = "flash";
    public static String HTML_PROB_TYPE = "html5";
//    public static String FORMALITY_PROB_TYPE = "formality";
    public static String TOPIC_INTRO_PROB_TYPE = "TopicIntro";




    public static String EXTERNAL_MODE = "external";






    public List<Hint> solution;  // sometimes a problem must be given back to the server with a set of hints that represent the solution to the problem


    // Column names in the Problem table
    public static final String ID = "id";
    public static final String ANSWER = "answer";
    public static final String ANIMATION_RESOURCE = "animationResource";
    public static final String INSTRUCTIONS = "instructions" ;
    public static final String SOURCE = "form" ;
    public static final String NAME = "name";
    public static final String NICKNAME = "nickname";
    public static final String HAS_STRATEGIC_HINT = "strategicHintExists";
    public static final String FORM = "form";
    public static final String TYPE = "type";
    public static final String META_INFO = "metainfo";
    public static final String IS_EXTERNAL_ACTIVITY = "isExternalActivity";
    public static final String HAS_VARS = "hasVars";
    public static final String SCREENSHOT_URL = "screenShotURL";
    private String video=null;
    private boolean HTML5;
    private ProblemParameters params;
    private String ssURL;
    private String units = null;

    public String toString() {
        return "Prob id="+Integer.toString(id) + " rsc="+getResource();
    }

    public Problem () {}

    public Problem (int id) {
        this.id =id;
    }

    public Problem(int id, String resource, String answer, String name, String nickname,
                   boolean hasStrategicHint, double diff, int[] topicIds,
                   String form, String _instructions, String type, String status, HashMap<String, ArrayList<String>> vars, String ssURL,
                   QuestType questType, String statementHTML, String imageURL, String audioResource, String units)
    {
        this.id = id;
        this.resource = resource;
        if (answer != null)
            this.answer = answer.trim();
        this.name = name;
        this.nickname=nickname;
        this.hasStrategicHint= hasStrategicHint;
        this.diff_level=diff;
        this.topicIds = topicIds;
        this.allHints = new ArrayList<Hint>();
        this.form = form  ;
        this.instructions = _instructions ;
        if (type != null)
            this.type = type;
        this.status = status;
        if (vars != null)
            this.params = new ProblemParameters(vars);
        this.ssURL = ssURL;
        this.questType = questType;
        // The statementHTML is placed into a JSP file and if there are line breaks it creates a string that is a syntax error.
        this.statementHTML = JSONUtil.changeSpecialChars(statementHTML);
        this.imageURL = imageURL;
        this.questionAudio = audioResource;
        this.units = units;
    }

    /** Constructor used by ProblemMgr in the service of AdaptiveProblemGroupProblemSelector which wants to know how many
     * hints a problem has when it makes a selection. 
    */

    public Problem(int id, String resource, String answer) {
        this(id,resource,answer,null,null,false,0,null,null,null,null, "ready",null, null, QuestType.multiChoice, null, null, null, null);
    }

    public int getId () { return id; }

    public String getForm() { return form; }

    public String getInstructions () { return instructions ; }
    public void setInstructions (String instr) {
        this.instructions = instr;
    }

    public int[] getTopicIds() {
        return topicIds;
    }

    // N.B.  In quickAuth problems we don't use an animationResource.  So we return the problem name
    public String getResource() {
        if (isQuickAuth())
            return name;
        else return resource ;
    }

    public void setResource (String r) {
        this.resource = r;
    }




    public String logEventName() {
        return "satProblem";
    }

    public String getHintsJSONString () {
        JSONObject jo = new JSONObject();
        for (Hint hint : getHints()) {
            jo.accumulate("hints", hint.getJSON(new JSONObject()));
        }

        return jo.toString();
    }

    public String getAnswersJSONString () {
        JSONObject jo = new JSONObject();
        for (ProblemAnswer ans : getAnswers()) {
            jo.accumulate("answers", ans.getJSON(new JSONObject()));
        }

        return jo.toString();
    }



    public JSONObject buildJSON(JSONObject jo) {
        if (isExternalActivity())
            jo.element("activityType", "ExternalActivity");

        else if (type != null && type.equalsIgnoreCase(FLASH_PROB_TYPE))
            jo.element("activityType", Problem.FLASH_PROB_TYPE);
        else if (type != null &&  type.equalsIgnoreCase(HTML_PROB_TYPE))
            jo.element("activityType", Problem.HTML_PROB_TYPE);
        else
            jo.element("activityType",this.activityType);
        jo.element("id",this.id);
        jo.element("isExternalActivity",isExternalActivity());
        jo.element("mode", mode);
        jo.element("topicId",inTopicId);
        jo.element("topicName",topicName);
        jo.element("standards",getStandardsString(this.standards));
        jo.element("answer",answer);
        jo.element("numHints",getNumNonAnswerHints());
        jo.element("form",form);
        jo.element("type",type);
        jo.element("resource",getResource());
        jo.element("instructions",instructions);
        if (isQuickAuth()) {
            jo.element("statementHTML", statementHTML);
            jo.element("questionAudio", questionAudio);
            jo.element("questionImage", imageURL);
            jo.element("units", units);
            jo.element("questType",this.questType.name());
            for (Hint hint : getHints()) {
                jo.accumulate("hints", hint.getJSON(new JSONObject()));
            }
            if (isMultiChoice()) {
                for (ProblemAnswer ans : getAnswers()) {
                   jo.accumulate("answers", ans.getJSON(new JSONObject()));
                }
            }
        }
        if (solution != null) {
            for (Hint h : solution) {
                jo.accumulate("solution",h.getJSON(new JSONObject()));
            }
        }
//        if (params != null) {
//            Map<String, String> bindings = params.getRandomAssignment(); // This needs to be changed to access the studentproblemhistory somehow
//            JSONObject jsonObj = params.getJSON(new JSONObject(), bindings);
//            jo.element("parameters",jsonObj);
//
//        }
        return jo;

    }

    public String getUnits() {
        return units;
    }

    private int getNumNonAnswerHints() {
        int n=0;
        if (this.getHints() == null) return 0;
        for (Hint h: this.getHints())
            if (!h.getGivesAnswer())
                n++;
        return n;
    }

    private String getStandardsString(List<CCStandard> standards) {
        StringBuilder sb = new StringBuilder();
        if (standards != null) {
            for (CCStandard s: standards)
                sb.append(s.getCode() + ",");
            String v = sb.toString();
            if (v.length() > 0)
                return v.substring(0,v.length()-1); // get rid of trailing comma
            else return "";
        }
        else return "";
    }


    /**
   * getDifficulty
   *
   * @return double
   */
  public double getDifficulty() {
    return diff_level;
  }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public int getNumHints() {
        return numHints;
    }

    public void setNumHints(int numHints) {
        this.numHints = numHints;
    }

    public boolean hasStrategicHint() {
        return hasStrategicHint;
    }

    public String getAnswer() {
        return answer;
    }

    public void setMode (String mode) {
        this.mode = mode;
    }

    public String getMode () {
        return mode;
    }

    public boolean isProblemToSolve() {
        if (  ! isIntervention() )
            return true ;

        return false ;
    }

    public boolean isIntro() {
        String[] name_parts ;

        name_parts = getResource().split("_") ;
        if (name_parts.length < 2) {
            return false;
        }
        String name_digits = name_parts[1] ;
        if ( name_digits.startsWith("9"))
            return true ;

        return false ;
    }

    public boolean isIntervention() {
        if ( id < 0 )
            return true ;
        return false ;
    }

    public void setSolution (List<Hint> soln) {
        this.solution= soln;
    }

    public boolean isPractice() {
        return this.mode.equals(PRACTICE);
    }

    public boolean isExample() {
        return this.mode.equals(DEMO);
    }

    public boolean hasVideo() {
        return video!=null;
    }

    public boolean hasExample() {
        return this.exampleID != -1;
    }

    public void setHasStrategicHint (boolean b) {
        this.hasStrategicHint=b;
    }

    public int getExample() {
        return this.exampleID;
    }

    public void setExample(int exampleID) {
        this.exampleID = exampleID;
    }

    public void setHints(List<Hint> hints) {
        this.allHints = hints;
        this.numHints = hints.size();
    }

    public List<Hint> getHints() {
        // return a clone to make sure that if the user of the returned list mutates it,
        // then this won't be harmed.
        if (allHints != null)
            return (List<Hint>) ((ArrayList<Hint>) allHints).clone();
        else return null;
    }

    public void setVideo(String url) {
        this.video=url;
    }

    public String  getVideo () {
        return video;
    }



    public void setExternalActivity (boolean b) {
        this.isExternalActivity=b;
    }

    public boolean isExternalActivity () {
        return this.isExternalActivity;
    }



    public String getProbNumber () {
        return this.name.substring(this.name.indexOf("_")+1);
    }



    public double getDiff_level() {
        return diff_level;
    }

    public void setDiff_level(double diff_level) {
        this.diff_level = diff_level;
    }

    public int getInTopicId() {
        return inTopicId;
    }

    public void setInTopicId(int inTopicId) {
        this.inTopicId = inTopicId;
    }

    public void setInTopicName (String topicName) {
        this.topicName = topicName;
    }

    public List<CCStandard> getStandards() {
        return standards;
    }

    public String getStandardsString () {
        return getStandardsString(getStandards());
    }

    public void setStandards(List<CCStandard> standards) {
        this.standards = standards;
    }

    public boolean isHTML() {
        return type.equalsIgnoreCase(HTML_PROB_TYPE);
    }

    public boolean isFlash() {
        return type.equalsIgnoreCase(FLASH_PROB_TYPE);
    }

    public void setType (String type) {
        this.type = type;
    }

    public String getType () {
        return this.type;
    }

    public String getHTMLDir () {
        String rsc = getResource();


        // If a resource is present but isn't a legit filename, make it be .html by default
        if (rsc.indexOf('.') == -1) {
            rsc +=".html";
        }
        // We assume the name of the html file and directory it lives in must be the same so we can
        // just return the name of the file minus its extension
        return rsc.substring(0,rsc.indexOf('.'));
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
        this.topicIds = new int[topics.size()];
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            topicIds[i]= topic.getId();
        }

    }

    public List<ProblemAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ProblemAnswer> answers) {
        this.answers = answers;
    }

    public static boolean isPracticeProblem (String mode) {
        return mode != null && mode.equals(PRACTICE) ;
    }

    public static boolean isExampleOrDemo (String mode) {
        return mode.equalsIgnoreCase(EXAMPLE) || mode.equalsIgnoreCase(DEMO);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public ProblemParameters getParams() {
        return params;
    }

    public void setParams(HashMap<String, ArrayList<String>> params) {
        this.params = new ProblemParameters(params);
    }

    public boolean isParametrized() {
        if (params == null || params.getBindings() == null) {
            return false;
        }
        return true;
    }

    public String getScreenshotUrl() {
        return ssURL;
    }

    public boolean isQuickAuth () {
        return form != null && form.equals(QUICK_AUTH);
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getStatementHTML() {
        return statementHTML;
    }

    public void setStatementHTML(String statementHTML) {
        this.statementHTML = statementHTML;
    }

    public String getQuestionAudio() {
        return questionAudio;
    }

    public String getQuestType () {
        return questType.name();
    }

    public void setQuestionAudio(String questionAudio) {
        this.questionAudio = questionAudio;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getPreviewerURL () {
        if (this.isFlash())
            return ProbPlayer.getURLToProbPlayer() + "?questionNum=" + this.getProbNumber();
        else if (this.isHTML())
            // the global var has a trailing /   , then we put in the directory, finally the filename
            return Settings.html5ProblemURI + this.getHTMLDir() + "/" + this.getResource();
        else return "";
    }

    public static void main(String[] args) {
        Problem p = new Problem(1,"problem_102","c","pname","nname",false,0.4,new int[] {1,2}, "Flash","instructions are dumb", "Flash", "ready",null, null, QuestType.multiChoice, null, null, null, null);
        Hint h1 = new Hint(3,"hi");
        Hint h2 = new Hint(4,"there");
        List<Hint> hints = new ArrayList<Hint>();
        hints.add(h1);
        hints.add(h2);
        p.setSolution(hints);
        String vars ="{\"$a\": [\"40\", \"40\"],\"$\b\": [\"30\", \"30\"],\"$c\": [\"x\", \"45\"],\"$d\": [\"25\", \"x\"],\"$ans_A\": [\"65\", \"65\"],\"$ans_B\": [\"45\", \"45\"],\"$ans_C\": [\"50\", \"50\"],\"$ans_D\": [\"35\", \"35\"],\"$ans_E\": [\"45\", \"25\"]}";
//        p.setParams(vars);
        System.out.println("JSON for problem is " + p.buildJSON(new JSONObject()));
    }

    public boolean isTestProblem() {
        return status.equals(TESTABLE_STATUS);
    }



    public boolean isShortAnswer () {
        return this.questType == QuestType.shortAnswer;
    }

    public boolean isMultiChoice () {
        return this.questType == QuestType.multiChoice;
    }


}
