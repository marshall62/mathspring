package edu.umass.ckc.wo.tutor.intervSel2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 3/5/14
 * Time: 10:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class InterventionState {

    // When stale sessions are cleaned up this is called to get rid of state that intervention selectors have saved into woproperty table
    public static int clearState(Connection conn, int studId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            // The assumption is that all intervention state starts with a string (the class name of the InterventionSelector) preceding the ".".
            // StudentState uses "st." and StudentModels (which should use their own table but may still have some vestiges in woproperty) start with "sm.".
            String q = "delete from woproperty where objid=? and property not like 'st.%' and property not like 'sm.%' and property not like 'wkspcst.%'";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, studId);
            return stmt.executeUpdate();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }
}
