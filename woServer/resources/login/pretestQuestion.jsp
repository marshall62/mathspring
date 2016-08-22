<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: david
  Date: 4/13/15
  Time: 3:42 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.umass.ckc.wo.content.PrePostProblemDefn" %>
<style>
    .ui-progressbar {
        position: relative;
    }
    .progress-label {
        position: absolute;
        left: 5%;
        top: 4px;
        font-weight: bold;
        text-shadow: 1px 1px 0 #fff;
    }
</style>

<%--  JQuery is loaded by the containing page loginK12Outer.jsp--%>
<script type="text/javascript">
    var startTime;
    var elapsedTime = 0;
    var showWaitMessage = true;
    // Pop up an alert after a period of time so student does not sit in the question too long
    $(document).ready(function () {
        var interval = ${question.waitTimeSecs} * 1000;  // convert to ms
        var d = new Date();
        startTime = d.getTime();
        setInterval(function () { alertUser(${question.isMultiChoice()})}, interval );
        // hide the controls that allow selecting 'I don't know'
        setIDontKnowControls(true, 'none');
    });

    // calculate the time since this page was shown and set the hidden input to have the time.
    // If the form is valid, the elapsedTime input will be sent.
    function updateElapsedTime () {
        var d = new Date();
        now = d.getTime();
        elapsedTime = now - startTime;
        document.getElementById("elapsedTimeInput").value = elapsedTime;
    }

    // value will be either 'none' or 'inline'.
    // none hides a control and inline exposes it.
    function setIDontKnowControls (isMultiChoice, value) {
        if (isMultiChoice) {
            var radBut = document.getElementById('IDontKnowRadioButton');
            if (radBut)
                radBut.style.display = value;   // hides the button control
            var butText = document.getElementById('IDontKnowText');
            if (butText)
                butText.style.display = value;
        }
        else {
            var but = document.getElementById('IDontKnowButton');
            if (but)
                but.style.display = value;
        }
    }

    function alertUser (isMultiChoice) {
        if (!showWaitMessage)
                return;
        if (isMultiChoice) {
            setIDontKnowControls(true, 'inline');
            alert("You are taking a while to answer this question.  I've added another choice to the list.   If you aren't sure, you can select 'I dont know' and submit to move on.");
        }
        else {
            // expose a button that allows the user to select 'I don't know'
            setIDontKnowControls(false, 'inline');
            alert("You are taking a while to answer this question.  I've added a new button.  If you aren't sure, click the 'I dont know' button to move on.");
        }
    }

    // when the user clicks the I don't know button, it autofills the input box and submits the form.
    function submitIDontKnow () {
        // put text in the input box so that it will be valid on submission
        document.getElementById("f").value = "I don't know.";
        // now submit the form
        document.getElementById("pretestQuestion").submit();
    }

    function validateForm(isMultipleChoiceQuest) {
        var a,b,c,d,e,idontknow,v;
        console.log("validateForm"+ isMultipleChoiceQuest);
        if (isMultipleChoiceQuest) {
            a = document.getElementById("a")!=null && document.getElementById("a").checked == true;
            b = document.getElementById("b")!=null && document.getElementById("b").checked == true;
            c = document.getElementById("c")!=null && document.getElementById("c").checked == true;
            d = document.getElementById("d")!=null && document.getElementById("d").checked == true;
            e = document.getElementById("e")!=null && document.getElementById("e").checked == true;
            idontknow =  document.getElementById("IDontKnowRadioButton")!=null &&
                    document.getElementById("IDontKnowRadioButton").checked == true;
            v = a || b || c || d || e || idontknow;

            if (!v) {
                alert("Please select one of the answers!");
            }
        }
        else {
            v = true;
            var input = document.getElementById("f").value.trim();
            if(input == "")
            {
                alert('Please answer the question!');
                document.getElementById("f").focus();
                v = false
            }
        }
        // once the user submits a valid answer, stop popping up messages about time.
        if (v)
                showWaitMessage = false;
        return v;
    }


    $( function() {

        var progressbar = $( "#progressbar" ),
                progressLabel = $( ".progress-label" );

        progressbar.progressbar({
            max: ${numProbsInTest},
            value: ${numProbsCompleted+1},
        });
        progressbar.width(300);

    } );
</script>

<%--@elvariable id="question" type="edu.umass.ckc.wo.content.PrePostProblemDefn"--%>

<c:if test="${message != null}">
    <b>${message}</b> <br><br>
</c:if>



<form id="pretestQuestion" method="post" name="login" onsubmit="updateElapsedTime(); return validateForm(${question.isMultiChoice()})" action="${pageContext.request.contextPath}/WoLoginServlet">
    <input type="hidden" name="action" value="LoginInterventionInput"/>
    <input type="hidden" name="sessionId" value="${sessionId}">
    <input type="hidden" name="skin" value="${skin}"/>
    <input type="hidden" name="interventionClass" value="${interventionClass}"/>
    <input type="hidden" name="probId" value="${question.id}"/>
    <input id="elapsedTimeInput" type="hidden" name="elapsedTime" value="0"/>

    <p>&nbsp;</p>
    <c:choose>
        <c:when test="${question.isMultiChoice()}">
            <p><b>${question.descr}:</b></p>
            <c:if test="${question.aAns != null}">
                <input id="a" type="radio" name="answer" value="${question.aAns}"> ${question.aAns}</input>
            </c:if>
            <c:if test="${question.bAns != null}">
                <br/><input id="b" type="radio" name="answer" value="${question.bAns}"> ${question.bAns}</input>
            </c:if>
            <c:if test="${question.cAns != null}">
                <br/><input id="c" type="radio" name="answer" value="${question.cAns}"> ${question.cAns}</input>
            </c:if>
            <c:if test="${question.dAns != null}">
                <br/><input id="d" type="radio" name="answer" value="${question.dAns}"> ${question.dAns}</input>
            </c:if>
            <c:if test="${question.eAns != null} ">
                <br/> <input id="e" type="radio" name="answer" value="${question.eAns}"> ${question.eAns}</input>
            </c:if>
            <br/>
            <input id="IDontKnowRadioButton" type="radio" name="answer" value="I dont know">
                <span id="IDontKnowText">I don't know</span>
            </input>

            <br>
        </c:when>
        <c:otherwise>
            <c:if test="${question.url != null}">
                <img src="${pageContext.request.contextPath}${question.url}"/>
                <br/>
            </c:if>
            <p><b>${question.descr}</b></p>
            <input id="f" type="text" name="answer"/>
            </br>
            <br>
        </c:otherwise>
    </c:choose>


    <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input  type="submit"  value="Submit" /> &nbsp;&nbsp;
    <input id="IDontKnowButton" onClick="submitIDontKnow()" type="submit" value="I don't know" style="display:none"/>
    <br><br>
    <div id="progressbar"> <div class="progress-label">${numProbsCompleted+1} of ${numProbsInTest} questions</div></div>
    </p>
</form>
<br>





