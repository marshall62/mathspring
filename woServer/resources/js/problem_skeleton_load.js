var pid, sessId, elapsedTime, eventCounter, servletContext, servletName, teacherId, addHintButton;

//function plugin(stmt, fig, audio, hints, answers, newAnswer, answer, units, mode, questType, resource, probContentPath, problemParams) {
function plugin(probId, sessid, elapsedtime, eventcounter, servcontext, servname, previewMode, teacherid, addhintbutton) {
    pid = probId;
    sessId = sessid;
    elapsedTime = elapsedtime;
    eventCounter = eventcounter;
    servletContext = servcontext;
    servletName = servname;
    teacherId = teacherid;
    addHintButton = addhintbutton;
//    alert("In plugin: " + probId);
    if (!previewMode)
        servletGet(true, "GetQuickAuthProblem",{probId: pid},pluginProblem);
    else
        servletGet(false, "AdminGetQuickAuthProblem",{probId: pid},pluginProblem);
}

function servletGet (useTutorServlet, action, args, callbackFn) {
    var extraArgs = "";
    for (var p in args) {
        value = args[p];
        extraArgs += "&" + p + "=" + value;
    }
    if (useTutorServlet)
        $.get(getTutorServletURL(action,extraArgs),callbackFn);
    else $.get(getAdminServletURL(action,extraArgs),callbackFn);
}

function getAdminServletURL (action, args) {
    return "/"+servletContext + "/WoAdmin?action=" +action+ "&teacherId="+ teacherId+ "&"+ args ;
}

function getTutorServletURL (action, args) {
    return "/"+servletContext + "/" + servletName+"?action=" +action+"&sessionId="+sessId+"&elapsedTime="
        + elapsedTime + "&eventCounter="+ eventCounter++ + "&"+ args ;
}

function pluginProblem (responseText, textStatus, XMLHttpRequest) {
    var activity = JSON.parse(responseText);
    var problem = activity.problem;
    var problemParams = activity.binding;
    var probContentPath = activity.probContentPath || problem.probContentPath;
    var stmt = problem.statementHTML;
    var audio = problem.questionAudio;
    var fig = problem.questionImage;
    var hints = problem.hints;
    var answers = problem.answers;
    var newAnswer = problem.newAnswer;
    var answer = problem.answer;
    var units = problem.units;
    var mode = problem.mode;
    var questType = problem.questType;
    var resource = problem.resource;
    var problemFormat = problem.format;

    var g = {stmt: stmt, fig: fig, audio: audio, hints: hints, answers: answers, newAnswer: newAnswer,
        answer: answer, units: units, mode: mode, questType: questType, resource: resource,
        probContentPath: probContentPath, problemParams: problemParams, problemFormat: problemFormat,
        addHintButton: addHintButton};
    prepareForData(document, g);
    plug(document, g);
    probUtilsInit(document, g);

}