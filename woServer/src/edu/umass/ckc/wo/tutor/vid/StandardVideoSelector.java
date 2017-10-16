package edu.umass.ckc.wo.tutor.vid;


import edu.umass.ckc.wo.cache.ProblemMgr;
import edu.umass.ckc.wo.content.CCStandard;
import edu.umass.ckc.wo.content.Problem;
import edu.umass.ckc.wo.db.DbProblem;
import edu.umass.ckc.wo.db.DbUtil;
import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.Settings;
import edu.umass.ckc.wo.tutormeta.VideoSelector;
import edu.umass.ckc.wo.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Oct 16 2017
 * Time: 12:52 PM
 *
 * Requested by Ivon.  Selects a video for a problem based on CCSS and difficulty.
 * If the cur problem doesn't have a video, find a problem with the same standard and the similar difficulty level that does
 * have a video.
 */
public class StandardVideoSelector implements VideoSelector {


    public void init(SessionManager smgr) throws Exception {
    }


    /**
     * Return the video with the most number of CCSS standards in common and the closest difficulty to the target problem.
     * @param conn
     * @param targetProbId
     * @return
     * @throws SQLException
     */
    public String selectVideo(Connection conn, int targetProbId) throws SQLException {
//        return "http://chinacat.cs.umass.edu/wayang/video/CorrespondingAnglesHard.flv";
        DbProblem pmgr = new DbProblem();
//        Problem p = pmgr.getProblemWithDifficulty(conn,curProbId) ;
        Problem p = ProblemMgr.getProblem(targetProbId);
        if (p.hasVideo())
            return p.getVideo();
        double targetDiff = p.getDiff_level();
        List<CCStandard> standards = p.getStandards();

        HashMap<String, List<Problem>> m = new HashMap<>();
        // map standards to lists of problems that have videos and that contain that standard
        for (CCStandard s : standards) {
            List<Problem> probs = ProblemMgr.getStandardProblems(conn, s.getCode());
            // get rid of problems that don't have videos
            Iterator itr = probs.iterator();
            while (itr.hasNext()) {
                Problem px = (Problem) itr.next();
                if (!px.hasVideo())
                    itr.remove();
            }
            // if there are problems in the standard with videos put them in the map
            if (probs.size() > 0)
                m.put(s.getCode(), probs);

        }
        // If no problems had videos or if there were none with matching standards, exit
        if (m.values().size() == 0) return null;

        Set s1 = m.keySet(); // s1 is the set of the standards in the given problem
        // Try to find a problem that shares the most standards with the current problem.
        // This will be the problem with the largest intersection.
        Collection<List<Problem>> vals = m.values();
        int max = 0;
        Stack<Pair<Integer, Problem>> bestMatches = new Stack<Pair<Integer, Problem>>();
        for (List<Problem> probsOfStd : vals) {
            for (Problem p2 : probsOfStd) {
                Set s2 = p2.getStandardsStringSet();
                s2.retainAll(s1); // turns s2 into the intersection of s2 and
                bestMatches.push(new Pair(s2.size(), p2));
            }
        }
        // its possible that all the problems were removed because they had no videos, so the stack will be empty.
        if (bestMatches.isEmpty())
            return null;
        // We now have a stack of pairs that tell us how many standards were in common for each problem.
        // We want to find the one with the most number in common and the lowest difference in difficulties.
        // So sort the pairs by number of standards.
        bestMatches.sort(new Comparator<Pair<Integer, Problem>>() {
            @Override
            public int compare(Pair<Integer, Problem> o1, Pair<Integer, Problem> o2) {
                if (o1.getP1() < o2.getP1())
                    return -1;
                else if (o1.getP1() > o2.getP1())
                    return 1;
                else return 0;
            }
        });
        int possible = bestMatches.size();
        Pair<Integer, Problem> best = bestMatches.pop();
        double bestdiff = Math.abs(targetDiff - best.getP2().getDiff_level());
        while (!bestMatches.isEmpty()) {
            Pair<Integer, Problem> n = bestMatches.pop();
            // if there are less standards in common in this problem than the best problem, quit
            if (best.getP1() > n.getP1())
                break;
                // the two problems have the same number of standards in common.  Check to see if this problem
                // has a better match of difficulty with the target problem than our previous best problem.
            else {
                double diff2 = Math.abs(targetDiff - n.getP2().getDiff_level());
                if (diff2 < bestdiff) {
                    best = n;
                    bestdiff = diff2;
                }
            }
        }
        Problem winner = best.getP2();
        System.out.println("Picked best from " + possible + " candidates");
        return winner.getVideo();
    }



    public static void main(String[] args) {
        DbUtil.loadDbDriver();
        try {
            Connection conn = DbUtil.getAConnection("localhost");
            ProblemMgr.loadProbs(conn);
            List<Problem> probs= ProblemMgr.getAllProblems();
            StandardVideoSelector s = new StandardVideoSelector();
            for (Problem p: probs)  {
                String vid = s.selectVideo(conn,p.getId());
                System.out.println("Problem id " + p.getId() + " name: " + p.getName() + " video: " + vid);
            }
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

}
