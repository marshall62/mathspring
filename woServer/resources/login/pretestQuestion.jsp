<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: david
  Date: 4/13/15
  Time: 3:42 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.umass.ckc.wo.content.PrePostProblemDefn" %>

<%--@elvariable id="question" type="edu.umass.ckc.wo.content.PrePostProblemDefn"--%>

<c:if test="${message != null}">
    <b>${message}</b> <br><br>
</c:if>
<script type="text/javascript" src="login/js/login.js"></script>


<form method="post" name="login" onsubmit="return validateForm(${question.isMultiChoice()})" action="${pageContext.request.contextPath}/WoLoginServlet">
    <input type="hidden" name="action" value="LoginInterventionInput"/>
    <input type="hidden" name="sessionId" value="${sessionId}">
    <input type="hidden" name="skin" value="${skin}"/>
    <input type="hidden" name="interventionClass" value="${interventionClass}"/>
    <input type="hidden" name="probId" value="${question.id}"/>

    <p>&nbsp;</p>
    <c:choose>
        <c:when test="${question.isMultiChoice()}">
            <p><b>${question.descr}:</b></p>
            <c:if test="${question.aAns != null}">
                <input id="a" type="radio" name="answer" value="${question.aAns}"> ${question.aAns}</input>
            </c:if>
            <c:if test="${question.bAns != null}">
                <br/><input id="b" type="radio" name="answer" value="${question.bAns}"> ${question.bAns}</input>
            </c:if>
            <c:if test="${question.cAns != null}">
                <br/><input id="c" type="radio" name="answer" value="${question.cAns}"> ${question.cAns}</input>
            </c:if>
            <c:if test="${question.dAns != null}">
                <br/><input id="d" type="radio" name="answer" value="${question.dAns}"> ${question.dAns}</input>
            </c:if>
            <c:if test="${question.eAns != null} ">
                <br/> <input id="e" type="radio" name="answer" value="${question.eAns}"> ${question.eAns}</input>
            </c:if>

            <br>
        </c:when>
        <c:otherwise>
            <c:if test="${question.url != null}">
                <img src="${pageContext.request.contextPath}${question.url}"/>
                <br/>
            </c:if>
            <p><b>${question.descr}</b></p>
            <input id="f" type="text" name="answer"/>
            </br>
            <br>
        </c:otherwise>
    </c:choose>


    <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input  type="submit"  value="Submit" />
    </p>
</form>
