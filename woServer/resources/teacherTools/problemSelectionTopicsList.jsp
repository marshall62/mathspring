<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--<jsp:include page="teacherToolsHeader.jsp" />      --%>
<jsp:include page="${sideMenu}" />
<jsp:useBean id="bean" scope="request" type="edu.umass.ckc.wo.beans.Classes"/>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class="mainPageMargin">
  <div id="Layer1" align="center">
    <p class="a2"><b><font face="Arial, Helvetica, sans-serif"> Problem Topic Selection</font></b></p>
    <p><font face="Arial, Helvetica, sans-serif">To select problems you must click on a topic below. </font></p>
    <p><font face="Arial, Helvetica, sans-serif" color="#FF0000"> Note: You may not select problems for deactivated topics </font></p>

    
      
      <table width="334" border="0" height="98">
          <tr>
                <td width="40"><font  face="Arial, Helvetica, sans-serif">ID</font></td>
                <td width="305"><font face="Arial, Helvetica, sans-serif">Topic</font></td>
          </tr>

          <%--@elvariable id="topics" type="edu.umass.ckc.wo.tutor.Topic[]"--%>

          <c:forEach var="topic" items="${topics}">

                 <tr>
                     <td  width="40"><font  face="Arial, Helvetica, sans-serif"><c:out value="${topic.id}"/></font></td>
                   <td class="a2" width="305">
                       <c:choose>
                       <c:when test="${topic.seqPos > 0}">

                           <a  href="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminSelectTopicProblems&topicId=<c:out value="${topic.id}"/>&classId=<c:out value="${classId}"/>&teacherId=<c:out value="${teacherId}"/>" >
                               <c:out value="${topic.name}"/>
                           </a>
                       </c:when>
                       <c:otherwise>
                             <font  face="Arial, Helvetica, sans-serif"><c:out value="${topic.name}"/></font>
                       </c:otherwise>
                       </c:choose>
                   </td>
                 </tr>


          </c:forEach>
      </table>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    
    <table width="200" border="1">
      <tr>

        <td>
            <form name="form2" id="form3" method="post" action="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminEditTopics">

            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                  <input type="submit" name="Submit" value="Activate/Reorganize Topics" />
                  <input type="hidden" name="classId" value="<c:out value="${classId}"/>">
                  <input type="hidden" name="teacherId" value="<c:out value="${teacherId}"/>">
            </form>
        </td>
      </tr>
    </table>
    <p>

  </div>
</div>

<jsp:include page="wayangTempTail.jsp" />