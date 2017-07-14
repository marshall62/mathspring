<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>MathSpring | Student Registration</title>
    <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png">
    <link rel="icon" type="image/png" href="/favicon-32x32.png" sizes="32x32">
    <link rel="icon" type="image/png" href="/favicon-16x16.png" sizes="16x16">
    <link rel="manifest" href="/manifest.json">
    <meta name="theme-color" content="#ffffff">
    <link rel="stylesheet" href="css/bootstrap.min.css">
    <link href="css/common_new.css" rel="stylesheet" type="text/css" />
    <link href="login/css/logink12Outer_new.css" rel="stylesheet" type="text/css" />
    <link href="js/jquery-ui-1.10.4.custom/css/spring/jquery-ui-1.10.4.custom.min.css" rel="stylesheet">

    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/jquery-ui-1.10.4.custom/js/jquery-ui-1.10.4.custom.min.js"></script>
    <script type="text/javascript" src="js/simple-slider.js"></script>
    <script type="text/javascript" src="js/login_new.js"></script>
    <script type="text/javascript">

        var huygui=true;
        // Unfortunately the back button will run this function too which means that it can generate a BeginExternalActivity
        $(document).ready(function() {
            surveyButton('${servletContext}', '${servletName}', '${URL}', '${skin}', ${sessionId}, '${interventionClass}');
        });
    </script>
</head>
<body>
<div class="main-content">
    <header class="site-header" role="banner">
        <div class="row" id="wrapper">
            <div class="navbar-header">
                <img class="logo" src="img/ms_mini_logo_new.png">
            </div><!-- navbar-header -->
        </div>
    </header>
    <div class="row additional-form-wrapper">
        <div class="col-sm-6 col-sm-offset-3 additional-form-box text-center">
            <h1>Thank you for using MathSpring</h1>
            <p>Please answer some questions so the software can be more personable in helping you.</p>
            <jsp:include page="${innerjsp}" />
        </div>
    </div>
</div>
<footer class="bottom-sticky-footer">
    &copy; 2016 University of Massachusetts Amherst and Worcester Polytechnic Institute ~ All Rights Reserved.
</footer>
</body>
</html>
