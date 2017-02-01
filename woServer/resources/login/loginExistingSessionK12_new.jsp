<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>MathSpring | Existing Session</title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/common_new.css" rel="stylesheet">
    <link href="login/css/loginExistingSessionK12_new.css" rel="stylesheet">
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
    <div class="row system-message-box-wrapper">
        <div class="col-sm-6 col-sm-offset-3 system-message-box">
            <h1 class="system-message-title">
                You have existing session
            </h1>
            <p class=".system-message-content">${message}</p>
            <div class="row">
                <form method="post" name="login" action="${pageContext.request.contextPath}/WoLoginServlet">
                    <input type="hidden" name="action" value="LoginK12_1"/>
                    <input type="hidden" name="skin" value="k12"/>
                    <input type="hidden" name="var" value="b"/>
                    <input
                            class="col-sm-offset-1 col-sm-4 btn btn-primary mathspring-btn return-home-btn"
                            value="Return to Login Page"
                            type="submit"/>
                </form>
                <form id="signupForm" action="${pageContext.request.contextPath}/WoLoginServlet">
                    <input type="hidden" name="action" value="LoginK12_2"/>
                    <input type="hidden" name="uname" value="${uname}"/>
                    <input type="hidden" name="password" value="${password}"/>
                    <input type="hidden" name="logoutExistingSession" value="true"/>
                    <input type="hidden" name="skin" value="k12"/>
                    <input type="hidden" name="var" value="b"/>
                    <input
                            class="col-sm-offset-2 col-sm-4 btn btn-primary mathspring-btn"
                            value="I'm sure I want to login"
                            type="submit"/>
                </form>
            </div>
        </div>
    </div>
</div>
<footer class="bottom-sticky-footer">
    &copy; 2016 University of Massachusetts Amherst and Worcester Polytechnic Institute ~ All Rights Reserved.
</footer>
</body>
</html>
