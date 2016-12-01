<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>MathSpring K-12</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <script type="text/javascript" src="login/js/p7EHCscripts.js"></script>
    <script type="text/javascript">
        function signup() {
            location.href = '${pageContext.request.contextPath}/WoAdmin?action=UserRegistrationStart&startPage=${startPage}';
        }
    </script>
</head>
<body class="container">
    <%-- @WPI login --%>
    <h1>New login page</h1>
</body>
</html>
