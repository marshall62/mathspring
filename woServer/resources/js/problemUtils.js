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

var debug = false;
var isMultiChoice = false;
var couldNotShuffle = false;
var maxHints = 10;

function probUtilsInit(doc, components) {
    isMultiChoice = components && components.questType.match(/multiChoice/i)
}

function answerClicked (doc, buttonName) {
    debugAlert(buttonName + " was clicked. Calling parent.answerChosen.");
    window.parent.tutorhut_answerChosen(doc, buttonName);
}

function prob_gradeAnswer (doc, answerChosen, isCorrect, showHint) {
    debugAlert("gradeAnswer got " + isCorrect);
    if (isMultiChoice) {
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
            doc.getElementById("Grade_Check").style.display="inline-block";
        }
        else {
            doc.getElementById("Grade_Check").style.display="none"; // hide it
            doc.getElementById("Grade_X").style.display="inline-block";
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

function findFigure(element) {
    if(element.tagName.match(/img|video/i)) return element;
    for(var i = 0; i < element.childNodes.length; ++i) {
        var found = findFigure(element.childNodes[i]);
        if(found) return found;
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
    var figure = findFigure(document.getElementById("ProblemFigure"));
    var figure_parent = figure != null ? figure.parentNode : null;
    var figure_html = figure != null ? figure.outerHTML : "";
    //Clear any overlaid hint images
    if(figure != null && figure.parentNode.dataset.figureHtml != null) {
        figure_html =  figure_parent.dataset.figureHtml
        figure_parent.innerHTML = figure_html;
        delete figure_parent.dataset.figureHtml;
    }
    //clear side images
    var hintFigure = document.getElementById("HintFigure");
    if(hintFigure != null) hintFigure.innerHTML = "";
    var image_parameters = JSON.parse(hint_thumb.dataset.parameters);
    //add overlay and queue up side images
    var side_figures = [];
    for(var image_id in image_parameters) {
        var parameter = image_parameters[image_id];
        var image = document.getElementById(image_id);
        if(parameter == "overlay" && figure_parent != null) {
            //don't overwrite it if it's already there (so multiple overlays don't screw everything up)
            figure_parent.dataset.figureHtml = figure_parent.dataset.figureHtml || figure_html;
            image.style.display = "block"; //set it to display before we grab the html
            figure_parent.innerHTML = image.outerHTML;
            image.style.display = "none"; //don't display the original in the hint area
        } else if(parameter == "side" && hintFigure != null) {
            image.style.display = "block"; //set it to display before we grab the html
            side_figures.push(image.outerHTML);
            image.style.display = "none"; //don't display the original in the hint area
        }
    }
    //add in the side images, scaled by how many there are
    for(var i = 0; i < side_figures.length; ++i) {
        var side_figure = document.createElement("div");
        side_figure.style.lineHeight = "0";
        side_figure.innerHTML = side_figures[i];
        side_figure.style.width = (100/side_figures.length) + "%";
        side_figure.style.display = "inline-block";
        hintFigure.appendChild(side_figure);
    }
    var videos = document.getElementsByTagName("VIDEO");
    for(var vi = 0; vi < videos.length; ++vi) {
        videos[vi].currentTime = 0;
        videos[vi].play();
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
    if(hintLabel === "Show Answer") hintLabel = "Hint 10";
    if(hintLabel.match(/Hint \d+/)) {
        //If it's "Hint ##", remove the whitespace
        return hintLabel.replace(/\s/g, "");
    }
    return ""; //it was something unrecognized
}

function getNextHint(hintLabel){
    var hintId = getIdCorrespondingToHint(hintLabel);
    if(hintId.match(/Hint\d+/)) {
        var num = parseInt(hintId.substring(4));
        if(num < 10) return "hint" + (num + 1);
    }
    return "";
}
