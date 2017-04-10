<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="problem" scope="request" type="edu.umass.ckc.wo.content.Problem"/>
<!DOCTYPE html>
<html>
    <head>
        <link rel="stylesheet" type="text/css" href="css/quickAuthProblem.css"/>
        <script src="js/jquery-1.10.2.js"></script>

        <script src="js/problemUtils.js"></script>
        <%--<script src="js/tutorutils.js"></script>--%>
        <script src="js/buildQuickAuth.js"></script>
        <script src="js/params.js"></script>
        <script src="js/problem_skeleton_load.js"></script>
        <script type="text/javascript">
            <%--   When a quickAuth problem is shown it is URL with problem_skeleton.jsp including the params below.  The params
            contain the various components of a quickAuth problem on the URL string.  As the JSP runs, it plugs these in.--%>

            <%--var stmt =  checkArg('${problem.statementHTML}');--%>
            <%--var fig =  checkArg('${problem.imageURL}');--%>
            <%--var audio =  checkArg('${problem.questionAudio}');--%>
            <%--var answer =  checkArg('${problem.answer}');--%>
//            var newAnswer;  // leave as undefined which means no shuffling
            <%--var units =  checkArg('${problem.units}');--%>
            <%--var mode =  checkArg('${problem.mode}');--%>
            <%--var questType =  checkArg('${problem.questType}');--%>
            <%--var resource =  checkArg('${problem.resource}');--%>
            <%--var probContentPath =  checkArg('${probContentPath}');--%>
            var sessId = ${sessionId};
            var eventCounter = ${eventCounter};
            var elapsedTime = ${elapsedTime};
            var servContext = '${servletContext}';
            var servletName = '${servletName}';


            <%--var x =  '${hints}';--%>
            <%--var hints = x != '' ? checkArg(JSON.parse('${hints}')) : undefined ;--%>
            <%--x = '${answers}';--%>
            <%--var answers = x != '' ? checkArg(JSON.parse('${answers}')) : undefined ;--%>
            <%--x = '${probParams}';--%>
            <%--var params = x != '' ? checkArg(JSON.parse('${probParams}')) : undefined ;--%>
            <%--var answers =  answers != undefined ? checkArg(JSON.parse('${answers}')) : answers;--%>
            <%--var params = params != undefined ? checkArg(JSON.parse('${problem.params}')) : params ;--%>

            function checkArg (x)  {
                if (x === "null")
                    return null;
                else if (x === '')
                    return undefined
                else if (x === "undefined")
                    return undefined;
                else return x
            }
        </script>
    </head>

    <%--<body onload="plugin(stmt,fig,audio,hints,answers,newAnswer,answer,units,mode,questType,resource,probContentPath,params)">--%>
    <body onload="plugin(${problem.id}, sessId, elapsedTime, eventCounter, servContext, servletName, ${previewMode}, ${teacherId})">
    <div id="ProblemContainer">
        <c:forTokens items="10,9,8,7,6,5,4,3,2,1" delims="," var="num">
            <c:out escapeXml = "false" value="<audio id = \"Hint${num}Sound\"></audio>"/>
        </c:forTokens>
        <audio id = "QuestionSound"></audio>
        <div id="HintContainer" style="display: none">
            <div class="hint-thumbs">
                <c:forTokens items="1,2,3,4,5,6,7,8,9" delims="," var="num">
                    <c:out escapeXml = "false" value="
                <div id=\"Hint${num}Thumb\" class=\"hint-thumb\" style=\"visibility:hidden;display:none;\">${num}</div>"/>
                </c:forTokens>
                <div id = "Hint10Thumb" class="hint-thumb" style="visibility:hidden;display:none">A
                </div>
            </div>
            <div class="clear"></div>
            <div class="hint-content">
                <c:forTokens items="1,2,3,4,5,6,7,8,9,10" delims="," var="num">
                    <c:out escapeXml = "false" value="
                        <div id=\"Hint${num}\" class=\"hint\"></div>"/>
                </c:forTokens>
            </div>
        </div>
        <div id="ProblemStatement"></div>
        <div id="ProblemFigure"></div>
        <div id="Answers">
            <div id="ShortAnswerBox" style="display: none">
                <!-- if you're getting weird bugs later, remember that you removed a div here, and there may be references to it that you missed-->
                <input id="answer_field" type="text"/>
                <span id="Units"></span><br/>
                <br/><button id="submit_answer" type="button">Submit Answer!</button>
                <div id="Grade_Check" class="short_answer_check"></div>
                <div id="Grade_X" class="short_answer_x"></div>
            </div>
            <div id="MultipleChoiceAnswers" style = "display: none">
                <c:forTokens items="A,B,C,D,E" delims="," var="letter">
                    <c:out escapeXml = "false" value="
                <div class=\"answer-row\" style=\"display:none;\">
                    <div id=\"${letter}Button\" class=\"button\">
                        <div id=\"${letter}Check\" class=\"check\"></div>
                        <div id=\"${letter}X\" class=\"x\"></div>
                        <div id=\"${letter}Text\" class=\"button_text\">${letter}</div>
                        <div id=\"${letter}Ellipse\" class=\"ellipse\"></div>
                    </div>
                    <div id=\"Answer${letter}\" class=\"answer_text\"></div>
                </div>"/>
                </c:forTokens>
            </div>
        </div>
    </div>
    </body>
</html>