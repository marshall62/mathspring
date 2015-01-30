/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 12/12/14
 * Time: 6:46 PM
 * To change this template use File | Settings | File Templates.     s
 */

var debug=false;
var isShortAnswer=false;
var couldNotShuffle = false;
var maxHints = 10;

function probUtilsInit(doc, multiChoice) {
    if (typeof multiChoice !== 'undefined')
        isShortAnswer = !multiChoice;
    if (!isShortAnswer)
        shuffleAnswers(doc);
}

function shuffleAnswers(doc) {
    var oldAnswer = window.parent.getAnswer();
    var newAnswer = window.parent.getNewAnswer();

    var oldAnsText = getElementCorrespondingToAns(oldAnswer);
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
    if (window.parent.getForm() === "quickAuth") {
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
    else {
        switch (ans) {
            case "a":
                return "AnswerAText";
            case "b":
                return "AnswerBText";
            case "c":
                return "AnswerCText";
            case "d":
                return "AnswerDText";
            case "e":
                return "AnswerEText";
            default:
                return "";
        }
    }
}

function answerClicked (doc, buttonName) {
    if (couldNotShuffle) {
        oldAnswer = window.parent.getAnswer();
        newAnswer = window.parent.getNewAnswer();
        if (oldAnswer && newAnswer && buttonName.toUpperCase() === oldAnswer.toUpperCase()) {
            buttonName = newAnswer.toUpperCase(); // TODO: This logic should maybe be in the server
        }
    }
    debugAlert(buttonName + " was clicked. Calling parent.answerChosen.");
    window.parent.tutorhut_answerChosen(doc, buttonName);
}

function prob_gradeAnswer (doc, answerChosen, isCorrect, showHint) {
    debugAlert("gradeAnswer got " + isCorrect);
    if (!isShortAnswer) {
        if (couldNotShuffle) {
            oldAnswer = window.parent.getAnswer();
            newAnswer = window.parent.getNewAnswer();
            if (oldAnswer && newAnswer && answerChosen.toUpperCase() === oldAnswer.toUpperCase()) {
               answerChosen = newAnswer.toUpperCase(); // TODO: This logic should maybe be in the server
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
    hint = getElementCorrespondingToHint(hintLabel);
    clearHintStage();
    stopAudio();
    document.getElementById(hint+"Thumb").style.display = "initial";
    document.getElementById(hint+"ThumbImg").style.display = "none";
    document.getElementById(hint+"ThumbImgPressed").style.display = "initial";
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

}

function clearHintStage(){
    for(i = 1; i <= maxHints; ++i){
        document.getElementById("Hint"+i.toString()).className = "hint default";
        document.getElementById("Hint"+i.toString()).style.display = "none";
        document.getElementById("Hint"+i+"ThumbImg").style.display = "initial";
        document.getElementById("Hint"+i+"ThumbImgPressed").style.display = "none";
    }
}

function stopAudio(){
    if(document.getElementById("QuestionSound").readyState > 0){
        document.getElementById("QuestionSound").pause();
        document.getElementById("QuestionSound").currentTime = 0;
    }
    for(i = 1; i <= maxHints; ++i){
        if(document.getElementById("Hint"+i+"Sound").readyState > 0){
            document.getElementById("Hint"+i+"Sound").pause();
            document.getElementById("Hint"+i+"Sound").currentTime = 0;
        }
    }
}

function getElementCorrespondingToHint(hintLabel){
    if (window.parent.getForm() === "quickAuth") {
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
    else{
        return hintLabel;
    }
}
