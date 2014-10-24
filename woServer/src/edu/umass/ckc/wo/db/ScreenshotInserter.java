package edu.umass.ckc.wo.db;

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

    public static void main(String[] args) throws FileNotFoundException, SQLException {
        File file = new File("C:\\Users\\jnewman\\Desktop\\problem_001.jpg");
        FileInputStream inputStream = new FileInputStream(file);
        Connection conn = DbUtil.getAConnection("rose.cs.umass.edu");
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
        finally {
            if (ps != null)
                ps.close();
        }
    }
}
