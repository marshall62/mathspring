var maxHints = 10;
var currentHint = "Question";
var shown = {};

function prepareForData(doc) {
    for (i = 1; i <= maxHints; ++i) {
        doc.getElementById("Hint"+i.toString()+"Thumb").style.display="none";
        doc.getElementById("Hint"+i.toString()).style.display = "none";
        addHintHandler(doc, i.toString());
    }
    if (isDemo()) {
        hideAnswers(doc, 0)
    }
    if (isMultiChoice() == false) {
        showShortAnswerBox(doc);
        addShortAnswerHandler(doc);
    }
    else {
        var answers = getAnswers();
        if (answers != undefined && answers != null){
            hideAnswers(doc, answers.length);
            addAnswerClickedHandlers(doc, answers.length);
        }
    }
}

function isDemo() {
    return window.parent.isDemoMode();
}

function isMultiChoice() {
    var answers = window.parent.getAnswers();
    if (answers == null || answers == undefined || answers.length == undefined) {
        return false;
    }
    return true;
}

function isArray(parsedItem) {
    return Object.prototype.toString.call(parsedItem) === '[object Array]';
}

function hideAnswers(doc, numAnswers) {
    doc.getElementById('ShortAnswerBox').style.display="none";
    switch (numAnswers) {
        case 4:
            doc.getElementById('EButton').style.display="none";
            doc.getElementById('AnswerE').style.display="none";
            break;
        case 3:
            doc.getElementById('DButton').style.display="none";
            doc.getElementById('AnswerD').style.display="none";
            doc.getElementById('EButton').style.display="none";
            doc.getElementById('AnswerE').style.display="none";
            break;
        case 2:
            doc.getElementById('CButton').style.display="none";
            doc.getElementById('AnswerC').style.display="none";
            doc.getElementById('DButton').style.display="none";
            doc.getElementById('AnswerD').style.display="none";
            doc.getElementById('EButton').style.display="none";
            doc.getElementById('AnswerE').style.display="none";
            break;
        case 1:
            doc.getElementById('BButton').style.display="none";
            doc.getElementById('AnswerB').style.display="none";
            doc.getElementById('CButton').style.display="none";
            doc.getElementById('AnswerC').style.display="none";
            doc.getElementById('DButton').style.display="none";
            doc.getElementById('AnswerD').style.display="none";
            doc.getElementById('EButton').style.display="none";
            doc.getElementById('AnswerE').style.display="none";
            break;
        case 0:
            doc.getElementById('AButton').style.display="none";
            doc.getElementById('AnswerA').style.display="none";
            doc.getElementById('BButton').style.display="none";
            doc.getElementById('AnswerB').style.display="none";
            doc.getElementById('CButton').style.display="none";
            doc.getElementById('AnswerC').style.display="none";
            doc.getElementById('DButton').style.display="none";
            doc.getElementById('AnswerD').style.display="none";
            doc.getElementById('EButton').style.display="none";
            doc.getElementById('AnswerE').style.display="none";
            break;
    }
}

function addAnswerClickedHandlers(doc, numAnswers){
    switch (numAnswers) {
        case 1:
            doc.getElementById('AButton').addEventListener("click", function(){answerClicked(doc, 'a');});
            break;
        case 2:
            doc.getElementById('AButton').addEventListener("click", function(){answerClicked(doc, 'a');});
            doc.getElementById('BButton').addEventListener("click", function(){answerClicked(doc, 'b');});
            break;
        case 3:
            doc.getElementById('AButton').addEventListener("click", function(){answerClicked(doc, 'a');});
            doc.getElementById('BButton').addEventListener("click", function(){answerClicked(doc, 'b');});
            doc.getElementById('CButton').addEventListener("click", function(){answerClicked(doc, 'c');});
            break;
        case 4:
            doc.getElementById('AButton').addEventListener("click", function(){answerClicked(doc, 'a');});
            doc.getElementById('BButton').addEventListener("click", function(){answerClicked(doc, 'b');});
            doc.getElementById('CButton').addEventListener("click", function(){answerClicked(doc, 'c');});
            doc.getElementById('DButton').addEventListener("click", function(){answerClicked(doc, 'd');});
            break;
        case 5:
            doc.getElementById('AButton').addEventListener("click", function(){answerClicked(doc, 'a');});
            doc.getElementById('BButton').addEventListener("click", function(){answerClicked(doc, 'b');});
            doc.getElementById('CButton').addEventListener("click", function(){answerClicked(doc, 'c');});
            doc.getElementById('DButton').addEventListener("click", function(){answerClicked(doc, 'd');});
            doc.getElementById('EButton').addEventListener("click", function(){answerClicked(doc, 'e');});
            break;
    }
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

function plug(doc) {
    var probStatement = getProblemStatement();
    var probFigure = getProblemFigure();
    var probSound = getProblemSound();
    var probUnits = getUnits();
    if(probStatement != null && probStatement != undefined && probStatement != "")
        doc.getElementById("ProblemStatement").innerHTML = parametrizeText(format(probStatement));
    if(probFigure != null && probFigure != undefined && probFigure != "")
        doc.getElementById("ProblemFigure").innerHTML = parametrizeText(format(probFigure));
    if(probSound != null && probSound != undefined && probSound != "") {
        //TODO I don't think the below lines do what I want them to.
        doc.getElementById("QuestionSound").setAttribute("src", getURL(probSound+".ogg"));
        doc.getElementById("QuestionSound").setAttribute("src", getURL(probSound+".mp3"));
    }
    if (probUnits != null && probUnits != undefined && probUnits != "") {
        doc.getElementById("Units").innerHTML = parametrizeText(format(getUnits()));
    }
    var hints = getHints();
    var hintID = "";
    if (hints != undefined && hints != null) {
        for (i=0; i<hints.length;++i)  {
            if(hints[i] )
            hintID = getElementCorrespondingToHint(hints[i].label);
            if(hints[i].statementHTML != undefined && hints[i].statementHTML != ""){
                doc.getElementById(hintID).innerHTML = parametrizeText(format(hints[i].statementHTML));
            }
            else{
                alert("text missing for hint: "+i);
            }
            doc.getElementById(hintID+"Thumb").setAttribute("title", parametrizeText(format(hints[i].hoverText)));
            //TODO I don't think this does what I want
            if (hints[i].audioResource != undefined && hints[i].audioResource != "")  {
                doc.getElementById(hintID+"Sound").setAttribute("src", getURL(hints[i].audioResource+".ogg"));
                doc.getElementById(hintID+"Sound").setAttribute("src", getURL(hints[i].audioResource+".mp3"));
            }
        }

        if(hints[0] == undefined && hints.label != undefined && hints.label != ""){
            doc.getElementById(getElementCorrespondingToHint(hints.label)+"Sound").load();
        }
        else if(hints[0].audioResource != undefined && hints[0].audioResource != ""){
            doc.getElementById(getElementCorrespondingToHint(hints[0].label)+"Sound").load();
        }
    }

    if (isMultiChoice()) {
        var answers = getAnswers();
        if (answers != null && answers != undefined) {
            for(i=0;i<answers.length;++i) {
                if (answers[i].a != null && answers[i].a != undefined) {
                    doc.getElementById("AnswerA").innerHTML = parametrizeText(format(answers[i].a));
                }
                if (answers[i].b != null && answers[i].b != undefined) {
                    doc.getElementById("AnswerB").innerHTML = parametrizeText(format(answers[i].b));
                }
                if (answers[i].c != null && answers[i].c != undefined) {
                    doc.getElementById("AnswerC").innerHTML = parametrizeText(format(answers[i].c));
                }
                if (answers[i].d != null && answers[i].d != undefined) {
                    doc.getElementById("AnswerD").innerHTML = parametrizeText(format(answers[i].d));
                }
                if (answers[i].e != null && answers[i].e != undefined) {
                    doc.getElementById("AnswerE").innerHTML = parametrizeText(format(answers[i].e));
                }
            }
        }
    }
}

function getURL (filename) {
    if (filename == null || filename == undefined)
        return filename;
    return window.parent.getProblemContentPath() + "/html5Probs/" + window.parent.getResource().split(".")[0] + "/" + filename;
}

function format (rawText) {
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
                        var toInsert  = replaceWithHTML(imgOrVid, extension);
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
                        var toInsert  = parseSimpleExp(imgOrVid+"."+extension);
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

function replaceWithHTML(file, ext){

    var toInsert = "";

    //Replace and image file name inside {} with the appropriate html
    if(ext == "gif" || ext == "png" || ext == "jpeg" || ext == "jpg"){
        toInsert = "<img src=\""+getURL(file+"."+ext)+"\"></img>";
    }

    //Do the same for a video
    else if(ext == "mp4" || ext == "ogg" || ext == "WebM"){
       toInsert = "<video src=\""+getURL(file+"."+ext)+" controls preload=\"auto\"></video>";
    }

    else{
        alert("invalid image or video");
    }
    return toInsert;
}

//TODO test try catches
//Parses correctly formatted expressions containing only operators from the set {+,-,/,*,^}
function parseSimpleExp(expression){
    //Convert parameters to their values
    expression = parametrizeText(expression);
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

function parametrizeText(rawText) {
    if (rawText == null || rawText == undefined)
        return rawText;
    var constraints = getConstraints();
    if (constraints == null) {
        return rawText;
    }

    var pastVars = "";
    var parametrizedText = rawText;
    for (var key in constraints) {
        var regex = new RegExp("\\"+key+"((?=\\W|$"+pastVars+"))", "gi");
        pastVars = pastVars + "|" + constraints[key];
    //    while(parametrizedText.search(regex)!= -1){
            parametrizedText = parametrizedText.replace(regex,constraints[key] + "$1");
      //  }
    }
    return parametrizedText;
}

function getConstraints() {
    var rand = -1;
    var constraints = {};
    var constraints = getConstraintJSon();
    if (getConstraintJSon() == null || getConstraintJSon() == undefined) {
        return null;
    }
    data = constraints;
    if (data == null) {
        return null;
    }
    for(var key in data){
        if (isArray(data[key])) {
            if (rand == -1) {
                rand = randomIntFromInterval(0, data[key].length-1);
            }
            constraints[key] = data[key][rand];
        }
        else {
            constraints[key] = data[key];
        }
    }
    return constraints;
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

function showShortAnswerBox(doc) {
    doc.getElementById('Answers').style.display="none";
    doc.getElementById('AnswerA').style.display="none";
    doc.getElementById('AnswerB').style.display="none";
    doc.getElementById('AnswerC').style.display="none";
    doc.getElementById('AnswerD').style.display="none";
    doc.getElementById('AnswerE').style.display="none";
}