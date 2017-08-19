<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>
<%-- <%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
//<base href="<%=basePath%>">
 --%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="jsp/header.jsp" %>
    <link rel="stylesheet" href="css/jsoneditor.css"/>
    <link rel="stylesheet" href="css/style.css?v=3"/>
    <script src="js/jquery.jsoneditor.js"></script>
    <script src="js/jsoneditor.js?v=3"></script>
<%
	javax.servlet.http.HttpSession session_comm = request
			.getSession(true);

	//out.println(session_comm.getMaxInactiveInterval());//显示当前超时时间

	String mysession = (String) session_comm.getAttribute("session");
	if (mysession == null || mysession == "") {
%>
<jsp:forward page="login.jsp"/>
<%
	}else{
		out.print("<script>var session = \""+mysession+"\""
				+";var canreward = "+(Integer) session_comm.getAttribute("canreward")
				+";var canview = "+(Integer) session_comm.getAttribute("canview")
				+";var canwrite = "+(Integer) session_comm.getAttribute("canwrite")
				+";var canmaster = "+(Integer) session_comm.getAttribute("canmaster")
				+";</script>");
	}
%>
</head>

<body>
<%@ include file="jsp/menu-panel.jsp" %>
<%@ include file="jsp/jsoneditor.jsp" %>

<%@ include file="jsp/user-page.jsp" %>

<%@ include file="jsp/server-page.jsp" %>

<%@ include file="jsp/config-page.jsp" %>

</body>
</html>