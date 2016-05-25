package edu.umass.ckc.wo.lc;

import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.Settings;

import java.sql.*;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 3/10/16
 * Time: 4:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class DbLCRule {
    
    public static LCRuleset readRuleSet (Connection conn, int ruleSetId, LCRuleset ruleset) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
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

    public static void writeRuleset (Connection conn, LCRuleset ruleset) throws SQLException {
        int id = ruleset.getId();
        if (id == -1) {
            id = insertRuleset(conn,ruleset);
            ruleset.setId(id);
        }
        else updateRuleset(conn,ruleset);
        for (LCRule r : ruleset.getRules())
            writeRule(conn,r,ruleset);
    }

    private static void updateRuleset(Connection conn, LCRuleset ruleset) throws SQLException {
        PreparedStatement ps = null;
        try {
            String q = "update ruleset set name=?, description=?, notes=?, modTimestamp=?,lastWriter=? where id=?";
            ps = conn.prepareStatement(q);
            ps.setString(1, ruleset.getName());
            ps.setString(2, ruleset.getDescription());
            ps.setString(3, ruleset.getNotes());
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setString(5, "David Marshall auto-dump");
            int n = ps.executeUpdate();
        } finally {
            if (ps != null)
                ps.close();
        }
    }

    private static int insertRuleset(Connection conn, LCRuleset ruleset) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "insert into ruleset (name, description, modTimestamp,notes, lastWriter, createdTimestamp ) values (?,?,?,?,?,?)";
            stmt = conn.prepareStatement(q,PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1,ruleset.getName());
            stmt.setString(2,ruleset.getDescription());
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, ruleset.getNotes());
            stmt.setString(5,"David Marshall auto-dump");
            stmt.setTimestamp(6,new Timestamp(System.currentTimeMillis()));
            stmt.execute();
            rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        catch (SQLException e) {
            System.out.println(e.getErrorCode());
            if (e.getErrorCode() == Settings.duplicateRowError ||e.getErrorCode() == Settings.keyConstraintViolation )
                ;
            else throw e;
        }
        finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
        return -1;
    }

    public static int writeRule (Connection conn, LCRule rule, LCRuleset ruleset) throws SQLException {
        int id = rule.getId();
        if (id != -1) {
            updateRule(conn, rule);
            updateRuleConditions(conn,rule);
            updateRuleAction(conn, rule);
        }
        else {
            id = insertRule(conn,rule,ruleset);
            rule.setId(id);
            insertRulesetRule(conn, ruleset, rule);
            insertRuleConditions(conn, rule);
            insertRuleAction(conn, rule);
        }
        return id;

    }

    private static void updateRuleAction(Connection conn, LCRule rule) throws SQLException {
        final LCAction action = rule.getAction();
        PreparedStatement ps = null;
        try {
            String q = "delete from ruleaction where id=?";
            ps = conn.prepareStatement(q);
            ps.setInt(1, action.getId());
            int n = ps.executeUpdate();
        } finally {
            if (ps != null)
                ps.close();
        }
        insertRuleAction(conn,rule);
    }

    private static int insertRuleAction(Connection conn, LCRule rule) throws SQLException {
        final LCAction action = rule.getAction();
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "insert into ruleaction (actionType, ruleId, lcmessageId, createdTimestamp, lastWriter) values (?,?,?,?,?)";
            stmt = conn.prepareStatement(q,PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1,action.getActionType());
            stmt.setInt(2, rule.getId());
            if (action.getMsgId() == -1)
                stmt.setNull(3,Types.INTEGER);
            else stmt.setInt(3, action.getMsgId());
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.setString(5,"David Marshall auto-dump");
            stmt.execute();
            rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        catch (SQLException e) {
            System.out.println(e.getErrorCode());
            if (e.getErrorCode() == Settings.duplicateRowError ||e.getErrorCode() == Settings.keyConstraintViolation )
                ;
            else throw e;
        }
        finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
        return -1;
    }

    private static void insertRuleConditions(Connection conn, LCRule rule) throws SQLException {
        final List<LCCondition> conditions = rule.getConditions();
        int i=0;
        for (LCCondition c: conditions) {
            insertRuleCondition(conn,c,rule.getId(),i++);
        }
    }

    private static int insertRuleCondition(Connection conn, LCCondition c, int ruleId, int order) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        LCExpr expr = c.getExpr();
        try {
            String q = "insert into rulecondition (functionName, params1, numparameters, relOp, prependOperator, ruleId, sequenceNum, createdTimestamp, lastWriter,paramType)" +
                    " values (?,?,?,?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(q,PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1,c.getFnName());
            stmt.setString(2,c.getParam());
            stmt.setInt(3, c.getNumParams());
            stmt.setString(4,c.getRelop());
            if (c.isApplyNot())
                stmt.setString(5,"NOT");
            else stmt.setNull(5,Types.VARCHAR);
            stmt.setInt(6,ruleId);
            stmt.setInt(7,order);
            stmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            stmt.setString(9,"David Marshall auto-dump");
            stmt.setString(10,c.getParamType());
            stmt.execute();
            rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        catch (SQLException e) {
            System.out.println(e.getErrorCode());
            if (e.getErrorCode() == Settings.duplicateRowError ||e.getErrorCode() == Settings.keyConstraintViolation )
                ;
            else throw e;
        }
        finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
        return -1;
    }



    // A rule that exists may have some conditions modified, some deleted, some added.
    // So the best way to update the rule is to delete all its conditions and then insert what is in the rule now.
    private static void updateRuleConditions(Connection conn, LCRule rule) throws SQLException {
        PreparedStatement ps = null;
        try {
            String q = "delete from rulecondition where ruleid=?";
            ps = conn.prepareStatement(q);
            ps.setInt(1, rule.getId());
            int n = ps.executeUpdate();
        } finally {
            if (ps != null)
                ps.close();
        }
        insertRuleConditions(conn,rule);
    }

    private static void updateRuleCondition(Connection conn, LCCondition c, int ruleId, int order) {
        //To change body of created methods use File | Settings | File Templates.
    }

    private static void insertRulesetRule(Connection conn, LCRuleset ruleset, LCRule rule) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "insert into rulesetMap (ruleId, rulesetId) values (?,?)";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, rule.getId());
            stmt.setInt(2, ruleset.getId());
            stmt.execute();
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }

    }

    private static int insertRule(Connection conn, LCRule rule, LCRuleset ruleset) throws SQLException {
        ResultSet rs=null;
        PreparedStatement ps=null;
        try {
            String q = "insert into rule (description, notes, interventionPointName, priority, modTimestamp,name,  onEvent, createdTimestamp,lastWriter)" +
                    " values (?,?,?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(q,PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, rule.getDescr());
            ps.setString(2, rule.getNotes());
            ps.setString(3, rule.getInterventionPointName());
            ps.setDouble(4, rule.getPriority());
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            ps.setString(6, ruleset.getName() + "." + rule.getName());
            ps.setString(7, rule.getOnEvent());
            ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            ps.setString(9, "David Marshall auto-dump");
            ps.execute();
            rs = ps.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        catch (SQLException e) {
            System.out.println(e.getErrorCode());
            if (e.getErrorCode() == Settings.duplicateRowError ||e.getErrorCode() == Settings.keyConstraintViolation )
                ;
            else throw e;
        }
        finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
        }
        return -1;

    }

    private static void updateRule(Connection conn, LCRule rule) throws SQLException {

        PreparedStatement ps = null;
        try {
            String q = "update rule set description=?, notes=?, priority=?, modTimestamp=?,name=?,onEvent=?, lastWriter=? where id=?";
            ps = conn.prepareStatement(q);
            ps.setString(1, rule.getDescr());
            ps.setString(2, rule.getNotes());
            ps.setDouble(3, rule.getPriority());
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setString(5, rule.getName());
            ps.setString(6, rule.getOnEvent());
            ps.setString(7, "David Marshall auto-dump");
            ps.setInt(8,rule.getId());
            int n = ps.executeUpdate();
        } finally {
            if (ps != null)
                ps.close();
        }

    }

    private static void readRuleAction(LCRule rule, Connection conn, int ruleId) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select a.id,a.name,,lcmessageId,m.messageText,m.name,a.actionType from ruleaction a, lcmessage m where ruleId=? and m.id=a.lcmessageId";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,ruleId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int c= rs.getInt(1);
                String name = rs.getString(2); // No longer used
                int messageId = rs.getInt(3); // the id of the lcmessage row
                String msgText = rs.getString(4); // The text that goes into the bubble next to the lc (and what the audio says)
                String name2 = rs.getString(5); // The name that ivon gave to each of these response strings in the past (e.g. correct1)
                String actionType = rs.getString(6);
                LCAction a = new LCAction(c, msgText,name2,actionType,messageId);  // This is something like playLearningCompanion , ShowUserMessage
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
            String q = "select id,functionName,relop, params1,prependOperator,paramType from rulecondition where ruleid=?";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,ruleId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                boolean applyNot = false;
                int condId= rs.getInt(1);
                String fn = rs.getString(2);
                String relop = rs.getString(3);
                String param1 = rs.getString(4);
                String unaryop = rs.getString(5);
                if (!rs.wasNull())
                    applyNot = unaryop.equals("NOT");
                String paramType = rs.getString(6);


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

    /*  A pedagogy may contain multiple ruleset names.  A ruleset maybe in the db or it may be from an xml file.
        <lcruleset source="db" name="MyRuleSet">
        <lcruleset source="ruleset.xml" name="MyOtherRuleSet">
         */
    public static void loadRuleSetIntoPedagogy(Connection conn, Pedagogy ped, LCRuleset ruleset) throws SQLException {

        int rulesetId = getRuleSetId(conn,ruleset.getName());
        if (rulesetId != -1) {
            LCRuleset rs = readRuleSet(conn,rulesetId,ruleset);

        }
    }
}
