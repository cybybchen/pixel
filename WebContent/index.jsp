<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
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
	<!-- <base href="<%=basePath%>"> -->
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Demos</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0"> 
	<link rel="shortcut icon" href="favicon.ico">
    <!-- <link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Open+Sans:300,400,700"> -->
	<link rel="stylesheet" href="css/themes/default/jquery.mobile-1.4.5.min.css">
    <link rel="stylesheet" href="css/jsoneditor.css"/>
    <link rel="stylesheet" href="css/style.css"/>
	<script src="js/jquery.js"></script>
	<script src="js/jquery.mobile-1.4.5.js"></script>
    <script src="js/jquery.jsoneditor.js"></script>
    <script src="js/jsoneditor.js"></script>
	<style type="text/css">
	
	</style>
	<script>
		( function( $, undefined ) {
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
<%@ include file="jsp/menu-panel.jsp" %>
<div data-role="page" id="user-page" class="jqm-demos" data-quicklinks="true">

	<!-- <div data-role="header" class="jqm-header">
	        <h1>标题</h1>
		<a href="#" class="new-tab ui-btn ui-btn-icon-notext ui-corner-all ui-icon-bars ui-nodisc-icon ui-alt-icon ui-btn-left">Menu</a>
		<a href="#" class="ui-btn ui-btn-inline">Anchor</a>
	</div>/header -->
	<div data-role="popup" id="popupNewUser" data-theme="a" class="ui-corner-all">
		<div style="padding:10px 20px;">
			<h3>Please sign in</h3>
	        <label for="userid" class="ui-hidden-accessible">userId:</label>
	        <input type="text" name="userid" value="" placeholder="userId">

	        <label for="username" class="ui-hidden-accessible">userName:</label>
	        <input type="text" name="username" value="" placeholder="userName">

	        <label for="serverid" class="ui-hidden-accessible">serverId:</label>
	        <input type="text" name="serverid" value="" placeholder="serverId">

	    	<a href="#" class="ui-btn ui-corner-all ui-shadow ui-btn-b ui-btn-icon-left ui-icon-check" data-rel="back" data-transition="flow" onclick="addNewUserTab()">Sign in</a>
		</div>
	</div>
    <div class="jqm-navmenu-panel" data-position="left" data-display="overlay">
    	<ul data-role="listview" class="jqm-list ui-alt-icon ui-nodisc-icon">
			<li  data-icon="home"><a href="#menu-panel">导航</a></li>
			<li ><a href="#menu-panel" data-ajax="false">Introduction</a></li>
			<li ><a href="#menu-panel" data-ajax="false">Buttons</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>

	     </ul>
	</div><!-- /navmenu -->

	<div role="main" class="ui-content jqm-content">
		<div data-role="controlgroup" id="user-controlgroup" class="ui-btn-inline">
			<a href="#popupNewUser" data-rel="popup" data-position-to="window" data-transition="pop" id="new-usertab" class="new-tab ui-btn ui-btn-inline ui-btn-icon-notext ui-icon-plus ui-nodisc-icon ui-alt-icon">Menu</a>
			<!-- <a href="#" class="nav-btn ui-btn ui-btn-inline" userid="2">Anchor</a> -->
		</div>
<div id="jsoneditor" style="display:none;">
	<jsoneditor>
		<div class="json-editor-title"><span>Introduction</span>
			<a href="#"  class="reload-btn editor-btn ui-btn ui-btn-inline">加载</a>
			<div style="right:0;float:right;"><a href="#"  class="del-btn editor-btn ui-btn ui-btn-inline">删除</a>
			<a href="#"  class="update-btn editor-btn ui-btn ui-btn-inline">更新</a></div>
		</div>
	    <div class="json-editor"></div>
	    <!-- <p class="json-note">Note.</p> -->
	    <textarea class="json" onchange="updateJSON(this);"></textarea><br/>
	</jsoneditor>
</div>
		<div id="user-editor">
	<jsoneditor>
		<div class="json-editor-title"><span>Introduction</span>
			<a href="#"  class="reload-btn editor-btn ui-btn ui-btn-inline">加载</a>
			<div style="right:0;float:right;"><a href="#"  class="del-btn editor-btn ui-btn ui-btn-inline">删除</a>
			<a href="#"  class="update-btn editor-btn ui-btn ui-btn-inline">更新</a></div>
		</div>
	    <div class="json-editor"></div>
	    <!-- <p class="json-note">Note.</p> -->
	    <textarea class="json" onchange="updateJSON(this);"></textarea><br/>
	</jsoneditor>
		</div>
	</div><!-- /content -->

	<%@ include file="jsp/footer.jsp" %>

</div><!-- /user-page -->

<div data-role="page" id="server-page" class="jqm-demos" data-quicklinks="true">
	<div data-role="popup" id="popupNewServer" data-theme="a" class="ui-corner-all">
		<div style="padding:10px 20px;">
			<h3>Please sign in</h3>

	        <label for="serverid" class="ui-hidden-accessible">serverId:</label>
	        <input type="text" name="serverid" value="" placeholder="serverId">

	    	<a href="#" class="ui-btn ui-corner-all ui-shadow ui-btn-b ui-btn-icon-left ui-icon-check" data-rel="back" data-transition="flow" onclick="addServerTab()">Sign in</a>
		</div>
	</div>
    <div class="jqm-navmenu-panel" data-position="left" data-display="overlay">
    	<ul data-role="listview" class="jqm-list ui-alt-icon ui-nodisc-icon">
			<li data-icon="home"><a href="#menu-panel">Home</a></li>
			<li ><a href="intro/" data-ajax="false">Introduction</a></li>
			<li ><a href="button-markup/" data-ajax="false">Buttons</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>

	     </ul>
	</div><!-- /navmenu -->

	<div role="main" class="ui-content jqm-content">
		<div data-role="controlgroup" id="server-controlgroup" class="ui-btn-inline">
			<a href="#popupNewServer" data-rel="popup" data-position-to="window" data-transition="pop" id="new-servertab" class="new-tab ui-btn ui-btn-inline ui-btn-icon-notext ui-icon-plus ui-nodisc-icon ui-alt-icon">Menu</a>
		</div>
		<div id="server-editor">
		</div>
	</div><!-- /content -->

	<%@ include file="jsp/footer.jsp" %>

</div><!-- /server-page -->

<div data-role="page" id="config-page" class="jqm-demos" data-quicklinks="true">
    <div class="jqm-navmenu-panel" data-position="left" data-display="overlay">
    	<ul data-role="listview" class="jqm-list ui-alt-icon ui-nodisc-icon">
			<li data-icon="home"><a href="#" onclick='$( ".menu-panel" ).panel( "open" );'>Home</a></li>
			<li ><a href="intro/" data-ajax="false">Introduction</a></li>
			<li ><a href="button-markup/" data-ajax="false">Buttons</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>
			<li ><a href="button/" data-ajax="false">Button widget</a></li>

	     </ul>
	</div><!-- /navmenu -->

	<div role="main" class="ui-content jqm-content">
		<div data-role="controlgroup" id="config-controlgroup" class="ui-btn-inline">
			<a href="#popupNewConfig" data-rel="popup" data-position-to="window" data-transition="pop" id="new-configtab" class="new-tab ui-btn ui-btn-inline ui-btn-icon-notext ui-icon-plus ui-nodisc-icon ui-alt-icon">Menu</a>
		</div>
		<div id="config-editor">
		</div>
	</div><!-- /content -->

	<%@ include file="jsp/footer.jsp" %>

</div><!-- /config-page -->

</body>
</html>