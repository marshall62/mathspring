var globals;
var sysGlobals;
var transients;

//var EXTERNAL = 'External';
//var FLASH = 'flash';
//var HTML5 = 'html5';
//var FORMALITY = '4Mality';
var MODE_DEMO = "demo";
var MODE_EXAMPLE = "example";
var MODE_PRACTICE = "practice";
var FLASH_CONTAINER_OUTER = "flashContainer1";
var FLASH_CONTAINER_INNER = "flashContainer2";
var FLASH_CONTAINER_OUTERID = "#"+FLASH_CONTAINER_OUTER;
var FLASH_CONTAINER_INNERID = "#"+FLASH_CONTAINER_INNER;
var PROBLEM_CONTAINER = "frameContainer";
var PROBLEM_CONTAINERID = "#"+PROBLEM_CONTAINER;
var PROBLEM_WINDOW = "problemWindow";
var PROBLEM_WINDOWID = "#"+PROBLEM_WINDOW;
var LEARNING_COMPANION_CONTAINER = "learningCompanionContainer";
var LEARNING_COMPANION_WINDOW = "learningCompanionWindow";
var LEARNING_COMPANION_WINDOW_ID = "#"+LEARNING_COMPANION_WINDOW;
var EXAMPLE_WINDOW = "exampleWindow";
var EXAMPLE_CONTAINER_DIV = "exampleContainer";
var EXAMPLE_CONTAINER_DIV_ID = "#"+EXAMPLE_CONTAINER_DIV;
var EXAMPLE_FRAME = "exampleFrame";
var EXAMPLE_FRAMEID = "#"+EXAMPLE_FRAME;
var FLASH_PROB_PLAYER = "flashprobplayer"; // the id we put on the swfobject tags in the main window
var EXAMPLE_FLASH_PROB_PLAYER = "xflashprobplayer"; // the id we put on the swfobject tags in the example dialog
var UTIL_DIALOG = "utilDialog";
var INTERVENTION_DIALOG = "interventionDialog";
var INTERVENTION_DIALOG_CONTENT = "interventionDialogContent";
var INSTRUCTIONS_DIALOG = "instructionsDialog";
var SELECT_PROBLEM_DIALOG = "selectProblemDialog";
var INSTRUCTIONS_TEXT_ELT= "#instructionsP";
var UTIL_DIALOG_IFRAME = "utilDialogIframe";
var NO_MORE_PROBLEMS = "noMoreProblems";
var NO_MORE_REVIEW_PROBLEMS = "noMoreReviewProblems";
var NO_MORE_CHALLENGE_PROBLEMS = "noMoreChallengeProblems";
var INPUT_RESPONSE_FORM = "inputResponseForm";

var FLASH_PROB_TYPE = "flash";
var SWF_TYPE = "swf";
var HTML_PROB_TYPE = "html5";
var EXTERNAL_PROB_TYPE = "ExternalActivity";
var TOPIC_INTRO_PROB_TYPE = "TopicIntro";
var INTERVENTION = "intervention";
var NEXT_PROBLEM_INTERVENTION = "NextProblemIntervention";
var IS_INPUT_INTERVENTION ="isInputIntervention";
var ATTEMPT_INTERVENTION = "AttemptIntervention";

var DELAY = 700, clicks = 0, timer = null; //Variables required for determining the difference between single and double clicks


function isFlashProblem() {
    return globals.probType === FLASH_PROB_TYPE;
}
function isHTML5Problem() {
    return globals.probType === HTML_PROB_TYPE;
}

function isFlashExample() {
    return globals.exampleProbType === FLASH_PROB_TYPE;
}
function isHTML5Example() {
    return globals.exampleProbType === HTML_PROB_TYPE;
}

function isDemoMode () {
    return globals.probMode === MODE_DEMO;
}

function isDemoOrExampleMode () {
    return globals.probMode === MODE_DEMO || globals.probMode === MODE_EXAMPLE;
}

function isIntervention () {
    return globals.probType === INTERVENTION;
}

function getProblemParams() {
    return globals.params;
}

function getAnswer() {
    return globals.answer;
}

function getProblemContentPath() {
    return sysGlobals.problemContentPath;
}

function getResource() {
    return globals.resource;
}

function getAnswers() {
    return globals.answers;
}

function getProblemStatement() {
    return globals.statementHTML;
}

function getProblemFigure() {
    return globals.questionImage;
}

function getProblemSound() {
    return globals.questionAudio;
}

function getHints() {
    return globals.hints;
}

function getUnits() {
    return globals.units;
}

function getForm() {
    return globals.form;
}

function isParameterized() {
    return (globals.params != null && globals.params != undefined)
}

// In the case of parameterized problems, we want to shuffle up the correct answer's position
function getNewAnswer() {
    return globals.newAnswer;
}


function updateTimers () {
    var now = new Date().getTime();
    globals.probElapsedTime += now - globals.clock;
    globals.elapsedTime += now - globals.clock;
    globals.clock = now;
}



function incrementTimers(globals) {
    var now = new Date().getTime();
    globals.probElapsedTime += now - globals.clock;
    globals.elapsedTime += now - globals.clock;
    globals.clock = now;
}

// can be called to find out if we are waiting for results from server (and hence interface is in wait state)
window.isWaiting = function () {
    return globals.guiLock;
}

function lockGui() {
    globals.guiLock = true;
    // need to pop up a wait timer
}

function unlockGui() {
    globals.guiLock = false;
}

function showHourglassCursor(b) {
     if (b) {
         lockGui();
         $("body").css("cursor", "wait");
     }
    else {
         unlockGui();
         $("body").css("cursor", "default");
     }
}

function showProblemInfo (pid, name, topic, standards) {
    $("#pid").text(pid + ":" + name);  // shows the problem ID + resource
    $("#problemTopicAndStandards").html("Topic:" + topic + "<br>Standards:" + standards)
}

function showUserInfo (userName) {
    $("#userDisplay").text("Logged in as: " + userName);
}

function showEffortInfo (effort) {
    $("#effort").text(effort);  // shows the effort of the last three problems (given as a string)
}

function showAnswer (ans) {
    $("#answer").text("Answer: " + ans);
}

function loadIframe (iframeId, url) {
    $(iframeId).attr("src", url);
}

///////////////////////////////////////////////////////////////////////
////  Buttons on navlog (new problem, instructions, my progress
////  call these three functions
///////////////////////////////////////////////////////////////////////


function nextProb(globals) {
    toggleSolveDialogue(false);
    if (!globals.showMPP)
        hideMPP()
    if (globals.trace)
        debugAlert("in NextProb ");
    incrementTimers(globals);
    // call the server with a nextProblem event and the callback fn processNextProblemResult will deal with result
    showHourglassCursor(true);
    servletGet("NextProblem", {probElapsedTime: globals.probElapsedTime, mode: globals.tutoringMode}, processNextProblemResult);
}

// This function can only be called if the button is showing
function selectProblemDialog () {
    $("#"+SELECT_PROBLEM_DIALOG).dialog('open');
    var url = "/"+sysGlobals.wayangServletContext + "/TutorBrain?action=GetProblemListForTester&sessionId=" + globals.sessionId +"&elapsedTime="+ globals.elapsedTime;
    loadIframe("#selectProblemDialogIframe",url)
}

function forceNextProblem (id) {
    $("#"+SELECT_PROBLEM_DIALOG).dialog('close');
     // send a NextProblemEvent that forces a particular problem
    incrementTimers(globals);
    // call the server with a nextProblem event and the callback fn processNextProblemResult will deal with result
    showHourglassCursor(true);
    servletGet("NextProblem", {probElapsedTime: globals.probElapsedTime, mode: globals.tutoringMode, probID: id}, processNextProblemResult);
}


function instructions () {
    // probably want something slicker than this alert dialog.
    if (globals.instructions == "")
        alert("Sorry.  There are no instructions for this problem.");
    else {
        $(INSTRUCTIONS_TEXT_ELT).text(globals.instructions);
        $("#"+INSTRUCTIONS_DIALOG).dialog('open');
    }
    sendSimpleNotificationEvent(globals,"ShowInstructions");
    return false;
}

function myprogress(globals) {
    debugAlert("in myprogress");
    globals.lastProbType = globals.probType;
    globals.lastProbId = globals.probId;
    document.location.href = "/"+sysGlobals.wayangServletContext + "/TutorBrain?action=navigation&sessionId=" + globals.sessionId + "&elapsedTime=" + globals.elapsedTime + "&probElapsedTime=" + globals.probElapsedTime + "&from=sat_hut&to=my_progress&topicId="+ globals.topicId +"&probId="+globals.probId;
}


///////////////////////////////////////////////////////////////////////
////  Buttons on left menu (read prob, hint, replay hint, solve prob, show ex, show vid, formulas, glossary
////  call these functions
///////////////////////////////////////////////////////////////////////

function callReadProb() {
    debugAlert("In  callReadProb");
    if (isFlashProblem() || isHTML5Problem())
        servletGet("ReadProblem", {probElapsedTime: globals.probElapsedTime});
    if (isHTML5Problem())
        document.getElementById(PROBLEM_WINDOW).contentWindow.prob_readProblem();
    else if (isFlashProblem())
        document.getElementById(FLASH_PROB_PLAYER).readProblem();

}

// fields the click on the hint button.
function requestHint(globals) {
    if (isFlashProblem() || isHTML5Problem()) {
        incrementTimers(globals);
        servletGetWait("Hint", {probElapsedTime: globals.probElapsedTime}, processRequestHintResult);
    }
}

// fields the click on the solve problem button
function requestSolution(globals) {
    if (isFlashProblem() || isHTML5Problem())
    {
        incrementTimers(globals);
        servletGet("ShowSolveProblem", {probElapsedTime: globals.probElapsedTime}, processRequestSolutionResult);
    }
}

function showExample (globals) {
    if (isFlashProblem() || isHTML5Problem()) {
        updateTimers();
        servletGet("ShowExample",{probElapsedTime: globals.probElapsedTime},processShowExample);
    }
}

function showVideo (globals) {
    if (isFlashProblem() || isHTML5Problem()) {
        updateTimers();
        servletGet("ShowVideo",{probElapsedTime: globals.probElapsedTime },processShowVideo);
    }
}

// TODO this should be changed to use a non-modal dialog
function showGlossary (globals) {
    var glossURL = "http://www.amathsdictionaryforkids.com/dictionary.html";
    utilDialogOpen(glossURL, "Glossary");
    sendSimpleNotificationEvent(globals,"ShowGlossary");
//    window.open(glossURL, "width=500, height=500");
}

// TODO this should be changed to use a non-modal dialog
function showFormulas (globals) {
    var formURL = "http://math2.org/math/geometry/areasvols.htm";
    utilDialogOpen(formURL, "Formulas");
    sendSimpleNotificationEvent(globals,"ShowFormulas");
//    window.open(formURL, "width=500, height=500");
}

function showUserPreferences (globals) {
    alert("Coming soon.  Editable user preferences!");
}

function showDashboard () {
    sendEndEvent(globals);
    globals.lastProbType = globals.probType;
    globals.lastProbId = globals.probId;
    document.location.href = "/"+sysGlobals.wayangServletContext + "/TutorBrain?action=Home&sessionId=" + globals.sessionId + "&elapsedTime=" + globals.elapsedTime + "&probElapsedTime=" + globals.probElapsedTime + "&probId="+ globals.probId + "&learningCompanion=" + globals.learningCompanion;

}



//////////////////////////////////////////////////////////////////////
///  end of button handlers on left menu
//////////////////////////////////////////////////////////////////////




function processShowExample (responseText, textStatus, XMLHttpRequest) {
    var activity = JSON.parse(responseText);
    if (activity.activityType === NO_MORE_PROBLEMS) {
        alert("There is not an example to show for this problem");
        return;
    }

    var mode = activity.mode; // this will be 'example'
    var pid = activity.id;
    var resource =activity.resource;
    var ans = activity.answer;
    var solution = activity.solution;

    globals.exampleProbType = activity.activityType;
    // solution is an array of hints.   Each hint has a label that we want to pull out and put in globals.example_hint_sequence
    if (isFlashExample())
        showFlashProblem(resource,ans,solution,EXAMPLE_FRAME, MODE_EXAMPLE) ;
    else showHTMLProblem(pid,solution,resource,MODE_EXAMPLE);

}



function processShowVideo (responseText, textStatus, XMLHttpRequest) {
    var activity = JSON.parse(responseText);
    var video = activity.video;
    // khanacademy won't play inside an iFrame because it sets X-Frame-Options to SAMEORIGIN.
    if (video != null)
        window.open(video, "width=500, height=500");
    else alert("There is no video to show for this problem");
}

function openExampleDialog(solution){
    if (solution != 'undefined' && solution != null) {
        globals.exampleHintSequence = new Array(solution.length);
        for (i=0;i<solution.length;i++) {
            globals.exampleHintSequence[i] = solution[i].label;
        }
        globals.exampleCurHint = globals.exampleHintSequence[0];
    }
//    globals.probMode = mode;
    // show a div that contains the example.
//    $("#frameContainer").hide();              // hide the current problem
    $(EXAMPLE_CONTAINER_DIV_ID).dialog("open");        // TODO need to shrink height
}



function showFlashProblem (resource,ans,solution, containerElement, mode) {
    // examples are requested by user during a practice problem so we don't want to mess up timers and properties
    globals.probMode = mode;
    if (mode != MODE_EXAMPLE) {
        hideHTMLProblem(true);
        globals.probElapsedTime = 0;
        globals.lastProbId = globals.probId;
        globals.lastProbType = FLASH_PROB_TYPE;
    }
    var isExample = (mode === MODE_DEMO || mode===MODE_EXAMPLE);
    if (typeof(isExample)==='undefined') {
        isExample = false;
    }
    if (isExample)
        openExampleDialog(solution);
    var questionNum = resource.substring(resource.indexOf("_") + 1, resource.length);
    var flashvars = {
//        hostURL: sysGlobals.isDevEnv ? 'mathspring/' : sysGlobals.webContentPath,
        hostURL: sysGlobals.webContentPath,
        correctAnswer: ans,
        readAloud: false,
        isExample: isExample

    }
    var params = {
        wmode: "transparent",
        allowscriptaccess: "always"
    }
    var attributes = {
        id:  isExample ? EXAMPLE_FLASH_PROB_PLAYER : FLASH_PROB_PLAYER ,
        name: isExample ? EXAMPLE_FLASH_PROB_PLAYER : FLASH_PROB_PLAYER
    }
    debugAlert("its a flash problem:" + resource + " The number is:" + questionNum);
    // send an END for the first xAct
    debugAlert("Calling servlet with EndExternalActivity");


    // This replaces a <div> (typically FLASH_CONTAINER_INNER)  with the actual swf object.
    swfobject.embedSWF(sysGlobals.probplayerPath + "?questionNum=" + questionNum, containerElement,
        "600", "475", "8", "#FFFFFF", flashvars, params, attributes);

    // We only request the solution for a problem in the main screen (problems return for ShowExample come to us with a solution)
    //else // plays the first hint of the example
    //    example_playHint(globals.exampleCurHint);


}



function showHTMLProblem (pid, solution, resource, mode) {
    hideHTMLProblem(false);
    globals.probMode = mode;
    transients.answersChosenSoFar=[];
    if (mode != MODE_EXAMPLE) {
        globals.probElapsedTime = 0;
        globals.lastProbType = HTML_PROB_TYPE;
        globals.lastProbId = pid;
    }
    var isDemo = mode === MODE_DEMO || mode == MODE_EXAMPLE;
    if (isDemo)
        openExampleDialog(solution);
    var dir = resource.split(".")[0];
    // the name of the problem (e.g. problem090.html) is stripped off to find a directory (e.g. problem090)
    if (!isDemo)  {
        if (globals.form!=="quickAuth")  {
            loadIframe(PROBLEM_WINDOWID, sysGlobals.problemContentPath + "/html5Probs/" + dir + "/" + resource);
        }
        else {
            loadIframe(PROBLEM_WINDOWID, sysGlobals.problemContentPath + "/html5Probs/problem_skeleton/problem_skeleton.html");
        }
//        The commented out lines below make the HTML problem have a white background,  but we cannot figure out how
        // to make FLash problems have a white background so we have abandoned this
//        $(PROBLEM_WINDOWID).load(function () {
//            var content = $(PROBLEM_WINDOWID).contents();
//            var body = content.find('body');
//            body.attr("style", "background-color: white !important");
//        });
        $(PROBLEM_WINDOWID).attr("domain", sysGlobals.problemContentDomain);
    }
    else {
        if (globals.form!=="quickAuth") {
            loadIframe(EXAMPLE_FRAMEID, sysGlobals.problemContentPath + "/html5Probs/" + dir + "/" + resource);
        }
        else {
            loadIframe(EXAMPLE_FRAMEID, sysGlobals.problemContentPath + "/html5Probs/problem_skeleton/problem_skeleton.html");
        }
    }


}

function showTopicIntro (resource, topic) {

    // TODO assumption is that TOpicIntro is built in Flash.   Other possibilities: nothing, HTML5
    // if nothing pop up an alert
    if (typeof(resource) != 'undefined' && resource != '')
        showFlashProblem(resource,null,null,FLASH_CONTAINER_INNER, false);

    else alert("Beginning topic: "  + topic + ".  No Flash movie to show")
}

// On EndProblem event we know the effort of the last problem so we get it and display it.
function processEndProblem  (responseText, textStatus, XMLHttpRequest) {
    var activity = JSON.parse(responseText);
    showEffortInfo(activity.effort);
}

function processNextProblemResult(responseText, textStatus, XMLHttpRequest) {
    debugAlert("Server returns " + responseText);
    // empty out the flashContainer div of any swfobjects and clear the iframe of any problems
    $(FLASH_CONTAINER_OUTERID).html('<div id="' +FLASH_CONTAINER_INNER+ '"></div>');
    $(PROBLEM_WINDOWID).attr("src","");
    // Replaceing the example div for the same reason as the above.
    $(EXAMPLE_CONTAINER_DIV_ID).html('<iframe id="'+EXAMPLE_FRAME+'" name="iframe2" width="600" height="600" src="" frameborder="no" scrolling="no"></iframe>');
    var activity = JSON.parse(responseText);
    var mode = activity.mode;
    var activityType = activity.activityType;
    var type = activity.type;
    if (activityType == NO_MORE_PROBLEMS || activityType == NO_MORE_CHALLENGE_PROBLEMS || activityType == NO_MORE_REVIEW_PROBLEMS)  {
        // send EndEvent for previous problem
        sendEndEvent(globals);
        hideHTMLProblem(false);
        var url = activity.endPage;
        loadIframe(PROBLEM_WINDOWID,url);
    }
    else  {
        var pid = activity.id;
        var resource =activity.resource;
        var topic = activity.topicName;
        var standards = activity.standards;
        showProblemInfo(pid,resource,topic,standards);
        showEffortInfo(activity.effort);
        if (globals.showAnswer) {
            // If server shuffles the answer to a different position, then newAnswer contains this position
            if (activity.newAnswer != null && activity.newAnswer != 'undefined') {
                globals.newAnswer = activity.newAnswer;
                showAnswer(activity.newAnswer);
            }
            else {
                globals.answer = activity.answer;
                showAnswer(activity.answer);
            }

        }
        globals.resource = activity.resource;
        // hardwired instructions since what comes from the server is mostly useless.
//        globals.instructions = activity.instructions;
        globals.instructions =  "Read, think and try to solve this problem (please use paper and pencil to write down the solution as many of these are hard!). Use the tools on the left to help you solve the problem (read aloud, hints, examples, videos). Don't be afraid of asking for help! We know that using the help tools is how you get to learn.";
        updateTimers();
        globals.probType = activityType;

        // If its an external problem
        if (isIntervention()) {
            processNextProblemIntervention(activity);

        }

        else if (isHTML5Problem()) {
            // send EndEvent for previous problem
            sendEndEvent(globals);
//            showProblemInfo(pid,resource);
            // formality problems call the servlet with their own begin /end events
            var solution = activity.solution;
            globals.params = activity.parameters;
            globals.oldAnswer = activity.oldAnswer;
            if (mode == MODE_DEMO) {
                globals.exampleProbType = activityType;
            }
            if (activity.form==="quickAuth") {
                globals.form = "quickAuth";
                globals.statementHTML = activity.statementHTML;
                globals.questionAudio = activity.questionAudio;
                globals.questionImage = activity.questionImage;
                globals.hints = activity.hints;
                globals.answers = activity.answers;
                globals.units = activity.units;
            }
            else {
                globals.form = null;
                globals.statementHTML = null;
                globals.questionAudio = null;
                globals.questionImage = null;
                globals.hints = null;
                globals.answers = null;
            }
            sendBeginEvent(globals);
            showHTMLProblem(pid,solution,resource,mode);
            if (activity.intervention != null) {
                processNextProblemIntervention(activity.intervention);
            }
            globals.topicId = activity.topicId;
            globals.probId = pid;
        }
        else if (isFlashProblem()) {
            // send EndEvent for previous problem
            sendEndEvent(globals);
//            showProblemInfo(pid,resource);
            var ans = activity.answer;
            var solution = activity.solution;
            var container;
            if (mode == MODE_DEMO)  {
                container = EXAMPLE_FRAME;
                globals.exampleProbType = activityType;
            }
            else {
                container =FLASH_CONTAINER_INNER;
            }
            sendBeginEvent(globals) ;
            showFlashProblem(resource,ans,solution,container,mode);
            if (activity.intervention != null) {
                processNextProblemIntervention(activity.intervention);
            }
            globals.topicId = activity.topicId;
            globals.probId = pid;
        }

        else if (activityType === TOPIC_INTRO_PROB_TYPE) {
            globals.instructions =  "This is an introduction to a topic.  Please review it before beginning work by clicking the new-problem button.";

            // send EndEvent for previous problem
            sendEndEvent(globals);
//            showProblemInfo(pid,resource);
            globals.probElapsedTime = 0;
            sendBeginEvent(globals);
            showTopicIntro(resource,activity.topicName);
            globals.topicId = activity.topicId;
            globals.probId = pid;

        }
        // We got XML that we don't understand so it must be an intervention.   We call Flash and pass it the XML
        else {
            globals.lastProbType = FLASH_PROB_TYPE;
            debugAlert('Unknown return result: ' + activity);
            globals.topicId = activity.topicId;
            globals.probId = pid;
//            document.location.href = sysGlobals.flashClientPath + "?sessnum=" + globals.sessionId + "&sessionId=" + globals.sessionId + "&learningHutChoice=true&elapsedTime=" + globals.elapsedTime + "&learningCompanion=" + globals.learningCompanion + "&intervention=" + encodeURIComponent(activityXML) + "&mode=intervention"; // &topicId=" + topicId;
        }
       showLearningCompanion(activity);
    }
    showHourglassCursor(false);
}

function showLearningCompanion (json) {

    var files = json.learningCompanionFiles;
    if (files != undefined && files != null)
        $("#"+LEARNING_COMPANION_CONTAINER).dialog("open");
//    alert("learning companion file: " + files);
    if (files instanceof Array)    {
        if (files[0] != globals.learningCompanionClip) {
            loadIframe(LEARNING_COMPANION_WINDOW_ID, sysGlobals.problemContentPath + "/LearningCompanion/" + files[0]);
            // $("#"+LEARNING_COMPANION_CONTAINER).dialog('option','title',files[0]);   // shows the media clip in the title of the dialog
            globals.learningCompanionClip = files[0];
        }

    }
    else {
        if (files != globals.learningCompanionClip) {
            loadIframe(LEARNING_COMPANION_WINDOW_ID, sysGlobals.problemContentPath + "/LearningCompanion/" + files);
            //$("#"+LEARNING_COMPANION_CONTAINER).dialog('option','title',files);     // shows the media clip in the title of the dialog
            globals.learningCompanionClip = files;
}
}
}


function hideNonDefaultInterventionDialogButtons () {
    $("#ok_button").show();
    $("#no_thanks_button").hide();
    $("#see_progress_button").hide();

}

function showMPP () {
    $("#mppButton").show();
}

function hideMPP () {
    $("#mppButton").hide();
}

function hideHTMLProblem (isHide) {
    if (isHide)
        $(PROBLEM_WINDOWID).hide();
    else
        $(PROBLEM_WINDOWID).show();
}



///////////////////////////////////////////////////////////////////////
//////   Main event loop established by tutorMain.hsp in its jquery onReady function
//////////////////////////////////////////////////////////////////////////

function exampleDialogCloseHandler () {
    sysGlobals.exampleWindowActive = false;
    globals.probMode = MODE_PRACTICE;
    //sendEndEvent(globals);

}

var DELAY = 700, clicks = 0, timer = null;
function clickHandling () {
    var agreed=false;
    $("#"+LEARNING_COMPANION_CONTAINER).dialog(  {
            autoOpen: false,
            width:300,
            height: 700,
            closeOnEscape: false,
            position: ['right', 'bottom'],
            open: function(event, ui) { $(".ui-dialog-titlebar-close").hide(); }
        }
    );
//    $("#"+LEARNING_COMPANION_CONTAINER).draggable(
//        {
//        start: function(e, ui) {
//            alert("start");
//
//        },
//        stop: function(e, ui) {
//            alert("stop");
//        }
//    });
    $("#solveNext").click(function () {
        solveNextHint();
    });

    $("#nextProb").click(function () {
        if (!isWaiting()) {
            nextProb(globals)
        }
    });
    $("#read").click(function () {
        callReadProb()
    });

    $("#hint").click(function () {
        clicks++;  //count clicks
        if(clicks === 1) {
            timer = setTimeout(function() {
//                alert("Single click");
                requestHint(globals);  // perform single-click action
                clicks = 0;             //after action performed, reset counter

            }, DELAY);

        } else {

            clearTimeout(timer);    //prevent single-click action
//            alert("Double click");
            requestHint(globals);  //perform double-click action
            clicks = 0;             //after action performed, reset counter
        }
    })
    $("#hint").dblclick(function (e) {
        e.preventDefault();
    })


    $("#replay").click(function () {
        callProblemReplayHint()
    });
    $("#solve").click(function () {
        requestSolution(globals)
    });
    $("#example").click(function () {
        showExample(globals)
    });
    $("#video").click(function () {
        showVideo(globals)
    });
    $("#formulas").click(function () {
        showFormulas(globals)
    });
    $("#glossary").click(function () {
        showGlossary(globals)
    });
    $('#'+INSTRUCTIONS_DIALOG).dialog({
//        autoOpen: ((globals.instructions == "") ? false : true),
        autoOpen: false,
        width: 600,
        buttons: {
            "Close": function () {
                $(this).dialog("close");
            }
        }
    });
    $("#"+SELECT_PROBLEM_DIALOG).dialog( {
        autoOpen: false,
        modal: true,
        width: 600,
        height: 600,
        buttons: [
            {
                text: "Cancel",
                click: function() {
                    $( this ).dialog( "close" );
                }
            }
        ]
    });
    $( "#"+INTERVENTION_DIALOG).dialog( {
        autoOpen: false,
        modal:true,
        width: 500,
        height:500,
        buttons: [
            {
                id: "see_progress_button",
                text: "See Progress",
                click: function () { myprogress(globals)  ;}
            },
            {
                id: "no_thanks_button",
                text: "No thanks",
                click: function() { interventionDialogClose() ;}
            },
            {
                id: "ok_button",
                text: "OK",
                click: function() { interventionDialogClose() ;}
            }
        ]
    });

    $( EXAMPLE_CONTAINER_DIV_ID).dialog({
        autoOpen:false,

        modal:true,
        width:650,
        height:675,
        open: function () {
            sysGlobals.exampleWindowActive = true;
            $(EXAMPLE_CONTAINER_DIV_ID).css('overflow', 'hidden'); //this line does the actual hiding
            var id_exists = document.getElementById('play_button');
            if (id_exists)  {
                document.getElementById('play_button').id = 'pulsate_play_button';
            }

        },
        close: function () { exampleDialogCloseHandler(); } ,
        buttons: [
            {
                id: 'play_button',
                text: "Play next step",
                click: function() {
                    var id_exists = document.getElementById('pulsate_play_button');
                    if (id_exists)  {
                        document.getElementById('pulsate_play_button').id = 'play_button';
                    }
                    clicks++;  //count clicks
                    if(clicks === 1) {
                        timer = setTimeout(function() {
                            example_solveNextHint();  // perform single-click action
                            clicks = 0;             //after action performed, reset counter

                        }, DELAY);

                    } else {

                        clearTimeout(timer);    //prevent single-click action
                        example_solveNextHint();  //perform double-click action
                        clicks = 0;             //after action performed, reset counter
                        }
                    },
                dblclick: (function (e) {
                    e.preventDefault();
                })
            },
            {
                text: "Done",
                click: function() {
                    if (isDemoMode())   {
                        // turn off demo mode
                        globals.probMode = null;
                        globals.probType = null;
                        globals.exampleProbType = null;
                        nextProb(globals);
                    }
                    $( this ).dialog( "close" );
                }
            }
        ]
    });
    $("#"+UTIL_DIALOG).dialog({
        autoOpen: false,
        modal:false,
        width:750,
        height:700,

        buttons: [
            {
                text: "Close",
                click: function() {
                    $( this ).dialog( "close" );
                }
            }
        ]
    });
    $("#selectProb").click(selectProblemDialog);
    $("#prefs").click(showUserPreferences);
    $("#home").click(showDashboard);
    $("#instructions").click(instructions);

    $("#myProg").click(function () {
        myprogress(globals)
    });
}



// This is called only when entering the tutor with specific problem (either from MPP or TeachTopic event from Assistments)
function showFlashProblemAtStart () {
    var activity = globals.activityJSON;
    var mode = activity.mode;
    var activityType = activity.activityType;
    var resource = activity.resource;
    var pid = activity.id;
    var topicName = activity.topicName;
    var standards = activity.standards;
    var type = activity.type;
    var ans = activity.answer;
    var solution = activity.solution;
    var isExample =  (mode == MODE_DEMO || mode == MODE_EXAMPLE);
    var container;
    if (isExample) {
        globals.exampleProbType = activityType;
        container = EXAMPLE_FRAME;
    }
    else {
        container = FLASH_CONTAINER_INNER;
    }
    // THe resumeProblem flag is on if a previous problem was unsolved and the user is returning to it
    if (globals.resumeProblem) {
        globals.resumeProblem = false;
        sendResumeProblemEvent(globals);
    }
    // end the last problem
    else {
        if (globals.lastProbId != -1)
            sendEndEvent(globals);
        sendBeginEvent(globals) ;
    }
    showProblemInfo(pid,resource,topicName,standards);
    if (globals.showAnswer)
        showAnswer(ans);
    showFlashProblem(resource,ans,solution,container, mode);
}

function showHTMLProblemAtStart () {
    var activity = globals.activityJSON;
    var mode = activity.mode;
    var isExample =  (mode == MODE_DEMO || mode == MODE_EXAMPLE);
    var pid = activity.id;
    var resource = activity.resource;
    var topicName = activity.topicName;
    var standards = activity.standards;
    var solution = activity.solution;
    var activityType = activity.activityType;
    var ans = activity.answer;

    var form = activity.form;
    if (form==="quickAuth") {
        globals.isQuickAuth = true;
        globals.statementHTML = activity.statementHTML;
        globals.questionAudio = activity.questionAudio;
        globals.questionImage = activity.questionImage;
        globals.hints = activity.hints;
        globals.answers = activity.answers;
    }
    globals.params = activity.parameters;
    if (isExample) {
        globals.exampleProbType = activityType;
    }
    // THe resumeProblem flag is on if a previous problem was unsolved and the user is returning to it
    if (globals.resumeProblem) {
        globals.resumeProblem = false;
        sendResumeProblemEvent(globals);
    }
    // end the last problem
    else {
        if (globals.lastProbId != -1)
            sendEndEvent(globals);
        sendBeginEvent(globals) ;
    }
    showProblemInfo(pid,resource,topicName,standards);

    if (globals.showAnswer)
        showAnswer(ans);
    showHTMLProblem(pid,solution,resource, mode);
}

// TODO need to create a test for this.
// This came up after being in a problem and attempting it (correctly), going to MPP, then return to hut.
function showInterventionAtStart () {
    if (sysGlobals.isDevEnv)
        alert("Returning to Mathspring and playing intervention: " + globals.activityJSON);

//        var ajson = globals.activityJSON;
//        var qt = '\\"';
//        var re = new RegExp(qt,'g');
//        var cleanJSON = ajson.replace(re,'\\\"');
        var activity = globals.activityJSON;
        if (sysGlobals.isDevEnv)
            alert("Activity is " + activity);
        if (globals.lastProbId != -1)
            sendEndEvent(globals);
        processNextProblemIntervention(activity);


}

function tutorhut_main(g, sysG, trans, learningCompanionMovieClip) {
    globals = g;
    sysGlobals = sysG;
    transients = trans;
    var d = new Date();
    var startTime = d.getTime();
    toggleSolveDialogue(false);
    setMPPVisibility(globals.showMPP);
    showUserInfo(globals.userName);
    clickHandling();
    var d = new Date();
    globals.clock = d.getTime();

    // If this is the first time the tutor is loaded (i.e. from a login) then we send a navigation event so the server initializes
    // correctly based on the student now being in the tutor page.
    // If not the first time, then we are re-entering the tutor page and we want to show a particular problem or intervention
    if (globals.isBeginningOfSession)
        nextProb(globals);

    else if (globals.activityJSON != null && (globals.probType === FLASH_PROB_TYPE || globals.probType === SWF_TYPE)) {
        showFlashProblemAtStart();
    }
    else if (globals.activityJSON != null && globals.probType === HTML_PROB_TYPE) {
        showHTMLProblemAtStart();
    }
    else if (globals.activityJSON != null && globals.probType === INTERVENTION) {
        showInterventionAtStart();
    }
    else {
        if (sysGlobals.isDevEnv)
            alert("making a cyclic call to nextprob from a new mathspring.jsp");
        nextProb(globals);
    }
    if (learningCompanionMovieClip != '')
        $("#"+LEARNING_COMPANION_CONTAINER).dialog("open");
    globals.isBeginningOfSession=false;

}
