<%--
  Created by IntelliJ IDEA.
  User: marshall
  Date: Dec 1, 2009
  Time: 12:24:37 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Wayang Outpost</title></head>

<BODY bgcolor="#9E6B6B" topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0" marginheight="0" marginwidth="0">
<!-- URL's used in the movie-->
<!-- text used in the movie-->
<!-- base was http://localhost:8082/woj/flash/ -->
<OBJECT classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
        codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0"
        WIDTH="100%" HEIGHT="100%" id="wayang_loader" ALIGN="">
    <PARAM name="base" value="../../wayang2/flash/">
    <PARAM NAME=movie VALUE="../flash/wayang_loader.swf?ip=<% out.print(request.getParameter("ipAddr")); %>">
    <PARAM NAME=quality VALUE=best> <PARAM NAME=bgcolor VALUE=#9E6B6B>
    <EMBED base="../../wayang2/flash/" src="../flash/wayang_loader.swf?ip=<% out.print(request.getParameter("ipAddr")); %>"
           quality=best bgcolor=#9E6B6B  WIDTH="100%" HEIGHT="100%" NAME="wayang_loader"
           ALIGN=""
           TYPE="application/x-shockwave-flash"
           PLUGINSPAGE="http://www.macromedia.com/go/getflashplayer">

   </EMBED>
</OBJECT>
</BODY>
</HTML>

