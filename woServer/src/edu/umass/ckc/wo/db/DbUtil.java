package edu.umass.ckc.wo.db;

import edu.umass.ckc.wo.tutor.Settings;

import java.io.*;
import java.sql.*;

/**
 * Created by IntelliJ IDEA.
 * User: marshall
 * Date: Mar 25, 2009
 * Time: 3:05:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class DbUtil {


    public static void loadDbDriver () {
        String dbDriver = "com.mysql.jdbc.Driver";
        try {
           Driver d = (Driver) Class.forName(dbDriver).newInstance(); // MySql
            System.out.println(d);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static Connection getAConnection (String host) throws SQLException {
      String dbPrefix = "jdbc:mysql";
        String dbHost = host;
        String dbSource = "wayangoutpostdb";
        String dbUser = "WayangServer";
        String dbPassword = "jupiter";

        String url;
        if (dbPrefix.equals("jdbc:mysql"))
            url = dbPrefix +"://"+ dbHost +"/"+ dbSource +"?user="+ dbUser +"&password="+ dbPassword; // preferred by MySQL
        else // JDBCODBCBridge
            url = dbPrefix +":"+ dbSource;
//        url = "jdbc:mysql://localhost:3306/test";
//        url = "jdbc:mysql://localhost/rashidb"; // this works
        try {
            System.out.println("connecting to db on url " + url);
            return DriverManager.getConnection(url,dbUser,dbPassword);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        }
    }

    public static Connection getAConnection () throws SQLException {
        return getAConnection("rose.cs.umass.edu");
     }


    // Go through the problem table and copy external problems into externalactivity table.   Create
    // an entry mapping each xact to its topic based on what the mapping was for the problem the xact is based on
    // Then delete the problem and the mappings from probprobgroup
    public static void doDbWork (Connection conn) throws SQLException {
        try {

            FileReader r = new FileReader (new File("u:\\wodb\\firstProbSolved.csv"));
            BufferedReader rr = new BufferedReader(r);
            rr.readLine(); // elim first line
            String l;
            while ((l = rr.readLine()) != null) {
                System.out.println(l);
                int id = Integer.parseInt(l);
                delWoProp(conn,id);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static void delWoProp(Connection conn, int id) throws SQLException {
        PreparedStatement stmt=null;
        try {
            String q = "delete from woproperty where wopropkey=?";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1,id);
            stmt.executeUpdate();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    public static void main(String[] args) {

           loadDbDriver ();
            try {

                Connection conn = getAConnection("cadmium.cs.umass.edu") ;
                doDbWork(conn);
                //setClassConfigs(conn);
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    public static String getHost(Connection conn) throws SQLException {
       String q ="select host from hostinfo";
        PreparedStatement ps = conn.prepareStatement(q);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String host = rs.getString(1);
            return host;
        }
        return null;
    }
}