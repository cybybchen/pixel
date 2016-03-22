<%@ page contentType="text/html;charset=utf8"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="jsp/header.jsp" %>
</head>

<body>
	<div data-role="page">
	<form action="login.jsp" method="post" data-ajax="false">
	<div style="padding:10px 20px;margin:0 auto;max-width: 300px;">
		<h3>Please sign in<a href="register.jsp" data-ajax="false" class="ui-btn ui-corner-all ui-btn-inline" style="margin:-10px 0;right:0;float:right;">Register</a></h3>
	
<jsp:useBean id="redis" class="com.trans.pixel.service.redis.GmAccountRedisService" scope="page" />
<%

javax.servlet.http.HttpSession session_comm = request.getSession(true);

if (request.getParameter("account")!=null && request.getParameter("password")!=null){
   String account = request.getParameter("account");
   String password = request.getParameter("password");
   Map<String, String> map = redis.getGmAccounts();
   boolean loginsuccess = false;
   for(Map.Entry<String, String> entry : map.entrySet()){
	   if(entry.getKey().equals(account) && entry.getValue().equals(password))
		   loginsuccess = true;
   }
   if(loginsuccess){
	   System.out.println(account+":"+password);
	   session_comm.setAttribute("account", account);
	   session_comm.setAttribute("password", password);
	   session_comm.setMaxInactiveInterval(3600*3);//单位：秒（实际经常到20秒以后才超时）
	   response.sendRedirect("index.jsp");
   }else{
	   out.print("<div style=\"border-radius: 0.3125em;padding:10px;border-width: 1px;border-style: solid;border-color: #F00;\">密码错误或用户不存在！</div>");
   }
}
%>
		<label for="account" class="ui-hidden-accessible">account:</label>
		<input type="text" name="account" id="account" value="" placeholder="account">

		<label for="password" class="ui-hidden-accessible">password:</label>
		<input type="password" name="password" id="password" value="" placeholder="password">

		<input type="submit" value="Sign in" class="ui-btn ui-corner-all ui-shadow ui-btn-icon-left ui-icon-check" data-rel="back" data-transition="flow">
		</div>
	</form>
	</div>

</body>
</html>
