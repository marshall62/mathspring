<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>MathSpring Login</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <link href="login/css/loginK12_new.css" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="login/js/p7EHCscripts.js"></script>
    <script type="text/javascript">
        function signup() {
            location.href = '${pageContext.request.contextPath}/WoAdmin?action=UserRegistrationStart&startPage=${startPage}';
        }
    </script>
</head>
<body>
    <div class="container main-content">
        <c:if test="${message != null}">
            <div class="alert alert-danger msg-bar" role="alert">${message}</div>
        </c:if>
        <h1>New login page</h1>
        <div class="row">
            <div class="col-md-8 col-md-offset-2 login-box">
                <div class="row sign-in-up-box">
                    <div class="col-md-6">
                        <form
                                method="post"
                                action="${pageContext.request.contextPath}/WoAdmin?action=AdminTeacherLogin">
                            <button
                                    class="btn btn-primary btn-lg btn-block signup-btn teacher-sign-up-btn"
                                    type="submit"
                                    name="register" value="Register"
                            >Sign up for Teacher</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/WoAdmin">
                            <button
                                    class="btn btn-primary btn-lg btn-block signup-btn student-sign-up-btn"
                                    type="button"
                                    onClick="javascript:signup();"
                            >Sign up for Student</button>
                        </form>
                        <form name="guest" action="${pageContext.request.contextPath}/WoLoginServlet">
                            <input type="hidden" name="action" value="GuestLogin"/>
                            <input type="hidden" name="clientType" value="${clientType}"/>
                            <button
                                    class="btn btn-primary btn-lg btn-block signup-btn guest-try-out-btn"
                                    type="submit">Try out as Guest</button>
                        </form>
                    </div>
                    <div class="col-md-6 login-form">
                        <p>Have a username and password already? Enter them here and click the login button</p>
                        <form
                                method="post"
                                name="login"
                                action="${pageContext.request.contextPath}/WoLoginServlet">
                            <input type="hidden" name="action" value="LoginK12_2"/>
                            <input type="hidden" name="skin" value="k12"/>
                            <input type="hidden" name="var" value="b"/>
                            <div class="form-group <c:if test="${message != null}">has-error</c:if>">
                                <input
                                        type="text"
                                        name="uname"
                                        value="${userName}"
                                        class="form-control nav-login"
                                        placeholder="Username"
                                />
                            </div>
                            <div class="form-group <c:if test="${message != null}">has-error</c:if>">
                                <input
                                        type="password"
                                        name="password"
                                        value="${password}"
                                        class="form-control nav-login"
                                        placeholder="Password"
                                />
                            </div>
                            <div class="row">
                                <div class="col-md-6"></div>
                                <div class="col-md-6">
                                    <button type="submit" class="btn btn-default btn-block sign-in-btn">Login</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="row information-box">
                    <p class="text-center">To have the best experience, enable pop-ups and turn on either speakers or a headset!</p>
                </div>
            </div>
        </div>
    </div>
    <footer>&copy; 2016 University of Massachusetts Amherst ~ All Rights Reserved.</footer>
</body>
</html>
