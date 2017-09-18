package edu.umass.ckc.wo.db;

import edu.umass.ckc.wo.lc.DbLCRule;
import edu.umass.ckc.wo.lc.LCRuleset;
import edu.umass.ckc.wo.strat.*;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorParam;
import edu.umass.ckc.wo.tutor.intervSel2.InterventionSelectorSpec;
import edu.umass.ckc.wo.tutormeta.LearningCompanion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marshall on 6/14/17.
 */
public class DbStrategy {


    public static List<TutorStrategy> getStrategies (Connection conn, int classId) throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            String q = "select s.id, s.strategyId, s.name, s.lcid " +
                    "from strategy_class s where s.classid = ?";
            ps = conn.prepareStatement(q);
            ps.setInt(1, classId);
            rs = ps.executeQuery();
            List<TutorStrategy> all = new ArrayList<TutorStrategy>();
            while (rs.next()) {
                int id = rs.getInt("id");
                int stratId = rs.getInt("strategyId");
                String name = rs.getString("name");
                int lcid = rs.getInt("lcid");
                TutorStrategy ts = new TutorStrategy();
                ts.setId(Integer.toString(id));
                ts.setStratId(stratId);
                ts.setName(name);
                // Note we don't fully instantiate the TutorStrategy object with its components because the
                // lookup context where this is called doesn't need anything but the strategy ids and names.
//                ts.setLogin_sc(getClassStrategyComponent(conn,login_SCId,classId));
//                ts.setLesson_sc(getClassStrategyComponent(conn,lesson_SCId,classId));
//                ts.setTutor_sc(getClassStrategyComponent(conn,tutor_SCId,classId));
                ts.setLcid(lcid);
//                loadLC(conn, ts);

                all.add(ts);
            }
            return all;
        } finally {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        }
    }
    /**
     * Load the TutorStrategy and its learning companion stuff.
     * @param conn
     * @param stratId
     * @param classId
     * @return
     * @throws Exception
     */
    public static TutorStrategy getStrategy (Connection conn, int stratId, int classId) throws Exception {
         ResultSet rs = null;
          PreparedStatement ps = null;
          try {
              String q = "select name, lcid from strategy_class s where s.id = ?";
              ps = conn.prepareStatement(q);
              ps.setInt(1, stratId);
              rs = ps.executeQuery();
              if (rs.next()) {
                  String name = rs.getString(1);
                  int lcid = rs.getInt(2);
                  TutorStrategy ts = new TutorStrategy();
                  ts.setStratId(stratId);
                  ts.setName(name);
                  int login_sc_id = getSC(conn,stratId,"login");
                  int lesson_sc_id = getSC(conn,stratId,"lesson");
                  int tutor_sc_id = getSC(conn,stratId,"tutor");
                  ts.setLogin_sc(getClassStrategyComponent(conn,login_sc_id,classId));
                  ts.setLesson_sc(getClassStrategyComponent(conn,lesson_sc_id,classId));
                  ts.setTutor_sc(getClassStrategyComponent(conn,tutor_sc_id,classId));
                  ts.setLcid(lcid);
                  loadLC(conn, ts);
                  return ts;
              }
              else return null;
          } finally {
                if (ps != null)
                     ps.close();
                if (rs != null)
                     rs.close();
          }
    }

    public static int getSC (Connection conn, int stratId, String type) throws SQLException {
         ResultSet rs = null;
          PreparedStatement ps = null;
          try {
              String q = "select c.scid from sc_class c, strategy_component s where c.scId=s.id and c.strategy_class_id=? and s.type=?";
              ps = conn.prepareStatement(q);
              ps.setInt(1, stratId);
              ps.setString(2, type);
              rs = ps.executeQuery();
              if (rs.next()) {
                  int id = rs.getInt(1);
                  return id;
              }
              return -1;
          } finally {
                if (ps != null)
                     ps.close();
                if (rs != null)
                     rs.close();
          }
    }

    /**
     * If its a simple LC (non rule-based), we'll just wind up with a classname, o/w we will be loading up all the rulesets
     * and the other junk that is part of a learning companion.
     * @param conn
     * @param ts
     * @throws Exception
     */
    public static void loadLC (Connection conn, TutorStrategy ts) throws Exception {
         ResultSet rs = null;
          PreparedStatement ps = null;
          try {
              String q = "select name,charName,classname from lc where id=?";
              ps = conn.prepareStatement(q);
              ps.setInt(1, ts.getLcid());
              rs = ps.executeQuery();
              if (rs.next()) {
                  LC lc = new LC();
                  ts.setLc(lc);
                  lc.setId(ts.getLcid());
                  lc.setName(rs.getString(1));
                  lc.setCharacter(rs.getString(2));
                  lc.setClassName(rs.getString(3));
                  loadRuleSets(conn,lc);
              }
          }  finally {
                if (ps != null)
                     ps.close();
                if (rs != null)
                     rs.close();
          }

    }

    /**
     * If the lc has rule sets mapped to it, load them up.
     * @param conn
     * @param lc
     * @throws Exception
     */
    private static void loadRuleSets(Connection conn, LC lc) throws Exception {
         ResultSet rs = null;
          PreparedStatement ps = null;
          try {
              String q = "select s.id, s.name, s.description, s.notes from lc_ruleset_map m, ruleset s where lcid=?";
              ps = conn.prepareStatement(q);

              ps.setInt(1, lc.getId());
              rs = ps.executeQuery();
              while (rs.next()) {
                  LCRuleset lcrs = new LCRuleset();
                  int id = rs.getInt(1);
                  String name = rs.getString(2);
                  String descr = rs.getString(3);
                  String notes = rs.getString(4);
                  lcrs.setId(id);
                  lcrs.setName(name);
                  lcrs.setSource("db");
                  lcrs.setDescription(descr);
                  lcrs.setNotes(notes);
                  DbLCRule.readRuleSet(conn,id,lcrs);
                  lc.addRuleset(lcrs);

              }
          } finally {
                if (ps != null)
                     ps.close();
                if (rs != null)
                     rs.close();
          }
    }

    /**
     * Given a strategyComponent id and a class id, create a ClassStrategyComponenent object from the db tables
     *
     *
     * @param conn
     * @param scId
     * @param classId
     * @return
     * @throws SQLException
     */
    private static ClassStrategyComponent getClassStrategyComponent(Connection conn, int scId, int classId) throws Exception {
         ResultSet rs = null;
          PreparedStatement ps = null;
          try {
              // retrieve info about the strategy component and its intervention selectors
              String q = "select sc.name, sc.className, cm.config, m.intervention_selector_id from class_sc_is_map cm, sc_is_map m, strategy_component sc  where m.strategy_component_id=?" +
                      " and cm.classId=? and cm.sc_is_map_id=m.id and sc.id=m.strategy_component_id and cm.isActive=1";
              ps = conn.prepareStatement(q);
              ps.setInt(1, scId);
              ps.setInt(2, classId);
              rs = ps.executeQuery();
              boolean flag = true;
              ClassStrategyComponent sc=null;
              // we'll get back a row for each intervention selector where the info about the sc is the same along with its
              // intervention selector ids.  Only create the sc object the first time when flag is true
              while (rs.next()) {
                  String name=rs.getString(1);
                  String className=rs.getString(2);
                  String config = rs.getString(3);
                  int iselId = rs.getInt(4);
                  if (flag) {
                      flag=false;
                      sc = new ClassStrategyComponent(scId,name,className);
                      List<SCParam> params = getSCParams(conn,scId,classId);
                      sc.setParams(params);
                  }
                  InterventionSelectorSpec interventionSelector = getClassSCInterventionSelector(conn,iselId,scId,classId);
                  interventionSelector.setConfig(config);
                  sc.addInterventionSelector(interventionSelector);

              }
              return sc;

          } finally {
                if (ps != null)
                     ps.close();
                if (rs != null)
                     rs.close();
          }
    }

    /**
     * For a given strategy component and class, get all the sc params.
     * @param conn
     * @param scId
     * @param classId
     * @return
     */
    private static List<SCParam> getSCParams(Connection conn, int scId, int classId) throws SQLException {
         ResultSet rs = null;
          PreparedStatement ps = null;
          try {
              String q = "select m.sc_param_id, p.name, cp.value from sc_param_map m, sc_param p, class_sc_param cp " +
                      "where m.strategy_component_id=? and m.sc_param_id=p.id and cp.classId=? and cp.isActive=1 and cp.sc_param_id=p.id";
              ps = conn.prepareStatement(q);
              ps.setInt(1, scId);
              ps.setInt(2, classId);
              rs = ps.executeQuery();
              List<SCParam> params = new ArrayList<SCParam>();
              while (rs.next()) {
                  int id = rs.getInt(1);
                  String name = rs.getString(2);
                  String value = rs.getString(3);
                  SCParam p = new SCParam(id,name,value);
                  params.add(p);
              }
              return params;
          } finally {
                if (ps != null)
                     ps.close();
                if (rs != null)
                     rs.close();
          }
    }

    /**
     * Create a ClassSCInterventionSelector by gettings its parts from the db.
     * @param conn
     * @param iselId
     * @param scId
     * @param classId
     * @return
     */
    private static InterventionSelectorSpec getClassSCInterventionSelector(Connection conn, int iselId, int scId, int classId) throws SQLException {
         ResultSet rs = null;
          PreparedStatement ps = null;
          try {
              String q = "select name,className,onEvent from intervention_selector isel where id=?";
              ps = conn.prepareStatement(q);
              ps.setInt(1, iselId);
              rs = ps.executeQuery();
              if (rs.next()) {
                  String name=rs.getString(1);
                  String className=rs.getString(2);
                  String onEvent=rs.getString(3);
                  InterventionSelectorSpec isel = new InterventionSelectorSpec(true);
                  isel.setId(iselId);
                  isel.setName(name);
                  isel.setClassName(className);
                  isel.setOnEvent(onEvent);
                  List<InterventionSelectorParam> params = getISParams(conn,iselId,classId);
                  isel.setParams(params);
                  return isel;
              }
              return null;

          } finally {
                if (ps != null)
                     ps.close();
                if (rs != null)
                     rs.close();
          }
    }

    /**
     * Return a list of ISParams that belong to the intervention selector and class.
     * @param conn
     * @param iselId
     * @param classId
     * @return
     */
    private static List<InterventionSelectorParam> getISParams(Connection conn, int iselId, int classId) throws SQLException {
         ResultSet rs = null;
          PreparedStatement ps = null;
          try {
              String q = "select p.id, p.name, p.value from is_param_class p, is_param_base b where b.intervention_selector_id=? and p.classId=? and p.isActive=1 and p.is_param_id=b.id";
              ps = conn.prepareStatement(q);
              ps.setInt(1, iselId);
              ps.setInt(2, classId);
              rs = ps.executeQuery();
              List<InterventionSelectorParam> params = new ArrayList<InterventionSelectorParam>();
              while (rs.next()) {
                  int paramId = rs.getInt(1);
                  String n = rs.getString(2);
                  String v = rs.getString(3);
                  InterventionSelectorParam p = new InterventionSelectorParam(paramId,n,v);
                  params.add(p);
              }
              return params;
          } finally {
                if (ps != null)
                     ps.close();
                if (rs != null)
                     rs.close();
          }
    }

    public static void main(String[] args) {
        try {
            Connection conn = DbUtil.getAConnection("localhost");
            TutorStrategy ts = DbStrategy.getStrategy(conn,5,1022);
            System.out.println(ts.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
