<%--
  Created by IntelliJ IDEA.
  User: marshall
  Date: 9/30/14
  Time: 11:42 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/jquery.tablesorter.js"></script>
    <script>

        function forceNextProblem (id) {
            window.parent.forceNextProblem(id);
        }

        $(document).ready(function()
                {
                    $("#myTable").tablesorter( {sortList: [[0,0], [1,0], [2,0]]} );
                }
        );


    </script>
    <title></title>
    <style>


    </style>
</head>
<body>
<table id="myTable" class="tablesorter">
    <thead>
    <tr><th>problem ID</th><th>name</th><th>status</th><th>select</th></tr>
    </thead>
    <tbody>
    <%--@elvariable id="problems" type="edu.umass.ckc.wo.content.Problem[]"--%>
<c:forEach var="problem" items="${problems}">
    <tr><td>${problem.id}</td><td>${problem.name}</td><td>${problem.status}</td><td>&nbsp; &nbsp;<a onclick="forceNextProblem(${problem.id})" href="#">choose</a></td></tr>
</c:forEach>
    </tbody>
</table>

</body>
</html>