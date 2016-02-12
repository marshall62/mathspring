package edu.umass.ckc.wo.interventions.lc;

import edu.umass.ckc.wo.db.DbPedagogy;
import edu.umass.ckc.wo.event.SessionEvent;
import edu.umass.ckc.wo.event.tutorhut.*;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.state.StudentState;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutor.studmod.StudentProblemData;
import edu.umass.ckc.wo.tutor.studmod.StudentProblemHistory;
import edu.umass.ckc.wo.tutormeta.StudentModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 1/25/16
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class LCAccessors {
    private SessionManager smgr;
    private Connection conn;
    private StudentState state;
    private StudentModel studentModel;
    private StudentProblemHistory probSolveHist;
    private SessionEvent event ; // the current event sent in by user

    /**
     * Creates the Object with a session manager instance and the current user event.   Must be done prior to calling
     * eval method to evaluate rules.
     * @param smgr
     * @param e
     */
    public LCAccessors(SessionManager smgr, SessionEvent e) {
        this.smgr = smgr;
        this.conn = smgr.getConnection();
        this.state = smgr.getStudentState();
        this.studentModel = smgr.getStudentModel();
        this.probSolveHist = studentModel.getStudentProblemHistory();
    }

    public Object eval (LCFn fn) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class c = this.getClass();
        Method m = c.getDeclaredMethod(fn.getFnName(),fn.getArgs());

        return m.invoke(this, this.event);
    }

    /**
     * Gets the mastery in the current topic.
     * @return
     */
    public double curTopicMastery (SessionEvent ev)  {
        try {
            return studentModel.getTopicMastery(state.getCurTopic());
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return 0.01;   // protection against exceptions and/or divide by zero errors.
        }
    }

    /**
     * Return true if the current user event is an attempt
     * @param ev
     * @return
     */
    public boolean isAttemptEvent  (SessionEvent ev) {
        return (ev instanceof AttemptEvent);
    }

    public boolean isHintEvent  (SessionEvent ev) {
        return (ev instanceof HintEvent);
    }

    public boolean isExampleEvent  (SessionEvent ev) {
        return (ev instanceof ShowExampleEvent);
    }

    public boolean isVideoEvent  (SessionEvent ev) {
        return (ev instanceof ShowVideoEvent);
    }

    public boolean isReadProblemEvent (SessionEvent ev) {
        return (ev instanceof ReadProblemEvent);
    }

    /**
     * Get the number of incorrect attempts in the current problem.
     * @return
     */
    public int curProbNumIncorrectAttempts (SessionEvent ev) {
        return state.getNumMistakesOnCurProblem();
    }

    /**
     * Get the number of attempts it took to solve the problem.  -1 indicates problem hasn't been solved.
     * @return
     */
    public int solvedOnAttempt (SessionEvent ev) {
        if (state.getTimeToSolve() > 0)
            return state.getNumMistakesOnCurProblem() + 1;
        return -1;
    }

    /**
     * Get how long it takes to solve the current problem. -1 indicates not solved.
     * @return
     */
    public long timeToSolve (SessionEvent ev) {
        return state.getTimeToSolve();
    }


    /**
     * Get the number of milliseconds it took for the user to make an attempt on the current problem.  -1 indicates no attempt made.
     * @return
     */
    public long curProbTimeToFirstAttempt (SessionEvent ev) {
        return state.getTimeToFirstAttempt();
    }

    /**
     * Get the effort of the nth problem where n is a number indicating how many problems to go back.
     * 1 indicates the last problem the student saw.
     *
     * @param n
     * @return The effort or empty string "" if nothing can be found
     */
    public String historyEffortN (int n) {
        int c = 1;
        String e = "";
        for (StudentProblemData d : probSolveHist.getReverseHistory()) {
            if (c == n) {
                e = d.getEffort();
                break;
            }
            c++;
        }
        return (e != null) ? e : "";
    }


    public static void main(String[] args) {
        try {
            Connection conn = SessionManager.getAConnection();
            Settings.lessonMap = DbPedagogy.buildAllLessons(conn);
            Settings.loginMap = DbPedagogy.buildAllLoginSequences(conn);
            Settings.pedagogyGroups = DbPedagogy.buildAllPedagogies(conn);
            SessionManager smgr = new SessionManager(conn);
            smgr.attemptSessionCreation("dm","dm",System.currentTimeMillis(),true);

            System.out.println("Testing LCAccessors with user dm/dm studId="+ smgr.getStudentId());
            LCAccessors fns = new LCAccessors(smgr,null);
            String eff = fns.historyEffortN(1);
            System.out.println("Last effort was " + eff);
            eff = fns.historyEffortN(2);
            System.out.println("Second to last effort was " + eff);

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
