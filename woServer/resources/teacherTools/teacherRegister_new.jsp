<%@ include file="partials/teacherRegister-header.jsp" %>

<%@ include file="partials/teacherRegister-nav.jsp" %>

<div class="bootstrap main-content">
    <div class="row registration-form">
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

<%@ include file="partials/teacherRegister-footer.jsp" %>
