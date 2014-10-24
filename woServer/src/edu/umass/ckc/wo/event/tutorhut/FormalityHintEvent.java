package edu.umass.ckc.wo.event.tutorhut;

import ckc.servlet.servbase.ServletParams;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: 7/5/11
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class FormalityHintEvent extends HintEvent {
    private String userInput;
    public static final String USER_INPUT = "userInput";
    private int probId;
    public static final String PROB_ID = "probId";

    public FormalityHintEvent(ServletParams p) throws Exception {
        super(p);
        // the userInput param contains 4MalityCoachName.hintID
        userInput= p.getString(USER_INPUT,null);
        probId = p.getInt(PROB_ID,-1);
    }

    public String getUserInput() {
        return userInput;
    }

    public int getHintId () {
        return Integer.parseInt(userInput.substring(userInput.indexOf('.') + 1));
    }

    public int getProbId() {
        return probId;
    }
}