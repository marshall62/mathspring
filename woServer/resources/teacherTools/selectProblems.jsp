<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--<jsp:include page="teacherToolsHeader.jsp" /> --%>

<jsp:include page="${sideMenu}"/>

<jsp:useBean id="bean" scope="request" type="edu.umass.ckc.wo.beans.Classes"/>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<div id="Layer1" align="center">
    <p class="a2"><font size="3" color="#000000"><b><font face="Arial, Helvetica, sans-serif"> Problem Selection</font></b></font>
    </p>
    <div align="left">
        &nbsp;&nbsp;<b>Topic:</b>  <c:out value="${topicName}"/>  <br>
        &nbsp;&nbsp;<b>Standards:</b>    <c:out value="${standards}"/> <br>
        &nbsp;&nbsp;<b>Summary:</b>    <c:out value="${summary}"/>      <br>
    </div>
    <p style="color: #000000"><font face="Arial, Helvetica, sans-serif">Problems that are checked will be shown to
        members of your class.</font></p>


    <form name="form1" id="form1" method="post"
          action="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminActivateProblems">
        <input type="hidden" name="classId" value="<c:out value="${classId}"/>"/>
        <input type="hidden" name="topicId" value="<c:out value="${topicId}"/>"/>
        <table width="515" border="1" height="98">
            <tr>
                <td width="60"><font color="#000000" face="Arial, Helvetica, sans-serif">Activated</font></td>
                <td width="35"><font color="#000000" face="Arial, Helvetica, sans-serif">ID</font></td>
                <td width="200"><font color="#00000" face="Arial, Helvetica, sans-serif">Name</font></td>
                <td width="200"><font color="#000000" face="Arial, Helvetica, sans-serif">Nickname</font></td>
                <td width="50"><font color="#000000" face="Arial, Helvetica, sans-serif">Diff.</font></td>
                <td width="50"><font color="#000000" face="Arial, Helvetica, sans-serif">CC Std</font></td>
                <td width="50"><font color="#000000" face="Arial, Helvetica, sans-serif">Type</font></td>
            </tr>

            <%--@elvariable id="problems" type="edu.umass.ckc.wo.beans.SATProb[]"--%>
            <%--@elvariable id="prob" type="edu.umass.ckc.wo.content.Problem"--%>

            <c:forEach var="problem" items="${problems}">
                <%-- Get the Problem object that lives in the SATProblem--%>
                <c:set var="prob" value="${problem.problem}"/>
                <tr>
                    <!-- TODO checked state is determined from the problem -->

                    <td><input type="checkbox" name="activated"
                               <c:if test="${problem.activated}">checked="checked"</c:if>
                               value="<c:out value="${problem.id}"/>">

                    </td>
                    <td>
                        <font color="#000000" face="Arial, Helvetica, sans-serif"><c:out value="${problem.id}"/></font>
                    </td>
                    <td class="a2">
                        <c:choose>
                            <c:when test="${problem.externalURL}">)
                                <a onclick="window.open('<c:out
                                value="${problem.resource}"/>','ProblemPreview','width=750,height=550,status=yes,resizable=yes');">
                                    <font color='#000000' face="Arial, Helvetica, sans-serif"><u><c:out value="${problem.name}"/></u></font>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <c:choose>
                                    <c:when test="${problem.type=='flash'}">
                                        <a onclick="window.open('<c:out value="${probPlayerHost}"/>?questionNum=<c:out
                                        value="${problem.questNum}"/>','ProblemPreview','width=750,height=550,status=yes,resizable=yes');">
                                            <font color='#000000' face="Arial, Helvetica, sans-serif"><u><c:out value="${problem.name}"/></u></font>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a onclick="window.open('${html5ProblemURI}${prob.getHTMLDir()}/${problem.resource}','ProblemPreview','width=750,height=550,status=yes,resizable=yes');">
                                            <font color='#000000' face="Arial, Helvetica, sans-serif"><u><c:out value="${problem.name}"/></u></font>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>



                    </td>
                    <td>
                        <font color="#000000" face="Arial, Helvetica, sans-serif"><c:out
                                value="${problem.nickname}"/></font>
                    </td>
                    <td>
                        <font color="#000000" face="Arial, Helvetica, sans-serif"><c:out
                                value="${problem.difficulty}"/></font>
                    </td>
                    <td>
                        <font color="#000000" face="Arial, Helvetica, sans-serif"><c:out
                                value="${prob.standardsString}"/></font>
                    </td>
                    <td>
                        <font color="#000000" face="Arial, Helvetica, sans-serif"><c:out
                                value="${problem.type}"/></font>
                    </td>
                </tr>


            </c:forEach>
        </table>

        <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

        <table width="200" border="1">
            <tr>
                <td><input type="submit" name="Submit" value="Save Changes"/>
                    <input type="hidden" name="classId" value="<c:out value="${classId}"/>">
                    <input type="hidden" name="teacherId" value="<c:out value="${teacherId}"/>">

                </td>
    </form>
    </tr>
    </table>
    <p>
</div>

<jsp:include page="wayangTempTail.jsp"/>

