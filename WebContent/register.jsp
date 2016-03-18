<%@ page contentType="text/html;charset=utf8"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="jsp/header.jsp" %>
</head>

<body>
	<div data-role="page">
	<form action="register.jsp" method="post" data-ajax="false">
	<div style="padding:10px 20px;margin:0 auto;max-width: 300px;">
		<h3>Please register<a href="login.jsp" data-ajax="false" class="ui-btn ui-corner-all ui-btn-inline" style="margin:-10px 0;right:0;float:right;">Login</a></h3>

		<label for="account" class="ui-hidden-accessible">account:</label>
		<input type="text" name="account" id="account" value="" placeholder="account">

		<label for="password" class="ui-hidden-accessible">password:</label>
		<input type="password" name="password" id="password" value="" placeholder="password">

		<input type="submit" value="Register" class="ui-btn ui-corner-all ui-shadow ui-btn-icon-left ui-icon-check" data-rel="back" data-transition="flow">
		</div>
	</form>
	</div>

</body>
</html>
<%

javax.servlet.http.HttpSession session_comm = request.getSession(true);

if (request.getParameter("account")!=null && request.getParameter("password")!=null){
   String account = request.getParameter("account");
   String password = request.getParameter("password");
   System.out.println(account+":"+password);
   session_comm.setAttribute("account", account);
   session_comm.setAttribute("password", password);
   session_comm.setMaxInactiveInterval(3600*3);//单位：秒（实际经常到20秒以后才超时）
   out.print("<script>alert(\"请求已提交！我们会尽快与您取得联系\");</script>");
   //response.sendRedirect("index.jsp");
}
%>

