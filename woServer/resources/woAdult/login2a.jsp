<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<title>Wayang Adult</title>

<link href="login/css/woColint.css" rel="stylesheet" type="text/css" />
<link href="login/css/p7ccm04.css" rel="stylesheet" type="text/css" media="all" />
<link href="css/simple-slider.css" rel="stylesheet" type="text/css" />
<!--[if lte IE 7]>
<link href="/p7ie_fixes/p7ccm_ie.css" rel="stylesheet" type="text/css" media="all" />
<![endif]-->
<script type="text/javascript" src="login/js/p7EHCscripts.js"></script>
<script type="text/javascript" src="js/jquery-1.9.1.js"></script>
<script type="text/javascript" src="js/simple-slider.js"></script>

<style type="text/css">
</style>
</head>

<body>

<div class="container">
<div class="content">
    <div id="p7CCM_1" class="p7CCM04 p7ccm04-fixed-980">
      <div class="p7ccm04-content-row p7ccm04-RGBA p7ccm-row">
        <div class="p7ccm04-3col-sidebar-left-right-column2 p7ccm-col">
          <div class="p7ccm04-3col-sidebar-left-right-column2-cnt p7ccm04-content">          </div>
        </div>
        </div>
      <div class="p7ccm04-content-row p7ccm04-RGBA p7ccm-row">
        <div class="p7ccm04-1col-column1 p7ccm-col">
          <div class="p7ccm04-1col-column1-cnt p7ccm04-content"> <img src="login/images/msinterfaceColandAdult.png" width="980" height="164" alt="MathSpring Math Tutor Adult" /></div>
        </div>
      </div>
      
      <div class="p7ccm04-content-row p7ccm04-RGBA p7ccm-row">
        <div class="p7ccm04-1col-column1 p7ccm-col">
          <div class="p7ccm04-1col-column1-cnt p7ccm04-content">
            <p>Thank you for using the MathSpring Mathematics Tutor, please tell the tutor your name so it can be more personable in helping you.</p>
            <div class="nest">
              <div class="set"> </div>
            </div>
            <div class="set">
              <form method="post" name="login" action="${pageContext.request.contextPath}/WoLoginServlet">
                <input type="hidden" name="action" value="LoginAdult_3"/>
                <input type="hidden" name="sessionId" value="${sessionId}">
              <p>&nbsp;</p>
                  <p><b>First Name:
                      <input type="text" name="fname" />
                      Last Initial:
                      <input type="text" name="lini" /> </b></p>
                  <br>
                  <p><b>How confident do you feel when solving math problems?:</b></p>
                  <input type="radio" name="confidence" value="1">Not at all</input>
                  <input type="radio" name="confidence" value="2">A little</input>
                  <input type="radio" name="confidence" value="3">Somewhat</input>
                  <input type="radio" name="confidence" value="4">Quite a bit</input>
                  <input type="radio" name="confidence" value="5">Extremely</input>
                  <br>

                  <br>
                  <p><b>How interested do you feel when solving math problems, in general?:</b></p>
                  <input type="radio" name="interest" value="1">Not at all</input>
                  <input type="radio" name="interest" value="2">A little</input>
                  <input type="radio" name="interest" value="3">Somewhat</input>
                  <input type="radio" name="interest" value="4">Quite a bit</input>
                  <input type="radio" name="interest" value="5">Extremely</input>
                  </br>
                  <br>
                  <p><b>In general, how exciting is it to solve math problems?:</b></p>
                  <input type="radio" name="excitement" value="1">Not at all</input>
                  <input type="radio" name="excitement" value="2">A little</input>
                  <input type="radio" name="excitement" value="3">Somewhat</input>
                  <input type="radio" name="excitement" value="4">Quite a bit</input>
                  <input type="radio" name="excitement" value="5">Extremely</input>
                  </br>


                  <br>
                  <p><b>How frustrated do you feel when solving math problems overall?:</b></p>
                  <input type="radio" name="frustration" value="1">Not at all</input>
                  <input type="radio" name="frustration" value="2">A little</input>
                  <input type="radio" name="frustration" value="3">Somewhat</input>
                  <input type="radio" name="frustration" value="4">Quite a bit</input>
                  <input type="radio" name="frustration" value="5">Extremely</input>
                  </br>

                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input  type="submit"  value="Submit" />
              </p>
            </form>
</div>

            <p>&nbsp;</p>
            <p>&nbsp;</p>
            <p class="centered">&nbsp;</p>
          </div>
        </div>
      </div>
      <div class="p7ccm04-content-row p7ccm-row">
        <div class="p7ccm04-3col-sidebar-left-right-column1 p7ccm-col">
          <div class="p7ccm04-3col-sidebar-left-right-column1-cnt p7ccm04-content p7ehc-1">
            <div class="footCR">The <a href="http://wayangoutpost.com/">MathSpring</a> Mathematics Tutor is under development by the <a href="http://centerforknowledgecommunication.com/">Center for Knowledge Communication</a> at the <a href="http://www.umass.edu/">University of Massachusetts Amherst</a></div>
          </div>
        </div>
        <div class="p7ccm04-3col-sidebar-left-right-column3 p7ccm-col"></div>
      </div>
    </div>
<!-- end .container --></div>
</body>
</html>
