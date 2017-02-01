<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>MathSpring | Student Registration</title>
    <link rel="stylesheet" href="css/bootstrap.min.css">
    <link href="css/common_new.css" rel="stylesheet" type="text/css" />
    <link href="login/css/userRegComplete_new.css" rel="stylesheet" type="text/css" />
</head>
<body>
    <div class="main-content">
        <header class="site-header" role="banner">
            <div class="row" id="wrapper">
                <div class="navbar-header">
                    <img class="logo" src="img/ms_mini_logo_new.svg">
                </div><!-- navbar-header -->
            </div>
        </header>
        <div class="row account-creation-announcement-wrapper">
            <div class="col-sm-6 col-sm-offset-3 account-creation-announcement">
                <h1>Your Mathspring user has been created</h1>
                <p>First Name: ${fName}</p>
                <p>Last Name: ${lName}</p>
                <p>Teacher: ${teacher}</p>
                <p>Class: ${className}</p>
                <p>User Name: ${userName}</p>
                <form id="form1" method="post" name="login" action="${pageContext.request.contextPath}/WoLoginServlet?action=LoginK12_1&var=b">
                    <input type="hidden" name="action" value="${startPage}"/>
                    <input class="btn btn-default btn-block mathspring-btn" type="submit" name="relogin" value="Return to Mathspring login">
                </form>
            </div>
        </div>
    </div>
    <footer class="bottom-sticky-footer">
        &copy; 2016 University of Massachusetts Amherst and Worcester Polytechnic Institute ~ All Rights Reserved.
    </footer>
</body>
</html>
