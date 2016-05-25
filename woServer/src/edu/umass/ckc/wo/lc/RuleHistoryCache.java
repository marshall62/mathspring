package edu.umass.ckc.wo.lc;

import edu.umass.ckc.wo.smgr.SessionManager;

import java.util.HashMap;

/**
 * Holds a static rule history in memory for each student's session.
 * User: david
 * Date: 5/24/16
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class RuleHistoryCache {

    private static RuleHistoryCache instance = null;

    private HashMap<Integer,StudentRuleHistory> map;

    public static RuleHistoryCache getInstance () {
        if (instance == null)
        {
            instance = new RuleHistoryCache();
            return instance;
        }
        else return instance;
    }

    private RuleHistoryCache() {
        map = new HashMap<Integer, StudentRuleHistory>();
    }

    public StudentRuleHistory getStudentHistory(SessionManager smgr) {
        return map.get(smgr.getStudentId());
    }


    public void addRuleInstantiation(SessionManager smgr, LCRule r) throws Exception {
        StudentRuleHistory h = map.get(smgr.getSessionNum());
        if (h == null)  {
            map.put(smgr.getSessionNum(),new StudentRuleHistory());
            h = map.get(smgr.getSessionNum());
        }
        h.addRule(r);
    }
}
