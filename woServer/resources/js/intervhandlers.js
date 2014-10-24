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





function processRapidAttemptIntervention (html) {
    interventionDialogOpen("Answering Rapidly", html, ATTEMPT_INTERVENTION );
}
