package edu.umass.ckc.wo.db;

import edu.umass.ckc.wo.strat.TutorStrategy;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.Connection;

/**
 * Created by marshall on 6/14/17.
 */
public class DbStrategy {

    public static TutorStrategy getStrategy (Connection conn, int stratId) {
        throw new NotImplementedException();
    }
}
