package edu.umass.ckc.wo.db;

import edu.umass.ckc.wo.strat.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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



    public static TutorStrategy getStrategy (Connection conn, int stratId, int classId) throws SQLException {
         ResultSet rs = null;
          PreparedStatement ps = null;
          try {
              String q = "select login_sc_id, lesson_sc_id, tutor_sc_id, name, className from strategy s where s.id = ?";
              ps = conn.prepareStatement(q);
              ps.setInt(1, stratId);
              rs = ps.executeQuery();
              if (rs.next()) {

                  int login_SCId = rs.getInt(1);
                  int lesson_SCId = rs.getInt(2);
                  int tutor_SCId = rs.getInt(3);
                  String name = rs.getString(4);
                  String className = rs.getString(5);
                  TutorStrategy ts = new TutorStrategy();
                  ts.setStratId(stratId);
                  ts.setName(name);
                  ts.setClassName(className);
                  ts.setLogin_sc(getClassStrategyComponent(conn,login_SCId,classId));
                  ts.setLesson_sc(getClassStrategyComponent(conn,lesson_SCId,classId));
                  ts.setTutor_sc(getClassStrategyComponent(conn,tutor_SCId,classId));
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
    private static ClassStrategyComponent getClassStrategyComponent(Connection conn, int scId, int classId) throws SQLException {
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
                  ClassSCInterventionSelector interventionSelector = getClassSCInterventionSelector(conn,iselId,scId,classId);
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
    private static ClassSCInterventionSelector getClassSCInterventionSelector(Connection conn, int iselId, int scId, int classId) throws SQLException {
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
                  ClassSCInterventionSelector isel = new ClassSCInterventionSelector();
                  isel.setId(iselId);
                  isel.setName(name);
                  isel.setClassName(className);
                  isel.setOnEvent(onEvent);
                  List<ISParam> params = getISParams(conn,iselId,classId);
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
    private static List<ISParam> getISParams(Connection conn, int iselId, int classId) throws SQLException {
         ResultSet rs = null;
          PreparedStatement ps = null;
          try {
              String q = "select p.id, p.name, p.value from is_param_class p, is_param_base b where b.intervention_selector_id=? and p.classId=? and p.isActive=1 and p.is_param_id=b.id";
              ps = conn.prepareStatement(q);
              ps.setInt(1, iselId);
              ps.setInt(2, classId);
              rs = ps.executeQuery();
              List<ISParam> params = new ArrayList<ISParam>();
              while (rs.next()) {
                  int paramId = rs.getInt(1);
                  String n = rs.getString(2);
                  String v = rs.getString(3);
                  ISParam p = new ISParam(paramId,n,v);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
