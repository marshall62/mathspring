<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>MathSpring | Topic Details</title>
    <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png">
    <link rel="icon" type="image/png" href="/favicon-32x32.png" sizes="32x32">
    <link rel="icon" type="image/png" href="/favicon-16x16.png" sizes="16x16">
    <link rel="manifest" href="/manifest.json">
    <meta name="theme-color" content="#ffffff">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/common_new.css" rel="stylesheet" type="text/css"/>
    <link href="css/TopicDetails_new.css" rel="stylesheet" type="text/css"/>
    <link href="css/graph_new.css" rel="stylesheet" type="text/css"/>
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/jchart.js"></script>

    <script language="javascript" type="text/javascript">
        var problemList=new Array();
        var currentProblem="";
        var currentTopic=${topicId};
        var currentProblemId=0;
        var currentEffort="";
        var formalityId="";
        var isFormality=false;
        var problemImagePath="";
        var effortFeedback="";
        var elapsedTime=0;
        var probElapsedTime=0;
        var startClockTime = 0;
        var startElapsedTime=0;
        var useHybridTutor=${useHybridTutor};
        var selectedCard = null;

        function initiateElapsedTime() {
            startElapsedTime= ${elapsedTime} ;
            var d = new Date();
            startClockTime = d.getTime();
        }

        function updateElapsedTime() {
            var d = new Date();
            var now = d.getTime();
            probElapsedTime += now-startClockTime;
            elapsedTime = startElapsedTime + probElapsedTime;
            return elapsedTime;
        }

        function initChart() {
            var chart = Chart;
            chart.init();
            chart.renderMastery("masteryChartDiv",${mastery} ,${problemsDone} );
            var i=0;

            <c:forEach var="pd" items="${problemDetailsList}">
                problemList[i]=new Array(8);
                problemList[i][0]="${pd.problemId}";
                problemList[i][1]="${pd.problemName}";
                problemList[i][2]="${pd.effort}";
                problemList[i][3]=${pd.numAttemptsToSolve};
                problemList[i][4]=${pd.numHints};
                problemList[i][6]="${pd.ccstds}";
                problemList[i][7]="${pd.snapshot}";
                i++;
            </c:forEach>

            chart.renderCharts(problemList,i,wrapperList);
        }

        $(document).ready(function(){
            initiateElapsedTime();
            initChart();

            function tryThisComplete (problemId) {
                window.location='${backToVillageURL}&sessionId=${sessionId}&learningHutChoice=true&elapsedTime='+updateElapsedTime()+'&learningCompanion=${learningCompanion}&mode=practice&topicId='+currentTopic+'&problemIdString='+problemId+'&var=b';
            }

            $('#wrapperList li').each(function(index) {
                $(this).addClass('non-selected-card');
                $(this)
                    .click(function() {
                        if (useHybridTutor)
                            window.location= "${pageContext.request.contextPath}/TutorBrain?action=MPPTryProblem&elapsedTime="+updateElapsedTime()+"&sessionId=${sessionId}&problemId="+currentProblemId+"&topicId="+currentTopic+"&studentAction=tryThis&mode=practice&var=b&comment=";
                        else
                            $.get("${pageContext.request.contextPath}/TutorBrain?action=MPPTryProblem&elapsedTime="+updateElapsedTime()+"&sessionId=${sessionId}&problemId="+currentProblemId+"&topicId="+currentTopic+"&studentAction=tryThis&var=b&comment=",tryThisComplete(currentProblemId));
                    })
                    .hover(function() {
                        loadProblem($(this));
                    });
                if (index == 0) $(this).hide();
                if (index == 1) loadProblem($(this));

                function loadProblem(card) {
                    if (selectedCard != null) {
                        selectedCard.removeClass('selected-card');
                        selectedCard.addClass('non-selected-card');
                    }
                    selectedCard = card;
                    card.removeClass('non-selected-card');
                    card.addClass("selected-card");
                    var position = $("#problemCards").position();
                    var tPosX = position.left ;
                    var tPosY = position.top+$("#problemCards").height();
                    $(".dropDownContent").css({top:tPosY, left: tPosX}).show();
                    currentProblemId=problemList[index-1][0];
                    currentProblem=problemList[index-1][1];
                    currentEffort=problemList[index-1][2];
                    effortFeedback=problemList[index-1][5];

                    if (currentProblem.substring(0,10)=="formality_")
                    {
                        isFormality=true;
                        formalityId= currentProblem.substring(10,currentProblem.length);
                    }
                    else isFormality=false;

                    if (!isFormality) {
                        $("#js-problem-view").text(effortFeedback);
                        $("#js-problem-view").append("<img id='problemImage' />");
                        document.getElementById("problemImage").src ="data:image/jpg;base64,"+problemList[index-1][7];
                        console.log("CCSS: " + problemList[index-1][6]);
                    } else {
                        $("#js-problem-view").text(currentProblem);
                        $("#js-problem-view").append("<iframe id='formalityProblemFrame' width='600' height='300'> </iframe>");
                        problemImagePath="http://cadmium.cs.umass.edu/formality/FormalityServlet?fxn=questionSnapshot&qID="+formalityId;
                        document.getElementById("formalityProblemFrame").src = problemImagePath;
                        console.log("CCSS: " + problemList[index-1][6]);
                    }

                    document.getElementById("problemDetailsButtons").onclick = function() {
                        if (useHybridTutor)
                            window.location="${pageContext.request.contextPath}/TutorBrain?action=MPPTryProblem&elapsedTime="+updateElapsedTime()+"&sessionId=${sessionId}&problemId="+currentProblemId+"&topicId="+currentTopic+"&studentAction=tryThis&mode=practice&comment=";
                        else $.get("${pageContext.request.contextPath}/TutorBrain?action=MPPTryProblem&elapsedTime="+updateElapsedTime()+"&sessionId=${sessionId}&problemId="+currentProblemId+"&topicId="+currentTopic+"&studentAction=tryThis&mode=practice&comment=",tryThisComplete(currentProblemId));
                    };
                }
            });

            $(".js-go-to-my-garden").click(function() {
                var currentTopicId = ${topicId};
                updateElapsedTime();
                if (useHybridTutor) {
                    window.location = "${pageContext.request.contextPath}/TutorBrain?action=Home&sessionId=${sessionId}&eventCounter=${eventCounter + 1}"
                        + "&topicId=" + currentTopicId
                        + "&probId=" + currentProblemId
                        + "&elapsedTime=" + elapsedTime
                        + "&probElapsedTime=" + probElapsedTime
                        + "&learningCompanion=${learningCompanion}"
                        + "&var=b";
                } else {
                    $.get("${pageContext.request.contextPath}/TutorBrain?action=MPPReturnToHut&sessionId=${sessionId}&eventCounter=${eventCounter + 1}&topicId="+currentTopicId+"&studentAction=backToSatHut&var=b&comment=",returnToHutComplete);
                }
            });
        });
    </script>
</head>
<body>
<header class="site-header" role="banner">
    <div id="wrapper">
        <div class="navbar-header">
            <img class="logo" src="img/ms_mini_logo_new.png">
        </div><!-- navbar-header -->

        <nav id="main_nav" class="nav navbar-nav navbar-right">
            <li class="dropdown dropdown-position custom-dropdown">
                <a class="dropdown-toggle custom-dropdown-toggle"
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
                    <li><a>HELP</a></li>
                    <li role="separator" class="divider"></li>
                    <li><a href="TutorBrain?action=Logout&sessionId=${sessionId}&elapsedTime=${elapsedTime}&var=b">LOGOUT</a>
                    </li>
                </ul><!-- dropdown-menu -->
            </li><!-- dropdown -->
        </nav>
    </div><!-- wrapper -->
</header>

<section id="toggle-nav">
    <ul class="nav nav-tabs">
        <li class="navigation-tab">
            <a class="js-go-to-my-garden" data-toggle="tab">My Garden</a>
        </li>
        <li class="navigation-tab active autofocus">
            <a data-toggle="tab"
               href="#"
               onclick="window.location='TutorBrain?action=navigation&from=sat_Hut&to=my_progress'
                       + '&elapsedTime=0&sessionId=${sessionId}'
                       + '&eventCounter=${eventCounter}'
                       + '&topicId=-1&probId=${probId}&probElapsedTime=0&var=b'"
            >
                My Progress > Topic Details</a>
        </li>
    </ul>
</section>

<div class="main-content">
    <div class="row topic-details-wrapper">
        <div class="topic-details-view">
            <h1>${topicName}</h1>
            <div class="row topic-overview">
                <div class="col-md-4">
                    <div class="row topic-statistics">
                        <h2>Mastery Level</h2>
                        <div id="masteryChartDiv"></div>
                        <div>
                            <p class="problem-done-num">${problemsDone}/${totalProblems}</p>
                            <p>Problems Done</p>
                        </div>
                    </div>
                    <div class="row" id="problemCards" rel="performanceDetails">
                        <ul id="wrapperList"><li></li></ul>
                    </div>
                </div>
                <div class="col-md-8 detail-problem-view">
                    <div id="js-problem-view"></div>
                    <div class="row">
                        <button type="button"
                                class="btn btn-lg mathspring-btn"
                                id="problemDetailsButtons">Click to try this problem</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<footer>&copy; 2016 University of Massachusetts Amherst and Worcester Polytechnic Institute ~ All Rights Reserved.
</footer>

</body>
</html>
