<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/demo/";
%>
<%
	javax.servlet.http.HttpSession session_comm = request
			.getSession(true);

	//out.println(session_comm.getMaxInactiveInterval());//显示当前超时时间

	String account = (String) session_comm.getAttribute("account");
	if (account == null || account == "") {
		out.println("<SCRIPT language=JavaScript>");
		out.println("alert(\"登录超时，请重新登录！\");");
		out.println("window.top.location='login.jsp'");
		out.println("</script>");
	}
%>

<!DOCTYPE html>
<html>
<head>
	<base href="<%=basePath%>">
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Demos</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0"> 
	<link rel="shortcut icon" href="favicon.ico">
    <link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Open+Sans:300,400,700">
	<link rel="stylesheet" href="css/themes/default/jquery.mobile-1.4.5.min.css">
    <link rel="stylesheet" href="jsoneditor.css"/>
    <link rel="stylesheet" href="style.css"/>
	<script src="jquery.js"></script>
	<script src="jquery.mobile-1.4.5.js"></script>
    <script src="jquery.jsoneditor.js"></script>
    <script src="jsoneditor.js"></script>
	<style type="text/css">
	
	</style>
	<script>
		( function( $, undefined ) {
			// var counter = 0;
			// function addUserTab(user) {
			// 	var userid = $('input[name="userid"]').val();
			// 	var username = $('input[name="username"]').val();
			// 	var serverid = $('input[name="serverid"]').val();
			// 	var $el = $( '<a href="#" onclick="updateUser(\''+user+'\')" class="nav-btn ui-btn ui-btn-inline">user ' + counter + "</a>" );
			// 	$("#new-tab").after($el);
			// 	$el.buttonMarkup();
			// 	$( "#user-controlgroup" ).controlgroup( "refresh" );
			// }
			$( document ).bind( "pagecreate", function( e ) {
				// $("#adduser").click(function() {
				// 	addUserTab();
				// });
			});
		})( jQuery );
	</script>
	<script>
		function time(){
			setTimeout(1000*3600*3,"document.location.href='login.jsp';");
		}
	</script>
</head>
<body onload="time()">
<div data-role="page" class="jqm-demos" data-quicklinks="true">

	<!-- <div data-role="header" class="jqm-header">
	        <h1>标题</h1>
		<a href="#" class="new-tab ui-btn ui-btn-icon-notext ui-corner-all ui-icon-bars ui-nodisc-icon ui-alt-icon ui-btn-left">Menu</a>
		<a href="#" class="ui-btn ui-btn-inline">Anchor</a>
	</div>/header -->

    <div data-role="panel" id="menu-panel" data-theme="b" data-position="left" data-display="overlay">
    	<ul data-role="listview" class="jqm-list ui-alt-icon ui-nodisc-icon">
			<li data-filtertext="demos homepage" data-icon="home"><a href="pages-dialog/dialog.html" data-transition="slidedown">用户</a></li>
			<li data-filtertext="introduction overview getting started"><a href="intro/" data-ajax="false">Introduction</a></li>
			<li data-filtertext="buttons button markup buttonmarkup method anchor link button element"><a href="button-markup/" data-ajax="false">Buttons</a></li>
			<li><a href="login.jsp" >注销</a></li>

	     </ul>
	</div><!-- /panel -->
	<div data-role="popup" id="popupNewUser" data-theme="a" class="ui-corner-all">
		<div style="padding:10px 20px;">
			<h3>Please sign in</h3>
	        <label for="userid" class="ui-hidden-accessible">userId:</label>
	        <input type="text" name="userid" id="userid" value="" placeholder="userId">

	        <label for="username" class="ui-hidden-accessible">userName:</label>
	        <input type="text" name="username" id="username" value="" placeholder="userName">

	        <label for="serverid" class="ui-hidden-accessible">serverId:</label>
	        <input type="text" name="serverid" id="serverid" value="" placeholder="serverId">

	    	<a href="#" id="adduser" class="ui-btn ui-corner-all ui-shadow ui-btn-b ui-btn-icon-left ui-icon-check" data-rel="back" data-transition="flow" onclick="addUserTab()">Sign in</a>
		</div>
	</div>
	<div id="user-page">
    <div id="navmenu-panel" class="jqm-navmenu-panel" data-position="left" data-display="overlay">
    	<ul data-role="listview" class="jqm-list ui-alt-icon ui-nodisc-icon">
			<li data-filtertext="demos homepage" data-icon="home"><a href="#menu-panel">Home</a></li>
			<li data-filtertext="introduction overview getting started"><a href="intro/" data-ajax="false">Introduction</a></li>
			<li data-filtertext="buttons button markup buttonmarkup method anchor link button element"><a href="button-markup/" data-ajax="false">Buttons</a></li>
			<li data-filtertext="form button widget input button submit reset"><a href="button/" data-ajax="false">Button widget</a></li>
			<li data-filtertext="form button widget input button submit reset"><a href="button/" data-ajax="false">Button widget</a></li>
			<li data-filtertext="form button widget input button submit reset"><a href="button/" data-ajax="false">Button widget</a></li>
			<li data-filtertext="form button widget input button submit reset"><a href="button/" data-ajax="false">Button widget</a></li>
			<li data-filtertext="form button widget input button submit reset"><a href="button/" data-ajax="false">Button widget</a></li>
			<li data-filtertext="form button widget input button submit reset"><a href="button/" data-ajax="false">Button widget</a></li>

	     </ul>
	</div><!-- /navmenu -->

	<div role="main" class="ui-content jqm-content">
		<div data-role="controlgroup" id="user-controlgroup" class="ui-btn-inline">
			<a href="#popupNewUser" data-rel="popup" data-position-to="window" data-transition="pop" id="new-tab" class="new-tab ui-btn ui-btn-inline ui-btn-icon-notext ui-icon-plus ui-nodisc-icon ui-alt-icon">Menu</a>
			<!-- <a href="#" class="nav-btn ui-btn ui-btn-inline" userid="2">Anchor</a> -->
		</div>
		<jsoneditor>
			<div class="json-editor-title"><span>Introduction</span>
				<a href="#"  class="editor-btn ui-btn ui-btn-inline">加载</a>
				<div style="right:0;float:right;"><a href="#"  class="editor-btn ui-btn ui-btn-inline">删除</a>
				<a href="#"  class="editor-btn ui-btn ui-btn-inline">更新</a></div>
			</div>
		    <div id="editor" class="json-editor"></div>
		    <!-- <p class="json-note">Note.</p> -->
		    <textarea id="json" class="json"></textarea><br/>
		</jsoneditor>
	</div><!-- /content -->
	</div>


	<div data-role="footer" data-position="fixed" data-tap-toggle="false" class="jqm-footer">
		<center>jQuery Mobile Demos version</center>
	</div><!-- /footer -->

</div><!-- /page -->

</body>
</html>