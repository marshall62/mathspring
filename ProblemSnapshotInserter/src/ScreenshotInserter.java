import com.mysql.jdbc.MysqlDataTruncation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;


/**
 * This file is used to populate the Problem table with snapshot images as BLOBs in the Problem.snapshot field.
 *
 * It is packaged as a jar file (see the project artifact ProblemSnapshotInserter which is an executable jar).
 * It is run like this: java -jar ProblemSnapshotInserter.jar
 * It is run like this: java -jar ProblemSnapshotInserter.jar d:/mydir/
 *
 * It will run over the Problem table in the db using Problem.name and look for an image by that name in the
 * directory.   If found, it will insert it into the snapshot field as a blob.
 *
 *  To package the jar:
 *      Build | Make module ProblemSnapshotInserter
 *      Build | Build Artifacts | ProblemSnapshotInserter
 *
 *      Jar file is in out/artifacts
 */

/**
 * Created with IntelliJ IDEA.
 * User: jnewman
 * Date: 10/20/14
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class ScreenshotInserter {

    public static Connection getAConnection (String host) throws SQLException {
        String dbPrefix = "jdbc:mysql";
        String dbHost = host;
        String dbSource = "wayangoutpostdb";
        String dbUser = "WayangServer";
        String dbPassword = "m4thspr1ng!";

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

    public static void main(String[] args) throws SQLException {

        Connection conn = getAConnection("rose.cs.umass.edu");
        System.out.println("Running snapshot inserter");

//        String dir = "U:\\MathspringProblemSnapshots\\screenshots\\";
        String dir = "";
        // if a dir is given as an arg to the program use it; otherwise use the current working dir.
        if (args.length > 0) {
            dir = args[0];
            if (!dir.endsWith("/"))
                dir += "/"; // needs to end with a slash
            System.out.println("Using directory " + dir + " for images");
        }
        else System.out.println("Using current directory for images");

        String problem_name = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String filename = null;
        try {
            String q = "select name, screenShotURL from Problem ";
            ps = conn.prepareStatement(q);
            rs = ps.executeQuery();
            while (rs.next()) {
                FileInputStream inputStream = null;
                String ssURL = null;
                problem_name = rs.getString("name");
                ssURL = rs.getString("screenShotURL");
                filename = dir + problem_name + ".jpg";
                if (ssURL != null) {
                    int p = ssURL.lastIndexOf("/");
                    String ssName = ssURL.substring(p + 1);
                    String ssn = ssName.split("\\.")[0];
                    if (!ssn.equals(problem_name)) {
                        try {
                            inputStream = new FileInputStream(dir + ssName);
                        } catch (FileNotFoundException e) {
                            System.out.println("Cant find file " + dir + ssName);
                            continue;
                        }
                    }
                }
                if (inputStream == null) {
                    try {
                        inputStream = new FileInputStream(filename);
                    } catch (FileNotFoundException e) {
                        System.out.println("Can't find file " + filename);
                        continue;
                    }
                }

                PreparedStatement ps2 = null;
                try {
                    q = "update Problem set snapshot=? where name=?";

                    ps2 = conn.prepareStatement(q);
                    ps2.setBlob(1, inputStream);
                    ps2.setString(2, problem_name);

                    ps2.executeUpdate();
                    System.out.println("Successfully wrote blob for " + problem_name);
                } catch (MysqlDataTruncation exc) {
                    System.out.println("Snapshot too large for " + filename);
                    continue;
                } catch (Exception e) {
                    System.out.println("Cannot process problem " + problem_name + " .. Omitting");
                    continue;
                } finally {
                    ps2.close();
                }
            }


        } finally {
            if (ps != null)
                ps.close();
        }
    }

}

