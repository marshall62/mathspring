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
          action="<c:out value="${pageContext.request.contextPath}"/>/WoLoginServlet">
        <p>&nbsp;</p>
        <p>First Name:
            <input type="text" name="fname" />
            Last Initial:
            <input type="text" name="lini" />
            <input type="submit" class="fltrt" value="Submit" />
        </p>
    </form>
</div>