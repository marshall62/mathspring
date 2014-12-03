package edu.umass.ckc.wo.db;

import com.mysql.jdbc.MysqlDataTruncation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: jnewman
 * Date: 10/20/14
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class ScreenshotInserter {

   public static void main(String[] args) throws SQLException {
        int numFiles = 1000;
        Connection conn = DbUtil.getAConnection("rose.cs.umass.edu");
        for (int i=0; i < numFiles; ++i) {
            // Use whatever directory your screenshots are in
            String dir = "C:\\Users\\jnewman\\Downloads\\screenshots\\";
            String filename = null;
            String nn = null;
            try {
                if (i < 10) {
                    nn = "problem_00"+i+".jpg";
                }
                else if (i < 100) {
                    nn="problem_0"+i+".jpg";
                }
                else {
                    nn="problem_"+i+".jpg";
                }
                filename=dir+nn;
                String[] name = nn.split("\\.");
                FileInputStream inputStream = null;
                String problem_name = name[0];
                PreparedStatement ps=null;
                ResultSet rs = null;
                try {
                    String q = "select screenShotURL from Problem where name=?";
                    ps = conn.prepareStatement(q);
                    ps.setString(1, problem_name);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        String ssURL = null;
                        ssURL = rs.getString("screenShotURL");
                        if (ssURL != null) {
                            int p = ssURL.lastIndexOf("/");
                            String ssName = ssURL.substring(p+1);
                            String ssn = ssName.split("\\.")[0];
                            if (!ssn.equals(problem_name)) {
                                inputStream = new FileInputStream(dir+ssName);
                            }
                        }
                        if (inputStream == null) {
                            inputStream = new FileInputStream(filename);
                        }
                    }

                    q = "update Problem set snapshot=? where name=?";

                    ps = conn.prepareStatement(q);
                    ps.setBlob(1, inputStream);
                    ps.setString(2, problem_name);

                    ps.executeUpdate();
                }
                catch (MysqlDataTruncation exc) {
                    System.out.println("ss too large for " + filename);
                    continue;
                }
                finally {
                    if (ps != null)
                        ps.close();
                }
            }
            catch (FileNotFoundException e) {
                //System.out.println("Couldn't find" + dir);
                continue;
            }
        }
}   }
