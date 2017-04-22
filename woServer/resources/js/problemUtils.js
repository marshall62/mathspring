/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 12/12/14
 * Time: 6:46 PM
 *
 * This file is used only with quickAuth problems.   There is another file with the same name that lives in the /html5/js folder that does a
 * similar thing for Edge problems.
 * To change this template use File | Settings | File Templates.     s
 */

var debug=false;
var isShortAnswer=false;
var couldNotShuffle = false;
var maxHints = 10;
var answer;
var newAnswer;

function probUtilsInit(doc, components) {
    isShortAnswer = components && components.questType === 'shortAnswer'
    if (!isShortAnswer)
        shuffleAnswers(doc,components);
}

function shuffleAnswers(doc, components) {
    //TODO(rezecib): rewrite this because it actually doesn't do anything...... and then verify that it scores correctly
    answer = components.answer;
    newAnswer = components.newAnswer;

    var oldAnsText = getElementCorrespondingToAns(answer);
    var newAnsText = getElementCorrespondingToAns(newAnswer);
    if (oldAnsText === "" || newAnsText === "" ) {
        couldNotShuffle = true;
        return;
    }


    var ansSel = doc.getElementById(oldAnsText);
    var newAnsSel = doc.getElementById(newAnsText);

    if(ansSel == null || newAnsSel == null){
        couldNotShuffle = true;
        return;
    }

    var ansContent = ansSel.innerHTML;
    var otherContent = newAnsSel.innerHTML;
    var temp = ansContent;

    ansSel.innerHTML = otherContent;
    newAnsSel.innerHTML = temp;
}

function getElementCorrespondingToAns(ans) {
        switch (ans) {
            case "a":
                return "AnswerA";
            case "b":
                return "AnswerB";
            case "c":
                return "AnswerC";
            case "d":
                return "AnswerD";
            case "e":
                return "AnswerE";
            default:
                return "";
        }

}

function answerClicked (doc, buttonName) {
    if (couldNotShuffle) {
        if (answer && newAnswer && buttonName.toUpperCase() === answer.toUpperCase()) {
            buttonName = newAnswer.toUpperCase();
        }
    }
    debugAlert(buttonName + " was clicked. Calling parent.answerChosen.");
    window.parent.tutorhut_answerChosen(doc, buttonName);
}

function prob_gradeAnswer (doc, answerChosen, isCorrect, showHint) {
    debugAlert("gradeAnswer got " + isCorrect);
    if (!isShortAnswer) {
        if (couldNotShuffle) {
            if (answer && newAnswer && answerChosen.toUpperCase() === answer.toUpperCase()) {
               answerChosen = newAnswer.toUpperCase();
            }
        }
        answerChosen = answerChosen.toUpperCase();
        if (isCorrect)
        {
            doc.getElementById("AX").style.display = "none";
            doc.getElementById("BX").style.display = "none";
            doc.getElementById("CX").style.display = "none";
            doc.getElementById("DX").style.display = "none";
            doc.getElementById("EX").style.display = "none";
            doc.getElementById(answerChosen+'Check').style.display = "initial";
        }
        else
        {
            doc.getElementById(answerChosen+"X").style.display = "initial";
        }
    }
    else {
        if (isCorrect) {
            doc.getElementById("Grade_X").style.display="none"; // hide it
            doc.getElementById("Grade_Check").style.display="initial";
        }
        else {
            doc.getElementById("Grade_Check").style.display="none"; // hide it
            doc.getElementById("Grade_X").style.display="initial";
        }
    }

}

function processShortAnswer(doc, ans) {
    window.parent.tutorhut_shortAnswerSubmitted(doc,ans);
}

function debugAlert(msg) {
    if (debug ) {
        alert(msg);
    }
}

function prob_readProblem() {
    stopAudio();
    document.getElementById("QuestionSound").play();
}

function prob_playHint (hintLabel) {
    document.getElementById("HintContainer").style.display = "block";
    hint = getIdCorrespondingToHint(hintLabel);
    clearHintStage();
    stopAudio();
    document.getElementById(hint+"Thumb").style.visibility = "visible";
    document.getElementById(hint+"Thumb").className = "hint-thumb-selected";
    document.getElementById(hint).style.display = "initial";
    //Animate the hints coming in.  We want them to alternate sliding in from the left or the bottom
/*    if(hint == "Hint10"){
        document.getElementById(hint).className = "hint default";
    }
    else{
        requestAnimationFrame(function(){
            if(hint == "Hint1" ||
                hint == "Hint3" ||
                hint == "Hint5" ||
                hint == "Hint7" ||
                hint == "Hint9"){
                document.getElementById(hint).className = "hint slide_left";
            }
            else{
                document.getElementById(hint).className = "hint slide_up";
            }
        }, document.getElementById(hint));
    }
  */
    document.getElementById(hint+"Sound").play();
    var preload = document.getElementById(getNextHint(hint) + "Sound");
    if(preload != null && document.getElementById(hint+"Thumb").style.display != "initial"){
        preload.load();
    }

}

function clearHintStage(){
    for(i = 1; i <= maxHints; ++i){
        document.getElementById("Hint"+i).style.display = "none";
        document.getElementById("Hint"+i+"Thumb").className = "hint-thumb";
    }
}

function stopAudio(){
    if(document.getElementById("QuestionSound").readyState > 0){
        document.getElementById("QuestionSound").pause();
        document.getElementById("QuestionSound").currentTime = 0;
    }
    for(i = 1; i <= maxHints; ++i){
        //TODO check that these will always all exist
        if(document.getElementById("Hint"+i+"Sound").readyState > 0){
            document.getElementById("Hint"+i+"Sound").pause();
            document.getElementById("Hint"+i+"Sound").currentTime = 0;
        }
    }
}

function getIdCorrespondingToHint(hintLabel){
        switch (hintLabel) {
            case "Hint 1":
                return "Hint1";
            case "Hint 2":
                return "Hint2";
            case "Hint 3":
                return "Hint3";
            case "Hint 4":
                return "Hint4";
            case "Hint 5":
                return "Hint5";
            case "Hint 6":
                return "Hint6";
            case "Hint 7":
                return "Hint7";
            case "Hint 8":
                return "Hint8";
            case "Hint 9":
                return "Hint9";
            case "Show Answer":
                return "Hint10";
            default:
                return "";
        }

}

function getNextHint(hintLabel){
        switch (hintLabel) {
            case "Hint 1":
                return "Hint2";
            case "Hint 2":
                return "Hint3";
            case "Hint 3":
                return "Hint4";
            case "Hint 4":
                return "Hint5";
            case "Hint 5":
                return "Hint6";
            case "Hint 6":
                return "Hint7";
            case "Hint 7":
                return "Hint8";
            case "Hint 8":
                return "Hint9";
            case "Hint 9":
                return "Hint10";
            default:
                return "";
        }

}
