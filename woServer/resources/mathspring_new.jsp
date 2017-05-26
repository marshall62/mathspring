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
    
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/jquery-ui-1.10.4.custom/js/jquery-ui-1.10.4.custom.min.js"></script>
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
