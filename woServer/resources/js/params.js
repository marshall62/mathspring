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
    data = constraints;
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
            this.nodeValue=this.nodeValue.replace(regex, "$1"+constraints[key]+"$2");
        }
    })
}

function parametrize(sym) {
    replaceVars(sym);
}

function plug(sym) {

}

// insert code to be run when the symbol is created here

// $.get("dynamic.html", {}, function(res, code) {
//    sym.$("Text").html(res);
//    sym.play(0);
// });
