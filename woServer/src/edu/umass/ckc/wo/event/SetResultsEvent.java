
package edu.umass.ckc.wo.event;

import edu.umass.ckc.wo.mrcommon.Names;
import ckc.servlet.servbase.ServletParams;


public class SetResultsEvent extends LoginEvent {
  private static final String RESULTS = Names.RESULTS;
  private String results_;

    public SetResultsEvent(ServletParams p) throws Exception {
        super(p);
        results_=p.getString(RESULTS);
    }

    public String getResults () { return results_;}


}