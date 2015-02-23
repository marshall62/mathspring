<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <link rel="stylesheet" type="text/css" href="css/problem_skeleton.css"/>
        <script src="js/problemUtils.js"></script>
        <script src="js/params.js"></script>
        <script src="js/problem_skeleton_load.js"></script>
    </head>
    <body onload="plugin()">
        <div id="ShortAnswerBox">
            <!-- if you're getting weird bugs later, remember that you removed a div here, and there may be references to it that you missed-->
            <input id="answer_field" type="text"/>
            <span id="Units"></span><br/>
            <br/><button id="submit_answer" type="button">Submit Answer!</button>
            <div id="Grade_Check" class="short_answer_check"></div>
            <div id="Grade_X" class="short_answer_x"></div>
        </div>
        <c:forTokens items="10,9,8,7,6,5,4,3,2,1" delims="," var="num">
            <c:out escapeXml = "false" value="<audio id = \"Hint${num}Sound\"></audio>"/>
        </c:forTokens>
        <audio id = "QuestionSound"></audio>
        <c:forTokens items="9,8,7,6,5,4,3,2,1" delims="," var="num">
            <c:out escapeXml = "false" value="
            <div id=\"Hint${num}Thumb\" class=\"hint_thumb\">
                <div id=\"Hint${num}ThumbText\" class=\"hint_thumb_text\">Hint ${num}</div>
                <div id=\"Hint${num}ThumbImg\" class=\"hint_thumb_img\"></div>
                <div id=\"Hint${num}ThumbImgPressed\" class=\"hint_thumb_img_pressed\"></div>
            </div>"/>
        </c:forTokens>
        <div id = "Hint10Thumb" class="hint_thumb">
            <div id="Hint10ThumbText" class="hint_thumb_text">Answer</div>
            <div id="Hint10ThumbImg" class="hint_thumb_img"></div>
            <div id="Hint10ThumbImgPressed" class="hint_thumb_img_pressed"></div>
        </div>
        <c:forTokens items="10,9,8,7,6,5,4,3,2,1" delims="," var="num">
            <c:out escapeXml = "false" value="
                <div id=\"Hint${num}\" class=\"hint\"></div>"/>
        </c:forTokens>
        <div id="ProblemFigure"></div>
        <div id="ProblemStatement"></div>
        <c:forTokens items="A,B,C,D,E" delims="," var="letter">
            <c:out escapeXml = "false" value="
                <div id=\"Answer${letter}\" class=\"answer_text\">
                 </div>"/>
        </c:forTokens>
        <div id="Answers">
            <c:forTokens items="A,B,C,D,E" delims="," var="letter">
                <c:out escapeXml = "false" value="
                <div id=\"${letter}Button\" class=\"button\">
                    <div id=\"${letter}Check\" class=\"check\"></div>
                    <div id=\"${letter}X\" class=\"x\"></div>
                    <div id=\"${letter}Text\" class=\"button_text\">${letter}</div>
                    <div id=\"${letter}Ellipse\" class=\"ellipse\"></div>
                </div>"/>
            </c:forTokens>
        </div>
    </body>
</html>