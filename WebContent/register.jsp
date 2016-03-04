<%@ page contentType="text/html;charset=utf8"%>
<!DOCTYPE html>
<html>
	<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Demos</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0"> 
	<link rel="shortcut icon" href="favicon.ico">
    <!-- <link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Open+Sans:300,400,700"> -->
	<link rel="stylesheet" href="css/themes/default/jquery.mobile-1.4.5.min.css">
	<script src="js/jquery.js"></script>
	<script src="js/jquery.mobile-1.4.5.js"></script>
</head>

<body>
	<div data-role="page">
	<form action="register.jsp" method="post" data-ajax="false">
	<div style="padding:10px 20px;margin:0 auto;max-width: 300px;">
		<h3>Please register</h3>

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

