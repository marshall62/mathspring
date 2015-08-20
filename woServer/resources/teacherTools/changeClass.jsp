<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="classInfo" scope="request" type="edu.umass.ckc.wo.beans.ClassInfo"/>

<div align="right">
    <table width="500" border="0" height="72">
        <tr>
            <td>
                <a href="http://wayangoutpost.info/"><img src="images/lighbulb.png" width="100" height="100" alt="help"></a>
            </td>
            <c:if test="${teacherName != null}">
                <td>Teacher:</td>
                <td>${teacherName}</td>
            </c:if>

            <td>

    <select name="classList" id="classSelection" onchange="changeClass(this,${teacherId});">;
        <c:forEach var="c" items="${bean.classes}">
            <%--<% for (var in bean.classes){  %>  --%>
            <c:if test ="${c.classid == classId}">
                <option value='${c.classid}' selected="selected">${c.classid}: ${c.name} ${c.section}</option>
            </c:if>
            <c:if test = "${c.classid != classId}">


                <option value='${c.classid}'>${c.classid}: ${c.name} ${c.section}</option>
            </c:if>
        </c:forEach>
    </select>
            </td>

        </tr>
        </table>

</div>