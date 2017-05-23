<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>MathSpring | My Garden</title>
    <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png">
    <link rel="icon" type="image/png" href="/favicon-32x32.png" sizes="32x32">
    <link rel="icon" type="image/png" href="/favicon-16x16.png" sizes="16x16">
    <link rel="manifest" href="/manifest.json">
    <link href="css/Dashboard_new.css" rel="stylesheet" type="text/css" />
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/jchart_new.js"></script>
    <script>
        $(document).ready(function() {
            var hasPots = false;
            <c:forEach var="ts" items="${topicSummaries}">
            <c:if test="${ts.problemsDone != 0}">
            hasPots = true;
            </c:if>
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
<%--<header class="site-header" role="banner">--%>
<%--<div id="wrapper">--%>
<%--<div class="navbar-header">--%>
<%--<img class="logo" src="img/ms_mini_logo_new.png">--%>
<%--</div><!-- navbar-header -->--%>

<%--<nav id="main_nav" class="nav navbar-nav navbar-right">--%>
<%--<li class="dropdown dropdown-position custom-dropdown">--%>
<%--<a href="#"--%>
<%--class="dropdown-toggle custom-dropdown-toggle"--%>
<%--data-toggle="dropdown"--%>
<%--role=button--%>
<%--aria-haspopup="true"--%>
<%--aria-expanded="false"--%>
<%-->--%>
<%--<i><img src="img/avatar.svg" alt="Avatar"></i>--%>
<%--&nbsp;--%>
<%--${studentFirstName}&nbsp;${studentLastName}--%>
<%--<span class="caret"></span>--%>
<%--</a><!-- dropdown-toggle -->--%>

<%--<ul class="dropdown-menu">--%>
<%--<li><a href="">HELP</a></li>--%>
<%--<li role="separator" class="divider"></li>--%>
<%--<li><a href="TutorBrain?action=Logout&sessionId=${sessionId}&elapsedTime=${elapsedTime}&var=b">LOGOUT</a></li>--%>
<%--</ul><!-- dropdown-menu -->--%>
<%--</li><!-- dropdown -->--%>
<%--</nav>--%>
<%--</div><!-- wrapper -->--%>
<%--</header>--%>

<div class="nav">
    <div class="nav__logo">
        <img src="img/mstile-150x150.png" alt="" class="nav__logo-image">
        <span class="nav__logo-text">MATHSPRING</span>
    </div>

    <ul class="nav__list">
        <li class="nav__item">
            <a href="#">My Garden</a>
        </li>
        <li class="nav__item">
            <a
                    onclick="window.location='TutorBrain?action=navigation&from=sat_Hut&to=my_progress&elapsedTime=0&sessionId=${sessionId}'+ '&eventCounter=${eventCounter}' + '&topicId=-1&probId=${probId}&probElapsedTime=0&var=b'"
            >
                My Progress
            </a>
        </li>
        <li class="nav__item">
            <c:choose>
            <c:when test="${newSession}">
            <a onclick="window.location='TutorBrain?action=EnterTutor&sessionId=${sessionId}'+'&elapsedTime=${elapsedTime}' + '&eventCounter=0&var=b'">Practice Area</a>
            </c:when>
            <c:otherwise>
        <a onclick="window.location='TutorBrain?action=MPPReturnToHut&sessionId=${sessionId}'+'&elapsedTime=${elapsedTime}' + '&eventCounter=${eventCounter}' + '&probId=${probId}&topicId=-1' + '&learningCompanion=${learningCompanion}&var=b'">Practice Area</a>
        </c:otherwise>
        </c:choose>
        </li>
        <li class="nav__item nav__item--last nav__dropdown">
            <a class="nav__dropdown-button">
                ${studentFirstName}&nbsp;${studentLastName}
            </a>
            <div class="nav__dropdown-content">
                <a href="#">Help</a>
                <a href="TutorBrain?action=Logout&sessionId=${sessionId}&elapsedTime=${elapsedTime}&var=">Logout</a>
            </div>
        </li>
    </ul>
</div>

<div class="topic-list">
    <div id="Clouds">
        <div class="Cloud Foreground"></div>
        <div class="Cloud Background"></div>
        <div class="Cloud Foreground"></div>
        <div class="Cloud Background"></div>
        <div class="Cloud Foreground"></div>
        <div class="Cloud Background"></div>
        <div class="Cloud Background"></div>
        <div class="Cloud Foreground"></div>
        <div class="Cloud Background"></div>
        <div class="Cloud Background"></div>
        <!--  <svg viewBox="0 0 40 24" class="Cloud"><use xlink:href="#Cloud"></use></svg>-->
    </div>

    <div class="container">
        <c:forEach var="ts" items="${topicSummaries}">
            <c:set var="topicName" value="${ts.topicName}"/>
            <c:set var="numProblemsDone" value="${ts.problemsDone}"/>
            <c:set var="numTotalProblems" value="${ts.totalProblems}"/>
            <c:set var="plantDiv" value="plant_${ts.topicId}"/>
            <c:set var="percentDone" value="${numProblemsDone/numTotalProblems}"/>
            <c:set var="colorClass" value="0"/>
            <c:choose>
                <c:when test="${percentDone <= 0.2}">
                    <c:set var="colorClass" value="0"/>
                </c:when>

                <c:when test="${percentDone > 0.2 && percentDone <= 0.4}">
                    <c:set var="colorClass" value="20"/>
                </c:when>

                <c:when test="${percentDone > 0.4 && percentDone <= 0.6}">
                    <c:set var="colorClass" value="40"/>
                </c:when>

                <c:when test="${percentDone > 0.6 && percentDone <= 0.8}">
                    <c:set var="colorClass" value="60"/>
                </c:when>

                <c:when test="${percentDone > 0.8 && percentDone <= 1 }">
                    <c:set var="colorClass" value="80"/>
                </c:when>

            </c:choose>
            <c:choose>
                <c:when test="${ts.problemsDone>0 && ts.hasAvailableContent}">
                    <c:set var="challengeTopicLink" value="${pageContext.request.contextPath}/TutorBrain?action=MPPChallengeTopic&sessionId=${sessionId}&eventCounter=${eventCounter + 1}&topicId=${ts.topicId}&studentAction=challenge&var=b&comment=" />
                    <c:set var="continueTopicLink" value="${pageContext.request.contextPath}/TutorBrain?action=MPPContinueTopic&sessionId=${sessionId}&eventCounter=${eventCounter + 1}&topicId=${ts.topicId}&studentAction=continue&var=b&comment=" />
                    <c:set var="reviewTopicLink" value="${pageContext.request.contextPath}/TutorBrain?action=MPPReviewTopic&sessionId=${sessionId}&eventCounter=${eventCounter + 1}&topicId=${ts.topicId}&studentAction=review&var=b&comment=" />
                </c:when>
                <c:when test="${ts.problemsDone==0}">
                </c:when>
                <%--The tutor sometimes can't continue a topic if some criteria are satisfied, so we only offer review and challenge--%>
                <c:otherwise>
                    <c:set var="challengeTopicLink" value="${pageContext.request.contextPath}/TutorBrain?action=MPPChallengeTopic&sessionId=${sessionId}&eventCounter=${eventCounter + 1}&topicId=${ts.topicId}&studentAction=challenge&var=b&comment=" />
                    <c:set var="reviewTopicLink" value="${pageContext.request.contextPath}/TutorBrain?action=MPPReviewTopic&sessionId=${sessionId}&eventCounter=${eventCounter + 1}&topicId=${ts.topicId}&studentAction=review&var=b&comment=" />
                </c:otherwise>
            </c:choose>
            <c:if test="${ts.problemsDone != 0}">
                <div class="topic-list__item">
                    <div class="topic-list__flipper">
                        <div class="topic-list__front topic-list__front--${colorClass}">
                            <p class="topic-list__title">${topicName}</p>
                            <p class="topic-list__info">${numProblemsDone} / ${numTotalProblems} problems done</p>
                            <p class="topic-list__info">In Progress</p>
                            <div class="pot" id="${plantDiv}">
                            </div>
                        </div>
                        <div class="topic-list__back">
                            <p class="topic-list__title">${topicName}</p>
                            <p class="topic-list__info">${numProblemsDone} / ${numTotalProblems} problems done</p>
                            <p class="topic-list__info">In Progress</p>
                            <div class="topic-list__buttons">
                                <div
                                        class="topic-list__button topic-list__button--green"
                                        onclick="window.location='${continueTopicLink}'"
                                >
                                    CONTINUE
                                </div>

                                <div
                                        class="topic-list__button topic-list__button--yellow"
                                        onclick="window.location='${reviewTopicLink}'"
                                >
                                    REVIEW
                                </div>

                                <div
                                        class="topic-list__button topic-list__button--brown"
                                        onclick="window.location='${challengeTopicLink}'"
                                >
                                    CHALLENGE
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>
        </c:forEach>

    </div>
</div>

<%--<section id="toggle-nav">--%>
<%--<ul class="nav nav-tabs">--%>
<%--<li class="navigation-tab active autofocus">--%>
<%--<a data-toggle="tab" href="#my_garden">My Garden</a>--%>
<%--</li>--%>
<%--<li class="navigation-tab">--%>
<%--<a data-toggle="tab"--%>
<%--onclick="window.location='TutorBrain?action=navigation&from=sat_Hut&to=my_progress&elapsedTime=0&sessionId=${sessionId}'+ '&eventCounter=${eventCounter}' + '&topicId=-1&probId=${probId}&probElapsedTime=0&var=b'">My Progress</a>--%>
<%--</li>--%>
<%--</ul>--%>
<%--</section>--%>

<%--<div class="tab-content">--%>
<%--<div id="my_garden" class="tab-pane fade in active">--%>
<%--<!-- ALL POTS SECTION -->--%>
<%--<section id="pots">--%>
<%--<div class="container">--%>
<%--<div class="row plant-garden">--%>
<%--<c:forEach var="ts" items="${topicSummaries}">--%>
<%--<c:set var="topicName" value="${ts.topicName}"/>--%>
<%--<c:set var="numProblemsDone" value="${ts.problemsDone}"/>--%>
<%--<c:set var="numTotalProblems" value="${ts.totalProblems}"/>--%>
<%--<c:set var="plantDiv" value="plant_${ts.topicId}"/>--%>
<%--<c:choose>--%>
<%--<c:when test="${ts.problemsDone>0 && ts.hasAvailableContent}">--%>
<%--<c:set var="challengeTopicLink" value="${pageContext.request.contextPath}/TutorBrain?action=MPPChallengeTopic&sessionId=${sessionId}&eventCounter=${eventCounter + 1}&topicId=${ts.topicId}&studentAction=challenge&var=b&comment=" />--%>
<%--<c:set var="continueTopicLink" value="${pageContext.request.contextPath}/TutorBrain?action=MPPContinueTopic&sessionId=${sessionId}&eventCounter=${eventCounter + 1}&topicId=${ts.topicId}&studentAction=continue&var=b&comment=" />--%>
<%--<c:set var="reviewTopicLink" value="${pageContext.request.contextPath}/TutorBrain?action=MPPReviewTopic&sessionId=${sessionId}&eventCounter=${eventCounter + 1}&topicId=${ts.topicId}&studentAction=review&var=b&comment=" />--%>
<%--</c:when>--%>
<%--<c:when test="${ts.problemsDone==0}">--%>
<%--</c:when>--%>
<%--&lt;%&ndash;The tutor sometimes can't continue a topic if some criteria are satisfied, so we only offer review and challenge&ndash;%&gt;--%>
<%--<c:otherwise>--%>
<%--<c:set var="challengeTopicLink" value="${pageContext.request.contextPath}/TutorBrain?action=MPPChallengeTopic&sessionId=${sessionId}&eventCounter=${eventCounter + 1}&topicId=${ts.topicId}&studentAction=challenge&var=b&comment=" />--%>
<%--<c:set var="reviewTopicLink" value="${pageContext.request.contextPath}/TutorBrain?action=MPPReviewTopic&sessionId=${sessionId}&eventCounter=${eventCounter + 1}&topicId=${ts.topicId}&studentAction=review&var=b&comment=" />--%>
<%--</c:otherwise>--%>
<%--</c:choose>--%>
<%--<c:if test="${ts.problemsDone != 0}">--%>
<%--<div class="col-md-3 text-center topic plant-garden-pot-wrapper"--%>
<%--id=${plantDiv}--%>
<%--topicTitle="${topicName}"--%>
<%--totalProblem="${numTotalProblems}"--%>
<%--completeProblem="${numProblemsDone}"--%>
<%--challengeTopicLink="${challengeTopicLink}"--%>
<%--continueTopicLink="${continueTopicLink}"--%>
<%--reviewTopicLink="${reviewTopicLink}"--%>
<%-->--%>
<%--<div class="talkbubble">--%>
<%--<p>${topicName}</p>--%>
<%--</div>--%>
<%--</div>--%>
<%--</c:if>--%>
<%--</c:forEach>--%>
<%--</div><!-- container -->--%>
<%--<div id="pots-overlay" style="display: none">--%>
<%--<div id="overlay-content" style="display: none">--%>
<%--<p>Start Practicing to fill your garden with plants</p>--%>
<%--<img src="img/pp/smallpot.png" alt="">--%>
<%--<img src="img/pp/smallpot.png" alt="">--%>
<%--</div>--%>
<%--</div>--%>
<%--</section>--%>
<%--</div><!-- student -->--%>

<%--<div id="my_progress" class="tab-pane fade">--%>
<%--</div>--%>
<%--</div><!-- tab-content -->

<!-- FIXED FOOTER, BUTTON FOR GETTING STARTED -->
<%--<div id="big-working-button">--%>
<%--<c:choose>--%>
<%--<c:when test="${newSession}">--%>
<%--<div class="container">--%>
<%--<div class="row">--%>
<%--<h1>Start Practicing</h1>--%>
<%--<p>Have a good time working on these math problems</p>--%>
<%--</div><!-- row -->--%>
<%--</div><!-- container -->--%>
<%--<a onclick="window.location='TutorBrain?action=EnterTutor&sessionId=${sessionId}'+'&elapsedTime=${elapsedTime}' + '&eventCounter=0&var=b'"><span class="link-spanner"></span></a>--%>
<%--</c:when>--%>
<%--<c:otherwise>--%>
<%--<div class="container">--%>
<%--<div class="row">--%>
<%--<h1>Continue Practicing</h1>--%>
<%--<p>Have a good time working on these math problems</p>--%>
<%--</div><!-- row -->--%>
<%--</div><!-- container -->--%>
<%--<a onclick="window.location='TutorBrain?action=MPPReturnToHut&sessionId=${sessionId}'+'&elapsedTime=${elapsedTime}' + '&eventCounter=${eventCounter}' + '&probId=${probId}&topicId=-1' + '&learningCompanion=${learningCompanion}&var=b'"><span class="link-spanner"></span></a>--%>
<%--</c:otherwise>--%>
<%--</c:choose>--%>

<%--</div>--%>


<!-- SCRIPT - LIBRARIES -->
<script src="js/bootstrap.min.js"></script>

</body>
</html>
