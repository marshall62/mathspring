<%--
  Created by IntelliJ IDEA.
  User: david
  Date: 4/14/15
  Time: 3:49 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div>
    <form method="post" name="login"
          action="${pageContext.request.contextPath}/WoLoginServlet">
        <input type="hidden" name="skin" value="${skin}"/>
        <input type="hidden" name="sessionId" value="${sessionId}"/>
        <input type="hidden" name="action" value="LoginInterventionInput"/>
        <input type="hidden" name="interventionClass" value="${interventionClass}"/>
        <p>&nbsp;
        Please fill out the survey in the new browser window that should be showing (if not, check that your
            browser is not blocking pop-up windows).   When you have completed the survey, close the window it is in and
            click the continue button below.  <br><br>
        &nbsp;&nbsp;&nbsp;<input type="submit" name="Continue" value="Continue"/>
        </p>

    </form>
</div>