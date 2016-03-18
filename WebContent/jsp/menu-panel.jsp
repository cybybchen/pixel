<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>
    <div data-role="panel" id="menu-panel" class="menu-panel" data-theme="b" data-position="left" data-display="overlay">
    	<ul data-role="listview" class="jqm-list ui-alt-icon ui-nodisc-icon ui-listview">
			<li><a href="#user-page" class="ui-btn ui-btn-icon-right ui-icon-carat-r">用户</a></li>
			<li><a href="#server-page" class="ui-btn ui-btn-icon-right ui-icon-carat-r">服务器</a></li>
			<li><a href="#config-page" class="ui-btn ui-btn-icon-right ui-icon-carat-r">配置</a></li>
			<li><a href="login.jsp" class="ui-btn ui-btn-icon-right ui-icon-carat-r">注销</a></li>

	     </ul>
	</div><!-- /panel -->
	<div data-role="popup" id="msg-popup" data-theme="a" style="padding:10px;border-color: #F00;">
		<p>I'm a simple popup.</p>
	</div>