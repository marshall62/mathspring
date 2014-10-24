package edu.umass.ckc.wo.event.admin;

import ckc.servlet.servbase.ServletParams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The second event in the series for creating a class.  Receives the fields
 * that define the class.
 */

public class AdminAlterClassSubmitSelectedPedagogiesEvent extends AdminClassEvent {

    private List<String> pedagogyIds;

    public AdminAlterClassSubmitSelectedPedagogiesEvent(ServletParams p) throws Exception {
        super(p);
        Map m = p.getMap();
        setSelectedPedagogies(m);
    }


    private void setSelectedPedagogies(Map m) {
        pedagogyIds = new ArrayList<String>();
        Iterator itr = m.keySet().iterator();
        // extract all params that have an integer key and place them in the pedagogyIds list.
        while (itr.hasNext()) {
            String k = (String) itr.next();
            try {
                Integer.parseInt(k);
                pedagogyIds.add(k);
            } catch (Exception e) {}
        }
    }

    public List<String> getPedagogyIds () {
        return pedagogyIds;
    }


}