package edu.umass.ckc.wo.db;

import com.mysql.jdbc.MysqlDataTruncation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Pull request test

/**
 * Created with IntelliJ IDEA.
 * User: jnewman
 * Date: 10/20/14
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class ScreenshotInserter {

    public static void main(String[] args) throws SQLException {

        Connection conn = DbUtil.getAConnection("rose.cs.umass.edu");

        String dir = "U:\\MathspringProblemSnapshots\\screenshots\\";


        String problem_name = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String filename = null;
        try {
            String q = "select name, screenShotURL from Problem ";
            ps = conn.prepareStatement(q);
            rs = ps.executeQuery();
            FileInputStream inputStream = null;
            while (rs.next()) {
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
                    System.out.println("ss too large for " + filename);
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

