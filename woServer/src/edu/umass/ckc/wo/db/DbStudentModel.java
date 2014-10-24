package edu.umass.ckc.wo.db;


import edu.umass.ckc.wo.tutormeta.StudentModel;
import org.apache.log4j.Logger;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 3/15/13
 * Time: 10:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class DbStudentModel {
    private static Logger logger = Logger.getLogger(DbStudentModel.class);


    private Connection conn;
//    private List<Column> cols;

    public DbStudentModel () {}

    public DbStudentModel(Connection c) throws SQLException {
        this.conn = c;
//        loadCols(tableName);

     }

    protected void create(int studId, String tableName) throws SQLException {
        PreparedStatement stmt=null;
        try {
            String q = "insert into " +tableName+ " (studId) values (?)";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, studId);
            stmt.execute();
        }
        finally {
            if (stmt != null)
                stmt.close();
        }
    }


    public void  printCols (String table) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "select COLUMN_NAME, DATA_TYPE from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = ? and table_schema='wayangoutpostdb'";
            stmt = conn.prepareStatement(q);
            stmt.setString(1,table);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String n=rs.getString(1);
                System.out.print("\"" + n + "\",");
            }

        }
        finally {
            if (stmt != null)
                stmt.close();
            if (rs != null)
                rs.close();
        }
    }






    protected String getColumnUpdateString (String[] cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < cols.length; i++) {
            String colname = cols[i];
            sb.append(" " + colname+"=?,");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    protected void setCols(Class c, StudentModel studentModel, PreparedStatement ps, String[] cols, Class smClass) {
        try {
            for (int i = 1; i < cols.length; i++) {
                String colName = cols[i];
                setTableCol(c, studentModel, ps, colName, i, smClass);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // set the value of the column in the db row using studentModel class.
    private void setTableCol(Class c, StudentModel studentModel, PreparedStatement ps, String colName, int colIx, Class smClass) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(colName,smClass);
            Method m = pd.getReadMethod();
            Type t= m.getGenericReturnType();
            Object val = m.invoke(studentModel);
            ps.setObject(colIx, val);
        } catch (IntrospectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public int updateIntColumn(int studId, String col, int value, String tableName) throws SQLException {
        PreparedStatement stmt=null;
        try {
            String q = "update "+tableName+ " set " +col+ "=? where studid=?";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, value);
            stmt.setInt(2,studId);
            return stmt.executeUpdate();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }



    public int save(StudentModel sm, int studId, String tableName, String[] tableCols, Class smClass) throws SQLException {
        PreparedStatement ps=null;
        try {
            String q = "update "  +tableName+ " set " + getColumnUpdateString(tableCols) + " where studId=?";
            ps = conn.prepareStatement(q);
            setCols(smClass,sm,ps, tableCols, smClass);
            int ix= tableCols.length;
            ps.setInt(ix,studId);
            return ps.executeUpdate();
        } finally {
            if (ps != null)
                ps.close();
        }
    }

    void setObjectPropVal(PropertyDescriptor d, String name, StudentModel sm, ResultSet rs)  {
        try {
            Method setter = d.getWriteMethod();
            Object o = rs.getObject(name);  // if declared as unsigned int, get Long, signed int => Integer
            if (o == null)
                return;
            // note: db stores time as unsigned int but class stores as long.

            setter.invoke(sm,o);
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }


    public void load(int studId, StudentModel sm, String tableName, String[] cols, Class smClass) throws SQLException, NoSuchFieldException, IllegalAccessException, IntrospectionException, InvocationTargetException {
           ResultSet rs=null;
           PreparedStatement stmt=null;
           try {
               String q = "select * from " + tableName + " where studId=?";
               stmt = conn.prepareStatement(q);
               stmt.setInt(1,studId);
               rs = stmt.executeQuery();
               // If no student model row exists for this user,  they must be new.   Create an empty row
               // and then load which will get all the default values declared in the db table.
               if (!rs.next()) {
                   create(studId, tableName);
                   load(studId,sm, tableName, cols, smClass);  // potential for infinite loop if create fails.
               }
               else {
                   for (int i=1;i<cols.length;i++) {
                       PropertyDescriptor d = new PropertyDescriptor(cols[i], smClass);
                       setObjectPropVal(d, cols[i], sm, rs);
                   }

               }
           }
           finally {
               if (stmt != null)
                   stmt.close();
               if (rs != null)
                   rs.close();
           }

       }


    public static int clear(Connection conn, String table, int studId) throws SQLException {
        PreparedStatement stmt=null;
        try {
            String q = "delete from "+table+" where studId=?";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,studId);
            return stmt.executeUpdate();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }
}
