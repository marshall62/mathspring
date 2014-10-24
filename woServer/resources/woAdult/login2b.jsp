<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

    <title>Wayang Adult</title>

    <link href="login/css/woColint.css" rel="stylesheet" type="text/css"/>
    <link href="login/css/p7ccm04.css" rel="stylesheet" type="text/css" media="all"/>
    <!--[if lte IE 7]>
    <link href="/p7ie_fixes/p7ccm_ie.css" rel="stylesheet" type="text/css" media="all" />
    <![endif]-->
    <script type="text/javascript" src="login/js/p7EHCscripts.js"></script>
</head>

<body>

<div class="container">
    <div class="content">
        <div id="p7CCM_1" class="p7CCM04 p7ccm04-fixed-980">
            <div class="p7ccm04-content-row p7ccm04-RGBA p7ccm-row">
                <div class="p7ccm04-3col-sidebar-left-right-column1 p7ccm-col">
                    <div class="p7ccm04-3col-sidebar-left-right-column1-cnt p7ccm04-content"></div>
                </div>
            </div>
            <div class="p7ccm04-content-row p7ccm04-RGBA p7ccm-row">
                <div class="p7ccm04-1col-column1 p7ccm-col">
                    <div class="p7ccm04-1col-column1-cnt p7ccm04-content"><img
                            src="login/images/msinterfaceColandAdult.png" width="980" height="164"
                            alt="MathSpring Math Tutor Adult"/></div>
                </div>
            </div>

            <div class="p7ccm04-content-row p7ccm04-RGBA p7ccm-row">
                <div class="p7ccm04-1col-column1 p7ccm-col">
                    <div class="p7ccm04-1col-column1-cnt p7ccm04-content">
                        <p>If you are working in a classroom or a lab the MathSpring Mathematics Tutor can better
                            help you if you let us know who is sitting next to you.</p>

                        <p>&nbsp;</p>

                        <div class="set">
                            <form method="post" name="login"
                                  action="<c:out value="${pageContext.request.contextPath}"/>/WoLoginServlet">
                                <input type="hidden" name="action" value="Login4"/>
                                <input type="hidden" name="sessionId" value="${sessionId}"/>
                                <c:out value="${message}"/>
                                <p>Who is on your left?
                                    <select name="left">
                                        <%--@elvariable id="students" type="java.util.List"--%>
                                        <%--@elvariable id="u" type="edu.umass.ckc.wo.smgr.User"--%>
                                        <option value="-1">No One</option>
                                        <c:forEach var="u" items="${students}">
                                            <option value="${u.id}">${u.uname} : ${u.fname} ${u.lname}</option>
                                        </c:forEach>
                                    </select>
                                    <img src="login/images/Devices-computer-icon.png" width="60" height="32"
                                         alt="computer"/>Who is on your right?
                                    <select name="right">
                                        <option value="-1">No One</option>
                                        <c:forEach var="u" items="${students}">
                                            <option value="${u.id}">${u.uname} : ${u.fname} ${u.lname}</option>
                                        </c:forEach>
                                    </select>
                                </p>
                                <p>&nbsp;</p>

                                <label>
                                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                    <input type="submit" name="button" id="button" value="Submit"/>
                                </label>
                            </form>
                            <p>&nbsp;              </p>
                        </div>
                        <p>&nbsp;</p>

                        <p>&nbsp;</p>
                    </div>
                </div>
            </div>
            <div class="p7ccm04-content-row p7ccm-row">
                <div class="p7ccm04-3col-sidebar-left-right-column1 p7ccm-col">
                    <div class="p7ccm04-3col-sidebar-left-right-column1-cnt p7ccm04-content p7ehc-1">
                        <div class="footCR">The <a href="http://wayangoutpost.com/">MathSpring</a> Mathematics Tutor
                            is under development by the <a href="http://centerforknowledgecommunication.com/">Center for
                            Knowledge Communication</a> at the <a href="http://www.umass.edu/">University of
                            Massachusetts Amherst</a></div>
                    </div>
                </div>
                <div class="p7ccm04-3col-sidebar-left-right-column3 p7ccm-col">
                    <div class="p7ccm04-3col-sidebar-left-right-column3-cnt p7ccm04-content p7ehc-1"></div>
                </div>
            </div>
        </div>
        <!-- end .container --></div>
</body>
</html>
