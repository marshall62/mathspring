<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>MathSpring | Student Registration</title>
    <link rel="stylesheet" href="css/bootstrap.min.css">
    <link href="css/common_new.css" rel="stylesheet" type="text/css" />
    <link href="login/css/userregistration_new.css" rel="stylesheet" type="text/css" />
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
                <div class="alert alert-danger msg-bar hidden" role="alert"></div>
                <h1 class="text-center form-label">Sign up for students</h1>
                <form class="student-registration-form" method="post">
                    <input type="hidden" name="action" value="LoginK12_2"/>
                    <input type="hidden" name="skin" value="k12"/>
                    <input type="hidden" name="var" value="b"/>
                    <div class="row">
                        <div class="form-group col-sm-6">
                            <input type="text" class="form-control" id="first_name" placeholder="Enter your first name" name="fname">
                        </div><!-- form-group -->
                        <div class="form-group col-sm-6">
                            <input type="text" class="form-control" id="last_name" placeholder="Enter your last name" name="lname">
                        </div><!-- form-group -->
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-6">
                           <input type="text" class="form-control" id="age" placeholder="Age" name="age">
                        </div><!-- form-group -->
                        <div class="form-group col-sm-6">
                            <select class="form-control" id="sel1" name="gender">
                                <option value="male">Male</option>
                                <option value="female">Female</option>
                            </select>
                        </div><!-- form-group -->
                    </div>
                    <div class="form-group">
                        <input type="email" class="form-control" id="email" placeholder="Enter email" name="email">
                    </div><!-- form-group -->
                    <div class="form-group username-wrapper">
                        <input type="text" class="form-control" id="username" placeholder="Enter username" name="uname">
                    </div><!-- form-group -->
                    <div class="form-group password-wrapper">
                        <input type="password" class="form-control" id="password" placeholder="Enter password" name="password">
                    </div><!-- form-group -->
                    <div class="form-group row">
                        <div class="col-md-offset-2 col-md-8">
                            <div class="form-check">
                                <label class="form-check-label">
                                    <input class="form-check-input" type="radio" name="userType" id="exampleRadios1" value="student" checked>
                                    &nbsp;Regular Student
                                </label>
                            </div>
                            <div class="form-check">
                                <label class="form-check-label">
                                    <input class="form-check-input" type="radio" name="userType" id="exampleRadios2" value="testStudent">
                                    &nbsp;System testing (student view)
                                </label>
                            </div>
                            <div class="form-check">
                                <label class="form-check-label">
                                    <input class="form-check-input" type="radio" name="userType" id="exampleRadios3" value="testDeveloper">
                                    &nbsp;System testing (developer view)
                                </label>
                            </div>
                        </div>
                    </div>
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
    <script type="text/javascript" src="js/jquery-1.10.2.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            var usernameInput = $('.username-wrapper');
            var passwordInput = $('.password-wrapper');
            var messageBar = $('.msg-bar');
            $('.student-registration-form').on('submit', function(event) {
                event.preventDefault();
                $.ajax({
                    url: "${pageContext.request.contextPath}/WoAdmin"
                    + "?action=UserRegistrationValidateUsername"
                    + "&userName=" + $('#username').val(),
                    success: function(responseText) {
                        if ($('#password').val() === '') {
                            messageBar.removeClass('hidden');
                            messageBar.text("Please provide a password");
                            passwordInput.addClass('has-error');
                        } else if (responseText === "") {
                            var fname = $('#first_name').val();
                            var lname = $('#last_name').val();
                            var uname = $('#username').val();
                            var email = $('#email').val();
                            var age = $('#age').val();
                            var gender = $('#gender').val();
                            var password = $('#password').val();
                            var userType = $('input:radio[name=userType]:checked').val();
                            location.href = "${pageContext.request.contextPath}/WoAdmin?"
                                    + "action=UserRegistrationAuthenticationInfo"
                                    + "&fname=" + fname
                                    + "&lname=" + lname
                                    + "&uname=" + uname
                                    + "&email=" + email
                                    + "&password=" + password
                                    + "&age=" + age
                                    + "&gender=" + gender
                                    + "&userType=" + userType
                                    + "&startPage=${startPage}"
                                    + "&var=b";
                        } else {
                            messageBar.removeClass('hidden');
                            messageBar.text(responseText);
                            usernameInput.addClass('has-error');
                        }
                    },
                    error: function() {
                        usernameInput.addClass('has-error');
                        messageBar.removeClass('hidden');
                    },
                    async: true});
            });
        });
    </script>
</body>
</html>
