package edu.umass.ckc.wo.tutor.hintSel;

import edu.umass.ckc.wo.content.Hint;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.event.StudentActionEvent;
import edu.umass.ckc.wo.smgr.SessionManager;

import edu.umass.ckc.wo.interventions.SelectHintSpecs;
import edu.umass.ckc.wo.tutormeta.HintSelector;
import ckc.servlet.servbase.UserException;
import edu.umass.ckc.wo.cache.ProblemMgr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Nov 6, 2008
 * Time: 3:38:09 PM
 */
public abstract class BaseHintSelector implements HintSelector {
    protected Problem problem;
    protected Connection conn_;
    protected SessionManager smgr;

    // The three following methods are only present because this implements HintSelector.   All subclasses of this
    // MUST override these methods.

    /**
     * This must be overriden by subclasses
     *
     * @param smgr
     * @param e
     * @return
     * @throws Exception
     */
    public abstract Hint selectHint(SessionManager smgr, StudentActionEvent e) throws Exception;

    /**
     * This must be overriden by subclasses
     *
     * @param smgr
     * @return
     * @throws Exception
     */
    public abstract Hint selectHint(SessionManager smgr) throws Exception;

    /**
     * This must be overriden by subclasses
     *
     * @param smgr
     * @param selectionCriteria
     * @return
     * @throws Exception
     */
    public abstract Hint selectHint(SessionManager smgr, SelectHintSpecs selectionCriteria) throws Exception;


    /**
     * Subclasses must override this method
     *
     * @param possibleHints
     * @param studId
     * @return
     * @throws Exception
     */
    protected Hint pickBranchHint(List<Hint> possibleHints,
                                  int studId) throws Exception {
        return null;
    }


    protected Hint getNextHintInSequence(List<Hint> hints, int probId, int prevHint, int studId, int groupId) throws Exception {
        List<Hint> possibleSuccessorHints = getPossibleSuccessorHints(probId, prevHint, hints, true);
        if (possibleSuccessorHints.size() == 0)
            // There are no more "next" hints, terminate with null
            return null;

        if (possibleSuccessorHints.size() == 1)
            // There is only 1 hint, so just return that one
            return possibleSuccessorHints.get(0);

        // return one of the possible.   Currently this always returns a hint that is ALGEBRAIC.
        return pickBranchHint(possibleSuccessorHints, studId);
    }

    /**
     * Going from the last hint given (maybe none for this problem), this will return a sequence of hints
     * that solves the problem.   If there is any branching in the hint path, this uses the pickOneHint
     * method which is part of this strategy to choose which branch to go down.   N.B. pickOneHint is currently
     * hardwired to go down only algebraic paths.
     *
     * @param smgr
     * @return
     * @throws Exception
     */
    public List<Hint> selectRemainingHints(SessionManager smgr) throws Exception {
        return selectHintPath(smgr, smgr.getStudentState().getCurProblem(), false);

    }

    public List<Hint> selectFullHintPath(SessionManager smgr, int probId) throws Exception {
        return selectHintPath(smgr, probId, true);
    }

    private List<Hint> selectHintPath(SessionManager smgr, int probId, boolean fromBeginning) throws Exception {
        int lastHintId;
        if (!fromBeginning)
            lastHintId = smgr.getStudentState().getCurHintId();
        else lastHintId = -1;
        int groupId = -1;
        // hint selections really don't do anything about multiple paths anymore so enforcing a groupId is unecessary
//        try {
//            groupId = state.getGroupNum();
//        } catch (Exception exc) {
//            throw new UserException("There is no groupId recorded. You probably should not be using PercentageHintSelector");
//        }
        List<Hint> allHints = getHintsForProblem(probId);
        List<Hint> hintSequence = new ArrayList<Hint>();
        Hint h = getNextHintInSequence(allHints, probId, lastHintId, smgr.getStudentId(), groupId);
        if (h != null)
            lastHintId = h.getId();
        while (h != null) {
            hintSequence.add(h);
            h = getNextHintInSequence(allHints, probId, lastHintId, smgr.getStudentId(), groupId);
            if (h != null)
                lastHintId = h.getId();
        }
        return hintSequence;
    }



    public void init(SessionManager smgr) throws Exception {
        conn_ = smgr.getConnection();
//        state = new SelectorState();
//        state.load(conn_, smgr.getStudentId());
        int probId = smgr.getStudentState().getCurProblem();
        problem = new DbProblem().getProblem(conn_, probId);
        this.smgr = smgr;

    }


    // Get a hint from the List of hints for this problem from the ID in the database
    protected Hint getHint(List<Hint> hints, int id) {
        for (Hint h: hints) {
            if (h.getId() == id)
                return h;
        }
        return null;
    }


    /**
     * Gets all the hints associated with the problem
     *
     * @param probId
     * @return
     * @throws Exception
     */
    protected List<Hint> getHintsForProblem(int probId) throws Exception {
        // the old way is to fetch the hints from the db.
        //return DbHint.getHintsForProblem(conn_,probId);

        // hints are now loaded into the problems that have all been loaded at servlet start time
        Problem p = ProblemMgr.getProblem(probId);
        if (p != null)
            return p.getHints();
        else return new ArrayList<Hint>(); // return an empty list when there are no hints or no problem
    }

    /**
     * Given the last hint (which may be -1) return the possible next hints.   If there are multiple
     * hint paths that fork after the last hint, this returns a list of more than 1 hint.  Usually, it just
     * returns 1 hint because there is usually no fork.
     *
     * The includeAnswerGivingHint is so that we can turn off the final hint in some cases which merely
     * says to "choose answer C" or the like.
     *
     * @param probId
     * @param lastHintId
     * @param includeAnswerGivingHint
     * @return
     * @throws Exception
     */
    protected List<Hint> getPossibleSuccessorHints(int probId, int lastHintId, List<Hint> hints, boolean includeAnswerGivingHint) throws Exception {
        List<Hint> possibleHints = new ArrayList<Hint>();
        // if no last hint, then add only the root hint to list of possible.
        if (lastHintId == -1) {
            for (int i = 0; i < hints.size(); i++) {
                if (((Hint) (hints.get(i))).isRoot()) {
                    possibleHints.add(hints.get(i));
                }
            }
            return possibleHints;
        } else {
            // if a previous hint was shown and there is only one next hint
            // (i.e. no branch in tree from the last hint) then select this hint.  If
            // there is a branch, then add all children of last hint to possible.
            //SqlQuery q = new SqlQuery();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String q = "select targetHint from SolutionPath " +
                        " where sourceHint = " + lastHintId;
                ps = conn_.prepareStatement(q);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int newhint_id = rs.getInt("targetHint");
                    Hint h = getHint(hints, newhint_id);
                    // DM 2/24/11  By request.   We no longer want to give out hints that say
                    // "Choose C" or the like.  There may be some hints that give some information
                    // about how to solve the problem AND tell which choice should be selected (these
                    // are typically hints that are root hints and marked as givesAnswer=true - these
                    // must still be given out or the system will have no way of helping students on these problems
                    // Because the example system would like to show the full hint path (including which answer
                    // to select) this function is called with a flag (by default it will be true).
                    // we don't want hints that give the answer (unless they are root hints)
                    if (!includeAnswerGivingHint && h.getGivesAnswer() && !h.isRoot() && !Problem.isExampleOrDemo(smgr.getStudentState().getCurProblemMode()))
                        ;
                    else possibleHints.add(h);
                }
                return possibleHints;
            }
            finally {
                if (ps != null)
                    ps.close();
                if (rs != null)
                    rs.close();
            }
        }
    }



}
