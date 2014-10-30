package edu.umass.ckc.wo.db;

import com.mysql.jdbc.MysqlDataTruncation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
            String dir = "C:\\Users\\jnewman\\Downloads\\screenshots\\";
            try {
                if (i < 10) {
                    dir+="problem_00"+i+".jpg";
                }
                else if (i < 100) {
                    dir+="problem_0"+i+".jpg";
                }
                else {
                    dir+="problem_"+i+".jpg";
                }
                File file = new File(dir);
                FileInputStream inputStream = new FileInputStream(file);

                //System.out.println("Updating " + dir);
                String[] name = file.getName().split("\\.");
                String problem_name = name[0];
                PreparedStatement ps=null;
                try {
                    String q = "update Problem set snapshot=? where name=?";

                    ps = conn.prepareStatement(q);
                    ps.setBlob(1, inputStream);
                    ps.setString(2, problem_name);

                    ps.executeUpdate();
                }
                catch (MysqlDataTruncation exc) {
                    System.out.println("ss too large for " + dir);
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
