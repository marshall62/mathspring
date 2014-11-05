/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 6/12/13
 * Time: 2:42 PM
 * To change this template use File | Settings | File Templates.
 */

function debugAlert(msg) {
    if (globals.debug || globals.trace) {
        alert(msg);
    }
}

// Given an action, an array of args like [p1, v1, p2, v2], and a callback fn,  call HTTP get using jquery

function servletGet (action, args, callbackFn) {
    var extraArgs = "";
    for (var p in args) {
        value = args[p];
        extraArgs += "&" + p + "=" + value;
    }
    debugAlert("The action is: <" + action +">");
    $.get("/"+sysGlobals.wayangServletContext + "/" + sysGlobals.servletName+"?action=" +action+"&sessionId="+globals.sessionId+"&elapsedTime="
        + globals.elapsedTime + extraArgs,
        callbackFn);
}

function servletPost(action, args, callbackFn) {
    var extraArgs = "";
    for (var p in args) {
        value = args[p];
        extraArgs += "&" + p + "=" + value;
    }
    $.post("/"+sysGlobals.wayangServletContext + "/" + sysGlobals.servletName+"?action=" +action+"&sessionId="+globals.sessionId+"&elapsedTime="
        + globals.elapsedTime + extraArgs,
        callbackFn)
}

function servletFormPost (action, args, callbackFn) {
    $.post("/"+sysGlobals.wayangServletContext + "/" + sysGlobals.servletName+"?action=" +action+"&sessionId="+globals.sessionId+"&elapsedTime="
        + globals.elapsedTime + "&"+ args,
        callbackFn)
}

// Makes a synchronous call to the server.
function servletGetWait (action, args, callbackFn) {
    var extraArgs = "";
    for (var p in args) {
        value = args[p];
        extraArgs += "&" + p + "=" + value;
    }
    debugAlert("The action is: <" + action +">");
    $.ajax({url: "/"+sysGlobals.wayangServletContext + "/" + sysGlobals.servletName+"?action=" +action+"&sessionId="+globals.sessionId+"&elapsedTime="
                + globals.elapsedTime + extraArgs,
           success: callbackFn,
           async: false});
}


// return the XML that is <elementName>xxx</elementName>
// Note that we must use begin and end tags as above.   No short-cuts.
function getXMLElement(xml, elementName) {
    debugAlert("in getXMLElemet" + xml);
    var eltbegin = "<" + elementName;
    var eltend = "</" + elementName + ">"
    var re = new RegExp("(<" + elementName + ".*(?:(?:</" + elementName + ">)|(?:/>)))");
    var m = re.exec(xml);
    if (m == null) {
        debugAlert("no match");
        return null;
    }
    else {
        debugAlert("match is " + m[0]);
        return m[0];
    }
}


// don't send an endExternalActivity if the last prob was 4mality.  4mality sends its own begin/ends
function sendEndEvent(globals) {
    updateTimers();
    if (globals.lastProbType == '')
        return;
    else if (globals.lastProbType == HTML_PROB_TYPE || globals.lastProbType == FLASH_PROB_TYPE || globals.lastProbType == TOPIC_INTRO_PROB_TYPE)
    {
        servletGetWait("EndProblem",{probId: globals.lastProbId, probElapsedTime: globals.probElapsedTime},processEndProblem);
    }
    else
        servletGetWait("EndExternalActivity", {xactId: globals.lastProbId,probElapsedTime: globals.probElapsedTime});


}

//send a BeginProblem event for HTMl5 problems.
function sendBeginEvent(globals) {
    incrementTimers(globals);
    globals.probElapsedTime=0;
    servletGetWait("BeginProblem", {probElapsedTime: globals.probElapsedTime});

}

function sendResumeProblemEvent (globals) {
    incrementTimers(globals);
    servletGetWait("ResumeProblem", {probElapsedTime: globals.probElapsedTime, probId: globals.lastProbId})
}


// don't send an endExternalActivity if the last prob was 4mality.  4mality sends its own begin/ends
function sendSimpleNotificationEvent(globals, eventName) {
    updateTimers();
    servletGetWait(eventName,{probId: globals.lastProbId, probElapsedTime: globals.probElapsedTime});
}



function utilDialogOpen (url, title, html) {
    $("#"+UTIL_DIALOG).attr("title", title);
    $("#"+UTIL_DIALOG_IFRAME).attr("src",url);
    $("#"+UTIL_DIALOG).dialog("open");

}
