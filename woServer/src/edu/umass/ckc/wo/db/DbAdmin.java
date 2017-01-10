package edu.umass.ckc.wo.db;

import edu.umass.ckc.wo.beans.Teacher;
import edu.umass.ckc.wo.login.PasswordAuthentication;
import edu.umass.ckc.wo.tutor.Settings;

import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 7/31/15
 * Time: 9:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class DbAdmin {

    public static int getNewSession(Connection conn, int adminId) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "insert into adminsession (userId, lastAccessTime, isActive) values (?,?,?)";
            stmt = conn.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1,adminId);
            stmt.setTimestamp(2,new Timestamp(System.currentTimeMillis()));
            stmt.setBoolean(3,true);
            stmt.execute();
            rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        catch (SQLException e) {
            System.out.println(e.getErrorCode());
            if (e.getErrorCode() == Settings.duplicateRowError ||e.getErrorCode() == Settings.keyConstraintViolation )
                throw e;
            else throw e;
        }
        finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
    }

    /**
     * If the id/password is an administrator, build a new session and return the session id and the userid.
     * @param conn
     * @param username
     * @param pw
     * @return
     * @throws SQLException
     */
    public static Teacher getAdminSession (Connection conn, String username, String pw) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("select ID,fname,lname,password from administrator where userName=?");
        ps.setString(1,username);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int uid = rs.getInt(1);
            String fname = rs.getString(2);
            String lname = rs.getString(3);
            String token = rs.getString(4);
            if (PasswordAuthentication.getInstance().authenticate(pw.toCharArray(),token))
                return new Teacher(null,uid,fname,lname,username,token);
            else return null;

        }
        else return null;
    }

    public static Teacher getAdmin (Connection conn, int adminId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("select fname,lname,username from administrator where id=?");
        ps.setInt(1, adminId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String fname = rs.getString(1);
            String lname = rs.getString(2);
            String uname = rs.getString(3);
            return new Teacher(null,adminId,fname,lname,uname,null);

        }
        else return null;
    }

}
