package edu.umass.ckc.wo.handler;

import edu.umass.ckc.wo.beans.ClassInfo;
import edu.umass.ckc.wo.beans.Classes;
import edu.umass.ckc.wo.db.DbClass;
import edu.umass.ckc.wo.event.admin.AdminTeacherLoginEvent;
import edu.umass.ckc.wo.html.admin.Variables;
import edu.umass.ckc.wo.tutor.Settings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;

/**
 * Copyright (c) University of Massachusetts
 * Written by: David Marshall
 * Date: Jan 31, 2006
 * Time: 2:06:10 PM
 */
public class AdminToolLoginHandler {
    public void handleEvent(Connection conn, final Variables v,
                            AdminTeacherLoginEvent event, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
        int id,sessId;
        String teacherName;
        // no user action has taken place. Just show the login screen
        if (!event.isLogin() && !event.isReg()) {
            servletRequest.setAttribute("message","");
            servletRequest.getRequestDispatcher("/teacherTools/teacherLogin.jsp").forward(servletRequest,servletResponse);
        }
        // admin logins
        else if (event.isLogin() && (sessId = getAdminSession(conn,event.getUname(),event.getPw())) != -1) {
            System.out.println("logging in as admin");
            servletRequest.setAttribute("sessId",Integer.toString(sessId));
            // TODO make a clone of the class selection JSP used by students.
            servletRequest.getRequestDispatcher("/teacherTools/selectClass.jsp").forward(servletRequest,servletResponse);
        }
        // teacher logins
        else if (event.isLogin()) {
            id = getTeacherId(conn,event.getUname(),event.getPw());
            if (id == -1) {
                servletRequest.setAttribute("message","Incorrect user/password");
                servletRequest.getRequestDispatcher("/teacherTools/teacherLogin.jsp").forward(servletRequest,servletResponse);
            }
            else {
                teacherName = getTeacherName(conn, id);
                //check for classes with teacherId
                ClassInfo[] classes1 = DbClass.getClasses(conn, id);
                Classes bean1 = new Classes(classes1);
                
                int hasClasses =-1;
                for (ClassInfo cl: classes1){
                    hasClasses = cl.getClassid();
                }
                if (hasClasses > 0){
                    ClassInfo classInfo = DbClass.getClass(conn,hasClasses);
                    //if hasClasses, pass in teacherid, classid, teacherName
                    servletRequest.setAttribute("action","AdminUpdateClassId");
                    servletRequest.setAttribute("bean", bean1);
                    servletRequest.setAttribute("classInfo", classInfo);
                    servletRequest.setAttribute("classId", Integer.toString(hasClasses));
                    servletRequest.setAttribute("teacherId",Integer.toString(id));
                    servletRequest.setAttribute("teacherName", teacherName);
                    servletRequest.getRequestDispatcher("/teacherTools/wayangMain.jsp").forward(servletRequest,servletResponse);
                    //servletRequest.getRequestDispatcher("/teacherTools/teacherActivities.jsp").forward(servletRequest,servletResponse);
                }else{
                    //pass in classId, and main page.
                    servletRequest.setAttribute("teacherId",Integer.toString(id));
                    servletRequest.getRequestDispatcher("/teacherTools/mainNoClasses.jsp").forward(servletRequest,servletResponse);
                }
                //ClassInfo classInfo = DbClass.getClass(conn,hasClasses);

                //servletRequest.setAttribute("teacherId",Integer.toString(id));
                //servletRequest.setAttribute("action","AdminUpdateClassId");
                //servletRequest.setAttribute("bean", bean1);
                //servletRequest.setAttribute("classInfo", classInfo);
                //servletRequest.setAttribute("classId", Integer.toString(hasClasses));
                //servletRequest.setAttribute("teacherName", teacherName);
                //servletRequest.getRequestDispatcher("/teacherTools/wayangMain.jsp").forward(servletRequest,servletResponse);
            }
        }
        else if (event.isReg()) {
            servletRequest.setAttribute("message","");
            servletRequest.getRequestDispatcher("/teacherTools/teacherRegister.jsp").forward(servletRequest,servletResponse);

        }
    }

    /**
     * If the id/password is an administrator, build a new session and return the session id.
     * @param conn
     * @param username
     * @param pw
     * @return
     * @throws SQLException
     */
    public int getAdminSession (Connection conn, String username, String pw) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("select ID from administrator where userName=? and password=?");
        ps.setString(1,username);
        ps.setString(2,pw);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
            return getNewSession(conn,rs.getInt(1));
        else return -1;
    }

    private int getNewSession(Connection conn, int adminId) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "insert into adminsession (userId, lastAccessTime, isActive) values (?,?,?)";
            stmt = conn.prepareStatement(q);
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

    public int getTeacherId (Connection conn, String username, String pw) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("select ID from Teacher where userName=? and password=?");
        ps.setString(1,username);
        ps.setString(2,pw);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
            return rs.getInt(1);
        else return -1;
    }
    
    public String getTeacherName (Connection conn, int teacherid) throws SQLException {
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
}