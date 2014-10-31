package edu.umass.ckc.wo.content;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import edu.umass.ckc.wo.db.DbStudentProblemHistory;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.tutor.response.ProblemResponse;
import net.sf.json.*;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Jess
 * Date: 10/9/14
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */



public class ProblemParameters {
    List<Binding> bindings;

    public ProblemParameters() {
        this.bindings = null;
    }

    public ProblemParameters(String params) {
        JSONObject jParams = (JSONObject) JSONSerializer.toJSON(params);
        Iterator<String> keys = jParams.keys();
        this.bindings = new ArrayList<Binding>();

        boolean firstJArray = true; // We only want to initialize bindings if we are looping through a variable's possible parameters for the first time. This is how we know how many there are.
        while (keys.hasNext()) {
            List<String> possibleParams = new ArrayList<String>();
            String key = (String)keys.next();
            Object jObject = jParams.get(key);
            if (jObject instanceof JSONArray) {
                JSONArray jPossibleParams = (JSONArray) jObject;
                int count = 0;
                for (Object o : jPossibleParams) {
                    Binding b;
                    if (firstJArray) {
                        b = new Binding();
                        this.bindings.add(b);
                    }
                    else {
                        b = this.bindings.get(count);  // We make the assumption that all variables have the same number of possible parameterizations
                    }
                    b.addKVPair(key, o.toString());
                    possibleParams.add(o.toString());
                    ++count;
                }
                firstJArray = false;
            }
        }
    }

    public List<Binding> getBindings() {
        return bindings;
    }

    public Map<String, String> getRandomAssignment() {
        if (bindings == null || bindings.size() == 0) {
            return null;
        }
        Random randomGenerator = new Random();
        int randomIndex = randomGenerator.nextInt(bindings.size());
        return bindings.get(randomIndex).getMap();
    }

    private List<Binding> generateBindings(List<String> jsonSeenBindings) {
        List<Binding> usedBindings = new ArrayList<Binding>();
        for (String bind : jsonSeenBindings) {
            Binding b = new Binding();
            JSONObject jParams = (JSONObject) JSONSerializer.toJSON(bind);
            Iterator<String> keys = jParams.keys();
            while (keys.hasNext()) {
                String key = (String)keys.next();
                String param = jParams.get(key).toString();
                b.addKVPair(key, param);
            }
            usedBindings.add(b);
        }
        return usedBindings;
    }

    private List<Binding> getUnusedBindings(List<Binding> seenBindings) {
        List<Binding> unusedBindings = new ArrayList<Binding>();
        for (Binding binding : bindings) {
            int i = seenBindings.indexOf(binding);
            if (i == -1) {
                unusedBindings.add(binding);
            }
        }
        return unusedBindings;
    }


    public Binding getUnusedAssignment(int probId, int studId, Connection conn) throws SQLException {
        if (bindings == null || bindings.size() == 0 || conn == null) {
            return null;
        }
        Random randomGenerator = new Random();
        int randomIndex = -1;

        List<Binding> seenBindings = generateBindings(DbStudentProblemHistory.getSeenBindings(probId, studId, conn));
        List<Binding> unusedBindings = getUnusedBindings(seenBindings);
        if (unusedBindings.size() == 0) {
            randomIndex = randomGenerator.nextInt(bindings.size());
            return bindings.get(randomIndex);
        }
        else {
            randomIndex = randomGenerator.nextInt(unusedBindings.size());
            Binding chosenBinding = unusedBindings.get(randomIndex);
            return chosenBinding;
        }
    }

    // In parameterized problems, we would like the answer slot to be chosen randomly, so that the answer
    // for a given problem is not always, for instance, c.
    // Not all problems have an e answer, so e is not a candidate for switching.
    private String chooseAnswerPosition(String oldAnswer) {
        if (!(oldAnswer.equals("a") || oldAnswer.equals("b") || oldAnswer.equals("c") || oldAnswer.equals("d") || oldAnswer.equals("e"))) {
            return "";
        }
        String newAns = "";
        Random randomGenerator = new Random();
        int randomIndex = randomGenerator.nextInt(4);
        switch (randomIndex) {
            case(0):
              newAns = "a";
              break;
            case(1):
                newAns = "b";
                break;
            case(2):
                newAns = "c";
                break;
            case(3):
                newAns = "d";
                break;
        }
        return newAns;
    }

    public void addBindings(ProblemResponse r, int studId, Connection conn, StudentState state) throws SQLException {
        JSONObject rJson = r.getJSON();
        Binding unusedBinding = getUnusedAssignment(r.getProblem().getId(), studId, conn);
        String oldAnswer = r.getProblem().getAnswer();
        String newAnswer = chooseAnswerPosition(oldAnswer);
        saveAssignment(unusedBinding, newAnswer, state);
        JSONObject pJson = unusedBinding.getJSON(new JSONObject());
        r.setParams(pJson.toString());
        rJson.element("parameters", pJson);
        rJson.element("oldAnswer", oldAnswer);
        rJson.element("newAnswer", newAnswer);

    }
    private void saveAssignment(Binding b, String ans, StudentState state) throws SQLException {
        state.setProblemBinding(b.toString());
        state.setProblemAnswer(ans);
    }

    public boolean hasUnusedParametrization(int timesEncountered) {
        if (bindings != null && bindings.size() > timesEncountered) {
            return true;
        }
        return false;
    }


    public JSONObject getJSON(JSONObject jo, Map<String, String> bindings) {
        for(Map.Entry<String, String> entry : bindings.entrySet()){
            jo.element(entry.getKey(), entry.getValue());
        }
        return jo;
    }

    public static void main(String[] args) {
        String jsonString = "{\n" +
                "  \"$a\": [\"40\", \"40\"],\n" +
                "  \"$b\": [\"30\", \"30\"],\n" +
                "  \"$c\": [\"x\", \"45\"],\n" +
                "  \"$d\": [\"25\", \"x\"],\n" +
                "  \"$ans_A\": [\"65\", \"65\"],\n" +
                "  \"$ans_B\": [\"45\", \"45\"],\n" +
                "  \"$ans_C\": [\"50\", \"50\"],\n" +
                "  \"$ans_D\": [\"35\", \"35\"],\n" +
                "  \"$ans_E\": [\"45\", \"25\"]\n" +
                "}";

        String usedBinding = "{\n" +
                "\"$a\": \"40\",\n" +
                "\"$b\":\"30\",\n" +
                "\"$c\": \"x\",\n" +
                "\"$d\": \"25\",\n" +
                "\"$ans_A\": \"65\",\n" +
                "\"$ans_B\": \"45\",\n" +
                "\"$ans_C\": \"50\",\n" +
                "\"$ans_D\": \"35\",\n" +
                "\"$ans_E\": \"45\"\n" +
                "}";
        List<String> bindingStrings = new ArrayList<String>();
        bindingStrings.add(usedBinding);
        ProblemParameters parameters = new ProblemParameters(jsonString);
        List<Binding> b = parameters.generateBindings(bindingStrings);
        System.out.println(parameters.getUnusedBindings(b));
    }
}
