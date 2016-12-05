<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>MathSpring | Dashboard</title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/common_new.css" rel="stylesheet" type="text/css" />
    <link href="css/Dashboard_new.css" rel="stylesheet" type="text/css" />
</head>
<body>
<!-- NAVIGATION BAR -->
<header class="site-header" role="banner">
    <div id="wrapper">
        <div class="navbar-header">
            <h1 class="logo"><span>M</span>ath<span>S</span>pring</h1>
        </div><!-- navbar-header -->

        <nav id="main_nav" class="nav navbar-nav navbar-right">
            <li class="dropdown dropdown-position custom-dropdown">
                <a href="#"
                    class="dropdown-toggle custom-dropdown-toggle"
                    data-toggle="dropdown"
                    role=button
                    aria-haspopup="true"
                    aria-expanded="false"
                >
                    <i><img src="img/avatar.svg" alt=""></i>
                    &nbsp;
                    ${studentFirstName}&nbsp;${studentLastName}
                    <span class="caret"></span>
                </a><!-- dropdown-toggle -->

                <ul class="dropdown-menu">
                    <li><a href="">HELP</a></li>
                    <li role="separator" class="divider"></li>
                    <li><a href="TutorBrain?action=Logout&sessionId=${sessionId}&elapsedTime=${elapsedTime}">LOGOUT</a></li>
                </ul><!-- dropdown-menu -->
            </li><!-- dropdown -->
        </nav>
    </div><!-- wrapper -->
</header>

<!-- CONTINUE WORKING BUTTON -->
<section id="big-working-button">
    <c:choose>
        <c:when test="${newSession}">
            <div class="container">
                <div class="row">
                    <h1>Start Working</h1>
                    <p>Have a good time working on these math problems</p>
                </div><!-- row -->
            </div><!-- container -->
            <a href="TutorBrain?action=EnterTutor&sessionId=${sessionId}&elapsedTime=${elapsedTime}&eventCounter=0">
                <span class="link-spanner"></span>
            </a>
        </c:when>
        <c:otherwise>
            <div class="container">
                <div class="row">
                    <h1>Continue Practicing</h1>
                    <p>Have a good time working on these math problems</p>
                </div><!-- row -->
            </div><!-- container -->
            <a href="TutorBrain?action=MPPReturnToHut&sessionId=${sessionId}&elapsedTime=${elapsedTime}&eventCounter=${eventCounter}&probId=${probId}&topicId=-1&learningCompanion=${learningCompanion}">
                <span class="link-spanner"></span>
            </a>
        </c:otherwise>
    </c:choose>
</section><!-- big-working-button -->

<!-- MATH-SECTIONS SECTION -->
<section id="math-section">
    <div class="container">
        <div class="row">
            <c:forEach var="ts" items="${topicSummaries}">
                <c:set var="topicName" value="${ts.topicName}"/>
                <c:set var="topicId" value="${ts.topicId}"/>
                <c:set var="masteryChartDiv" value="masteryChart_${topicId}"/>
                <c:set var="remarksDiv" value="remarks_${topicId}"/>
                <c:set var="problemsDiv" value="problemsDone_${topicId}"/>
                <c:set var="plantDiv" value="plant_${topicId}"/>
                <c:set var="commentLink" value="commentLink_${topicId}"/>
                <c:set var="plantLink" value="plantLink_${topicId}"/>
                <c:set var="backToVillageURL" value="${backToVillageURL}"/>

                <c:choose>
                    <c:when test="${ts.mastery > 0.33 && ts.mastery <= 0.66}">
                        <c:set var="progressClass" value="thirty-three-progress"/>
                    </c:when>
                    <c:when test="${ts.mastery > 0.66}">
                        <c:set var="progressClass" value="sixty-six-progress"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="progressClass" value="zero-progress"/>
                    </c:otherwise>
                </c:choose>
                <div class="col-md-4">
                    <div class="${progressClass} section-panel">
                        <h4>${topicName}</h4>
                        <div class="visible num-questions">
                            <span class="count">${ts.problemsDone}</span> / ${ts.totalProblems}
                            <p>questions</p>
                        </div>
                        <div class="progress overlay">
                            <div
                                    class="progress-bar ${progressClass}"
                                    role="progressbar"
                                    aria-valuenow="${ts.mastery * 100}"
                                    aria-valuemin="0"
                                    aria-valuemax="100"
                                    style="width: ${ts.mastery * 100}%">
                            </div><!-- progress-bar -->
                        </div><!-- progress -->
                    </div><!-- section-panel -->
                </div><!-- col-md-4 -->
            </c:forEach>
        </div> <!-- row -->
    </div> <!-- container -->
</section><!-- math section -->

<!-- SCRIPT - LIBRARIES -->
<script src="js/jquery-1.10.2.js"></script>
<script src="js/bootstrap.min.js"></script>

</body>
</html>
