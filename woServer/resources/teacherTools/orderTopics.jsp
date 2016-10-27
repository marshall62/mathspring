<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:include page="${sideMenu}" />


<jsp:useBean id="params" scope="request" type="edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class="mainPageMargin">
  <div id="Layer1" align="left" >
    <p align="center" class="a2"><font color="#000000"><b><font face="Arial, Helvetica, sans-serif">Active Topic Order </font></b></font></p>
    <p style="color: #000000"><font face="Arial, Helvetica, sans-serif">Topics will be presented in the order below. </font></p>

    <form name="form1" method="post" action="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminTopicControl">
      <input type="hidden" name="classId" value="<c:out value="${classId}"/>">
      <input type="hidden" name="teacherId" value="<c:out value="${teacherId}"/>">
      <table width="374" border="0" height="98">
          <tr>
            <td></td>
            <td valign="center"><font color="#00000" face="Arial, Helvetica, sans-serif">Order</font></td>
               <td></td>
            <td><font color="#000000" face="Arial, Helvetica, sans-serif">Topic</font></td>
              <td><font color="#000000" face="Arial, Helvetica, sans-serif">Number of Problems</font> </td>
              <%--<td><font color="#000000" face="Arial, Helvetica, sans-serif">Standards</font></td>--%>
          </tr>

          <c:set var="ix" value="0"/>
          <%--@elvariable id="topics" type="edu.umass.ckc.wo.tutor.Topic[]"--%>
          <c:forEach var="topic" items="${topics}">
              <c:set var="ix" value="${ix+1}"/>
              <tr>
                  <td valign="center" width="40">
                     <a title="move up" href="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminReorderTopics&direction=up&teacherId=<c:out value="${teacherId}"/>&classId=<c:out value="${classId}"/>&topicId=<c:out value="${topic.id}"/>">
                          <img  src='<c:out value="${pageContext.request.contextPath}"/>/images/moveup.gif' alt="Move Up"></a></td>
                  <td valign="center" width="40">
                     <a title="move down" href="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminReorderTopics&direction=down&teacherId=<c:out value="${teacherId}"/>&classId=<c:out value="${classId}"/>&topicId=<c:out value="${topic.id}"/>">
                          <img  src='<c:out value="${pageContext.request.contextPath}"/>/images/movedown.gif' alt="Move Down"></a></td>
                  <td valign="center" width="40">
                     <a title="deactivate" href="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminReorderTopics&direction=omit&teacherId=<c:out value="${teacherId}"/>&classId=<c:out value="${classId}"/>&topicId=<c:out value="${topic.id}"/>">
                          <img  src='<c:out value="${pageContext.request.contextPath}"/>/images/del.gif' alt="Don't Play"></a></td>

              <!--    <input name='<c:out value="topicPosition"/>' type="text" value='<c:out value="${topic.seqPos}"/>' size="3" /></td> -->
                  <td width="305"><a title="${topic.standards}" href="#"> <font color="#00000" face="Arial, Helvetica, sans-serif"><c:out value="${topic.name}"/></font></a></td>

                  <td width="40">
                      <a href="${pageContext.request.contextPath}/WoAdmin?action=AdminSelectTopicProblems&teacherId=${teacherId}&classId=${classId}&topicId=${topic.id}">
                          <font color="#00000" face="Arial, Helvetica, sans-serif"><c:out value="${topic.numProbs}"/></font>
                      </a></td>

              <%--<td width="600">  <font color="#000000" face="Arial, Helvetica, sans-serif">  ${topic.standards}</font>--%>
                                   <%--</td>--%>
              </tr>


          </c:forEach>
      </table>
      <p/>
      <c:if test="${!empty inactiveTopics}">
      <p align="center" class="a2"><font color="#00000"><b><font face="Arial, Helvetica, sans-serif">Inactive Topics </font></b></p>
      <table width="334" border="0" height="98">
          <tr>
            <td valign="center"><font color="#00000" face="Arial, Helvetica, sans-serif">Reactivate</font></td>

            <td><font color="#00000" face="Arial, Helvetica, sans-serif">Topic</font></td>
          </tr>
          <c:set var="ix" value="0"/>
          <%--@elvariable id="inactiveTopics" type="edu.umass.ckc.wo.tutor.Topic[]"--%>
          <c:forEach var="topic" items="${inactiveTopics}">
              <c:set var="ix" value="${ix+1}"/>
              <tr>
                  <td valign="center" width="40">
                     <a title="activate" href="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminReorderTopics&direction=reactivate&teacherId=<c:out value="${teacherId}"/>&classId=<c:out value="${classId}"/>&topicId=<c:out value="${topic.id}"/>">
                          <img  src='<c:out value="${pageContext.request.contextPath}"/>/images/moveup.gif' alt="Reactivate"></a></td>

              <!--    <input name='<c:out value="topicPosition"/>' type="text" value='<c:out value="${topic.seqPos}"/>' size="3" /></td> -->
                  <td width="305"><font color="#00000" face="Arial, Helvetica, sans-serif"><c:out value="${topic.name}"/></font></td>
              </tr>
              

          </c:forEach>
      </table>
     </c:if>
    <br>
      <c:if test="${isAdmin}">
    <%--@elvariable id="params" type="edu.umass.ckc.wo.tutor.probSel.ProblemSelectorParamters"--%>
      <table>
      <tr>
          <td><font color="#00000"><font face="Arial, Helvetica, sans-serif">Max Number of Problems Per Topic: </font></td>
          <td>
              <script language="JavaScript">
                  var A_TPL1h = {
                      'b_vertical' : false,
                      'b_watch': true,
                      'n_controlWidth': 120,
                      'n_controlHeight': 16,
                      'n_sliderWidth': 16,
                      'n_sliderHeight': 15,
                      'n_pathLeft' : 1,
                      'n_pathTop' : 1,
                      'n_pathLength' : 103,
                      's_imgControl': 'img/redh_bg.gif',
                      's_imgSlider': 'img/redh_sl.gif',
                      'n_zIndex': 1
                  }

                  var A_INIT1h = {
                      's_form' : 0,
                      's_name': 'maxNumberProbsPerTopic',
                      'n_minValue' : 0,
                      'n_maxValue' : 30,
                      'n_value' : <c:out value="${params.maxNumberProbs}"/>,
                      'n_step' : 1
                  }
                  new slider(A_INIT1h, A_TPL1h);
              </script>



          </td>
          <td><input type="text" size="3" name="maxNumberProbsPerTopic" value="<c:out value="${params.maxNumberProbs}"/>"/>
          </td>
      </tr>

      <tr>
          <td><font color="#00000"><font face="Arial, Helvetica, sans-serif">Min Number of Problems Per Topic: </font></td>
          <td>
              <script language="JavaScript">
                  var A_TPL1h = {
                      'b_vertical' : false,
                      'b_watch': true,
                      'n_controlWidth': 120,
                      'n_controlHeight': 16,
                      'n_sliderWidth': 16,
                      'n_sliderHeight': 15,
                      'n_pathLeft' : 1,
                      'n_pathTop' : 1,
                      'n_pathLength' : 103,
                      's_imgControl': 'img/redh_bg.gif',
                      's_imgSlider': 'img/redh_sl.gif',
                      'n_zIndex': 1
                  }

                  var A_INIT1h = {
                      's_form' : 0,
                      's_name': 'minNumberProbsPerTopic',
                      'n_minValue' : 0,
                      'n_maxValue' : 30,
                      'n_value' : <c:out value="${params.minNumberProbs}"/>,
                      'n_step' : 1
                  }
                  new slider(A_INIT1h, A_TPL1h);
              </script>



          </td>
          <td><input type="text" size="3" name="minNumberProbsPerTopic" value="<c:out value="${params.minNumberProbs}"/>"/>
          </td>
      </tr>

      <tr>
          <td><font color="#00000"><font face="Arial, Helvetica, sans-serif">Max Time In a Topic (min): </font></td>
          <td>
              <script language="JavaScript">
                  var A_TPL1h = {
                      'b_vertical' : false,
                      'b_watch': true,
                      'n_controlWidth': 120,
                      'n_controlHeight': 16,
                      'n_sliderWidth': 16,
                      'n_sliderHeight': 15,
                      'n_pathLeft' : 1,
                      'n_pathTop' : 1,
                      'n_pathLength' : 103,
                      's_imgControl': 'img/redh_bg.gif',
                      's_imgSlider': 'img/redh_sl.gif',
                      'n_zIndex': 1
                  }

                  var A_INIT1h = {
                      's_form' : 0,
                      's_name': 'maxTimeInTopic',
                      'n_minValue' : 1,
                      'n_maxValue' : 30,
                      'n_value' : <c:out value="${params.maxTimeInTopicMinutes}"/>,
                      'n_step' : 1
                  }
                  new slider(A_INIT1h, A_TPL1h);
              </script>

          </td>
          <td><input type="text" size="3" name="maxTimeInTopic"value="<c:out value="${params.maxTimeInTopicMinutes}"/>"/> </td>
      </tr>

      <tr>
          <td><font color="#00000"><font face="Arial, Helvetica, sans-serif">Min Time In a Topic (min): </font></td>
          <td>
              <script language="JavaScript">
                  var A_TPL1h = {
                      'b_vertical' : false,
                      'b_watch': true,
                      'n_controlWidth': 120,
                      'n_controlHeight': 16,
                      'n_sliderWidth': 16,
                      'n_sliderHeight': 15,
                      'n_pathLeft' : 1,
                      'n_pathTop' : 1,
                      'n_pathLength' : 103,
                      's_imgControl': 'img/redh_bg.gif',
                      's_imgSlider': 'img/redh_sl.gif',
                      'n_zIndex': 1
                  }

                  var A_INIT1h = {
                      's_form' : 0,
                      's_name': 'minTimeInTopic',
                      'n_minValue' : 1,
                      'n_maxValue' : 30,
                      'n_value' : <c:out value="${params.minTimeInTopicMinutes}"/>,
                      'n_step' : 1
                  }
                  new slider(A_INIT1h, A_TPL1h);
              </script>

          </td>
          <td><input type="text" size="3" name="minTimeInTopic"value="<c:out value="${params.minTimeInTopicMinutes}"/>"/> </td>
      </tr>

      <tr>
          <td><font color="#00000"><font face="Arial, Helvetica, sans-serif">Content Failure Threshold: </font></td>
          <td>
              <script language="JavaScript">
                  var A_TPL1h = {
                      'b_vertical' : false,
                      'b_watch': true,
                      'n_controlWidth': 120,
                      'n_controlHeight': 16,
                      'n_sliderWidth': 16,
                      'n_sliderHeight': 15,
                      'n_pathLeft' : 1,
                      'n_pathTop' : 1,
                      'n_pathLength' : 103,
                      's_imgControl': 'img/redh_bg.gif',
                      's_imgSlider': 'img/redh_sl.gif',
                      'n_zIndex': 1
                  }

                  var A_INIT1h = {
                      's_form' : 0,
                      's_name': 'contentFailureThreshold',
                      'n_minValue' : 1,
                      'n_maxValue' : 5,
                      'n_value' : <c:out value="${params.contentFailureThreshold}"/>,
                      'n_step' : 1
                  }
                  new slider(A_INIT1h, A_TPL1h);
              </script>
          </td>
          <td><input type="text" size="3" name="contentFailureThreshold"value="<c:out value="${params.contentFailureThreshold}"/>"/> </td>
      </tr>
      <tr>
          <td><font color="#00000"><font face="Arial, Helvetica, sans-serif">Topic Mastery: </font></td>
          <td>
              <script language="JavaScript">
                  var A_TPL1h = {
                      'b_vertical' : false,
                      'b_watch': true,
                      'n_controlWidth': 120,
                      'n_controlHeight': 16,
                      'n_sliderWidth': 16,
                      'n_sliderHeight': 15,
                      'n_pathLeft' : 1,
                      'n_pathTop' : 1,
                      'n_pathLength' : 103,
                      's_imgControl': 'img/redh_bg.gif',
                      's_imgSlider': 'img/redh_sl.gif',
                      'n_zIndex': 1
                  }

                  var A_INIT1h = {
                      's_form' : 0,
                      's_name': 'topicMastery',
                      'n_minValue' : 0.5,
                      'n_maxValue' : 1.2,
                      'n_value' : <c:out value="${params.topicMastery}"/>,
                      'n_step' : 0.05
                  }
                  new slider(A_INIT1h, A_TPL1h);
              </script>
          </td>
          <td><input type="text" size="3" name="mastery" value="<c:out value="${params.topicMastery}"/>"/> </td> </tr>


      <tr>
          <td><font color="#00000"><font face="Arial, Helvetica, sans-serif">Difficulty Rate: </font></td>
          <td>
              <script language="JavaScript">
                  var A_TPL1h = {
                      'b_vertical' : false,
                      'b_watch': true,
                      'n_controlWidth': 120,
                      'n_controlHeight': 16,
                      'n_sliderWidth': 16,
                      'n_sliderHeight': 15,
                      'n_pathLeft' : 1,
                      'n_pathTop' : 1,
                      'n_pathLength' : 103,
                      's_imgControl': 'img/redh_bg.gif',
                      's_imgSlider': 'img/redh_sl.gif',
                      'n_zIndex': 1
                  }

                  var A_INIT1h = {
                      's_form' : 0,
                      's_name': 'difficultyRate',
                      'n_minValue' : 1,
                      'n_maxValue' : 10,
                      'n_value' : <c:out value="${params.difficultyRate}"/>,
                      'n_step' : 1
                  }
                  new slider(A_INIT1h, A_TPL1h);
              </script>
          </td>
          <td><input type="text" size="3" name="difficultyRate" value="<c:out value="${params.difficultyRate}"/>"/> </td> </tr>


      <tr>
          <td><font color="#00000"><font face="Arial, Helvetica, sans-serif">Time Before External Activities Begin: </font></td>
          <td>
              <script language="JavaScript">
                  var A_TPL1h = {
                      'b_vertical' : false,
                      'b_watch': true,
                      'n_controlWidth': 120,
                      'n_controlHeight': 16,
                      'n_sliderWidth': 16,
                      'n_sliderHeight': 15,
                      'n_pathLeft' : 1,
                      'n_pathTop' : 1,
                      'n_pathLength' : 103,
                      's_imgControl': 'img/redh_bg.gif',
                      's_imgSlider': 'img/redh_sl.gif',
                      'n_zIndex': 1
                  }

                  var A_INIT1h = {
                      's_form' : 0,
                      's_name': 'externalActivityTimeThreshold',
                      'n_minValue' : -1,
                      'n_maxValue' : 20,
                      'n_value' : <c:out value="${params.externalActivityTimeThreshold}"/>,
                      'n_step' : 1
                  }
                  new slider(A_INIT1h, A_TPL1h);
              </script>
          </td>
          <td><input type="text" size="3" name="externalActivityTimeThreshold" value="<c:out value="${params.externalActivityTimeThreshold}"/>"/> </td> </tr>



      </table>
      </c:if>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;


    </form>

    <form name="form3" id="form3" method="post" action="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminProblemSelection">

    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <input type="submit" name="Submit" value="Examine Problems in Each Topic" />
          <input type="hidden" name="classId" value="<c:out value="${classId}"/>">
          <input type="hidden" name="teacherId" value="<c:out value="${teacherId}"/>">
    </form>



</div>
</div>



</body>
</html>
