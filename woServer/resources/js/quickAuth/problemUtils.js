/**
 * Created with IntelliJ IDEA.
 * User: Melissa
 * Date: 12/12/14
 * Time: 6:46 PM
 *
 * This file was mostly rewritten by rezecib.
 * It is used only with quickAuth problems.
 * There is another file with the same name that lives in the /html5/js folder that does a similar thing for Edge problems.
 *
 * Note that several of the functions here need to have particular names,
 * because the window containing the problem will call them on the problem iframe.
 * The functions are:
 * prob_gradeAnswer
 * prob_playHint
 * prob_readProblem
 *
 * To change this template use File | Settings | File Templates.
 */

//Module pattern for better scoping
var problemUtils = (function() {

    //The module we are exporting
var m = {},
    isMultiChoice = false,
    //Stores the click listeners for multiple choice questions,
    // so they can be removed when the right answer has been clicked
    clickListeners = {};

m.initialize = function(multiChoice) {
    isMultiChoice = multiChoice;
}

function answerClicked(doc, buttonName) {
    window.parent.tutorhut_answerChosen(doc, buttonName);
}

m.addMultiChoiceClickListener = function(doc, letter) {
    var listener = function() { answerClicked(doc, letter); };
    clickListeners[letter] = listener;
    doc.getElementById(letter + "Button").addEventListener("click", listener);
}

m.prob_gradeAnswer = function(doc, answerChosen, isCorrect, showHint) {
    if (isMultiChoice) {
        answerChosen = answerChosen.toUpperCase();
        if (isCorrect) {
            var answers = doc.getElementsByClassName("answer-row");
            for(var i = 0; i < answers.length; i++) {
                var letter = answers[i].dataset.letter;
                doc.getElementById(letter + "X").style.display = "none";
                doc.getElementById(letter + "Button").removeEventListener("click", clickListeners[letter]);
                delete clickListeners[letter];
            }
            doc.getElementById(answerChosen+'Check').style.display = "initial";
        } else {
            doc.getElementById(answerChosen+"X").style.display = "initial";
        }
    } else {
        if (isCorrect) {
            doc.getElementById("Grade_X").style.display="none"; // hide it
            doc.getElementById("Grade_Check").style.display="inline-block";
        } else {
            doc.getElementById("Grade_Check").style.display="none"; // hide it
            doc.getElementById("Grade_X").style.display="inline-block";
        }
    }
}

m.processShortAnswer = function(doc, ans) {
    window.parent.tutorhut_shortAnswerSubmitted(doc, ans);
}

m.prob_readProblem = function() {
    stopAudio();
    document.getElementById("QuestionSound").play();
}

m.prob_playHint = function(hintLabel) {
    document.getElementById("HintContainer").style.display = "block";
    hintId = m.getIdCorrespondingToHint(hintLabel);
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
        if(image == null) continue;
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
        videos[vi].play().catch(function(){});
    }
    var hint = document.getElementById(hintId);
    hint.style.display = "initial";
    document.getElementById(hintId+"Sound").play();
    var preload = document.getElementById(getNextHint(hintId) + "Sound");
    if(preload != null && document.getElementById(hintId+"Thumb").style.display != "initial"){
        preload.load();
    }
}

function findFigure(element) {
    if(element.tagName.match(/img|video/i)) return element;
    for(var i = 0; i < element.childNodes.length; ++i) {
        var found = findFigure(element.childNodes[i]);
        if(found) return found;
    }
    return null;
}

function clearHintStage(){
    var hint_contents = document.getElementById("HintContent").children;
    for(var i = 0; i < hint_contents.length; ++i) {
        hint_contents[i].style.display = "none";
    }
    var hint_thumbs = document.getElementById("HintThumbs").children;
    for(var i = 0; i < hint_thumbs.length; ++i) {
        hint_thumbs[i].className = "hint-thumb";
    }
}

function stopAudio(){
    var sounds = document.getElementsByTagName("AUDIO");
    for(i = 1; i < sounds.length; i++) {
        if(sounds[i].readyState > 0) {
            sounds[i].pause();
            sounds[i].currentTime = 0;
        }
    }
}

m.getIdCorrespondingToHint = function(hintLabel){
    if(hintLabel === "Show Answer") hintLabel = "Hint 10";
    if(hintLabel.match(/Hint \d+/)) {
        //If it's "Hint ##", remove the whitespace
        return hintLabel.replace(/\s/g, "");
    }
    return ""; //it was something unrecognized
}

function getNextHint(hintLabel){
    var hintId = m.getIdCorrespondingToHint(hintLabel);
    if(hintId.match(/Hint\d+/)) {
        var num = parseInt(hintId.substring(4));
        if(num < 10) return "hint" + (num + 1);
    }
    return "";
}

m.shuffleAnswers = function(fixLetters) {
    var container = document.getElementById("MultipleChoiceAnswers")
    var answers = container.children;
    //Store the original letter ordering
    var letters = [];
    if(fixLetters) {
        for(var i = 0; i < answers.length; ++i) {
            letters.push(answers[i].children[0].children[2].innerHTML);
        }
    }
    //Do the shuffle
    for(var i = answers.length; i >= 0; --i) {
        container.appendChild(answers[Math.random()*i | 0]);
    }
    //Fix the letters
    if(fixLetters) {
        for(var i = 0; i < answers.length; ++i) {
            answers[i].children[0].children[2].innerHTML = letters[i];
        }
    }
}

return m;

})();

//These unfortunately need to be globals,
// unless the parts of tutorhut that reference them are rewritten
//TODO: do that?
var prob_gradeAnswer = problemUtils.prob_gradeAnswer;
var prob_playHint = problemUtils.prob_playHint;
var prob_readProblem = problemUtils.prob_readProblem;
