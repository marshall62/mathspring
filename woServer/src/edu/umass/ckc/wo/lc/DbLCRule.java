package edu.umass.ckc.wo.lc;

import edu.umass.ckc.wo.tutor.Pedagogy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 3/10/16
 * Time: 4:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class DbLCRule {
    
    public static LCRuleset readRuleSet (Connection conn, int ruleSetId) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            LCRuleset ruleset = new LCRuleset();
            String q = "select r.id, r.name, r.description, r.interventionpointName, r.priority from rule r, rulesetmap m where m.rulesetid=? and m.ruleid=r.id";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,ruleSetId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String name= rs.getString(2);
                String descr= rs.getString(3);
                String onEvent= rs.getString(4);
                double priority = rs.getDouble(5);
                LCRule rule = new LCRule(id,name,descr,onEvent,priority);
                readRuleConditions(rule,conn,id);
                readRuleAction(rule,conn,id);
                ruleset.addRule(rule);
            }
            return ruleset;
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        } 
    }

    private static void readRuleAction(LCRule rule, Connection conn, int ruleId) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select id,a.name,lcmessageId,m.messageText,m.name from ruleaction a, lcmessage m where ruleId=? and m.id=a.lcmessageId";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,ruleId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int c= rs.getInt(1);
                String name1 = rs.getString(2); // I'm not sure what this name is used for
                int messageId = rs.getInt(3); // the id of the lcmessage row
                String msgText = rs.getString(4); // The text that goes into the bubble next to the lc (and what the audio says)
                String name2 = rs.getString(5); // The name that ivon gave to each of these response strings in the past (e.g. correct1)
                LCAction a = new LCAction(c, msgText,name2);
                rule.setAction(a);

            }
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        } 
    }

    private static void readRuleConditions(LCRule rule, Connection conn, int ruleId) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select id,name,aboveBelowBetween, params1,prependOperator,paramType from rulecondition where ruleid=?";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,ruleId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int condId= rs.getInt(1);
                String fn = rs.getString(2);
                String relop = rs.getString(3);
                String param1 = rs.getString(4);
                String unaryop = rs.getString(5);
                String paramType = rs.getString(6);
                boolean applyNot = false;
                if (!rs.wasNull())
                    applyNot = unaryop.equals("NOT");
                LCCondition ruleCond = new LCCondition(condId, fn,relop,param1,paramType,applyNot);
                rule.addCondition(ruleCond);
            }
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }

    public static int getRuleSetId (Connection conn, String name) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select id from ruleset where name=?";
            stmt = conn.prepareStatement(q);
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int c= rs.getInt(1);
                return c;
            }
            else return -1;
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }

    public static void loadRuleSetIntoPedagogy(Connection conn, Pedagogy ped) throws SQLException {
        int rulesetId = getRuleSetId(conn,ped.getLearningCompanionRuleSetName());
        if (rulesetId != -1) {
            LCRuleset rs = readRuleSet(conn,rulesetId);
            ped.setLearningCompanionRuleSet(rs);
        }
    }
}
