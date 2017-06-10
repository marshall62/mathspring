<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>MathSpring | Tutoring</title>
    <link href="js/jquery-ui-1.10.4.custom/css/spring/jquery-ui-1.10.4.custom.min.css" rel="stylesheet">
    <%--<link href="css/bootstrap.min.css" rel="stylesheet">--%>
    <link href="css/animate.css" rel="stylesheet">
    <%--<link href="css/common_new.css" rel="stylesheet">--%>
    <link href="css/mathspring_new.css" rel="stylesheet">
    <!-- css for data table -->
    <link href="https://cdn.datatables.net/1.10.13/css/dataTables.bootstrap4.min.css" rel="stylesheet" type="text/css">
    <link href="https://cdn.datatables.net/colreorder/1.3.2/css/colReorder.bootstrap4.min.css" rel="stylesheet" type="text/css">

    <!-- css for bootstrap / Font Awesome -->
    <link rel="stylesheet" href="<c:url value="/js/bootstrap/css/bootstrap-prefix.css" />" />
    <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet">
    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.2/css/bootstrap-select.min.css">

    <!-- updated Jquery to 2.2.2 to make use of bootstrap js-->
    <script type="text/javascript" src="<c:url value="/js/bootstrap/js/jquery-2.2.2.min.js" />"></script>
    <!-- js for bootstrap-->
    <script type="text/javascript" src="<c:url value="/js/bootstrap/js/bootstrap.min.js" />"></script>

    <%--<script src="js/jquery-ui-1.10.3/ui/jquery-ui.js"></script>--%>
    <script src="js/jquery-ui-1.10.4.custom/js/jquery-ui-1.10.4.custom.min.js"></script>
    <script src="js/jquery.dialogextend.min.js"></script>
    <!-- Latest compiled and minified JavaScript -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.2/js/bootstrap-select.min.js"></script>


    <!-- js for data table -->
    <script type="text/javascript" src="<c:url value="https://cdn.datatables.net/1.10.13/js/jquery.dataTables.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="https://cdn.datatables.net/1.10.13/js/dataTables.bootstrap4.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="https://cdn.datatables.net/colreorder/1.3.2/js/dataTables.colReorder.min.js" />"></script>
    <!-- js for bootstrap-->
    <script type="text/javascript" src="js/simple-slider.js"></script>
    <script type="text/javascript" src="js/tutorutils.js"></script>
    <script type="text/javascript" src="js/tutorAnswer.js"></script>
    <script type="text/javascript" src="js/tutorhint.js"></script>
    <script type="text/javascript" src="js/tutorhut_new.js"></script>
    <script type="text/javascript" src="js/tutorintervention.js"></script>
    <script type="text/javascript" src="js/intervhandlers_new.js"></script>
    <script type="text/javascript" src="js/swfobject.js"></script>
    <script type="text/javascript">
        var globals = {
            lastProbType: '${lastProbType}',
            isBeginningOfSession: ${isBeginningOfSession},
            sessionId: ${sessionId},
            elapsedTime: ${elapsedTime},
            probElapsedTime: 0,
            clock: 0,
            curHint: null,
            exampleCurHint: null,
            hintSequence: null,
            exampleHintSequence: null,
            lastProbId: ${lastProbId},
            trace: false,
            debug: false,
            topicId: ${topicId},
            guiLock: false,
            learningCompanion: '${learningCompanion}',
            learningCompanionMessageSelectionStrategy: '${learningCompanionMessageSelectionStrategy}',
            userName: '${userName}',
            studId : ${studId} ,
            probType : '${probType}',
            exampleProbType : null,
            probId : ${probId},
            probMode: '${probMode}',
            tutoringMode: '${tutoringMode}',
            instructions : '${instructions}',
            resource : '${resource}',
            form : '${form}',
            answer : '${answer}',
            interventionType: null,
            isInputIntervention: false ,
            learningCompanionClip: null,
            activityJSON: ${activityJSON},
            showMPP: ${showMPP},
            units: null,
            <%--The fields below turn on things for test users --%>
            showSelectProblemButton: ${showProblemSelector},
            showAnswer: ${showAnswer},
            newAnswer: null,
            params: null ,
            resumeProblem: ${resumeProblem},
            statementHTML: null,
            questionAudio: null,
            questionImage: null,
            hints: null,
            answers: null ,
            numHintsSeen: 0,
            numHints: 0 ,
            destinationInterventionSelector: null ,
            clickTime: 0

        }

        var sysGlobals = {
            isDevEnv: ${isDevEnv},
            wayangServletContext: '${wayangServletContext}',
            problemContentDomain : '${problemContentDomain}',
            problemContentPath : '${problemContentPath}',
            webContentPath : '${webContentPath}',
            <%--servletContextPath : '${pageContext.request.contextPath}',--%>
            servletName : '${servletName}',
            probplayerPath : '${probplayerPath}',
            wait: false,
            eventCounter: ${eventCounter}

        }

        var transients = {
            answerChoice: null  ,
            selectedButton: null ,
            answersChosenSoFar: [] ,
            sym: null,
            component: null,
            componentAction: null,
            learningCompanionTextMessage: null
        }


        // Unfortunately the back button will run this function too which means that it can generate a BeginExternalActivity
        $(document).ready(function () {
            tutorhut_main(globals,sysGlobals,transients, "${learningCompanionMovie}");
            $('.ui-dialog-buttonset > button').each(function() {
                $(this).addClass('btn btn-lg mathspring-btn');
            });
            if (!globals.showAnswer) {
                $(".dev-view").remove();
            } else {
                $(".dev-view").show();
            }

            // Adjust the width of the character window
            var srcCompanion = $('#learningCompanionWindow').attr('src');
            var isJake = /.*Jake/.test(srcCompanion);
            if (isJake) {
                $('.huytran-practice__character-window').width(269);
            } else {
                $('.huytran-practice__character-window').width(260);
            }
        });
    </script>


    <style type="text/css">
        .leftcol {
            padding: 8px;
            float: right;
            width: 1000px;
        }

        .empty {
        }

    </style>
</head>
<body>



<!-- NAVIGATION BAR -->
<%--<header class="site-header" role="banner">--%>
    <%--<div id="wrapper">--%>
        <%--<div class="navbar-header">--%>
            <%--<img class="logo goto-dashboard-js" src="img/ms_mini_logo_new.png" alt="MathSpring Logo">--%>
        <%--</div><!-- navbar-header -->--%>

        <%--<nav id="main_nav" class="nav navbar-nav navbar-right">--%>
            <%--<li class="dropdown dropdown-position custom-dropdown">--%>
                <%--<a  href="#"--%>
                    <%--class="dropdown-toggle custom-dropdown-toggle"--%>
                    <%--data-toggle="dropdown"--%>
                    <%--role=button--%>
                    <%--aria-haspopup="true"--%>
                    <%--aria-expanded="false"--%>
                <%-->--%>
                    <%--<i><img src="img/avatar.svg" alt="Avatar"></i>--%>
                    <%--&nbsp;--%>
                    <%--${studentFirstName} ${studentLastName}--%>
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

<%--<section id="navigation-back">--%>
    <%--<div id="home" class="col-md-6">My Garden</div>--%>
    <%--<div id="myProg" class="col-md-6">My Progress</div>--%>
<%--</section>--%>

<audio id='questionaudio' name='questionaudio'>
    <source id='questionogg' src='' type='audio/ogg'>
    <source id='questionmp3' src='' type='audio/mpeg'>Your browser does not support the audio element.
</audio>

<%-- This div is a dialog that is shown when the user clicks on Show Example.  It plays an example problem in the dialog--%>
<div id="exampleContainer" width="600" height="600" title="Watch/listen to this example. Use 'Play Next Step' to move along" >
    <%-- This iframe gets replaced by swfobject.embed.   It replaces it with the Flash object/embed tags for showing a problem OR an the html
     of an HTML5 problem (perhaps in an iframe if we must)--%>
    <iframe id="exampleFrame"
                name="iframe2"
                width="600"
                height="600"
                src=""
                frameborder="no"
                scrolling="no">
        </iframe>
    </div>

<div id="utilDialog" title="">
    <iframe id="utilDialogIframe" width="675" height="675"> </iframe>
</div>

<div id="interventionDialog" title="">
    <div id="interventionDialogContent"></div>
</div>

<div id="selectProblemDialog" title="Select Problem">
    <iframe id="selectProblemDialogIframe" width="500" height="500"></iframe>
</div>

<%--<section id="main-tutoring">--%>
    <%--<div class="container">--%>
        <%--<div class="row">--%>
            <%--<div class="col-md-1 vertical-button">--%>
                <%--<div data-balloon="Read Problem" data-balloon-pos="right">--%>
                    <%--<a id="read">--%>
                        <%--<img src="img/speaker.svg" alt="Read Problem">--%>
                    <%--</a>--%>
                <%--</div>--%>
                <%--<div data-balloon="Show Instruction" data-balloon-pos="right">--%>
                    <%--<a id="instructions">--%>
                        <%--<img src="img/info.svg" alt="Show Instruction">--%>
                    <%--</a>--%>
                <%--</div>--%>
                <%--<div class="dropdown custom-dropdown"--%>
                     <%--data-balloon="More Resources"--%>
                     <%--data-balloon-pos="right">--%>
                    <%--<a  href="#"--%>
                        <%--class="dropdown-toggle custom-dropdown-toggle"--%>
                        <%--data-toggle="dropdown"--%>
                        <%--role=button--%>
                        <%--aria-haspopup="true"--%>
                        <%--aria-expanded="false"--%>
                    <%-->--%>
                        <%--<img src="img/menu.svg" alt="">--%>
                    <%--</a><!-- dropdown-toggle -->--%>

                    <%--<ul class="dropdown-menu">--%>

                        <%--<li><a id="example" href="#">Show Example</a></li>--%>
                        <%--<li role="separator" class="divider"></li>--%>

                        <%--<li><a id="video" href="#">Show Video</a></li>--%>
                        <%--<li role="separator" class="divider"></li>--%>

                        <%--<li><a id="formulas" href="#">Fomular</a></li>--%>
                        <%--<li role="separator" class="divider"></li>--%>

                        <%--<li><a id="glossary" href="#">Glossary</a></li>--%>
                    <%--</ul><!-- dropdown-menu -->--%>
                <%--</div>--%>
            <%--</div>--%>
            <%--<div class="col-md-7 main-tutoring-frame">--%>
                <%--<div class="row buttons-below">--%>
                    <%--<div class="col-sm-3 main-tutoring-button">--%>
                        <%--<a id="hint" class="problem-control-button">--%>
                            <%--<img id="hint-lightbulb" src="img/lightbulb.svg" alt=""><span id="hint_label">Hint</span>--%>
                        <%--</a>--%>
                    <%--</div>--%>
                    <%--<div class="col-sm-3 col-md-offset-1 main-tutoring-button">--%>
                        <%--<a id="replay" class="problem-control-button">--%>
                            <%--<img src="img/reload.svg" alt="">Replay Hint--%>
                        <%--</a>--%>
                    <%--</div>--%>
                    <%--<div class="col-sm-3 col-md-offset-1 main-tutoring-button">--%>
                        <%--<a id="nextProb" class="problem-control-button">--%>
                            <%--<img src="img/right-arrow.svg" alt="">New Problem--%>
                        <%--</a>--%>
                    <%--</div>--%>
                <%--</div>--%>
                <%--<div id="frameContainer" class="problemDiv">--%>
                    <%--<iframe id="problemWindow" class="probWindow"--%>
                            <%--name="iframe1"--%>
                            <%--width="600"--%>
                            <%--height="600"--%>
                            <%--src="${activityURL}"--%>
                            <%--frameborder="no"--%>
                            <%--scrolling="no">--%>
                    <%--</iframe>--%>
                <%--</div>--%>
                <%--<div id="flashContainer1">--%>
                    <%--<div id="flashContainer2"></div>--%>
                <%--</div>--%>
            <%--</div>--%>
            <%--<div class="col-md-3 virtual-character">--%>
                <%--<iframe id="learningCompanionWindow"--%>
                        <%--name="lciframe"--%>
                        <%--width="280"--%>
                        <%--height="600"--%>
                        <%--src="${learningCompanionMovie}"--%>
                        <%--scrolling="no">--%>
                <%--</iframe>--%>
            <%--</div>--%>
        <%--</div>--%>
    <%--</div>--%>
<%--</section>--%>

<%--<div class="dev-view">--%>
    <%--<p>--%>
        <%--Developer Info >>--%>
        <%--<span class="dev-view-label">Problem ID: </span>--%>
        <%--<span id="pid">${probId}</span> ||--%>
        <%--<span class="dev-view-label">Effort: </span>--%>
        <%--<span id="effort">${effort}</span> ||--%>
        <%--<span class="dev-view-label">Answer: </span>--%>
        <%--<span id="answer">${globals.answer}</span>--%>
    <%--</p>--%>
<%--</div>--%>

<div class="huytran-tutor">
    <div class="huytran-sitenav">
        <div class="huytran-sitenav__menu">
            <div class="huytran-sitenav__burger" onclick="toggleNav()">
                <i class="fa fa-bars" aria-hidden="true" scale="1.5"></i>
            </div>
        </div>

        <div class="huytran-sitenav__main">
            <input type="checkbox" class="huytran-sitenav__showmore-state" id="post" />

            <a href="#" class="huytran-sitenav__button huytran-sitenav__button--first" id="hint">
					<span class="huytran-sitenav__icon">
						<i class="fa fa-lightbulb-o" aria-hidden="true"></i>
					</span>
                <span class="huytran-sitenav__buttontitle"><span id="hint_label">Hints</span></span>
            </a>

            <a href="#" class="huytran-sitenav__button" id="replay">
					<span class="huytran-sitenav__icon">
						<i class="fa fa-repeat" aria-hidden="true"></i>
					</span>
                <span class="huytran-sitenav__buttontitle">Replay Hints</span>
            </a>

            <a href="#" class="huytran-sitenav__button" id="read">
					<span class="huytran-sitenav__icon">
						<i class="fa fa-bullhorn" aria-hidden="true"></i>
					</span>
                <span class="huytran-sitenav__buttontitle">Read Question</span>
            </a>

            <a href="#" class="huytran-sitenav__button huytran-sitenav__showmore-target" id="example">
					<span class="huytran-sitenav__icon">
						<i class="fa fa-question" aria-hidden="true"></i>
					</span>
                <span class="huytran-sitenav__buttontitle">Show Example</span>
            </a>
            <a href="#" class="huytran-sitenav__button huytran-sitenav__showmore-target" id="video">
					<span class="huytran-sitenav__icon">
						<i class="fa fa-video-camera" aria-hidden="true"></i>
					</span>
                <span class="huytran-sitenav__buttontitle">Show Video</span>
            </a>
            <a href="#" class="huytran-sitenav__button huytran-sitenav__showmore-target" id="formulas">
					<span class="huytran-sitenav__icon">
						<i class="fa fa-magic" aria-hidden="true"></i>
					</span>
                <span class="huytran-sitenav__buttontitle">Formula</span>
            </a>
            <a href="#" class="huytran-sitenav__button huytran-sitenav__showmore-target" id="glossary">
					<span class="huytran-sitenav__icon">
						<i class="fa fa-sticky-note-o" aria-hidden="true"></i>
					</span>
                <span class="huytran-sitenav__buttontitle">Glossary</span>
            </a>
            <c:if test="${showProblemSelector}">
                <a id="selectProb" href="#" class="huytran-sitenav__button">
                    <span class="huytran-sitenav__icon">
						<i class="fa fa-check" aria-hidden="true"></i>
                    </span>
                    <span class="huytran-sitenav__buttontitle">Select Prob</span>
                </a>

                <a id="getEventLogs" href="#" class="huytran-sitenav__button">
                    <span class="huytran-sitenav__icon">
						<i class="fa fa-eye" aria-hidden="true"></i>
                    </span>
                    <span class="huytran-sitenav__buttontitle">View Log</span>
                </a>
            </c:if>
            <label class="huytran-sitenav__showmore-trigger" for="post">
            </label>
        </div>
    </div>

    <div class="huytran-practice">
        <div class="huytran-practice__menu">
            <a href="#" class="huytran-sitenav__button" id="nextProb">
					<span class="huytran-sitenav__icon">
						<i class="fa fa-plus" aria-hidden="true"></i>
					</span>
                <span class="huytran-sitenav__buttontitle">New Problem</span>
            </a>
            <div class="huytran-practice__nav">
                <a class="huytran-practice__navitem" id="home">My Garden</a>
                <a class="huytran-practice__navitem" id="myProg">My Progress</a>
                <a class="huytran-practice__navitem" href="#">Practice Area</a>
                <a class="huytran-practice__navitem huytran-practice__navitem--last" href="TutorBrain?action=Logout&sessionId=${sessionId}&elapsedTime=${elapsedTime}&var=">
                    Log Out &nbsp;
                    <span class="fa fa-sign-out"></span>
                </a>
            </div>
        </div>

        <div class="huytran-practice__container">
            <div class="huytran-practice__main" id="frameContainer">
                <iframe id="problemWindow" class="probWindow"
                    name="iframe1"
                    width="600"
                    height="600"
                    src="${activityURL}"
                    frameborder="no"
                    scrolling="no">
                </iframe>
                <div id="flashContainer1">
                    <div id="flashContainer2"></div>
                </div>
            </div>
            <div class="huytran-practice__character">
                <div class="huytran-practice__hide-button"
                     onclick="toggleCharacter()"
                >
                    <span class="fa fa-minus"></span>
                </div>
                <div class="huytran-practice__character-window">
                    <div class="learningCompanionContainer">
                        <iframe id="learningCompanionWindow"
                                name="lciframe"
                                width="280"
                                height="600"
                                src="${learningCompanionMovie}"
                                onload="lcLoaded(this)"
                                scrolling="no">
                        </iframe>
                    </div>
                </div>
            </div>
            <div class="huytran-practice__character-collapse hide">
					<span class="huytran-practice__show-button"
                          onclick="toggleCharacter()"
                    >
						<span class="fa fa-plus"></span>
					</span>
                <span>Character</span>
            </div>
        </div>
    </div>
</div>




<div id="eventLogWindow" title="Event Logs" style="display:none;">
    <div class="bootstrap">
    <div class = "containers">
        <div class="panel-group" id="accordion">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseOne">
                            Event Log
                        </a>
                    </h4>
                </div>
                <div id="collapseOne" class="panel-collapse collapse in">
                    <div class="panel-body">
                        <div class='scrolledTable'>

                            <fieldset class="scheduler-border">
                                <legend>Add/Remove Columns</legend>

                                <a type="button" class="btn btn-default toggle-vis" data-column="0">
                                    <span class="glyphicon glyphicon-remove"></span> id
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="1">
                                    <span class="glyphicon glyphicon-remove"></span> studId
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="2">
                                    <span class="glyphicon glyphicon-remove"></span> sessNum
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="3">
                                    <span class="glyphicon glyphicon-remove"></span> action
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="4">
                                    <span class="glyphicon glyphicon-remove"></span> userInput
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="5">
                                    <span class="glyphicon glyphicon-remove"></span> isCorrect
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="6">
                                    <span class="glyphicon glyphicon-remove"></span> elapsedTime
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="7">
                                    <span class="glyphicon glyphicon-remove"></span> probElapsed
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="8">
                                    <span class="glyphicon glyphicon-remove"></span> problemId
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="9">
                                    <span class="glyphicon glyphicon-remove"></span> hintStep
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="10">
                                    <span class="glyphicon glyphicon-remove"></span> hintId
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="11">
                                    <span class="glyphicon glyphicon-remove"></span> emotion
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="12">
                                    <span class="glyphicon glyphicon-remove"></span> activityName
                                </a>
                                <a type="button" class="btn btn-default toggle-vis"data-column="13">
                                    <span class="glyphicon glyphicon-remove"></span> auxId
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="14">
                                    <span class="glyphicon glyphicon-remove"></span> auxTable
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="15">
                                    <span class="glyphicon glyphicon-remove"></span> time
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="16">
                                    <span class="glyphicon glyphicon-remove"></span> curTopicId
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="17">
                                    <span class="glyphicon glyphicon-remove"></span> testerNote
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> clickTime
                                </a>
                            </fieldset>
                            <fieldset class="scheduler-border">
                                <legend>Hightlight Rule Editor</legend>
                                <div class="form-group">
                                    <div class="row">
                                        <button class="btn btn-success" role="button" id="newHighlightRule_eventLog" aria-label="Add new hightlightRule">
                                            <i class="fa fa-plus" aria-hidden="true"></i>&nbsp;Add new Hightlight Rule
                                        </button>
                                    </div>
                                </div>
                                <div id="newHighlightRulecontainer_eventLog"></div>
                            </fieldset>

                            <table id="eventLogTable" class="table table-striped table-bordered" cellspacing="0" width="100%">
                                <thead><tr>
                                    <th class="details-control">Make a Note</th>
                                    <th>id</th>
                                    <th>studId</th>
                                    <th>sessNum</th>
                                    <th>action</th>
                                    <th>userInput</th>
                                    <th>isCorrect</th>
                                    <th>elapsedTime</th>
                                    <th>probElapsed</th>
                                    <th>problemId</th>
                                    <th>hintStep</th>
                                    <th>hintId</th>
                                    <th>emotion</th>
                                    <th>activityName</th>
                                    <th>auxId</th>
                                    <th>auxTable</th>
                                    <th>time</th>
                                    <th>curTopicId</th>
                                    <th>testerNote</th>
                                    <th>clickTime</th>
                                </tr>
                                </thead>
                                <tbody>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

            </div>

            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">
                            Student Problem History Log
                        </a>
                    </h4>
                </div>
                <div id="collapseTwo" class="accordion-body collapse">
                    <div class="panel-body">
                        <div class='scrolledTable'>

                            <fieldset class="scheduler-border">
                                <legend>Add/Remove Columns</legend>

                                <a type="button" class="btn btn-default toggle-vis" data-column="0">
                                    <span class="glyphicon glyphicon-remove"></span> id
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="1">
                                    <span class="glyphicon glyphicon-remove"></span> studId
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="2">
                                    <span class="glyphicon glyphicon-remove"></span> sessionId
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="8">
                                    <span class="glyphicon glyphicon-remove"></span> problemId
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="3">
                                    <span class="glyphicon glyphicon-remove"></span> topicId
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="4">
                                    <span class="glyphicon glyphicon-remove"></span> problemBeginTime
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="5">
                                    <span class="glyphicon glyphicon-remove"></span> problemEndTime
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="6">
                                    <span class="glyphicon glyphicon-remove"></span> timeInSession
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="7">
                                    <span class="glyphicon glyphicon-remove"></span> timeInTutor
                                </a>

                                <a type="button" class="btn btn-default toggle-vis" data-column="9">
                                    <span class="glyphicon glyphicon-remove"></span> timeToFirstAttempt
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="10">
                                    <span class="glyphicon glyphicon-remove"></span> timeToFirstHint
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="11">
                                    <span class="glyphicon glyphicon-remove"></span> timeToSolve
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="12">
                                    <span class="glyphicon glyphicon-remove"></span> numMistakes
                                </a>
                                <a type="button" class="btn btn-default toggle-vis"data-column="13">
                                    <span class="glyphicon glyphicon-remove"></span> numHints
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="14">
                                    <span class="glyphicon glyphicon-remove"></span> videoSeen
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="15">
                                    <span class="glyphicon glyphicon-remove"></span> numAttemptsToSolve
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="16">
                                    <span class="glyphicon glyphicon-remove"></span> solutionHintGiven
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="17">
                                    <span class="glyphicon glyphicon-remove"></span> mode
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> mastery
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> emotionAfter
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> emotionLevel
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> effort
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> exampleSeen
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> textReaderUsed
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> numHintsBeforeSolve
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> isSolved
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> adminFlag
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> authorFlag
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> collaboratedWith
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> timeToSecondAttempt
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> timeToThirdAttempt
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> timeToSecondHint
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> timeToThirdHint
                                </a>
                                <a type="button" class="btn btn-default toggle-vis" data-column="18">
                                    <span class="glyphicon glyphicon-remove"></span> probDiff
                                </a>
                            </fieldset>
                            <fieldset class="scheduler-border">
                                <legend>Hightlight Rule Editor</legend>
                                <div class="form-group">
                                    <div class="row">
                                        <a class="btn btn-success" role="button" id="newHighlightRule_studentProblemHistoryLog" aria-label="Add new hightlightRule">
                                            <i class="fa fa-plus" aria-hidden="true"></i>&nbsp;Add new Hightlight Rule
                                        </a>
                                    </div>
                                </div>
                                <div id="newHighlightRulecontainer_studentProblemHistoryLog"></div>
                            </fieldset>

                            <table id="studentProblemHistoryTable" class="table table-striped table-bordered" cellspacing="0" width="100%">
                                <thead><tr>
                                    <th class="details-control">Make a Note</th>
                                    <th>id</th>
                                    <th>studId</th>
                                    <th>sessionId</th>
                                    <th>problemId</th>
                                    <th>topicId</th>
                                    <th>problemBeginTime</th>
                                    <th>problemEndTime</th>
                                    <th>timeInSession</th>
                                    <th>timeInTutor</th>
                                    <th>timeToFirstAttempt</th>
                                    <th>timeToFirstHint</th>
                                    <th>timeToSolve</th>
                                    <th>numMistakes</th>
                                    <th>numHints</th>
                                    <th>videoSeen</th>
                                    <th>numAttemptsToSolve</th>
                                    <th>solutionHintGiven</th>
                                    <th>mode</th>
                                    <th>mastery</th>
                                    <th>emotionAfter</th>
                                    <th>emotionLevel</th>
                                    <th>effort</th>
                                    <th>exampleSeen</th>
                                    <th>textReaderUsed</th>
                                    <th>numHintsBeforeSolve</th>
                                    <th>isSolved</th>
                                    <th>adminFlag</th>
                                    <th>authorFlag</th>
                                    <th>collaboratedWith</th>
                                    <th>timeToSecondAttempt</th>
                                    <th>timeToThirdAttempt</th>
                                    <th>timeToSecondHint</th>
                                    <th>timeToThirdHint</th>
                                    <th>probDiff</th>
                                </tr>
                                </thead>
                                <tbody>
                                </tbody>
                            </table>


                        </div>
                    </div>
                </div>
            </div>


        </div>
    </div>
</div>





<script>
    function toggleNav() {
        $('.huytran-sitenav__main').toggleClass('hide');
    }

    function toggleCharacter() {
        $('.huytran-practice__character').toggleClass('hide');
        $('.huytran-practice__character-collapse').toggleClass('hide');
    }
</script>


<div style="z-index:100;" id="instructionsDialog" title="Instructions">
    <p id="instructionsP">${instructions}</p>
    <div class="empty"></div>
</div>
<%-- This div contains information about the current problem (its topic and standard)--%>
<div id="problemTopicAndStandards" style="display: none;">Topic:<br/>Standards:</div>
<%-- Only shown to test users--%>
<div id="varBindings" style="display: none;"></div>

</body>
</html>
