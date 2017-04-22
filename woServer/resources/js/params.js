var maxHints = 10;
var currentHint = "Question";
var shown = {};


function prepareForData(doc, components) {
    for (i = 1; i <= maxHints; ++i) {
        addHintHandler(doc, i.toString());
    }
    // DM 1/10/16 added in check for example mode
    if (components.mode === 'demo' || components.mode === 'example') {
        hideAnswers(doc, 0)
    }
    if (!isMultiChoice(components.questType)) {
        doc.getElementById("ShortAnswerBox").style.display = "block";
        addShortAnswerHandler(doc);
    }
    else {
        doc.getElementById("MultipleChoiceAnswers").style.display = "block";
        //individual answer event listeners and display will get set later in plug()
    }
}

function isMultiChoice(questType) {
    return (questType === 'multiChoice');


}

function isArray(parsedItem) {
    return Object.prototype.toString.call(parsedItem) === '[object Array]';
}

function addShortAnswerHandler(doc){
    doc.getElementById("submit_answer").addEventListener("click", function(){processShortAnswer(doc, doc.getElementById("answer_field").value);});
}

function addHintHandler(doc, hintLabel){
    if(hintLabel == 10){
        doc.getElementById("Hint"+hintLabel+"Thumb").addEventListener("click", function(){prob_playHint("Show Answer");});
    }
    else{
       doc.getElementById("Hint"+hintLabel+"Thumb").addEventListener("click", function(){prob_playHint("Hint "+hintLabel);});
    }
}

function isNotEmpty(value) {
    //note that simple equality considers null == undefined, so this will catch both
    return value != null && value != "";
}

function plug(doc, components) {
    var probStatement = components.stmt;
    var probFigure = components.fig;
    var probSound = components.audio;
    var probUnits = components.units;
    var problemParams = components.problemParams;
    var hints = components.hints;
    var problemFormat = components.problemFormat;

    buildProblem(document.getElementById("ProblemContainer"), problemFormat, true, false, false);
    if(isNotEmpty(probStatement)){
        doc.getElementById("ProblemStatement").innerHTML = parameterizeText(format(probStatement, components), problemParams);
    }
    if(isNotEmpty(probFigure)){
        doc.getElementById("ProblemFigure").innerHTML = parameterizeText(format(probFigure, components), problemParams);
    }
    if(isNotEmpty(probSound)) {
        //I don't think the below lines do what I want them to.
        //#rafael: not sure what they meant above, sound seems to be working fine
        doc.getElementById("QuestionSound").setAttribute("src", getURL(probSound + ".ogg", components.resource, components.probContentPath));
        doc.getElementById("QuestionSound").setAttribute("src", getURL(probSound + ".mp3", components.resource, components.probContentPath));
    }
    if(isNotEmpty(probUnits)) {
        doc.getElementById("Units").innerHTML = parameterizeText(format(getUnits(), components), problemParams);
    }

    var hintID = "";
    if(isNotEmpty(hints)) {
        for (i=0; i<hints.length;++i)  {
            hintID = getIdCorrespondingToHint(hints[i].label);
            doc.getElementById(hintID+"Thumb").style.display = "block";
            if(hints[i].statementHTML != undefined && hints[i].statementHTML != ""){
                doc.getElementById(hintID).innerHTML = parameterizeText(format(hints[i].statementHTML, components), problemParams);
            }
            else{
                alert("text missing for hint: "+i);
            }
            if(isNotEmpty(hints[i].hoverText)){
                doc.getElementById(hintID+"Thumb").setAttribute("title", parameterizeText(format(hints[i].hoverText, components), problemParams));
            }
            //I don't think this does what I want
            //#rafael: not sure what they meant above, sound seems to be working fine
            if (isNotEmpty(hints[i].audioResource)) {
                doc.getElementById(hintID+"Sound").setAttribute("src", getURL(hints[i].audioResource + ".ogg", components.resource, components.probContentPath));
                doc.getElementById(hintID+"Sound").setAttribute("src", getURL(hints[i].audioResource + ".mp3", components.resource, components.probContentPath));
            }
        }

        if(hints[0] == undefined && isNotEmpty(hints.label)){
            doc.getElementById(getIdCorrespondingToHint(hints.label)+"Sound").load();
        }
        else if(isNotEmpty(hints[0].audioResource)){
            doc.getElementById(getIdCorrespondingToHint(hints[0].label)+"Sound").load();
        }
    }

    if(isMultiChoice(components.questType)) {
        var answers = components.answers;
        if(isNotEmpty(answers)) {
            for(var letter in answers) {
                if(!answers.hasOwnProperty(letter)) continue;
                var answerElt = doc.getElementById("Answer" + letter.toUpperCase());
                answerElt.parentNode.style.display = "block";
                answerElt.innerHTML = parameterizeText(format(answers[letter], components), problemParams);
                doc.getElementById(letter.toUpperCase() + "Button").addEventListener("click", function(){answerClicked(doc, letter);});
            }
        }
    }
}

function getURL(filename, resource, probContentPath) {
    if (filename == null || filename == undefined)
        return filename;
    return probContentPath + "/html5Probs/" + resource.split(".")[0] + "/" + filename;
}

function format(rawText, components) {
    if(rawText == null || rawText == undefined){
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

        //TODO refactor this
        switch (rawText.charAt(j)) {
            case '\\':
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
                        var toInsert  = replaceWithHTML(imgOrVid, extension, components.resource, components.probContentPath);
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

function replaceWithHTML(file, ext, resource, probContentPath){

    var toInsert = "";

    //Replace and image file name inside {} with the appropriate html
    // DM 9/16 removed svg from the list of extensions because they correctly scale themselves
    if(ext == "gif" || ext == "png" || ext == "jpeg" || ext == "jpg" ){
        toInsert = '<img style="max-height: 100%; max-width: 100%" src="' +getURL(file + "." + ext, resource, probContentPath)+ '">';
    }
    // DM 9/16 added svg on its own without resizing
    else if (ext == 'svg')
        toInsert = '<img src="' +getURL(file + "." + ext, resource, probContentPath)+ '" >'


    //Do the same for a video
    else if(ext == "mp4" || ext == "ogg" || ext == "WebM"){
       toInsert = '<video src="' +getURL(file + "." + ext, resource, probContentPath)+ '" controls preload="auto"></video>';
    }

    else{
        console.log("invalid image or video", file + "." + ext, resource, probContentPath);
    }
    return toInsert;
}

//TODO test try catches
//Parses correctly formatted expressions containing only operators from the set {+,-,/,*,^}
function parseSimpleExp(expression, components){
    //Convert parameters to their values
    expression = parameterizeText(expression, components.problemParams);
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
    pickParams(problemParams);

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

//From what I can tell, all this does is take each vlaue in problemParams,
// and if it's an array, pick use the same random index to pick one of its elements
//Originally called "getConstraints", which really doesn't make sense
function pickParams(problemParams) {
    var rand = -1;
    for(var key in problemParams){
        if (isArray(problemParams[key])) {
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