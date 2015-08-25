package edu.umass.ckc.wo.db;

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

    public static List<Teacher> getAllTeachers(Connection conn) throws SQLException {
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
}
