package edu.umass.ckc.wo.login;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 12, 2012
 * Time: 7:30:17 PM
 * This takes the input from the login2a.jsp page which provides a first and last initial.   If good inputs are given,  the next page is login2b.jsp
 */
public class LoginAdult_3 extends Login3 {
    public LoginAdult_3() {
        next_jsp = "woAdult/login2b.jsp";
    }
}
