
package edu.umass.ckc.wo.event;

import edu.umass.ckc.wo.mrcommon.Names;
import ckc.servlet.servbase.ServletParams;

/** This event type is used by both the MRtest and the Flash interface.  The MRTest will
 * pass it a group, the Flash will not.
 */

public class SetVitalsEvent extends LoginEvent {
    public final static String GENDER = Names.VITALS_GENDER;
    public final static String SATM = Names.VITALS_SATM;
    public final static String SATV = Names.VITALS_SATV;
    private String gender_;
    private int satm_;
    private int satv_;
    private String group_;

    public SetVitalsEvent(ServletParams p) throws Exception {
        super(p);
        gender_ = p.getString(GENDER);
        satm_ = p.getInt(SATM);
        satv_ = p.getInt(SATV);
        group_ = p.getString(Names.GROUP,"");
    }

    public String getGender () { return gender_; }
    public int getSATm () { return satm_; }
    public int getSATv () { return satv_; }
    public String getGroup () { return group_; }

}