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

function getFirstFigureImage() {
    var figure_block = document.getElementById("ProblemFigure");
    for(var i = 0; i < figure_block.children.length; i++) {
        if(figure_block.children[i].tagName.match(/img/i)) return figure_block.children[i];
    }
    return null;
}

function prob_playHint (hintLabel) {
    document.getElementById("HintContainer").style.display = "block";
    hintId = getIdCorrespondingToHint(hintLabel);
    clearHintStage();
    stopAudio();
    var hint_thumb = document.getElementById(hintId+"Thumb");
    hint_thumb.style.visibility = "visible";
    hint_thumb.className = "hint-thumb-selected";
    var figure = getFirstFigureImage();
    //Clear any overlaid hint images
    if(figure != null && figure.dataset.figureSrc != null) {
        figure.setAttribute("src", figure.dataset.figureSrc);
        delete figure.dataset.figureSrc;
    }
    //clear side images
    var hintFigure = document.getElementById("HintFigure");
    if(hintFigure != null) hintFigure.innerHTML = "";
    var image_parameters = JSON.parse(hint_thumb.dataset.parameters);
    //add overlay and side images
    for(var image_id in image_parameters) {
        var parameter = image_parameters[image_id];
        var image = document.getElementById(image_id);
        if(parameter == "overlay" && figure != null) {
            // don't overwrite figureSrc if it's already been set, because then it would store a hint image instead
            figure.dataset.figureSrc = figure.dataset.figureSrc || figure.getAttribute("src");
            figure.setAttribute("src", image.getAttribute("src"));
            image.style.display = "none"; //don't display the original in the hint area
        } else if(parameter == "side" && hintFigure != null) {
            var side_image = document.createElement("img");
            side_image.setAttribute("src", image.getAttribute("src"));
            hintFigure.appendChild(side_image);
            image.style.display = "none"; //don't display the original in the hint area
        }
    }
    var hint = document.getElementById(hintId);
    hint.style.display = "initial";
    //Animate the hints coming in.  We want them to alternate sliding in from the left or the bottom
/*    if(hintId == "Hint10"){
        document.getElementById(hintId).className = "hint default";
    }
    else{
        requestAnimationFrame(function(){
            if(hintId == "Hint1" ||
                hintId == "Hint3" ||
                hintId == "Hint5" ||
                hintId == "Hint7" ||
                hintId == "Hint9"){
                document.getElementById(hintId).className = "hint slide_left";
            }
            else{
                document.getElementById(hintId).className = "hint slide_up";
            }
        }, document.getElementById(hintId));
    }
  */
    document.getElementById(hintId+"Sound").play();
    var preload = document.getElementById(getNextHint(hintId) + "Sound");
    if(preload != null && document.getElementById(hintId+"Thumb").style.display != "initial"){
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
