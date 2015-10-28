package edu.umass.ckc.wo.admin;

import edu.umass.ckc.wo.db.DbUser;
import edu.umass.ckc.wo.exc.AdminException;
import edu.umass.ckc.wo.tutor.Pedagogy;
import edu.umass.ckc.wo.tutor.Settings;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) University of Massachusetts
 * Written by: David Marshall
 * Date: Feb 21, 2008
 * Time: 3:22:47 PM
 */
public class PedagogyRetriever {

    public static List<Pedagogy> getDefaultPedagogies () {
        List<Pedagogy> defaultPeds = new ArrayList<Pedagogy>();
        for (Pedagogy p : Settings.pedagogyGroups.values()) {
            if (p.isDefault())
                defaultPeds.add(p);
        }
        return defaultPeds;
    }

    /**
     * return the Pedagogy that applies to a given student.
     *
     * Note a student gets a pedagogy assigned to him when he registers in a class.  See UserRegistrationHandler.
     * This method is called after registration so every student should have a pedagogy id.
     * The only sitution where they won't is for a student who has used the system prior to the
     * the institution of pedagogies (probably a developer testing the system).
     * @param conn
     * @param studId
     * @return
     * @throws SQLException
     */
    public static Pedagogy getPedagogy (Connection conn, int studId) throws SQLException, AdminException {
        int pedagogyId = DbUser.getStudentPedagogy(conn,studId);
        // Assistments users are typically using a given pedagogy for the Assistments class.  Occasionally some students come into MS with params like lessonId which means using a
        // pedagogy designed for Common Core content.   These users have an override pedagogy which deals with these requests.
        int overridePedagogyId = DbUser.getStudentOverridePedagogy(conn,studId);
        if (overridePedagogyId != -1) pedagogyId = overridePedagogyId;
        // users who have been in the system prior to the institution of pedagogy ids will
        // not have a pedagogy assigned.   We will automatically set their pedagogy id to be the
        // first default.
        if (pedagogyId == -1)  {
            List<Pedagogy> peds = getDefaultPedagogies();
            return peds.get(0);
        }
        else  {
            // We use the students group number to find the Pedagogy.  Then we can build a StudentModel object
            // for use in this session.
            Pedagogy p = Settings.pedagogyGroups.get(Integer.toString(pedagogyId));
            if (p != null)
                return p;
                // we have a student with pedagogy id that is not mapped to a pedagogy.   This could be
                // an old user who is mapped to a pedagogy that was deleted or some other error.
                // Not sure what to do: so throw exception
            else {
                throw new AdminException("This user has been assigned the pedagogy " + pedagogyId + " but " +
                        "no Pedagogy can be found in the pedagogy.xml that matches this.");
            }

        }
    }
}
