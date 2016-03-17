<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>

<div data-role="page" id="config-page" class="jqm-demos" data-quicklinks="true">
    <div id="config-navmenu-panel" class="jqm-navmenu-panel" data-position="left" data-display="overlay">
    	<ul data-role="listview" id="config-nav" class="jqm-list ui-alt-icon ui-nodisc-icon">
			<li  id="nav-locate" data-icon="home">配置</li>
			<li data-theme="b" data-icon="home"><a href="#menu-panel">导航</a></li>
			<li ><a href="#" data-type="base">基本</a></li>
			<li ><a href="#" data-type="0">其他</a></li>

	     </ul>
	</div><!-- /navmenu -->

	<div role="main" class="ui-content jqm-content">
		<div id="config-editor">
		</div>
	</div><!-- /content -->

	<%@ include file="footer.jsp" %>

</div><!-- /config-page -->