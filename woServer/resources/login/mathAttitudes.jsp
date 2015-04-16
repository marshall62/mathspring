<%--
  Created by IntelliJ IDEA.
  User: david
  Date: 4/13/15
  Time: 3:42 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<form method="post" name="login" action="${pageContext.request.contextPath}/WoLoginServlet">
    <input type="hidden" name="action" value="LoginAdult_3"/>
    <input type="hidden" name="sessionId" value="${sessionId}">
    <p>&nbsp;</p>
    <p><b>First Name:
        <input type="text" name="fname" />
        Last Initial:
        <input type="text" name="lini" /> </b></p>
    <br>
    <p><b>How confident do you feel when solving math problems?:</b></p>
    <input type="radio" name="confidence" value="1">Not at all</input>
    <input type="radio" name="confidence" value="2">A little</input>
    <input type="radio" name="confidence" value="3">Somewhat</input>
    <input type="radio" name="confidence" value="4">Quite a bit</input>
    <input type="radio" name="confidence" value="5">Extremely</input>
    <br>

    <br>
    <p><b>How interested do you feel when solving math problems, in general?:</b></p>
    <input type="radio" name="interest" value="1">Not at all</input>
    <input type="radio" name="interest" value="2">A little</input>
    <input type="radio" name="interest" value="3">Somewhat</input>
    <input type="radio" name="interest" value="4">Quite a bit</input>
    <input type="radio" name="interest" value="5">Extremely</input>
    </br>
    <br>
    <p><b>In general, how exciting is it to solve math problems?:</b></p>
    <input type="radio" name="excitement" value="1">Not at all</input>
    <input type="radio" name="excitement" value="2">A little</input>
    <input type="radio" name="excitement" value="3">Somewhat</input>
    <input type="radio" name="excitement" value="4">Quite a bit</input>
    <input type="radio" name="excitement" value="5">Extremely</input>
    </br>


    <br>
    <p><b>How frustrated do you feel when solving math problems overall?:</b></p>
    <input type="radio" name="frustration" value="1">Not at all</input>
    <input type="radio" name="frustration" value="2">A little</input>
    <input type="radio" name="frustration" value="3">Somewhat</input>
    <input type="radio" name="frustration" value="4">Quite a bit</input>
    <input type="radio" name="frustration" value="5">Extremely</input>
    </br>

    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input  type="submit"  value="Submit" />
    </p>
</form>
