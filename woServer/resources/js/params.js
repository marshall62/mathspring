function isNotEmpty(value) {
    //note that simple equality considers null == undefined, so this will catch both
    return value != null && value != "";
}

function plug(components) {
    var probStatement = components.stmt;
    var probFigure = components.fig;
    var probSound = components.audio;
    var probUnits = components.units;
    var problemParams = components.problemParams;
    pickParams(problemParams); //chooses which set of parameters to use
    var hints = components.hints;
    var problemFormat = components.problemFormat;

    var problemContainer = document.getElementById("ProblemContainer");
    buildProblem(problemContainer, problemFormat, true, false, false);
    if(isNotEmpty(probStatement)){
        document.getElementById("ProblemStatement").innerHTML = parameterizeText(formatText(probStatement, components), problemParams);
    }
    if(isNotEmpty(probFigure)){
        document.getElementById("ProblemFigure").innerHTML = parameterizeText(formatText(probFigure, components), problemParams);
    }
    if(isNotEmpty(probSound)) {
        //I don't think the below lines do what I want them to.
        //#rezecib: not sure what they meant above, sound seems to be working fine
        document.getElementById("QuestionSound").setAttribute("src", getURL(probSound + ".ogg", components.resource, components.probContentPath));
        document.getElementById("QuestionSound").setAttribute("src", getURL(probSound + ".mp3", components.resource, components.probContentPath));
    }
    if(isNotEmpty(probUnits)) {
        document.getElementById("Units").innerHTML = parameterizeText(formatText(getUnits(), components), problemParams);
    }

    var hint_labels = [];
    if(isNotEmpty(hints)) {
        for (i = 0; i < hints.length; ++i) {
            var hintLabel = hints[i].label;
            hint_labels.push(hintLabel);
            var hintId = getIdCorrespondingToHint(hintLabel);
            document.getElementById(hintId+"Thumb").addEventListener("click",
                //This looks weird but is necessary to save the hintLabel value properly
                function(hint) {
                    return function(){prob_playHint(hint);};
                }(hintLabel)
            );
            hint_thumb = document.getElementById(hintId+"Thumb");
            hint_thumb.style.display = "block";
            if(hints[i].statementHTML != undefined && hints[i].statementHTML != ""){
                var image_parameters = {};
                var formatted_text = formatTextWithImageParameters(hints[i].statementHTML, components, image_parameters, hintId);
                hint_thumb.dataset.parameters = JSON.stringify(image_parameters);
                document.getElementById(hintId).innerHTML = parameterizeText(formatted_text, problemParams);
            }
            else{
                alert("text missing for hint: "+i);
            }
            if(isNotEmpty(hints[i].hoverText)){
                document.getElementById(hintId+"Thumb").setAttribute("title", parameterizeText(formatText(hints[i].hoverText, components), problemParams));
            }
            //I don't think this does what I want
            //#rezecib: not sure what they meant above, sound seems to be working fine
            if (isNotEmpty(hints[i].audioResource)) {
                document.getElementById(hintId+"Sound").setAttribute("src", getURL(hints[i].audioResource + ".ogg", components.resource, components.probContentPath));
                document.getElementById(hintId+"Sound").setAttribute("src", getURL(hints[i].audioResource + ".mp3", components.resource, components.probContentPath));
            }
        }

        if(hints[0] == undefined && isNotEmpty(hints.label)){
            document.getElementById(getIdCorrespondingToHint(hints.label)+"Sound").load();
        }
        else if(isNotEmpty(hints[0].audioResource)){
            document.getElementById(getIdCorrespondingToHint(hints[0].label)+"Sound").load();
        }
    }

    //For demo and example modes all answers should stay hidden
    if (components.mode !== "demo" && components.mode !== "example") {
        var questType = components.questType;
        if (questType.match(/^multi(Choice|Select)$/)) {
            var multiSelect = questType === "multiSelect";
            document.getElementById("MultipleChoiceAnswers").style.display = "block";
            var answers = components.answers;
            if(isNotEmpty(answers)) {
                for(var letter in answers) {
                    if(!answers.hasOwnProperty(letter)) continue;
                    var answerElt = document.getElementById("Answer" + letter.toUpperCase());
                    answerElt.parentNode.style.display = "block";
                    answerElt.innerHTML = parameterizeText(formatText(answers[letter], components), problemParams);
                    var button = document.getElementById(letter.toUpperCase() + "Button");
                    if(multiSelect) {
                        button.style.display = "none";
                        // resets it to what the CSS says, which is inline-block
                        document.getElementById(letter.toUpperCase() + "Checkbox").style.display = "";
                    } else {
                        button.addEventListener("click",
                            function(answer) {
                                return function(){answerClicked(document, answer);};
                            }(letter)
                        );
                    }
                }
            }
            if(multiSelect) {
                document.getElementById("submit_answer").addEventListener("click", submitMultiSelectAnswer);
            }
            if(!components.previewMode) shuffleAnswers();
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

    if(components.previewMode) {
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
}

function submitShortAnswer() {
    processShortAnswer(document, document.getElementById("answer_field").value);
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
    processShortAnswer(document, selections);
}

function shuffleAnswers() {
    var container = document.getElementById("MultipleChoiceAnswers")
    var answers = container.children;
    //Store the original letter ordering
    var letters = [];
    for(var i = 0; i < answers.length; ++i) {
        if(answers[i].style.display == "block") {
            letters.push(answers[i].children[0].children[2].innerHTML);
        }
    }
    //Do the shuffle
    for(var i = answers.length; i >= 0; --i) {
        container.appendChild(answers[Math.random()*i | 0]);
    }
    //Fix the letters
    var l = 0;
    for(var i = 0; i < answers.length; ++i) {
        if(answers[i].style.display == "block") {
            answers[i].children[0].children[2].innerHTML = letters[l];
            ++l;
        }
    }
}

function getURL(filename, resource, probContentPath) {
    if (filename == null || filename == undefined)
        return filename;
    return probContentPath + "/html5Probs/" + resource.split(".")[0] + "/" + filename;
}

function getImageHtml(file, ext, resource, probContentPath, id){
    //if id is null, don't add an id to the image
    id = id == null ? "" : ' id="' + id + '"';

    //Replace and image file name inside {} with the appropriate html
    if(ext != null) {
        var size_style = ' style="max-height: 100%; max-width: 100%"';
        if(ext.match(/^(gif|png|jpe?g|svg)$/i)){
            return '<img' + id + size_style + ' src="' + getURL(file + "." + ext, resource, probContentPath) + '">';
        } else if(ext.match(/^(mp4|ogg|webm)$/i)) { //Do the same for a video
            return '<video' + id + size_style + ' src="' + getURL(file + "." + ext, resource, probContentPath) + '" controls preload="auto"></video>';
        }
    }
    console.log("invalid image or video", file + "." + ext, resource, probContentPath);
    //TODO(rezecib): for test users (or admin preview?) display an "invalid image" directly
    return "";
}

//Just a wrapper for when you don't care about the parameters
function formatText(text, components) {
    return formatTextWithImageParameters(text, components, {}, "x");
}

//Extracts images/video contained by {[]}
// also processes and stores placement parameters for the image
// e.g. whether to place it over the figure, side-by-side, or in the hint
//text: string; the text to format
//components: object; components of the problem
//parameters: object; will be used to store parameters for each image, as a mapping of {image_id -> parameter}
function formatTextWithImageParameters(text, components, parameters, base_id) {
    // this is just a pattern to extract all occurrences of {[...]}
    var matches = text.match(/\{\[[^\{\}\[\]]*\]\}/igm);
    if(matches == null) matches = [];
    for(var i = 0; i < matches.length; i++) {
        var match = matches[i].slice(2, -2); //remove {[]}
        var parameterized_match = parameterizeText(match, components.problemParams);
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
                parameters[image_id] = image_parameters[1].trim();
            }
            replacement = getImageHtml(image_parts[0], image_parts[1], components.resource, components.probContentPath, image_id);
            if(replacement.match(/<video/)) {
                parameters[image_id] = "play_video";
            }
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

function getConstraintJSon() {
    var bindings = window.parent.getProblemParams();
    return bindings;
}

function getProblemStatement() {
    return window.parent.getProblemStatement();

}

function getProblemFigure() {
    return window.parent.getProblemFigure();
}

function getProblemSound() {
    return window.parent.getProblemSound();
}

function getAnswers () {
    return window.parent.getAnswers();
}

function getHints() {
    return window.parent.getHints();
}

function getUnits() {
    return window.parent.getUnits();
}