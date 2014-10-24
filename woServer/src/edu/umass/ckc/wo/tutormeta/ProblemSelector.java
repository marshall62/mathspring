package edu.umass.ckc.wo.tutormeta;


import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.smgr.StudentState;
import edu.umass.ckc.wo.interventions.SelectProblemSpecs;
import edu.umass.ckc.wo.event.tutorhut.NextProblemEvent;
import edu.umass.ckc.wo.tutor.pedModel.ProblemGrader;
import edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters;

import java.util.List;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface ProblemSelector {


    public void init (SessionManager smgr) throws Exception;

    void setParameters(PedagogicalModelParameters params);

    public Problem selectProblem(SessionManager smgr, NextProblemEvent e, ProblemGrader.difficulty nextProblemDesiredDifficulty) throws Exception;
}