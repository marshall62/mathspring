package edu.umass.ckc.wo.db;

import edu.umass.ckc.wo.beans.ClassInfo;
import edu.umass.ckc.wo.beans.Teacher;
import edu.umass.ckc.wo.event.admin.AdminTeacherRegistrationEvent;
import edu.umass.ckc.wo.login.PasswordAuthentication;

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

        PreparedStatement ps = conn.prepareStatement("select ID,password from Teacher where userName=?");

        ps.setString(1,username);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int id =  rs.getInt("ID");
            String token = rs.getString("password");
            boolean m = PasswordAuthentication.getInstance().authenticate(pw.toCharArray(),token);
            if (m)
                return id;
            else return -1;
        }
        else return -1;
    }

    public static void insertTeacher (Connection conn, String userName, String fname, String lname, String pw, String email) throws SQLException {

        PreparedStatement ps = null;
        try {
            String s = "insert into Teacher (fname,lname,password,userName,email) values (?,?,?,?,?)";
            ps = conn.prepareStatement(s);
            ps.setString(1, fname);
            ps.setString(2, lname);
            String token = PasswordAuthentication.getInstance(0).hash(pw.toCharArray());
            ps.setString(3, token);
            ps.setString(4, userName);
            ps.setString(5, email);
            ps.executeUpdate();
        } finally {
            if (ps != null)
                ps.close();
        }
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

    public static boolean modifyTeacher(Connection conn, int teacherId, String fname, String lname, String uname, String pw) throws SQLException {
        PreparedStatement ps = null;
        try {
            if (pw.length() > 0) {
                String q = "update teacher set username=?, lname=?, fname=?,password=? where id=?";
                ps = conn.prepareStatement(q);
                ps.setString(1, uname);
                ps.setString(2, lname);
                ps.setString(3, fname);
                ps.setString(4, PasswordAuthentication.getInstance().hash(pw.toCharArray()));
                ps.setInt(5, teacherId);
                int n = ps.executeUpdate();
                return n == 1;
            } else {
                String q = "update teacher set username=?, lname=?, fname=? where id=?";
                ps = conn.prepareStatement(q);
                ps.setString(1, uname);
                ps.setString(2, lname);
                ps.setString(3, fname);
                ps.setInt(4, teacherId);
                int n = ps.executeUpdate();
                return n == 1;
            }
        }
            finally{
                if (ps != null)
                    ps.close();
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
            Connection conn = DbUtil.getAConnection("rose.cs.umass.edu");
            PreparedStatement ps = null;
            ResultSet rs = null;
            PreparedStatement stmt = null;
            try {
                String q = "select id, password,oldpw  from teacher where id=70";
                stmt = conn.prepareStatement(q,ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    int c = rs.getInt(1);
                    String pw = rs.getString("password");
                    String opw = rs.getString("oldpw");
                    String token = PasswordAuthentication.getInstance(0).hash(opw.toCharArray());
                    rs.updateString("password",token);
                    rs.updateRow();
                    System.out.println(token);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (stmt != null)
                    stmt.close();
                if (rs != null)
                    rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}