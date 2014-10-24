package edu.umass.ckc.wo.content;

import edu.umass.ckc.wo.beans.Topic;
import edu.umass.ckc.wo.db.DbTopics;
//import edu.umass.ckc.wo.event.admin.AdminReorderTopicsEvent;
import edu.umass.ckc.wo.event.admin.AdminReorderTopicsEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <p> Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 23, 2008
 * Time: 9:09:47 AM
 */
public class TopicMgr {


    public List<Topic> omitTopic (Connection conn,  AdminReorderTopicsEvent e) throws SQLException {
        int classId= e.getClassId();
        int topicId= e.getTopicId();
        List<Topic> topics = DbTopics.getClassActiveTopics(conn,classId);
        DbTopics.removeClassActiveTopics(conn,classId);

        Iterator<Topic> itr = topics.iterator();
        while (itr.hasNext()) {
            Topic t =  itr.next();
            if (t.getId() == topicId)  {
                DbTopics.insertClassInactiveTopic(conn,classId,t);
                itr.remove();
            }
        }
        DbTopics.insertTopics(conn,classId,topics);
        return topics;
    }

    public void reactivateTopic (Connection conn,  AdminReorderTopicsEvent e) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select max(seqPos)+1 from classLessonPlan where classId=?";
            stmt =conn.prepareStatement(q);
            stmt.setInt(1,e.getClassId());
            int seqPos=-1;
            rs =stmt.executeQuery();
            if (rs.next()) {
                seqPos = rs.getInt(1);
                if ( seqPos == 0 )
                    seqPos=1 ;
            }
            rs.close();
            stmt.close();
            if (isTopicInactiveInPlan(conn,e.getTopicId(),e.getClassId()))
                updateLessonPlanMakeTopicActive(conn,e.getTopicId(),e.getClassId(),seqPos);
            else insertLessonPlanActiveTopic(conn,e.getTopicId(),e.getClassId(),seqPos);
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }


    }

    private void insertLessonPlanActiveTopic(Connection conn, int topicId, int classId, int seqPos) throws SQLException {
        ResultSet rs = null;
        PreparedStatement s = null;
        try {
            String q = "insert into classLessonPlan (classId, probGroupId, seqPos, isDefault) " +
                    "values (?,?,?,?)";
            s = conn.prepareStatement(q);
            s.setInt(1, classId);
            s.setInt(2, topicId);
            s.setInt(3, seqPos);
            s.setInt(4, 0);
            s.execute();
        } finally {
            if (rs != null)
                rs.close();
            if (s != null)
                s.close();
        }
    }

    private void updateLessonPlanMakeTopicActive(Connection conn, int topicId, int classId, int seqPos) throws SQLException {
        PreparedStatement stmt = null;
        try {
            String q = "update classlessonplan set seqpos=? where probgroupid=? and classId=?";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, seqPos);
            stmt.setInt(2, topicId);
            stmt.setInt(3, classId);
            stmt.executeUpdate();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    private boolean isTopicInactiveInPlan (Connection conn, int topicId, int classId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String q = "select seqPos from classlessonplan where probgroupId=? and classId=?";
            ps = conn.prepareStatement(q);
            ps.setInt(1,topicId);
            ps.setInt(2,classId);
            rs = ps.executeQuery();
            if (rs.next()) {
                int pos = rs.getInt(1);
                return pos < 0;
            }
            return false;
        } finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
        }
    }



    public List<Topic> moveTopic (Connection conn, AdminReorderTopicsEvent e) throws SQLException {
        int classId= e.getClassId();
        int topicId= e.getTopicId();
        String dir = e.getDirection();
        List<Topic> topics = DbTopics.getClassActiveTopics(conn,classId);
        boolean swapped = moveTopicAux(topics, topicId, dir.equals("up")? -1 : 1);
        // if the swap happens in the list, the list elements (topics) are swapped and the seqPos field of each object is mutated.
        //  Then we remove all active topics from the classlessonplan and stick them all back in again.
        if (swapped) {
            DbTopics.removeClassActiveTopics(conn,classId);
            DbTopics.insertTopics(conn,classId,topics);
        }
        return topics;
    }

    private boolean moveTopicAux(List<Topic> topics, int topicId, int incr) {
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            if (topic.getId() == topicId) {
                if ((incr < 0 && i > 0) || (incr > 0 && i < topics.size()-1)) {
                    int pos = topics.get(i).getSeqPos();
                    topics.get(i).setSeqPos(topics.get(i+incr).getSeqPos());
                    topics.get(i+incr).setSeqPos(pos);
                    Collections.swap(topics,i,i+incr);

                   return true;
                }
            }
        }
        return false;
    }







}
