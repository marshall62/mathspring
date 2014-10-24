package edu.umass.ckc.wo.event;

import ckc.servlet.servbase.ServletParams;
import ckc.servlet.servbase.UserException;

/**
 * Copyright (c) University of Massachusetts
 * Written by: David Marshall
 * Date: Jun 20, 2005
 * Time: 12:53:57 PM
 */
public class SystemTestLoginEvent extends LoginEvent {
    public SystemTestLoginEvent(ServletParams p) throws Exception {
        super(p);    //To change body of overridden methods use File | Settings | File Templates.
        if (this.getUname().equals("SystemTest") && this.getPassword().equals("WoAdmin"))
            ;
        else throw new UserException("to login for system test you must use the correct uname/password");
    }
}
