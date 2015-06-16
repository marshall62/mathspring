var timeout = 0;

// This is for an attempt event that asks to highlight the hint button
// its a shame this function has to know the image files that are defined in the CSS rather than fetching them from it.
function highlightHintButton() {
    $("#hint").css('background-image','url(img/hint1.png)').fadeTo(500,0, function() {
        $("#hint").css('background-image', 'url(img/hint4.png)').fadeTo(1000, 1, function() {
            setTimeout(function() {
                $("#hint").css('background-image', 'url(img/hint4.png)').fadeTo(1000, 0, function() {
                    $("#hint").css('background-image','url(img/hint1.png)').fadeTo(300,1);
                });
            } ,2000);
        });
    });
}

// this pops up a dialog informing or asking about topic switching
function processTopicSwitchIntervention(html) {
    //alert("Switching topics because " + reason)
    interventionDialogOpen("Switching Topics", html, NEXT_PROBLEM_INTERVENTION );


}

function processAskEmotionIntervention(html) {
    //alert("Switching topics because " + reason);
    interventionDialogOpen("How are you doing", html, NEXT_PROBLEM_INTERVENTION );

}

function processShowMPPIntervention () {
     showMPP();
}

function processMyProgressNavIntervention (html) {
    interventionDialogOpen("Let's see our progress!", html, NEXT_PROBLEM_INTERVENTION);
    $("#ok_button").hide();
    $("#see_progress_button").show();

}

function processMyProgressNavAskIntervention (html) {
    interventionDialogOpen("Let's see our progress!", html, NEXT_PROBLEM_INTERVENTION);
    $("#ok_button").hide();
    $("#see_progress_button").show();
    $("#no_thanks_button").show();

}

// This sets up an intervention on the helpers screen that freezes their input until the collaboration with
// a partner (on the partners computer) is done.   This checks every 3 seconds to see if this intervention
// is complete.   The server keeps returning the same intervention/learningCompanion when it's not complete and a different
// intervention when it is complete
function processCollaborationPartnerIntervention(html) {
    globals.destinationInterventionSelector = "edu.umass.ckc.wo.tutor.intervSel2.CollaborationPartnerIS";
    interventionDialogOpen("Work with a partner", html, NEXT_PROBLEM_INTERVENTION);
    $("#ok_button").hide();
    setTimeout(function(){
        // must provide destination because delegate intervention selectors no longer work
        servletGet("ContinueNextProblemIntervention", {probElapsedTime: globals.probElapsedTime, destination: globals.destinationInterventionSelector}, processNextProblemResult);
        globals.interventionType = null;
        globals.isInputIntervention= false;
    }, 3000);
}

function processCollaborationConfirmationIntervention(html) {
    globals.destinationInterventionSelector = "edu.umass.ckc.wo.tutor.intervSel2.CollaborationOriginatorIS";
    interventionDialogOpen("Work with a partner", html, NEXT_PROBLEM_INTERVENTION);
}

// When the originator is waiting for a partner this checks the server every 5 seconds to see if the partner is available to work
// with.   Every 60 seconds it asks if they want to continue waiting for a partner.
function processCollaborationOriginatorIntervention(html) {
    globals.destinationInterventionSelector = "edu.umass.ckc.wo.tutor.intervSel2.CollaborationOriginatorIS";
    interventionDialogOpen("Waiting for a partner", html, NEXT_PROBLEM_INTERVENTION);
    $("#ok_button").hide();
    // every minute it makes a request that results in an intervention that asks if they want to continue waiting for a partner
    // every 5 seconds it makes a request to see if the partner is available which results in the same intervention being put up if not
    // and a different intervention if the partner is available.
    setTimeout(function(){
            if(timeout >= 60000){
                timeout = 0;
                // must provide destination because delegate intervention selectors no longer work
                servletGet("TimedIntervention", {probElapsedTime: globals.probElapsedTime, destination: globals.destinationInterventionSelector}, processNextProblemResult);
            }
            else{
                timeout = timeout + 5000;
                // must provide destination because delegate intervention selectors no longer work
                servletGet("ContinueNextProblemIntervention", {probElapsedTime: globals.probElapsedTime, destination: globals.destinationInterventionSelector}, processNextProblemResult);
            }
            globals.interventionType = null;
            globals.isInputIntervention= false;}
        , 5000);
}

function processCollaborationFinishedIntervention(html) {
    globals.destinationInterventionSelector = "edu.umass.ckc.wo.tutor.intervSel2.CollaborationPartnerIS";
    interventionDialogOpen("Collaboration over", html, NEXT_PROBLEM_INTERVENTION);
}

function processCollaborationTimeoutIntervention(html) {
    globals.destinationInterventionSelector = "edu.umass.ckc.wo.tutor.intervSel2.CollaborationOriginatorIS";
    interventionDialogOpen("Continue Waiting?", html, NEXT_PROBLEM_INTERVENTION );
}

function processCollaborationOptionIntervention(html) {
    globals.destinationInterventionSelector = "edu.umass.ckc.wo.tutor.intervSel2.CollaborationOriginatorIS";
    interventionDialogOpen("Work with a partner?", html, NEXT_PROBLEM_INTERVENTION);
}


function processRapidAttemptIntervention (html) {
    interventionDialogOpen("Answering Rapidly", html, ATTEMPT_INTERVENTION );
}
