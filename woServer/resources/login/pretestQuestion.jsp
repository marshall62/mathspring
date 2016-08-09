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

<%--@elvariable id="pretestQuestion" type="edu.umass.ckc.wo.content.PrePostProblemDefn"--%>

<c:if test="${message != null}">
    <b>${message}</b> <br><br>
</c:if>
<form method="post" name="login" action="${pageContext.request.contextPath}/WoLoginServlet">
    <input type="hidden" name="action" value="LoginInterventionInput"/>
    <input type="hidden" name="sessionId" value="${sessionId}">
    <input type="hidden" name="skin" value="${skin}"/>
    <input type="hidden" name="interventionClass" value="${interventionClass}"/>
    <input type="hidden" name="probId" value="${pretestQuestion.id}"/>

    <p>&nbsp;</p>
    <c:choose>
        <c:when test="${pretestQuestion.isMultiChoice()}">
            <p><b>${pretestQuestion.descr}:</b></p>
            <c:if test="${pretestQuestion.aAns != null}">
                <input type="radio" name="answer" value="${pretestQuestion.aAns}"> ${pretestQuestion.aAns}</input>
            </c:if>
            <c:if test="${pretestQuestion.bAns != null}">
                <br/><input type="radio" name="answer" value="${pretestQuestion.bAns}"> ${pretestQuestion.bAns}</input>
            </c:if>
            <c:if test="${pretestQuestion.cAns != null}">
                <br/><input type="radio" name="answer" value="${pretestQuestion.cAns}"> ${pretestQuestion.cAns}</input>
            </c:if>
            <c:if test="${pretestQuestion.dAns != null}">
                <br/><input type="radio" name="answer" value="${pretestQuestion.dAns}"> ${pretestQuestion.dAns}</input>
            </c:if>
            <c:if test="${pretestQuestion.eAns != null} ">
                <br/> <input type="radio" name="answer" value="${pretestQuestion.eAns}"> ${pretestQuestion.eAns}</input>
            </c:if>

            <br>
        </c:when>
        <c:otherwise>
            <c:if test="${pretestQuestion.url != null}">
                <img src="${pageContext.request.contextPath}${pretestQuestion.url}"/>
                <br/>
            </c:if>
            <p><b>${pretestQuestion.descr}</b></p>
            <input type="text" name="answer"/>
            </br>
            <br>
        </c:otherwise>
    </c:choose>


    <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input  type="submit"  value="Submit" />
    </p>
</form>
