<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- <jsp:include page="teacherToolsHeader.jsp" />  --%>
<jsp:include page="wayangTempHead.jsp" />

<jsp:useBean id="bean" scope="request" type="edu.umass.ckc.wo.beans.Classes"/>

<script type="text/javascript">
    function isFormValid (thisform) {
	// place any other field validations that you require here
	// validate myradiobuttons
	myOption = -1;
	for (i=thisform.poolId.length-1; i > -1; i--) {
		if (thisform.poolId[i].checked) {
			myOption = i; i = -1;
		}
	}
	if (myOption == -1) {
		alert("You must select a radio button");
		return false;
	}

	// place any other field validations that you require here
	//thisform.submit(); // this line submits the form after validation
	return true;
}

</script>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class="mainPageMargin">
  <div id="Layer1" align = "center"><%-- width:400px; height:375px; z-index:1; left: 350px; top: 5px"--%>
      <p><b>Pre/Posttest Selection</b></p>
      <p style="color: #000000"><font face="Arial, Helvetica, sans-serif" size=".5">Select one pool from below, where a pool contains a set of pretests and posttests.
          </font></p>
      <p style="color: #000000"><font face="Arial, Helvetica, sans-serif" size=".5">>The Wayang system will select different pretest and posttest the chosen pool.</font>

    <form name="form1" method="post" onsubmit="return isFormValid(form1);"
          action="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=<c:out value="${formSubmissionEvent}"/>" >

      <table width="334" border="0" height="98">
         <tr><td><input type="radio" name="poolId" value="noPretest" <c:if test="${selectedPool==0}">checked="checked"</c:if> /> </td>
              <td><font color="#000000" face="Arial, Helvetica, sans-serif">Do not give student a pretest</font></td> </tr>
          <%--@elvariable id="pools" type="edu.umass.ckc.wo.beans.PretestPool[]"--%>
          <c:forEach var="pool" items="${pools}">
              <tr><td><input <c:if test="${selectedPool==pool.id}">checked="checked"</c:if> type="radio" name="poolId" value="<c:out value="${pool.id}"/>"/></td>
                  <td><font color="#000000" face="Arial, Helvetica, sans-serif"><c:out value="${pool.description}"/></font></td></tr>

          </c:forEach>
      </table>
      <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input type="submit" name="Submit" value="Submit">
        <input type="hidden" name="classId" value="<c:out value="${classId}"/>">
        <input type="hidden" name="teacherId" value="<c:out value="${teacherId}"/>">
    </form>




      </div>
</div>

<jsp:include page="wayangTempTail.jsp" />
