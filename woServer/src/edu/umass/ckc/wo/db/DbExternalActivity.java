package edu.umass.ckc.wo.db;

import edu.umass.ckc.wo.content.ExternalActivity;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: 8/22/12
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class DbExternalActivity {
    

    // Get external activities within the topic that has not been shown to the student before (look in the event log to find
    // BEGIN events with that xactID
    public static List<ExternalActivity> getActivitiesForStudent (Connection conn, int topicId, int studId) throws SQLException {
       ResultSet rs=null;
       PreparedStatement stmt=null;
       try {
           List<ExternalActivity> l = new ArrayList<ExternalActivity>();
           String q = "select a.id,a.name,a.description,a.url,a.creator,a.lastModifier,a.ready,a.instructions,a.metainfo,a.creationtime, d.diff_level " +
                   "from externalactivity a, externalactivitytopic t, overallprobdifficulty d where a.id=t.xactid and d.problemId=a.id and t.topicId=? and a.ready=1 and " +
                   "t.xactid not in (select problemid from eventlog where studid=? and " +
                   "(action='BeginExternalActivity' or action='Formality_BeginProblem'))";
           stmt = conn.prepareStatement(q);
           stmt.setInt(1,topicId);
           stmt.setInt(2,studId);
           rs = stmt.executeQuery();
           while (rs.next()) {
               int id= rs.getInt(1);
               String name= rs.getString(2);
               String descr = rs.getString(3);
               String url = rs.getString(4);
               String crea = rs.getString(5);
               String lastMod = rs.getString(6);
               boolean isReady = rs.getBoolean(7);
               String instr = rs.getString(8);
               String meta = rs.getString(9);
               Timestamp ts = rs.getTimestamp(10);
               double diff = rs.getDouble(11);
               ExternalActivity a = new ExternalActivity(id,name,descr,url, instr,meta,diff);
               l.add(a);
           }
           return l;
       }
       finally {
           if (stmt != null)
               stmt.close();
           if (rs != null)
               rs.close();
       }      
    }

    public static ExternalActivity getExternalActivity (Connection conn, int xactId) throws SQLException {
        ResultSet rs=null;
       PreparedStatement stmt=null;
       try {
           List<ExternalActivity> l = new ArrayList<ExternalActivity>();
           String q = "select a.id,a.name,a.description,a.url,a.creator,a.lastModifier,a.ready,a.instructions,a.metainfo,a.creationtime,d.diff_level " +
                   "from externalactivity a, overallprobdifficulty d where a.id=? and a.id=d.problemId";
           stmt = conn.prepareStatement(q);
           stmt.setInt(1,xactId);
           rs = stmt.executeQuery();
           while (rs.next()) {
               int id= rs.getInt(1);
               String name= rs.getString(2);
               String descr = rs.getString(3);
               String url = rs.getString(4);
               String crea = rs.getString(5);
               String lastMod = rs.getString(6);
               boolean isReady = rs.getBoolean(7);
               String instr = rs.getString(8);
               String meta = rs.getString(9);
               Timestamp ts = rs.getTimestamp(10);
               double diff = rs.getDouble(11);
               ExternalActivity a = new ExternalActivity(id,name,descr,url, instr,meta,diff);
               return a;
           }
           return null;
       }
       finally {
           if (stmt != null)
               stmt.close();
           if (rs != null)
               rs.close();
       }
    }

    public static void main(String[] args) {
        
    }
}
