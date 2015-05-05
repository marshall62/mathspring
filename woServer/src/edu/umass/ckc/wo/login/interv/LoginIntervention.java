package edu.umass.ckc.wo.login.interv;

import edu.umass.ckc.wo.tutormeta.Intervention;
import net.sf.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 4/27/15
 * Time: 10:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class LoginIntervention implements Intervention {
    private String view;  // name of JSP
    private String url;   // URL which will be shown in a separate browser window
    private boolean hasURL=false; // tells whether this is just a simple JSP or JSP + URL

    public LoginIntervention(String view, String url, boolean hasURL) {
        this.view = view;
        this.url = url;
        this.hasURL=hasURL;
    }

    public LoginIntervention(String view) {
        this(view,null,false);

    }

    public String getView () {
        return this.view;
    }

    public boolean hasURL () {
        return this.hasURL;
    }

    public String getURL () {
        return this.url;
    }

    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getResource() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JSONObject buildJSON(JSONObject jo) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String logEventName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
