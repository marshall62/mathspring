package edu.umass.ckc.wo.tutor.pedModel;

import edu.umass.ckc.wo.smgr.SessionManager;
import edu.umass.ckc.wo.tutor.Pedagogy;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 8/25/15
 * Time: 8:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class DynamicPedagogicalModel extends CollabPedagogicalModel {

    public DynamicPedagogicalModel(){
        super();
    }

    public DynamicPedagogicalModel(SessionManager smgr, Pedagogy pedagogy) throws SQLException {
        super(smgr, pedagogy);
    }






}
