<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
    <title>Topic Mastery history for student - Eureka14 in class Turners Falls - Camp Eureka</title>
    <script src="js/jquery-1.10.2.js"></script>
    <%--<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>--%>
    <script type="text/javascript" src="js/jqplot/jquery.jqplot.min.js"></script>
    <script type="text/javascript" src="js/jqplot/plugins/jqplot.canvasTextRenderer.min.js"></script>
    <script type="text/javascript" src="js/jqplot/plugins/jqplot.canvasAxisLabelRenderer.min.js"></script>
    <link rel="stylesheet" type="text/css" hrf="js/jqplot/jquery.jqplot.min.css" />
    <script class="code" type="text/javascript">

        function formHandler(form){
            var URL = document.form.site.options[document.form.site.selectedIndex].value;
            window.open( URL);
        }

        $(document).ready(function(){
            var ticks = [${xLabels}] ;
            var line1 = [${masterySequence}];
            var plot1 = $.jqplot ('chart1', [line1], {
                animate: true,
                axes: {
                    xaxis: {

                        label: 'Problems in order seen' ,
                        ticks: ticks

                    },
                    yaxis: {
                        min: 0.0,
                        max: 1.0,
                        label: 'Mastery',
                        labelRenderer: $.jqplot.CanvasAxisLabelRenderer
                    }
                }
            });
        });
    </script>

</head>
<h3>Topic Mastery history for student Eureka14</h3>
<h5><a href='?action=AdminViewReport&teacherId=${teacherId}&classId=${classId}'>Choose another report</a></h5>
<br>


<div id="chart1" style="height:300px; width:500px;"></div>

<pre class="code prettyprint brush: js"></pre>


<form name="form">
    <%--@elvariable id="problems" type="edu.umass.ckc.wo.content.Problem[]"--%>
    <select name="site" size=1>
        <c:forEach var="problem" items="${problems}">
            <option value="${pageContext.request.contextPath}/mathspring/mscontent/Problems/probplayer.swf?questionNum=${problem.getProbNumber()}">${problem.id}:${problem.name}-${problem.nickname}</option>
        </c:forEach>
    </select>
    <input type=button value="Preview" onClick="javascript:formHandler(this)">
</form>