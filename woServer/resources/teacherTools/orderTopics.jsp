<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:include page="${sideMenu}" />


<jsp:useBean id="params" scope="request" type="edu.umass.ckc.wo.tutor.probSel.PedagogicalModelParameters"/>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class="mainPageMargin">
  <div id="Layer1" align="center" >
      <p class="a2"><b>Active Topic Order</b></p>
      <p>Topics will be presented in the order below.</p>

      <%--@elvariable id="classGradeColumn" type="int"--%>
      <%--@elvariable id="gradeColumnMask" type="boolean[]"--%>

      <form name="form1" method="post" action="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminTopicControl">
      <input type="hidden" name="classId" value="<c:out value="${classId}"/>">
      <input type="hidden" name="teacherId" value="<c:out value="${teacherId}"/>">
      <table class="altrows" data-context-path="<c:out value="${pageContext.request.contextPath}"/>" data-teacher-id="<c:out value="${teacherId}"/>" data-class-id="<c:out value="${classId}"/>">
          <tr class="rowheader">
              <td rowspan="2" colspan="4">Reorder</td>
              <td rowspan="2">Topic</td>
              <td rowspan="2" style="max-width:100px">Total Active Problems</td>
              <td colspan="10">Active Problems by Grade</td>
          </tr>
          <tr class="rowheader">
              <c:forEach var="visible" varStatus="status" items="${gradeColumnMask}">
                  <td style="${status.index eq classGradeColumn ? 'font-weight:bold;' : ''}${gradeColumnMask[status.index] ? '' : 'display:none'}">
                      <c:out value="${status.index eq 0 ? 'K' : (status.index eq 9 ? 'H' : status.index.toString().concat('th'))}"/>
                  </td>
              </c:forEach>
          </tr>

          <c:set var="ix" value="0"/>
          <%--@elvariable id="topics" type="edu.umass.ckc.wo.tutor.Topic[]"--%>
          <%--@elvariable id="numTopics" type="int"--%>
          <c:forEach var="topic" items="${topics}">
              <tr draggable="true" data-topic-id="<c:out value="${topic.id}"/>" data-index="<c:out value="${ix}"/>">
                  <td><a title="drag and drop" class="dragdrophandle" draggable="false" href="#"></a></td>
                  <td><a title="move up" class="moveupbutton<c:out value="${ix > 0 ? '' : ' disabled' }"/>" draggable="false" href="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminReorderTopics&reorderType=move&topicFrom=<c:out value="${ix}"/>&topicTo=<c:out value="${ix-1}"/>&teacherId=<c:out value="${teacherId}"/>&classId=<c:out value="${classId}"/>&topicId=<c:out value="${topic.id}"/>"></a></td>
                  <td><a title="move down" class="movedownbutton<c:out value="${ix < numTopics - 1 ? '' : ' disabled' }"/>" draggable="false" href="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminReorderTopics&reorderType=move&topicFrom=<c:out value="${ix}"/>&topicTo=<c:out value="${ix+1}"/>&teacherId=<c:out value="${teacherId}"/>&classId=<c:out value="${classId}"/>&topicId=<c:out value="${topic.id}"/>"></a></td>
                  <td><a title="deactivate" class="removebutton" draggable="false" href="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminReorderTopics&reorderType=omit&teacherId=<c:out value="${teacherId}"/>&classId=<c:out value="${classId}"/>&topicId=<c:out value="${topic.id}"/>"></a></td>
                  <td><a title="${topic.standards}" draggable="false" href="#"><c:out value="${topic.name}"/></a></td>
                  <td>
                      <a href="${pageContext.request.contextPath}/WoAdmin?action=AdminSelectTopicProblems&teacherId=${teacherId}&classId=${classId}&topicId=${topic.id}">
                          <c:out value="${topic.numProbs}"/>
                      </a>
                  </td>
                  <c:forEach var="problemsByGrade" varStatus="status" items="${topic.problemsByGrade}">
                      <td style="${status.index eq classGradeColumn ? 'font-weight:bold' : ''}${gradeColumnMask[status.index] ? '' : 'display:none;'}">
                          <c:if test="${problemsByGrade > 0}"><c:out value="${problemsByGrade}"/></c:if>
                      </td>
                  </c:forEach>
              </tr>
              <c:set var="ix" value="${ix+1}"/>
          </c:forEach>
      </table>
      <p/>
      <c:if test="${!empty inactiveTopics}">
      <p align="center" class="a2"><b>Inactive Topics </b></p>
      <table class="altrows">
          <tr class="rowheader">
              <td rowspan="2" valign="center">Reactivate</td>
              <td rowspan="2">Topic</td>
              <td rowspan="2" style="max-width:100px">Total Active Problems</td>
              <td colspan="10">Active Problems by Grade</td>
          </tr>
          <tr class="rowheader">
              <c:forEach var="visible" varStatus="status" items="${gradeColumnMask}">
                  <td style="${status.index eq classGradeColumn ? 'font-weight:bold;' : ''}${gradeColumnMask[status.index] ? '' : 'display:none'}">
                      <c:out value="${status.index eq 0 ? 'K' : (status.index eq 9 ? 'H' : status.index.toString().concat('th'))}"/>
                  </td>
              </c:forEach>
          </tr>
          <c:set var="ix" value="0"/>
          <%--@elvariable id="inactiveTopics" type="edu.umass.ckc.wo.tutor.Topic[]"--%>
          <c:forEach var="topic" items="${inactiveTopics}">
              <c:set var="ix" value="${ix+1}"/>
              <tr>
                  <td valign="center">
                     <a title="activate" class="moveupbutton" href="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminReorderTopics&reorderType=reactivate&teacherId=<c:out value="${teacherId}"/>&classId=<c:out value="${classId}"/>&topicId=<c:out value="${topic.id}"/>"></a>
                  </td>

              <!--    <input name='<c:out value="topicPosition"/>' type="text" value='<c:out value="${topic.seqPos}"/>' size="3" /></td> -->
                  <td><c:out value="${topic.name}"/></td>
                  <td>
                      <a href="${pageContext.request.contextPath}/WoAdmin?action=AdminSelectTopicProblems&teacherId=${teacherId}&classId=${classId}&topicId=${topic.id}">
                          <c:out value="${topic.numProbs}"/>
                      </a>
                  </td>
                  <c:forEach var="problemsByGrade" varStatus="status" items="${topic.problemsByGrade}">
                      <td style="${status.index eq classGradeColumn ? 'font-weight:bold' : ''}${gradeColumnMask[status.index] ? '' : 'display:none;'}">
                          <c:if test="${problemsByGrade > 0}"><c:out value="${problemsByGrade}"/></c:if>
                      </td>
                  </c:forEach>
              </tr>
          </c:forEach>
      </table>
     </c:if>
    <br>
      <c:if test="${isAdmin}">
    <%--@elvariable id="params" type="edu.umass.ckc.wo.tutor.probSel.ProblemSelectorParamters"--%>
      <table>
      <tr>
          <td>Max Number of Problems Per Topic: </td>
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
          <td>Min Number of Problems Per Topic: </td>
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
          <td>Max Time In a Topic (min): </td>
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
          <td>Min Time In a Topic (min): </td>
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
          <td>Content Failure Threshold: </td>
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
          <td>Topic Mastery: </td>
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
          <td>Difficulty Rate: </td>
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
          <td>Time Before External Activities Begin: </td>
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
    </form>

    <div style="height:54px"></div>
    <form name="form3" id="form3" method="post" action="<c:out value="${pageContext.request.contextPath}"/>/WoAdmin?action=AdminProblemSelection">
          <input type="submit" name="Submit" value="Examine Problems in Each Topic" style="font-size:16px;padding:10px"/>
          <input type="hidden" name="classId" value="<c:out value="${classId}"/>">
          <input type="hidden" name="teacherId" value="<c:out value="${teacherId}"/>">
    </form>
    <div style="height:54px"></div>
</div>
</div>
</body>
</html>
