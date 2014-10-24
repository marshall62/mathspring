package edu.umass.ckc.wo.event;

import ckc.servlet.servbase.ActionEvent;
import ckc.servlet.servbase.ServletParams;
import ckc.servlet.servbase.UserException;


public class LoginEvent extends ActionEvent {
    public final String UNAME = "uname";
    public final String MOMSNAME = "momsname";
    public final String PASSWORD = "password";
    public final String CLIENTBEGINTIME = "loginTime";
    public final String WRISTID = "wristID" ;  // defunct - will get from Emote Servlet
    public final String IPADDRESS = "ipAddress" ;  // only necessary for Emote Servlet.
    public final String INTERFACE_TYPE ="interfaceType"; // k12 or adult
    public static final String LOGOUT_EXISTING_SESSION ="logoutExistingSession"; // flag sent to kill other sessions
    public final String ELAPSED_TIME = "elapsedTime" ;
    public final String FLASH_CLIENT = "flashClient" ;
    private String password;
    private String uname;
    private String clientBeginTime ;
    private String wristID;
    private String momsName;// this lets us know what mode the user logged in under
    private String ipAddress;
    private String interfaceType;
    private boolean logoutExistingSession = false;
    private String flashClient ;

    public LoginEvent(ServletParams p) throws Exception {
        super(p);
        uname = p.getMandatoryString(UNAME).trim();
        password = p.getString(PASSWORD, null);
        momsName = p.getString(MOMSNAME,null);
        ipAddress = p.getString(IPADDRESS,null);
        clientBeginTime = p.getString(CLIENTBEGINTIME,null);
        wristID = p.getString(WRISTID,null) ;
        interfaceType= p.getString(INTERFACE_TYPE,"k12");
        flashClient= p.getString(FLASH_CLIENT, "WoLogin_dm");
        logoutExistingSession = p.getBoolean(LOGOUT_EXISTING_SESSION, false);

        if (password != null)
          password = password.trim();
        if (momsName != null)
          momsName = momsName.trim();
        if ((uname != null && password != null) ||
            (uname != null && momsName != null))
          ;
        else throw new UserException("Must provide values for either " + UNAME + "/" + PASSWORD +
                                     " or for " + UNAME + "/" + MOMSNAME);

    }
  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    this.password = password;
  }
  public void setUname(String uname) {
    this.uname = uname;
  }
  public String getUname() {
    return uname;
  }
  public void setMomsName(String momsName) {
    this.momsName = momsName;
  }
  public String getMomsName() {
    return momsName;
  }

  public String getClientBeginTime()
  {
     return this.clientBeginTime ;
  }

  public String getWristID() {
      return this.wristID ;
  }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isK12Interface() {
        return interfaceType.equalsIgnoreCase("k12");
    }

   public boolean isAdultInterface() {
        return interfaceType.equalsIgnoreCase("adult");
    }

    public boolean isLogoutExistingSession() {
        return logoutExistingSession;
    }


    public String getFlashClient() {
        return flashClient;
    }

    public void setFlashClient(String flashClient) {
        this.flashClient = flashClient;
    }
}
