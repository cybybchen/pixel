<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>
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
<%@ page import="java.sql.*,java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*,javax.mail.*"%>
<%@ page import="javax.mail.internet.*"%>
<%

//javax.servlet.http.HttpSession session_comm = request.getSession(true);

String account = request.getParameter("account");
String password = request.getParameter("password");
if (account!=null && account!="" && password!=null){
   System.out.println(account+":"+password);
   //session_comm.setAttribute("account", account);
   //session_comm.setAttribute("password", password);
   //session_comm.setMaxInactiveInterval(3600*3);//单位：秒（实际经常到20秒以后才超时）
   //response.sendRedirect("index.jsp");
   out.print("<script>alert(\"请求已提交！我们会尽快与您取得联系\");</script>");
   String qm ="ltsgxwotqbzabbed"; //您的QQ密码
   String tu = "qq.com"; //你邮箱的后缀域名
   String tto="xinji.wang@transmension.com"; //接收邮件的邮箱
   String ttitle="新的GM注册!操作人:"+account;
   String tcontent="新的GM:"+account+"<br />\r\n密码:"+password;
   Properties props=new Properties();
   props.put("mail.smtp.host","smtp."+tu);
   props.put("mail.smtp.auth","true");
   //props.put("mail.smtp.socketFactory.port", 465);
   props.put("mail.smtp.starttls.enable","true");
   Session s=Session.getInstance(props);
   s.setDebug(true);
   MimeMessage message=new MimeMessage(s);
   //给消息对象设置发件人/收件人/主题/发信时间
   InternetAddress from=new InternetAddress("910321700@"+tu); //这里的910321700 改为您发信的QQ号
   message.setFrom(from);
   InternetAddress to=new InternetAddress(tto);
   message.setRecipient(Message.RecipientType.TO,to);
   message.setSubject(ttitle);
   message.setSentDate(new Date());
   //给消息对象设置内容
   BodyPart mdp=new MimeBodyPart();//新建一个存放信件内容的BodyPart对象
   mdp.setContent(tcontent,"text/html;charset=utf8");//给BodyPart对象设置内容和格式/编码方式
   Multipart mm=new MimeMultipart();//新建一个MimeMultipart对象用来存放BodyPart对象(事实上可以存放多个)
   mm.addBodyPart(mdp);//将BodyPart加入到MimeMultipart对象中(可以加入多个BodyPart)
   message.setContent(mm);//把mm作为消息对象的内容
   message.saveChanges();
   Transport transport=s.getTransport("smtp");
   transport.connect("smtp."+tu,"910321700",qm); //这里的910321700也要修改为您的QQ号码
   transport.sendMessage(message,message.getAllRecipients());
   transport.close();
}
%>

