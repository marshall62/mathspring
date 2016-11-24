package edu.umass.ckc.wo.db;

import edu.umass.ckc.wo.beans.ClassInfo;
import edu.umass.ckc.wo.beans.Teacher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 7/31/15
 * Time: 9:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class DbTeacher {

    public static int getTeacherId (Connection conn, String username, String pw) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("select ID from Teacher where userName=? and password=?");
        ps.setString(1,username);
        ps.setString(2,pw);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
            return rs.getInt(1);
        else return -1;
    }

    public static String getTeacherName (Connection conn, int teacherid) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("select fname, lname from Teacher where id=?");
        ps.setString(1,Integer.toString(teacherid));
        ResultSet rs = ps.executeQuery();
        String tname;
        if (rs.next()){
            tname = rs.getString(1);
            if (rs.next())
                tname = tname+" "+rs.getString(2);
            return tname;
        }else{
            tname = "Teacher";
            return tname;
        }

    }

    public static List<Teacher> getAllTeachers(Connection conn, boolean includeClasses) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select id, fname,lname,username from teacher order by lname asc";
            stmt = conn.prepareStatement(q);
            List<Teacher> result = new ArrayList<Teacher>();
            rs = stmt.executeQuery();
            while (rs.next()) {
                int id= rs.getInt(1);
                String fname = rs.getString("fname");
                String lname = rs.getString("lname");
                String uname = rs.getString("username");
                Teacher t = new Teacher(null,id,fname,lname,uname,null);
                if (includeClasses) {
                    List<ClassInfo> classes = DbClass.getTeacherClasses(conn,t.getId());
                    t.setClasses(classes);
                }
                result.add(t);
            }
            return result;
        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }

    public static Teacher getTeacher(Connection conn, int teacherId) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select fname,lname,username, email from teacher where id=?";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,teacherId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                String f = rs.getString(1);
                String l = rs.getString(2);
                String u = rs.getString(3);
                String e = rs.getString(4);
                return new Teacher(e,teacherId,f,l,u,null);
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

    /**
     * Deleting teachers depends on first deleting their classes
     * @param conn
     * @param teacherIds
     * @throws SQLException
     */
    public static void deleteTeachers(Connection conn, int[] teacherIds) throws SQLException {
        for (int tid: teacherIds) {
            List<Integer> classIds = DbClass.getTeacherClassIds(conn,tid);
            for (int cid : classIds)
                DbClass.deleteClass(conn,cid);
            // once all the classes are gone there should be no other tables related to a teacher and now we delete the teacher row.
            deleteTeacher(conn, tid);
        }
    }


    private static int deleteTeacher(Connection conn, int tid) throws SQLException {
        PreparedStatement stmt = null;
        try {
            String q = "delete from teacher where id=?";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, tid);
            return stmt.executeUpdate();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    public static void main(String[] args) {
        try {
            Connection c = DbUtil.getAConnection("rose.cs.umass.edu");
            deleteTeachers(c,new int[] {554} );
            System.out.println("Teachers deleted");

        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}