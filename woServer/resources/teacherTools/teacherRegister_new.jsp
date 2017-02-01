<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>MathSpring | Teacher Registration</title>
    <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png">
    <link rel="icon" type="image/png" href="/favicon-32x32.png" sizes="32x32">
    <link rel="icon" type="image/png" href="/favicon-16x16.png" sizes="16x16">
    <link rel="manifest" href="/manifest.json">
    <meta name="theme-color" content="#ffffff">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/common_new.css" rel="stylesheet">
    <link href="teacherTools/css/teacherRegister_new.css" rel="stylesheet">
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
        <div class="row registration-box-wrapper">
            <div class="col-sm-6 col-sm-offset-3 registration-box">
                <c:if test="${message != null && not empty message}">
                    <div class="alert alert-danger msg-bar" role="alert">${message}</div>
                </c:if>
                <h3 class="text-center form-label">Sign up for teachers</h3>
                <form
                        class="form-horizontal teacher-registration-form"
                        method="post"
                        action="${pageContext.request.contextPath}/WoAdmin?action=AdminTeacherRegistration"
                >
                    <div class="form-group">
                        <label class="control-label col-sm-4" for="first_name">First Name:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="first_name" placeholder="Enter your first name">
                        </div>
                    </div><!-- form-group -->
                    <div class="form-group">
                        <label class="control-label col-sm-4" for="last_name">Last Name:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="last_name" placeholder="Enter your last name">
                        </div>
                    </div><!-- form-group -->
                    <div class="form-group">
                        <label class="control-label col-sm-4" for="email">Email:</label>
                        <div class="col-sm-8">
                            <input type="email" class="form-control" id="email" placeholder="Enter email">
                        </div>
                    </div><!-- form-group -->
                    <div class="form-group">
                        <label class="control-label col-sm-4" for="username">Username:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="username" placeholder="Enter username">
                        </div>
                    </div><!-- form-group -->
                    <div class="form-group">
                        <label class="control-label col-sm-4" for="password">Password:</label>
                        <div class="col-sm-8">
                            <input type="password" class="form-control" id="password" placeholder="Enter password">
                        </div>
                    </div><!-- form-group -->
                    <div class="form-group">
                        <label class="control-label col-sm-4" for="password">Retype password:</label>
                        <div class="col-sm-8">
                            <input type="password" class="form-control" id="password-confirmation" placeholder="Retype password">
                        </div>
                    </div><!-- form-group -->
                    <div class="form-group row">
                        <div class="col-sm-offset-3 col-sm-6">
                            <button type="submit" class="btn btn-default btn-block mathspring-btn">Submit</button>
                        </div>
                    </div><!-- form-group -->
                </form>
            </div>
        </div>
    </div>
    <footer class="bottom-sticky-footer">
        &copy; 2016 University of Massachusetts Amherst and Worcester Polytechnic Institute ~ All Rights Reserved.
    </footer>
</body>
</html>
