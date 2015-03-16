package edu.umass.ckc.wo.tutor.model;

import edu.umass.ckc.wo.content.Hint;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutormeta.HintSelector;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 3/16/15
 * Time: 5:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class TutorModelUtils {

    public void setupDemoProblem(Problem problem, SessionManager smgr, HintSelector hintSelector) throws Exception {
        hintSelector.init(smgr);
        // need to put in the solution since its an example
        List<Hint> soln = hintSelector.selectFullHintPath(smgr, problem.getId());
        problem.setSolution(soln);
        problem.setMode(Problem.DEMO);
    }
}
