<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--<jsp:include page="teacherToolsHeader.jsp" /> --%>

<jsp:include page="${sideMenu}"/>

<jsp:useBean id="bean" scope="request" type="edu.umass.ckc.wo.beans.Classes"/>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<div id="Layer1" align="center">
    <p class="a2"><b>Problem Selection</b></p>
    <div align="left">
        &nbsp;&nbsp;<b>Topic:</b>  <c:out value="${topicName}"/>  <br>
        &nbsp;&nbsp;<b>Standards:</b>    <c:out value="${standards}"/> <br>
        &nbsp;&nbsp;<b>Summary:</b>    <c:out value="${summary}"/>      <br>
    </div>
    <p>Problems that are checked will be shown to students in your class.</p>


    <form name="form1" id="form1" method="post"
          action="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminActivateProblems">
        <input type="hidden" name="classId" value="<c:out value="${classId}"/>"/>
        <input type="hidden" name="topicId" value="<c:out value="${topicId}"/>"/>
        <table class="altrows">
            <tr class="rowheader">
                <td>Activated</td>
                <td>ID</td>
                <td>Name</td>
                <td>Nickname</td>
                <td>Difficulty</td>
                <td>CC Std</td>
                <td>Type</td>
            </tr>
            <tr></tr> <%-- Prevents altrows from shading both the header and the first row --%>

            <%--@elvariable id="problems" type="edu.umass.ckc.wo.beans.SATProb[]"--%>
            <%--@elvariable id="prob" type="edu.umass.ckc.wo.content.Problem"--%>

            <c:forEach var="problem" items="${problems}">
                <%-- Get the Problem object that lives in the SATProblem--%>
                <c:set var="prob" value="${problem.problem}"/>
                <tr>
                    <td>
                        <input type="checkbox" name="activated"
                               <c:if test="${problem.activated}">checked="checked"</c:if>
                               value="<c:out value="${problem.id}"/>"
                        >
                    </td>
                    <td><c:out value="${problem.id}"/></td>
                    <td class="a2">
                        <c:choose>
                            <c:when test="${problem.externalURL}">)
                                <a onclick="window.open('<c:out value="${problem.resource}"/>','ProblemPreview','width=750,height=550,status=yes,resizable=yes');">
                                    <u><c:out value="${problem.name}"/></u>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <c:choose>
                                    <c:when test="${problem.type=='flash'}">
                                        <a onclick="window.open('<c:out value="${probPlayerHost}"/>?questionNum=<c:out
                                        value="${problem.questNum}"/>','ProblemPreview','width=750,height=550,status=yes,resizable=yes');">
                                            <u><c:out value="${problem.name}"/></u>
                                        </a>
                                    </c:when>
                                    <c:when test="${(prob.form != null) && (prob.form=='quickAuth')}">
                                        <a onclick="window.open('${pageContext.request.contextPath}/WoAdmin?action=AdminGetQuickAuthSkeleton&probId=${problem.id}&teacherId=${teacherId}',
                                                'ProblemPreview','width=750,height=550,status=yes,resizable=yes');">
                                            <u><c:out value="${problem.name}"/></u>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a onclick="window.open('${html5ProblemURI}${prob.getHTMLDir()}/${problem.resource}','ProblemPreview','width=750,height=550,status=yes,resizable=yes');">
                                            <u><c:out value="${problem.name}"/></u>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td><c:out value="${problem.nickname}"/></td>
                    <td><c:out value="${problem.difficulty}"/></td>
                    <td><c:out value="${prob.standardsString}"/></td>
                    <td><c:out value="${problem.type}"/></td>
                </tr>
            </c:forEach>
        </table>
        <div style="height:54px"></div>
        <input type="submit" name="Submit" value="Save Changes" style="font-size:16px;padding:10px"/>
        <input type="hidden" name="classId" value="<c:out value="${classId}"/>">
        <input type="hidden" name="teacherId" value="<c:out value="${teacherId}"/>">
        <div style="height:54px"></div>
    </form>
    </tr>
    </table>
    <p>
</div>

<jsp:include page="wayangTempTail.jsp"/>

