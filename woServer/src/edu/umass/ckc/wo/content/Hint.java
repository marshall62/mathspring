package edu.umass.ckc.wo.content;

import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.tutor.Settings;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import java.sql.SQLException;

public class Hint  {
    private int id;
    private String label; // a label within the problem Flash or Edge  program
    private transient int problemId;     // the fields marked transient are not serialized into a JSON output
    private transient boolean is_root ;
    private transient boolean is_visual ;
    private transient boolean givesAnswer;

    // Column names in the Hint table
    public static final String ID = "id";
    public static final String PROBLEM_ID = "problemId";
    public static final String NAME = "name"; // also serves as the label within the Flash
    public static final String GIVES_ANSWER = "givesAnswer";
    public static final String IS_ROOT = "is_root";
    public static final String ATTRIBUTE = "attribute" ;
    public static final String ATT_VALUE = "value" ;

    public static final String STRATEGIC_HINT_LABEL = "strategic_hint";
    // a constant hint object which is the strategic hint for any problem.   Note it must have an id that is not -1
    // so that the hint selector will think that a real hint was given .
    public static final Hint STRATEGIC_HINT  = new Hint(-113,STRATEGIC_HINT_LABEL,-1,false,false);
    public static final Hint NO_STRATEGIC_HINT  = new Hint(-114,"noStrategicHint",-1,false,false);
    public static final Hint STRATEGIC_HINT_PLAYED  = new Hint(-115,"strategicAlreadyPlayed",-1,false,false);

    private String statementHTML;
    private String audioResource;
    private String hoverText;

    // This only exists to represent Hint information about a Hint event in 4mality.
    public Hint (int id, String label) {
        this.id = id;
        this.label = label;
    }

    public Hint(int id, String label, int problemId, boolean givesAnswer, boolean is_root) {
        this.id=id;
        this.label=label;
        this.is_root=is_root;
        this.problemId=problemId;
        this.givesAnswer=givesAnswer;
    }

    public Hint(int id, String label, int problemId, boolean givesAnswer, boolean is_root, String statementHTML, String audioResource, String hoverText)  {
        this.id=id;
        this.label=label;
        this.is_root=is_root;
        this.problemId=problemId;
        this.givesAnswer=givesAnswer;
        this.is_visual = true ;
        this.statementHTML = statementHTML;
        this.audioResource = audioResource;
        this.hoverText = hoverText;
    }


    public JSONObject buildJSON(JSONObject jo) {
        return jo;
    }


    public JSONObject getJSON (JSONObject jo) {
        jo.element("id",this.id);
        jo.element("label",this.label);
        Problem p = null;
        try {
            p = ProblemMgr.getProblem(getProblemId());
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (p != null && p.isQuickAuth()) {
            jo.element("statementHTML", statementHTML);
            jo.element("audioResource", audioResource);
            jo.element("hoverText", hoverText);
        }

        return jo;
    }


    public void setLabel (String label) {
        this.label = label;
    }

    public String getLabel() {
      return this.label;
    }

    public int getId() {
      return this.id;
    }

    public boolean isRoot() {
      return this.is_root;
    }

    public boolean isVisual() {
      return this.is_visual;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public int getProblemId() {
        return this.problemId;
    }

    public boolean getGivesAnswer () {
        return this.givesAnswer;
    }

    public static boolean isBottomOut(String name) {
        return name.startsWith("choose");
    }

    public void setIs_root(boolean is_root) {
        this.is_root = is_root;
    }

    public static void main(String[] args) {
        Hint h = new Hint(10,"myLab",345,true,false);
        String json = h.getJSON(new JSONObject()).toString();
        System.out.println("Hint json is " + json);
    }
}