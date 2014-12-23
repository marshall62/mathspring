var maxHints = 10;
var currentHint = "Question";
var shown = {};

function randomIntFromInterval(min,max)
{
    return Math.floor(Math.random()*(max-min+1)+min);
}

function isArray(parsedItem) {
    return Object.prototype.toString.call(parsedItem) === '[object Array]';
}

// var plusScore = sym.getVariable("myScore");
// plusScore = plusScore + 1;
// sym.setVariable("myScore", plusScore);
// sym.$("Score").html(plusScore);

function getTextNodesIn(node, includeWhitespaceNodes) {
    var textNodes = [], nonWhitespaceMatcher = /\S/;

    function getTextNodes(node) {
        // 1 = element, 2 = attr, 3 = text
        if (node.nodeType == 3) {
            if (includeWhitespaceNodes || nonWhitespaceMatcher.test(node.nodeValue)) {
                textNodes.push(node);
            }
        } else {
            for (var i = 0, len = node.childNodes.length; i < len; ++i) {
                getTextNodes(node.childNodes[i]);
            }
        }
    }

    getTextNodes(node);
    return textNodes;
}

function getConstraintJSon() {
    var bindings = window.parent.getProblemParams();
    return bindings;
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
    $.each( data, function(key, val) {
        if (isArray(val)) {
            if (rand == -1) {
                rand = randomIntFromInterval(0, val.length-1);
            }
            constraints[key] = val[rand];
        }
        else {
            constraints[key] = val;
        }
    });
    return constraints;
}

function replaceVars(sym) {
    // create map from var name to selected random item or regular item
    // then iterate over children of stage and replace instances of var with corresponding item


    var constraints = getConstraints();
    var collection = getTextNodesIn(sym.$("Stage").get(0));
    $.each(collection,function(){
        for (var key in constraints) {
            var regex = new RegExp("(\\W|^)\\"+key+"(\\W|$)", "gi");
            if (this.nodeValue != null && this.nodeValue != undefined)
                this.nodeValue=this.nodeValue.replace(regex, "$1"+constraints[key]+"$2");
        }
    })
}

function parametrizeText(rawText) {
    if (rawText == null || rawText == undefined)
        return rawText;
    var constraints = getConstraints();
    if (constraints == null) {
        return rawText;
    }
    var parametrizedText = rawText;
    for (var key in constraints) {
        var regex = new RegExp("(\\W|^)\\"+key+"(\\W|$)", "gi");
        parametrizedText = parametrizedText.replace(regex, "$1"+constraints[key]+"$2");
    }
    return parametrizedText;
}

function parametrize(sym) {
    replaceVars(sym);
}

function getURL (filename) {
    if (filename == null || filename == undefined)
        return filename;
    return window.parent.getProblemContentPath() + "/html5Probs/" + window.parent.getResource().split()[0] + "/" + filename;
}

function format (rawText) {
    if (rawText == null || rawText == undefined)
        return rawText;
    var regex = new RegExp("\\{(.*?)\\}", "gi");
    return rawText.replace(regex, "<img src="+getURL("$1")+">");
}

function getProblemName() {
    return window.parent.getResource();
}

function isDemo() {
    return window.parent.isDemoMode();
}

function isMultiChoice() {
    var answers = window.parent.getAnswers();
    if (answers == null || answers == undefined) {
        return false;
    }
    return true;
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
    var answers = window.parent.getAnswers();
    if (answers == null || answers == undefined) {
        return null;
    }
    if (answers.length == undefined) {
        return [answers];
    }
    return answers;
}

function getHints() {
    var hints = window.parent.getHints();
    if (hints == null || hints == undefined) {
        return null;
    }
    if (hints.length == undefined) {
        return [hints];
    }
    return hints;
}

function isParameterized() {
    return window.parent.isParameterized();
}

function getUnits() {
    return window.parent.getUnits();
}

function showShortAnswerBox(sym) {
    sym.$('Answers').hide();
    sym.$('AnswerA').hide();
    sym.$('AnswerB').hide();
    sym.$('AnswerC').hide();
    sym.$('AnswerD').hide();
    sym.$('AnswerE').hide();
    var input_answer = '<input id="answer_field" type="text">';
    var input_button = '<br><br><button id="submit_answer" type="button">Submit Answer!</button>';
    if (getUnits() != null) {
        var units_div = '<span id="Units">' + parametrizeText(format(getUnits())) + '</span><br>';
        sym.$('Answer_Container').html(input_answer.concat(units_div).concat(input_button));
    }
    else {
        sym.$('Answer_Container').html(input_answer.concat(input_button));
    }
    $("#submit_answer").on("click", function() {
        processShortAnswer(sym,$("#answer_field").val());
    });
}

function hideAnswers(sym, numAnswers) {
    sym.$('ShortAnswerBox').hide();
    switch (numAnswers) {
        case 4:
            sym.getSymbol("Answers").$('EButton').hide();
            sym.$('AnswerE').hide();
            break;
        case 3:
            sym.getSymbol("Answers").$('DButton').hide();
            sym.$('AnswerD').hide();
            sym.getSymbol("Answers").$('EButton').hide();
            sym.$('AnswerE').hide();
            break;
        case 2:
            sym.getSymbol("Answers").$('CButton').hide();
            sym.$('AnswerC').hide();
            sym.getSymbol("Answers").$('DButton').hide();
            sym.$('AnswerD').hide();
            sym.getSymbol("Answers").$('EButton').hide();
            sym.$('AnswerE').hide();
            break;
        case 1:
            sym.getSymbol("Answers").$('BButton').hide();
            sym.$('AnswerB').hide();
            sym.getSymbol("Answers").$('CButton').hide();
            sym.$('AnswerC').hide();
            sym.getSymbol("Answers").$('DButton').hide();
            sym.$('AnswerD').hide();
            sym.getSymbol("Answers").$('EButton').hide();
            sym.$('AnswerE').hide();
            break;
        case 0:
            sym.getSymbol("Answers").$('AButton').hide();
            sym.$('AnswerA').hide();
            sym.getSymbol("Answers").$('BButton').hide();
            sym.$('AnswerB').hide();
            sym.getSymbol("Answers").$('CButton').hide();
            sym.$('AnswerC').hide();
            sym.getSymbol("Answers").$('DButton').hide();
            sym.$('AnswerD').hide();
            sym.getSymbol("Answers").$('EButton').hide();
            sym.$('AnswerE').hide();
            break;
    }
}

function markShown(sym, num) {
    shown["Hint"+num.toString()] = true;
    sym.$("Hint"+num.toString()+"Thumb").show();
}

function resetButtons(sym) {
    for (i = 1; i <= maxHints; ++i) {
        sym.$("Hint"+i.toString()+"ThumbImg").show();
    }
}

function hintActive(sym, num) {
    sym.stop();
    sym.$(currentHint+"Sound")[0].currentTime = 0;
    sym.$(currentHint+"Sound")[0].pause();
    resetButtons(sym);
    sym.$("Hint"+num.toString()+"ThumbImg").hide();
    if (num == 10) {
        currentHint = "Answer";
        sym.play("Show Answer");
        sym.$("AnswerSound")[0].play();
    }
    else {
        currentHint = "Hint"+num.toString();
        sym.play("Hint "+num.toString());
        // Play the audio track.
        sym.$("Hint"+num.toString()+"Sound")[0].play();


    }
}

function prepareForData(sym) {
    var hints = getHints();
    if (hints != null && hints != undefined) {
        for (i = 1; i <= getHints().length; ++i) {
            shown["Hint"+i.toString()] = Boolean(false);
        }
    }
    for (i = 1; i <= maxHints; ++i) {
        sym.$("Hint"+i.toString()+"Thumb").hide();
    }
    if (isDemo()) {
        hideAnswers(sym, 0)
    }
    if (isMultiChoice() == false) {
        showShortAnswerBox(sym);
    }
    else {
        var answers = getAnswers();
        if (answers != undefined && answers != null)
            hideAnswers(sym, answers.length);
    }

}

function plug(sym) {
    sym.$("ProblemStatement").html(parametrizeText(format(getProblemStatement())));
    sym.$("ProblemFigure").html(parametrizeText(format(getProblemFigure())));
    if (getProblemSound() != undefined && getProblemSound() != "") {
        sym.$("QuestionSound").attr("src", getURL(getProblemSound()+".ogg"));
        sym.$("QuestionSound").attr("src", getURL(getProblemSound()+".mp3"));
    }
    var hints = getHints();
    var hintID = "";
    if (hints != undefined && hints != null) {
        for (i=0; i<hints.length;++i)  {
            switch (hints[i].label) {
              case "Hint 1":
                 hintID = "Hint1";
                 break;
              case "Hint 2":
                  hintID = "Hint2";
                  break;
              case "Hint 3":
                  hintID = "Hint3";
                  break;
              case "Hint 4":
                  hintID = "Hint4";
                  break;
              case "Hint 5":
                  hintID = "Hint5";
                  break;
              case "Hint 6":
                  hintID = "Hint6";
                  break;
              case "Hint 7":
                  hintID = "Hint7";
                  break;
              case "Hint 8":
                  hintID = "Hint8";
                  break;
              case "Hint 9":
                  hintID = "Hint9";
                  break;
              case "Show Answer":
                  hintID = "Answer";
                  break;
            }
            sym.$(hintID).html(parametrizeText(format(hints[i].statementHTML)));
            if (hintID==="Answer") {
                sym.$("Hint10Thumb").attr("title", parametrizeText(hints[i].hoverText));
            }
            else {
                sym.$(hintID+"Thumb").attr("title", parametrizeText(hints[i].hoverText));
            }
            if (hints[i].audioResource != undefined && hints[i].audioResource != "")  {
                sym.$(hintID+"Sound").attr("src", getURL(hints[i].audioResource+".ogg"));
                sym.$(hintID+"Sound").attr("src", getURL(hints[i].audioResource+".mp3"));
            }
        }
    }

    if (isMultiChoice()) {
        var answers = getAnswers();
        if (answers != null && answers != undefined) {
            for(i=0;i<answers.length;++i) {
                if (answers[i].a != null && answers[i].a != undefined) {
                    sym.$("AnswerA").html(parametrizeText(format(answers[i].a)));
                }
                if (answers[i].b != null && answers[i].b != undefined) {
                    sym.$("AnswerB").html(parametrizeText(format(answers[i].b)));
                }
                if (answers[i].c != null && answers[i].c != undefined) {
                    sym.$("AnswerC").html(parametrizeText(format(answers[i].c)));
                }
                if (answers[i].d != null && answers[i].d != undefined) {
                    sym.$("AnswerD").html(parametrizeText(format(answers[i].d)));
                }
                if (answers[i].e != null && answers[i].e != undefined) {
                    sym.$("AnswerE").html(parametrizeText(format(answers[i].e)));
                }
            }
        }
    }
}

// insert code to be run when the symbol is created here

// $.get("dynamic.html", {}, function(res, code) {
//    sym.$("Text").html(res);
//    sym.play(0);
// });
