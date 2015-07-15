/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 11/15/13
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */

function processNextProblemIntervention(activityJSON) {
    var interventionType = activityJSON.interventionType;
    checkIfInputIntervention(activityJSON);
    var resource = activityJSON.resource;
    var pid = activityJSON.id;
    var changeGUIIntervention = activityJSON.changeGUI==true;

    if (interventionType === "TopicSwitch") {
        processTopicSwitchIntervention(activityJSON.html)
    }
    else if (changeGUIIntervention)
        processChangeGUIIntervention(activityJSON);
    else if (interventionType === "TopicIntro")
        processTopicIntroIntervention(activityJSON);
    else if (interventionType === "ExternalActivity") {
       processExternalActivityIntervention(pid, resource,activityJSON.instructions);
    }
    else if (interventionType === "AskEmotionIntervention")
        processAskEmotionIntervention(activityJSON.html);
    else if (interventionType === "ShowMPPButton")
        processShowMPPIntervention(activityJSON.html);
    else if (interventionType === "MyProgressNavigation")
        processMyProgressNavIntervention(activityJSON.html);
    else if (interventionType === "MyProgressNavigationAsk")
        processMyProgressNavAskIntervention(activityJSON.html);
    else if(interventionType === "CollaborationPartnerIntervention")
        processCollaborationPartnerIntervention(activityJSON.html);
    else if(interventionType === "CollaborationConfirmationIntervention")
        processCollaborationConfirmationIntervention(activityJSON.html);
    else if(interventionType === "CollaborationOriginatorIntervention")
        processCollaborationOriginatorIntervention(activityJSON.html);
    else if(interventionType === "FinishCollaborationIntervention")
        processCollaborationFinishedIntervention(activityJSON.html);
    else if(interventionType === "CollaborationTimeoutIntervention")
        processCollaborationTimeoutIntervention(activityJSON.html);
    else if(interventionType === "CollaborationOptionIntervention")
        processCollaborationOptionIntervention(activityJSON.html);
    sendBeginIntervention(globals,interventionType);

}




function processTopicIntroIntervention (interv) {
    globals.instructions =  "This is an introduction to a topic.  Please review it before beginning work by clicking the new-problem button.";
    globals.destinationInterventionSelector = interv.destinationInterventionSelector;  // needs this so we can send back to IS when topic intro ends
    // send EndEvent  to end the previous problem
    sendEndEvent(globals);
//            showProblemInfo(pid,resource);
    globals.probElapsedTime = 0;
//    sendBeginEvent(globals);
    showTopicIntro(interv.resource,interv.topicName);
    globals.topicId = interv.topicId;
    globals.probId = 999;  // a dummy indicator that this "problem" is a topic intro
    globals.lastProbType=TOPIC_INTRO_PROB_TYPE; // needed so that nextproblem button knows that its ending a topic intro
}


function showTopicIntro (resource, topic) {

    // if nothing pop up an alert
    if (typeof(resource) != 'undefined' && resource != '')
        showFlashProblem(resource,null,null,FLASH_CONTAINER_INNER, false);
    else if (interv.resourceType === 'html')
        showHTMLProblem(null,null,resource,false);

    else alert("Beginning topic: "  + topic + ".  No Flash movie to show")


}


function processAttemptIntervention (interv) {
    if (interv != null) {
        var type = interv.interventionType;
        var changeGUIIntervention = interv.changeGUI==true;

        checkIfInputIntervention(interv);
        if (changeGUIIntervention)
            processChangeGUIIntervention(interv);
        else if (type === 'HighlightHintButton')
            highlightHintButton();
        else if (type === 'RapidAttemptIntervention')
            processRapidAttemptIntervention(interv.html);
        sendBeginIntervention(globals,type);
    }
}


function processExternalActivityIntervention(pid, resource, instructions) {
    debugAlert("Its an external problem.   Changing problemWindow src attribute to " + resource);
    globals.probElapsedTime = 0;
    servletGet("BeginExternalActivity", {xactId: pid, probElapsedTime: globals.probElapsedTime});
    globals.lastProbId = pid;
    globals.lastProbType = EXTERNAL_PROB_TYPE;
    globals.instructions= instructions;
    showInstructionsDialog(instructions);
    window.open(resource,'_blank');
//    $("#" + PROBLEM_WINDOW).attr("src", resource);
}


// check to see if the isInputIntervention flag is true and set a global variable so that
// we send back correct event when intervention ends
function checkIfInputIntervention (interv) {
    var isInputIntervention = interv.isInputIntervention;
    // this flag will alter the event sent when the intervention dialog closes
    if (isInputIntervention || isInputIntervention === 'true')
        globals.isInputIntervention = true;
    else globals.isInputIntervention = false;
    return globals.isInputIntervention;
}

//  The intervention dialog can be opened with either an OK button or a Yes-No buttons.  When those buttons are clicked, one of the three buttons below is
// called.
// At the time the intervention dialog is opened (say with Yes-No buttons), two function names are also provided - one for handling yes, one for no.
// These function names are stored inside click attribute of the button part of the dialog

function interventionDialogYesClick () {
    myprogress(globals)  ;
}

function interventionDialogNoClick () {
    interventionDialogClose();
}

function interventionDialogOKClick () {
    interventionDialogClose();
}




function interventionDialogClose () {
    if (globals.interventionType === NEXT_PROBLEM_INTERVENTION && !globals.isInputIntervention)
        servletGet("ContinueNextProblemIntervention", {probElapsedTime: globals.probElapsedTime, destination: globals.destinationInterventionSelector}, processNextProblemResult);
    else if (globals.interventionType === ATTEMPT_INTERVENTION && !globals.isInputIntervention)
        servletGet("ContinueAttemptIntervention", {probElapsedTime: globals.probElapsedTime, destination: globals.destinationInterventionSelector}, processNextProblemResult);

    // If closing down an intervention dialog for next problem we send the InputResponse event and ask processNextProblemResult
    // to handle what the server returns.   For attempts we send InputResponse event but, FOR NOW, we don't expect the server
    // to return anything so no callback is given.
    // TODO We may want to actually do something based on an input response intervention dialog on attempts so we'd need some
    // kind of handler in place that can react to what the server returns.
    else if (globals.interventionType === NEXT_PROBLEM_INTERVENTION && globals.isInputIntervention)
        sendInterventionDialogInputResponse("InputResponseNextProblemIntervention",processNextProblemResult);

    else if (globals.interventionType === ATTEMPT_INTERVENTION && globals.isInputIntervention)
        sendInterventionDialogInputResponse("InputResponseAttemptIntervention");

    $("#"+INTERVENTION_DIALOG_CONTENT).html("");
    globals.interventionType = null;
    globals.isInputIntervention= false;
    $("#"+INTERVENTION_DIALOG).dialog("close");
}

// Presumably this is being used as a message that stays up until a timeout delay and then the timeoutFunction gets called.
function interventionDialogOpenNoButtons (title, html, type, timeoutFunction,delay) {
    $("#ok_button").hide();
    $("#noButton").hide();
    $("#yesButton").hide();
    openInterventionDialog(title,html,type);
    setTimeout(timeoutFunction, delay)
}

function interventionDialogOpenAsConfirm (title, html, type, confirmFunction) {
    $("#ok_button").show();
    $("#noButton").hide();
    $("#yesButton").hide();
    setInterventionDialogButtonHandlerFunction("#ok_button",confirmFunction) ;
    openInterventionDialog(title,html,type);
}

function interventionDialogOpenAsYesNo (title, html, type, yesFunction, noFunction) {
    $("#ok_button").hide();
    $("#noButton").show();
    $("#yesButton").show();
    setInterventionDialogButtonHandlerFunction("#noButton",noFunction);
    setInterventionDialogButtonHandlerFunction("#yesButton",yesFunction);
    openInterventionDialog(title,html,type);
}

function setInterventionDialogButtonHandlerFunction (buttonId, buttonHandlerFunction)  {
    // get the button by its id  and replace its click attribute with the given function
   var button = $(buttonId)
   button.unbind("click");  // get rid of previous handlers attached to the button
   button.click(buttonHandlerFunction);
}

function openInterventionDialog (title, html, type) {
    globals.interventionType = type;
    $("#"+INTERVENTION_DIALOG).attr("title", title);
    $("#"+INTERVENTION_DIALOG_CONTENT).html(html);
    $("#"+INTERVENTION_DIALOG).dialog("open");
}



function sendInterventionDialogInputResponse (event, fn) {
    var formInputs = $("#"+INPUT_RESPONSE_FORM).serialize() ;
    incrementTimers(globals);
    servletFormPost(event,formInputs + "&probElapsedTime="+globals.probElapsedTime  + "&destination="+globals.destinationInterventionSelector,fn)
}


//send a BeginProblem event for HTMl5 problems.
function sendBeginIntervention(globals, intervType) {
    incrementTimers(globals);
    servletGetWait("BeginIntervention", {probElapsedTime: globals.probElapsedTime, interventionType: intervType});

}

