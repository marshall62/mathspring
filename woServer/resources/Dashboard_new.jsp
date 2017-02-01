<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>MathSpring | My Garden</title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/common_new.css" rel="stylesheet" type="text/css" />
    <link href="css/Dashboard_new.css" rel="stylesheet" type="text/css" />
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/jchart.js"></script>
    <script>
        $(document).ready(function() {
            <c:forEach var="ts" items="${topicSummaries}">
                var topicState = "${ts.topicState}";
                var topicId = ${ts.topicId};
                var topicMastery = ${ts.mastery};
                var problemsDone = ${ts.problemsDone};
                var problemsSolved = ${ts.numProbsSolved};
                var totalProblems = ${ts.totalProblems};
                var problemsDoneWithEffort = ${ts.problemsDoneWithEffort};
                var SHINT_SOF_sequence = ${ts.SHINT_SOF_sequence};
                var SOF_SOF_sequence = ${ts.SOF_SOF_sequence};
                var neglectful_count = ${ts.neglectful_count};
                var studentState_disengaged = false;
                var chart = Chart;

                chart.init();
                chart.giveFeedbackAndPlant(
                    null,
                    "plant_"+topicId,
                    topicState,
                    studentState_disengaged,
                    topicMastery,
                    problemsDoneWithEffort,
                    SHINT_SOF_sequence,
                    SOF_SOF_sequence,
                    neglectful_count,
                    problemsDone,
                    problemsSolved);
            </c:forEach>
        });
    </script>
</head>
<body>

<!-- NAVIGATION BAR -->
<header class="site-header" role="banner">
    <div id="wrapper">
        <div class="navbar-header">
            <img class="logo" src="img/ms_mini_logo_new.svg">
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
                    <li><a href="TutorBrain?action=Logout&sessionId=${sessionId}&elapsedTime=${elapsedTime}&var=b">LOGOUT</a></li>
                </ul><!-- dropdown-menu -->
            </li><!-- dropdown -->
        </nav>
    </div><!-- wrapper -->
</header>

<section id="toggle-nav">
    <ul class="nav nav-tabs">
        <li class="navigation-tab active autofocus">
            <a data-toggle="tab" href="#my_garden">My Garden</a>
        </li>
        <li class="navigation-tab">
            <a data-toggle="tab"
               onclick="window.location='TutorBrain?action=navigation&from=sat_Hut&to=my_progress&elapsedTime=0&sessionId=${sessionId}'+ '&eventCounter=${eventCounter}' + '&topicId=-1&probId=${probId}&probElapsedTime=0&var=b'">My Progress</a>
        </li>
    </ul>
</section>

<div class="tab-content">
    <div id="my_garden" class="tab-pane fade in active">
        <!-- ALL POTS SECTION -->
        <section id="pots">
            <div class="container">
                <div class="row">
                    <c:forEach var="ts" items="${topicSummaries}">
                        <c:set var="topicName" value="${ts.topicName}"/>
                        <c:set var="numProblemsDone" value="${ts.problemsDone}"/>
                        <c:set var="numTotalProblems" value="${ts.numProbsSolved}"/>
                        <c:set var="plantDiv" value="plant_${ts.topicId}"/>
                        <c:if test="${ts.problemsDone != 0}">
                            <div class="col-md-3 text-center topic"
                                 topicTitle="${topicName}"
                                 totalProblem="${numTotalProblems}"
                                 completeProblem="${numProblemsDone}">
                                <a href="#" id=${plantLink}><div id=${plantDiv}></div></a>
                                <%--<img class="pot-image"--%>
                                     <%--onclick="showOverlay(--%>
                                        <%--this.parentElement.getAttribute('topicTitle'),--%>
                                        <%--this.parentElement.getAttribute('completeProblem'),--%>
                                        <%--this.parentElement.getAttribute('totalProblem')--%>
                                    <%--)"--%>
                                     <%--alt="Plant pot"--%>
                                <%-->--%>
                                <div class="talkbubble">
                                    <p>${topicName}</p>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
            </div><!-- container -->
        </section>
    </div><!-- student -->

    <div id="my_progress" class="tab-pane fade">
    </div>
</div><!-- tab-content -->


<!-- FIXED FOOTER, BUTTON FOR GETTING STARTED -->
<footer id="big-working-button">
    <div class="container">
        <div class="row">
            <h1>Continue Practicing</h1>
            <p>Have a good time working on these math problems</p>
        </div><!-- row -->
    </div><!-- container -->
    <a href="#">
        <span class="link-spanner"></span>
</footer>

<!-- SCRIPT - LIBRARIES -->
<script src="js/bootstrap.min.js"></script>

</body>
</html>
