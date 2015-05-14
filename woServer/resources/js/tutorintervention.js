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
    if (interventionType === "TopicSwitch") {
        processTopicSwitchIntervention(activityJSON.html)
    }
    else if (interventionType === "ExternalActivity") {
       processExternalActivityIntervention(pid, resource);
    }
    else if (interventionType === "AskEmotionIntervention")
        processAskEmotionIntervention(activityJSON.html);
    else if (interventionType === "ShowMPPButton")
        processShowMPPIntervention(activityJSON.html);
    else if (interventionType === "MyProgressNavigation")
        processMyProgressNavIntervention(activityJSON.html);
    else if (interventionType === "MyProgressNavigationAsk")
        processMyProgressNavAskIntervention(activityJSON.html);
    sendBeginIntervention(globals, interventionType);

}





function processAttemptIntervention (interv) {
    if (interv != null) {
        var type = interv.interventionType;
        checkIfInputIntervention(interv);
        if (type === 'HighlightHintButton')
            highlightHintButton();
        else if (type === 'RapidAttemptIntervention')
            processRapidAttemptIntervention(interv.html);
        sendBeginIntervention(globals,type);
    }
}


function processExternalActivityIntervention(pid, resource) {
    debugAlert("Its an external problem.   Changing problemWindow src attribute to " + resource);
    globals.probElapsedTime = 0;
    servletGet("BeginExternalActivity", {xactId: pid, probElapsedTime: globals.probElapsedTime});
    globals.lastProbId = pid;
    globals.lastProbType = EXTERNAL_PROB_TYPE;
    $("#" + PROBLEM_WINDOW).attr("src", resource);
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




function interventionDialogClose () {

    if (globals.interventionType === NEXT_PROBLEM_INTERVENTION && !globals.isInputIntervention)
        servletGet("ContinueNextProblemIntervention", {probElapsedTime: globals.probElapsedTime}, processNextProblemResult);
    else if (globals.interventionType === ATTEMPT_INTERVENTION && !globals.isInputIntervention)
        servletGet("ContinueAttemptIntervention", {probElapsedTime: globals.probElapsedTime}, processNextProblemResult);

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

function interventionDialogOpen (title, html, type) {
    hideNonDefaultInterventionDialogButtons();
    globals.interventionType = type;
    $("#"+INTERVENTION_DIALOG).attr("title", title);
    $("#"+INTERVENTION_DIALOG_CONTENT).html(html);
    $("#"+INTERVENTION_DIALOG).dialog("open");
}

function sendInterventionDialogInputResponse (event, fn) {
    var formInputs = $("#"+INPUT_RESPONSE_FORM).serialize() ;
    incrementTimers(globals);
    servletFormPost(event,formInputs + "&probElapsedTime="+globals.probElapsedTime,fn)
}


//send a BeginProblem event for HTMl5 problems.
function sendBeginIntervention(globals, intervType) {
    incrementTimers(globals);
    servletGetWait("BeginIntervention", {probElapsedTime: globals.probElapsedTime, interventionType: intervType});

}

