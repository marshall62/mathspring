<%--
  Created by IntelliJ IDEA.
  User: marshall
  Date: Apr 20, 2011
  Time: 3:27:11 PM
  To change this template use File | Settings | File Templates.
--%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%--<jsp:include page="teacherToolsHeader.jsp" />
<jsp:include page= "classInfoHeader.jsp" />                --%>
<jsp:include page="wayangTempHead.jsp" />

<jsp:useBean id="activeHuts" scope="request" type="edu.umass.ckc.wo.beans.ClassConfig"/>
<jsp:useBean id="classInfo" scope="request" type="edu.umass.ckc.wo.beans.ClassInfo" />




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
  <div id="Layer1" align="center">
    <p  class="a2"><font color="#000000"><b><font face="Arial, Helvetica, sans-serif">Activate Huts</font></b></font></p>
    <p   size=".5"> Select huts to be active and available. </p>
    <form name="form1" method="post" onsubmit="return isFormValid(form1);"
          action="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=<c:out value="${formSubmissionEvent}"/>" >
      <table width="334" border="0" height="98">
         <tr><td valign="top" width="40"><input type="checkbox" <c:if test="${activeHuts.useDefaults}">checked</c:if> name="useDefaultActivationRules"/> </td>
              <td valign="top" width="305"><font color="#000000" face="Arial, Helvetica, sans-serif">Use Default Activation Rules</font></td>
        </tr>
         <tr><td valign="top" width="40"><input type="checkbox" <c:if test="${!activeHuts.useDefaults && activeHuts.pretest==1}">checked</c:if> name="pretest"/> </td>
              <td valign="top" width="305"><font color="#000000" face="Arial, Helvetica, sans-serif">Pre test</font></td>
        </tr>
        <tr><td valign="top" width="40"><input type="checkbox" <c:if test="${!activeHuts.useDefaults && activeHuts.tutoring==1}">checked</c:if> name="tutor"/> </td>
              <td valign="top" width="305"><font color="#000000" face="Arial, Helvetica, sans-serif">Tutor</font></td>
        </tr>  
        <tr><td valign="top" width="40"><input type="checkbox" <c:if test="${!activeHuts.useDefaults && activeHuts.posttest==1}">checked</c:if> name="posttest"/> </td>
              <td valign="top" width="305"><font color="#000000" face="Arial, Helvetica, sans-serif">Post test</font></td>
        </tr>  
        <tr><td valign="top" width="40"><input type="checkbox" <c:if test="${!activeHuts.useDefaults && activeHuts.fantasy==1}">checked</c:if> name="adventures"/> </td>
              <td valign="top" width="305"><font color="#000000" face="Arial, Helvetica, sans-serif">Adventures</font></td>
        </tr>   

        <tr><td valign="top" width="40"><input type="checkbox" <c:if test="${!activeHuts.useDefaults && activeHuts.mfr==1}">checked</c:if> name="mfr"/> </td>
              <td valign="top" width="305"><font  face="Arial, Helvetica, sans-serif">Math Fact Retrieval</font></td>
        </tr>
        
        <tr><td valign="top" width="40"><input type="checkbox" <c:if test="${!activeHuts.useDefaults && activeHuts.spatial==1}">checked</c:if> name="mr"/> </td>
              <td valign="top" width="305"><font  face="Arial, Helvetica, sans-serif">Mental Rotation</font></td>

      </table>
      <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input type="submit" name="submit" value="Submit">
          <p>&nbsp;</p>
        <input type="submit" name="restoreDefaults" value="Restore Defaults">
        <input type="hidden" name="classId" value="<c:out value="${classId}"/>">
        <input type="hidden" name="teacherId" value="<c:out value="${teacherId}"/>">

    </form>
      <c:choose>
      <c:when test="${activeHuts.useDefaults}"><br><b><font color="#000000">Default Rules are currently in use.</font></b></c:when>
      <c:otherwise><br><b><font color="#000000">Default Rules are NOT currently in use.</font></b></c:otherwise>
      </c:choose>
      <br> <br> <font color="#000000">By default, the huts activate on a per-student basis according to certain rules.   If you use this
        form to activate and deactivate huts, each student in the class will see the same active huts. The rules will no longer be in play and you will be required
      to use this form to turn on/off huts for the duration of this class's use of the tutor unless you select the "Restore Defaults" button.  </font>
      <p style="color: #000000">

      <p>

  </div>
</div>


<jsp:include page="wayangTempTail.jsp" />
