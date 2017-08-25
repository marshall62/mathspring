//Module pattern for better scoping
var quickAuthBuildProblem = (function() {

    //The module we are exporting
var m = {};

function isNotEmpty(value) {
    //note that simple equality considers null == undefined, so this will catch both
    return value != null && value != "";
}

m.build = function(activity, previewMode) {
    var problem = activity.problem;
    var questType = problem.questType;
    var mode = problem.mode;
    var probContentPath = activity.probContentPath || problem.probContentPath;
    probContentPath = probContentPath.replace(/\/$/g, "");
    var resource = problem.resource;
    var probSound = problem.questionAudio;
    var probUnits = problem.units;
    var problemParams = activity.binding;
    pickParams(problemParams); //chooses which set of parameters to use

    var problemContainer = document.getElementById("ProblemContainer");
    var probStatement = problem.statementHTML;
    var probFigure = problem.questionImage;
    var answers = problem.answers;
    var hints = problem.hints;
    var problemFormat = problem.format;

    quickAuthFormatBuilder.buildProblem(problemContainer, problemFormat, true, false, false);
    if(isNotEmpty(probStatement)){
        document.getElementById("ProblemStatement").innerHTML = parameterizeText(formatText(probStatement, resource, probContentPath, problemParams, previewMode), problemParams);
    }
    if(isNotEmpty(probFigure)){
        document.getElementById("ProblemFigure").innerHTML = parameterizeText(formatText(probFigure, resource, probContentPath, problemParams, previewMode), problemParams);
    }
    if(isNotEmpty(probSound)) {
        sound_elt = document.createElement("audio");
        sound_elt.id = "QuestionSound";
        //I think this was intended to support ogg as well,
        // but all hint audioResources have no extension
        // and there's no way to magically figure out which one is right
        // sound_elt.setAttribute("src", getURL(probSound + ".ogg", resource, probContentPath));
        sound_elt.setAttribute("src", getURL(probSound + ".mp3", resource, probContentPath));
        document.getElementById("ProblemStatement").appendChild(sound_elt);
    }
    if(isNotEmpty(probUnits)) {
        //Why can't we just use probUnits directly?
        document.getElementById("Units").innerHTML = parameterizeText(formatText(window.parent.getUnits(), resource, probContentPath, problemParams, previewMode), problemParams);
    }

    var hint_labels = [];
    var hint_thumbs = document.getElementById("HintThumbs");
    if(isNotEmpty(hints)) {
        for (i = 0; i < hints.length; ++i) {
            var hintLabel = hints[i].label;
            hint_labels.push(hintLabel);
            var hintId = problemUtils.getIdCorrespondingToHint(hintLabel);
            var hint_thumb = document.createElement("div");
            hint_thumb.className = "hint-thumb";
            hint_thumb.id = hintId + "Thumb";
            hint_thumb.style.visibility = "hidden";
            hint_thumb.innerHTML = i+1;
            hint_thumb.addEventListener("click",
                //This looks weird but is necessary to save the hintLabel value properly
                function(hint) {
                    return function(){prob_playHint(hint);};
                }(hintLabel)
            );
            hint_thumbs.appendChild(hint_thumb);
            var hint_content = document.getElementById("HintContent");
            var hint = document.createElement("div");
            hint.id = hintId;
            hint.className = "hint";
            hint_content.appendChild(hint);
            if(hints[i].statementHTML != undefined && hints[i].statementHTML != ""){
                var image_parameters = {};
                var formatted_text = formatTextWithImageParameters(hints[i].statementHTML, resource, probContentPath, problemParams, image_parameters, hintId, previewMode);
                hint_thumb.dataset.parameters = JSON.stringify(image_parameters);
                hint.innerHTML = parameterizeText(formatted_text, problemParams);
            }
            else{
                alert("text missing for hint: "+i);
            }
            if(isNotEmpty(hints[i].hoverText)){
                document.getElementById(hintId+"Thumb").setAttribute("title", parameterizeText(formatText(hints[i].hoverText, resource, probContentPath, problemParams, previewMode), problemParams));
            }
            if (isNotEmpty(hints[i].audioResource)) {
                //I think this was intended to support ogg as well,
                // but all hint audioResources have no extension
                // and there's no way to magically figure out which one is right
                var sound_elt = document.createElement("audio");
                sound_elt.id = hintId+"Sound";
                // sound_elt.setAttribute("src", getURL(hints[i].audioResource + ".ogg", resource, probContentPath));
                sound_elt.setAttribute("src", getURL(hints[i].audioResource + ".mp3", resource, probContentPath));
                hint.appendChild(sound_elt);
                if(i == 0) {
                    sound_elt.load()
                }
            }
        }
    }

    //For demo and example modes all answers should stay hidden
    if (mode !== "demo" && mode !== "example") {
        if (questType.match(/^multi(Choice|Select)$/)) {
            var multi_answers = document.getElementById("MultipleChoiceAnswers");
            var multiSelect = questType === "multiSelect";
            multi_answers.style.display = "block";
            if(isNotEmpty(answers)) {
                for(var letter in answers) {
                    if(!answers.hasOwnProperty(letter)) continue;
                    var answer_html = parameterizeText(formatText(answers[letter], resource, probContentPath, problemParams, previewMode), problemParams);
                    var answerElt = buildAnswerRow(multiSelect, letter.toUpperCase(), answer_html);
                    multi_answers.appendChild(answerElt);
                    var button = document.getElementById(letter.toUpperCase() + "Button");
                    if(multiSelect) {
                        // button.style.display = "none";
                        // resets it to what the CSS says, which is inline-block
                        // document.getElementById(letter.toUpperCase() + "Checkbox").style.display = "";
                    } else {
                        problemUtils.addMultiChoiceClickListener(document, letter.toUpperCase());
                    }
                }
            }
            if(multiSelect) {
                document.getElementById("submit_answer").addEventListener("click", submitMultiSelectAnswer);
            }
            if(!previewMode) problemUtils.shuffleAnswers(!multiSelect);
        } else {
            if(questType === "shortAnswer") {
                document.getElementById("ShortAnswerBox").style.display = "block";
                document.getElementById("submit_answer").addEventListener("click", submitShortAnswer);
            }
        }
        if(questType.match(/^(multiSelect|shortAnswer)$/)) {
            document.getElementById("SubmitAnswerBox").style.display = "block";
        }
    }

    if(previewMode) {
        problemContainer.style.float = "left";
        var play_hint_button = document.createElement("div");
        play_hint_button.className = "play-hint-button";
        play_hint_button.innerHTML = "Play Hint";
        var hint_label_index = 0;
        play_hint_button.onclick = function() {
            prob_playHint(hint_labels[hint_label_index]);
            if(hint_label_index < hint_labels.length - 1) ++hint_label_index;
        }
        play_hint_button.style.transform = document.getElementById("ProblemContainer").style.transform;
        play_hint_button.style.transformOrigin = "top left";
        var matches = play_hint_button.style.transform.match(/scale\(([^\)]+)\)/);
        if(matches) {
            play_hint_button.style.marginLeft =
                (problemContainer.offsetWidth * parseFloat(matches[1])) + "px";
        }
        document.body.appendChild(play_hint_button);
    }
    // Detects LaTeX code and turns it into nice HTML
    MathJax.Hub.Typeset();

    problemUtils.initialize(questType && questType.match(/multichoice/i));
}

function buildAnswerRow(multiSelect, letter, answer_html) {
    var answer_row = document.createElement("div");
    answer_row.className = "answer-row";
    answer_row.dataset.letter = letter;
    var answer_button = multiSelect ?
        `<input id="${letter}Checkbox" class="multiselect-checkbox" type="checkbox">`:
        `
        <div id="${letter}Button" class="button">
            <div id="${letter}Check" class="check"></div>
            <div id="${letter}X" class="x"></div>
            <div id="${letter}Text" class="button_text">${letter}</div>
            <div id="${letter}Ellipse" class="ellipse"></div>
        </div>
        `;
    answer_row.innerHTML = answer_button + `<div id="Answer${letter}" class="answer_text">${answer_html}</div>`;
    return answer_row;
}

function submitShortAnswer() {
    problemUtils.processShortAnswer(document, document.getElementById("answer_field").value);
}

function submitMultiSelectAnswer() {
    var selections = "";
    var answerRows = document.getElementById("MultipleChoiceAnswers").children;
    for(var i = 0; i < answerRows.length; ++i) {
        if(answerRows[i].style.display != "none") { //ignore rows for nonexistent answers
            var letter = answerRows[i].dataset.letter;
            //if the answer is selected, append it to the string
            if(document.getElementById(letter + "Checkbox").checked) {
                selections += letter;
            }
        }
    }
    problemUtils.processShortAnswer(document, selections);
}

function getURL(filename, resource, probContentPath) {
    if (filename == null || filename == undefined)
        return filename;
    return probContentPath + "/html5Probs/" + resource.split(".")[0] + "/" + filename;
}

function getImageHtml(file, ext, resource, probContentPath, id, previewMode){
    //if id is null, don't add an id to the image
    id = id == null ? "" : ' id="' + id + '"';

    //Replace and image file name inside {} with the appropriate html
    var pre = '<div style="line-height:0;">';
    var pst = '</div>';
    if(ext != null) {
        var size_style = ' style="max-height: 100%; max-width: 100%"';
        if(ext.match(/^(gif|png|jpe?g|svg)$/i)){
            return pre + '<img' + id + size_style + ' src="' + getURL(file + "." + ext, resource, probContentPath) + '">' + pst;
        } else if(ext.match(/^(mp4|ogg|webm)$/i)) { //Do the same for a video
            return pre + '<video' + id + size_style + ' src="' + getURL(file + "." + ext, resource, probContentPath) + '" preload="auto"></video>' + pst;
        }
    }
    console.log("invalid image or video", file + "." + ext, resource, probContentPath);
    if(previewMode) {
        return pre + "<div style=\"color:red;line-height:16px;\">INVALID IMAGE OR VIDEO: " + file + "." + ext + "</div>" + pst;
    }
    return "";
}

//Just a wrapper for when you don't care about the parameters
function formatText(text, resource, probContentPath, problemParams, previewMode) {
    return formatTextWithImageParameters(text, resource, probContentPath, problemParams, {}, "unclassified", previewMode);
}

//Extracts images/video contained by {[]}
// also processes and stores placement parameters for the image
// e.g. whether to place it over the figure, side-by-side, or in the hint
//text: string; the text to format
//components: object; components of the problem
//parameters: object; will be used to store parameters for each image/video, as a mapping of {image_id -> parameter}
function formatTextWithImageParameters(text, resource, probContentPath, problemParams, imageParams, base_id, previewMode) {
    // this is just a pattern to extract all occurrences of {[...]}
    var matches = text.match(/\{\[[^\{\}\[\]]*\]\}/igm);
    if(matches == null) matches = [];
    for(var i = 0; i < matches.length; i++) {
        var match = matches[i].slice(2, -2); //remove {[]}
        var parameterized_match = parameterizeText(match, problemParams);
        var replacement = matches[i];
        if(parameterized_match != match) { //then this is an expression
            replacement = parseSimpleExp(parameterized_match);
        } else { //it's an image
            //an image consists of {[image.png, parameter]}
            // where parameter is supposed to specify where the image needs to be moved
            // when the hint is displayed (e.g. overlay, side, hint)
            var image_parameters = match.split(",");
            var image_parts = image_parameters[0].trim().split("."); //separate into filename, extension
            var image_id = base_id + "-" + i;
            // currently assuming only one parameter
            if (base_id != null && image_parameters.length > 1) {
                imageParams[image_id] = image_parameters[1].trim();
            }
            replacement = getImageHtml(image_parts[0], image_parts[1], resource, probContentPath, image_id, previewMode);
        }
        text = text.replace(matches[i], replacement);
        //Note that we don't need to worry about duplicates;
        // replace will only do one replacement, and the matches are extracted in order
    }
    return text;
}

//This is Melissa's original format function
//It seems way more complicated than it needs to be,
// but perhaps has some additional behavior (expression parsing?),
// so I'm keeping it around for now
function formatTextOld(rawText, components) {
    if(rawText == null){
        return rawText;
    }

    var escaped = false;
    var imgOrVid = "";
    var extension = "";
    var isExtension = false;
    var isImgOrVid = false;
    var startIndex = undefined;
    var endIndex = undefined;
    var maxLength = rawText.length;
    for(var j = 0; j < maxLength; ++j){

        if(j == maxLength-1 && startIndex != undefined)
            alert("unclosed '{[' or '{#'");

        switch (rawText.charAt(j)) {
            case '\\':
                //If not already escaped, adds a backlash
                //Otherwise, marks as escaped
                if(!escaped){
                    escaped = true;
                }
                else{
                    if(startIndex != undefined){
                        if(isExtension){
                            extension = extension + '\\';
                        }
                        else{
                            imgOrVid = imgOrVid + '\\';
                        }
                    }
                    escaped = false;
                }
                break;

            case '{':
                if(!escaped && rawText.charAt(j+1) == "["){
                    startIndex = j;
                    j++;
                    isImgOrVid = true;
                }
                else if(!escaped && rawText.charAt(j+1) == "#"){
                    startIndex = j;
                    j++;
                }
                else{
                    if(startIndex != undefined){
                        if(isExtension){
                            extension = extension + '{';
                        }
                        else{
                            imgOrVid = imgOrVid + '{';
                        }
                    }
                    escaped = false;
                }
                break;

            case ']':
                if(!escaped && rawText.charAt(j+1) == "}"){
                    if(!isImgOrVid){
                        alert("end image or video tag present without start tag");
                        startIndex = undefined;
                        endIndex = undefined;
                        isExtension = false;
                        extension = "";
                        imgOrVid = "";
                    }
                    else if(startIndex != undefined){
                        j++;
                        endIndex = j;
                        var toInsert = getImageHtml(imgOrVid, extension, components.resource, components.probContentPath);
                        rawText = rawText.substring(0, startIndex) + toInsert + rawText.substring(endIndex+1, rawText.length);
                        var newLen = toInsert.toString().length;
                        j = startIndex + newLen;
                        maxLength = maxLength + newLen - (endIndex - startIndex + 1);
                        startIndex = undefined;
                        endIndex = undefined;
                        isExtension = false;
                        extension = "";
                        imgOrVid = "";
                        isImgOrVid = false;
                    }
                }
                else{
                    if(startIndex != undefined){
                        if(isExtension){
                            extension = extension + ']';
                        }
                        else{
                            imgOrVid = imgOrVid + ']';
                        }
                    }
                    escaped = false;
                }
                break;
            case '#':
                if(!escaped && rawText.charAt(j+1) == "}"){
                    if(isImgOrVid){
                        alert("Cannot have an equation inside of an image or video");
                        startIndex = undefined;
                        endIndex = undefined;
                        isExtension = false;
                        extension = "";
                        imgOrVid = "";
                    }
                    else if(startIndex != undefined){
                        j++;
                        endIndex = j;
                        var toInsert  = parseSimpleExp(imgOrVid + "." + extension, components);
                        if(typeof toInsert === 'number' && isNaN(toInsert)){
                            alert("invalid expression detected");
                        }
                        rawText = rawText.substring(0, startIndex) + toInsert + rawText.substring(endIndex+1, rawText.length);
                        var newLen = toInsert.toString().length;
                        j = startIndex + newLen;
                        maxLength = maxLength + newLen - (endIndex - startIndex + 1);
                        startIndex = undefined;
                        endIndex = undefined;
                        isExtension = false;
                        extension = "";
                        imgOrVid = "";
                    }
                }
                else{
                    if(startIndex != undefined){
                        if(isExtension){
                            extension = extension + '#';
                        }
                        else{
                            imgOrVid = imgOrVid + '#';
                        }
                    }
                    escaped = false;
                }
                break;

            case '.':
                if(startIndex != undefined){
                    if(isExtension){
                        extension = extension + '.';
                    }
                    else{
                        isExtension = true;
                    }
                }
                escaped = false;
                break;

            default:
                if(startIndex != undefined){
                    if(isExtension){
                        extension = extension + rawText.charAt(j);
                    }
                    else{
                        imgOrVid = imgOrVid + rawText.charAt(j);
                    }
                }
                escaped = false;
                break;
        }


    }
    return rawText;
}

//TODO(mfrechet) test try catches
//If we want fancier expressions it may make more sense to use something like math.js
//Parses correctly formatted expressions containing only operators from the set {+,-,/,*,^}
function parseSimpleExp(expression){
    //Remove white space to make processing easier
    expression = expression.replace(/\s/g,"");

    var operands = new Array();
    var operators = new Array();

    for(var j = 0, expLength = expression.length; j < expLength; j++){
        var currChar = expression.charAt(j);
        //Add an operand to the operand stack.  Remember it could have more than one digit.
        if(((currChar >= '0' && currChar <= '9') || currChar == '.')){
            var buffer = ""
            while(j < expLength && ((currChar >= '0' && currChar <= '9') || currChar == '.')){
                buffer = buffer + currChar;
                j++;
                currChar = expression.charAt(j);
            }
            operands.push(parseFloat(buffer));
        }

        //Push opening brace to operator stack.  Must be if and not else if, due to incrementing j when processing operands.
        if(currChar == '('){
            operators.push(currChar);
        }

        //When a closing brace is found, solve the brace
        else if(currChar == ')'){
            try{
                while(operators[operators.length -1] != '('){
                    operands.push(applyOperator(operators.pop(), operands.pop(), operands.pop()));
                }
            operators.pop();
            }
            catch(err){
                return undefined;
            }
        }

        //If the token is an operator
        else if(currChar == '+' || currChar == '-' || currChar == '/' || currChar == '*' || currChar == '^'){

            try{
                //Make sure order of operations is obeyed by performing operators on stack before current if they have higher precedence
                while(operators.length != 0 && hasPrecedence(currChar, operators[operators.length -1])){
                    operands.push(applyOperator(operators.pop(), operands.pop(), operands.pop()));
                }
                operators.push(currChar);
            }
            catch(err){
                return undefined;
            }
        }

    }

    //compute what remains in the stacks after the expression has been parsed
    while(operators.length != 0){
        try{
            operands.push(applyOperator(operators.pop(), operands.pop(), operands.pop()));
        }
        catch(err){
            return undefined;
        }
    }

    try{
        return operands.pop();
    }
    catch(err){
        return undefined;
    }
}

//Returns true if op2 has >= precedence to op1.  Note that false is returned when op2 is a paren.  This is because we process parens separately.
function hasPrecedence(op1, op2){
    if(op2 == '(' || op2 == ')'){
        return false;
    }
    if(op1 == '^' && op2 != '^'){
        return false;
    }
    if((op1 == '/' || op1 == '*') && (op2 == '+' || op2 == '-')){
        return false;
    }
    else{
        return true;
    }
}

//Applies an operator to a pair of operands
function applyOperator(operator, opd1, opd2){
    switch(operator){
        case '-':
            return opd2 - opd1;
            break;
        case '+':
            return opd2 + opd1;
            break;
        case '/':
            return opd2 / opd1;
            break;
        case '*':
            return opd2 * opd1;
            break;
        case '^':
            return Math.pow(opd2, opd1);
            break;
    }
}

function parameterizeText(rawText, problemParams) {
    if (rawText == null || problemParams == null)
        return rawText;
    if (problemParams == null) {
        return rawText;
    }

    var keys = Object.keys(problemParams);
    keys.sort().reverse();

    var parameterizedText = rawText;
    for (k = 0; k < keys.length; k++){
        var key = keys[k];
        var regex = new RegExp("\\"+key, "gi");
        parameterizedText = parameterizedText.replace(regex, problemParams[key]);
    }
    return parameterizedText;
}

//This takes all of the parameter sets for the problems and selects one set to use
//It modifies the problemParams object to only have the selected parameter set
//Originally called "getConstraints", which really doesn't make sense
function pickParams(problemParams) {
    if(problemParams == null) return;
    var rand = -1;
    for(var key in problemParams){
        if (problemParams[key].constructor === Array) {
            if (rand == -1) {
                rand = randomIntFromInterval(0, problemParams[key].length-1);
            }
            problemParams[key] = problemParams[key][rand];
        }
        else {
            problemParams[key] = problemParams[key];
        }
    }
}

return m;

})();
