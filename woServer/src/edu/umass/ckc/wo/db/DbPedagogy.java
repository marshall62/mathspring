package edu.umass.ckc.wo.db;

import edu.umass.ckc.wo.xml.JDOMUtils;
import org.jdom.Document;
import org.jdom.Element;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 11/18/15
 * Time: 5:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class DbPedagogy {
    private static Connection conn;

    public static void main(String[] args) {
        try {
            DbPedagogy p = new DbPedagogy();
            DbPedagogy.conn = DbUtil.getAConnection("localhost");
            readPedagogiesFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readPedagogiesFromFile() throws FileNotFoundException, SQLException {
        FileInputStream str = new FileInputStream("f:\\dev\\mathspring\\woServer\\resources\\pedagogies.xml");
        Document d = JDOMUtils.makeDocument(str);
        Element root = d.getRootElement();
        List<Element> peds =  root.getChildren("pedagogy");
        for (Element ped : peds) {
            readPedagogy(ped);
        }
    }

    private static void readPedagogy(Element ped) throws SQLException {

        String name = ped.getChild("name").getTextTrim();
        Element e = ped.getChild("provideInSimpleConfig");
        String simpleConfigName=null;
        boolean isBasic = false;
        if (e != null) {
            String s = e.getAttributeValue("name");
            simpleConfigName = s;
            isBasic = true;
        }

        String lesson = ped.getChild("lesson").getTextTrim();
        String login = ped.getChild("login").getTextTrim();
        String xml = JDOMUtils.toXML(ped);
        writeToDb(conn,name,simpleConfigName,lesson,login,xml,isBasic );
    }

    private static int writeToDb(Connection conn, String name, String simpleConfigName,
                                  String lesson, String login, String xml, boolean isBasic) throws SQLException {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try {
            String q = "insert into pedagogy (isBasic,login,lesson,name,simpleConfigName,definition,active)" +
                    " values (?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(q);
            stmt.setBoolean(1,isBasic);
            stmt.setString(2, login);
            stmt.setString(3, lesson);
            stmt.setString(4, name);
            stmt.setString(5, simpleConfigName);
            stmt.setString(6, xml);
            stmt.setBoolean(7, true);
            stmt.execute();
            rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        catch (SQLException e) {

        }
        finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
        return 21;
    }
}
