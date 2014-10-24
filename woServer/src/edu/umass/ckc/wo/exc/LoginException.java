package edu.umass.ckc.wo.exc;
import edu.umass.ckc.wo.event.LoginEvent;


public class LoginException extends Exception {

  public LoginException(LoginEvent e) {
    super("Failed to get login info in database for uname=" + e.getUname());
  }
}