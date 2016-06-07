<%--
  Created by IntelliJ IDEA.
  User: david
  Date: 2/6/13
  Time: 9:49 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>UMass Math Tutor</title>
    <link href="css/wanDutBG.css" rel="stylesheet" type="text/css">
    <link href="css/menu.css" rel="stylesheet" type="text/css">
    <link href="css/navlog.css" rel="stylesheet" type="text/css">
    <link href="css/wayang.css" rel="stylesheet" type="text/css">
    <link href="css/button.css" rel="stylesheet" type="text/css">
    <link href="css/simple-slider.css" rel="stylesheet" type="text/css" />


    <%--<link href="css/ui-lightness/jquery-ui-1.10.3.custom.css" rel="stylesheet">--%>
    <link href="js/jquery-ui-1.10.4.custom/css/spring/jquery-ui-1.10.4.custom.min.css" rel="stylesheet">
    <script src="js/jquery-1.10.2.js"></script>
    <%--<script src="js/jquery-ui-1.10.3/ui/jquery-ui.js"></script>--%>
    <script src="js/jquery-ui-1.10.4.custom/js/jquery-ui-1.10.4.custom.min.js"></script>
    <script type="text/javascript" src="js/simple-slider.js"></script>

    <script type="text/javascript" src="js/tutorutils.js"></script>
    <script type="text/javascript" src="js/tutorAnswer.js"></script>
    <script type="text/javascript" src="js/tutorhint.js"></script>
    <script type="text/javascript" src="js/tutorhut.js"></script>
    <script type="text/javascript" src="js/tutorintervention.js"></script>
    <script type="text/javascript" src="js/intervhandlers.js"></script>
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
            destinationInterventionSelector: null

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
        $(document).ready(
                function () {
                    tutorhut_main(globals,sysGlobals,transients, "${learningCompanionMovie}");
                }
        );
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
<audio id='questionaudio' name='questionaudio'><source id='questionogg' src='' type='audio/ogg'><source id='questionmp3' src='' type='audio/mpeg'>Your browser does not support the audio element.</audio>
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


<div class="wrapper">
    <div class="container"><img src="img/header3.png" width="1200" height="106" alt="mathematics">
        <div id="userDisplay" style="position:absolute; top:10px; right:10px; width:300px; height:50px; ">Logged in as:</div>

    </div>
    <div class="container">
        <div class="content">

            <ul class="buttons">
                <li class="prob1">
                    <a id="nextProb" href="#" class="button">New Problem</a>
                </li>
                <li class="inst2">
                    <a id="instructions" href="#" class="button">Instructions</a>
                </li>
                <li id="mppButton" class="inst2">
                    <a id="myProg" href="#" class="button">My Progress</a>
                </li>
                <c:if test="${showProblemSelector}">
                    <li id="selectProblemButton" class="probSel">
                        <a id="selectProb" href="#" class="button">Select Problem</a>
                    </li>
                </c:if>

            </ul>

            <div class="leftCol">
                <div class="mntop">
                    Help
                </div>
                <ul class="mnCNT">
                    <li class="elb_read">
                        <a id="read" href="#">Read Problem</a>
                    </li>
                    <li class="elb_hint">
                        <a id="hint" href="#">Hint</a>
                    </li>
                    <li class="elb_replay">
                        <a id="replay" href="#">Replay Hint</a>
                    </li>
                    <%--<li class="elb_solve">--%>
                    <%--<a id="solve" href="#">Show Answer</a>--%>
                    <%--</li>--%>
                    <%--<li class="elb_solve">--%>
                        <%--<a id="solve" href="#">Solve Problem</a>--%>
                    <%--</li>--%>
                </ul>
                <br>
                <div class="mntop">
                    Resources
                </div>
                <ul class="mnCNT">
                    <li class="elb_show">
                        <a id="example" href="#">Show Example</a>
                    </li>
                    <li class="elb_vid">
                        <a id="video" href="#">Show Video</a>
                    </li>
                    <li class="elb_formulas">
                        <a id="formulas" href="#">Formulas</a>
                        </li>
                        <li class="elb_glossary">
                            <a id="glossary" href="#">Glossary</a>
                        </li>
                </ul>
                <br>
                <div class="mntop">
                    Places
                </div>
                <ul class="mnCNT">
                    <li class="elb_mpp">
                        <a id="prefs" href="#">Preferences</a>
                    </li>
                    <li class="elb_home">
                        <a id="home" href="#">Home</a>
                    </li>
                    <li class="elb_pid">
                        <div id="pid">${probId}</div>
                    </li>
                    <li class="elb_pid">
                        <div id="effort">${effort}</div>
                    </li>
                    <%-- Answer field is only filled for test users to show the correct answer--%>
                    <li class="elb_pid">
                            <div id="answer">${globals.answer}</div>
                         </li>
                </ul>
                <p>&nbsp;

                </p>
            </div>

            <%-- This div contains information about the current problem (its topic and standard)--%>
            <div id="problemTopicAndStandards" style="position:absolute; top:650px; right:400px">Topic:<br/>Standards:</div>
           <%-- Only shown to test users--%>
            <div id="varBindings" style="position:absolute; top:690px; left:200px"></div>

            <div id="flashContainer1" >
                <div id="flashContainer2"></div>
            </div>

            <div id="frameContainer" class="problemDiv">
                <iframe id="problemWindow" class="probWindow"
                        name="iframe1"
                        width="600"
                        height="600"
                        src="${activityURL}"
                        frameborder="no"
                        scrolling="no">
                </iframe>
            </div>


            <div  id="learningCompanionContainer">
                <iframe id="learningCompanionWindow"
                        name="lciframe"
                        width="260"
                        height="600"
                        src="${learningCompanionMovie}"
                        frameborder="yes"
                        scrolling="no">
                </iframe>
            </div>




            <%--<iframe id="iframe" name="iframe1" width="1000" height="600" src="http://cadmium.cs.umass.edu/wayang2/flash/Problems/AW_001/AdditionDecimals.swf" frameborder="yes" scrolling="yes"> </iframe>--%>

            <!-- end .content -->
        </div>
        <!-- end .container -->
    </div>
    <!--<div class="container">
    -->

    <div style="z-index:100;" id="instructionsDialog" title="Instructions">
        <p id="instructionsP">${instructions}</p>

        <div class="empty"></div>
    </div>





    <!-- end .container -->
    <!--</div>-->
    <!-- end .content -->
</div>

<%--Old code here--%>

</body>
</html>
