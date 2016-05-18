<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>

<div data-role="page" id="server-page" class="jqm-demos" data-quicklinks="true">
	<div data-role="popup" id="popupNewServer" data-theme="a" class="ui-corner-all">
		<div style="padding:10px 20px;">
			<h3>Please sign in</h3>

	        <label for="serverid" class="ui-hidden-accessible">serverId:</label>
	        <input type="text" name="serverid" value="" placeholder="serverId">

	    	<a href="#" class="ui-btn ui-corner-all ui-shadow ui-btn-b ui-btn-icon-left ui-icon-check" data-rel="back" data-transition="flow" onclick="addNewServerTab()">Sign in</a>
		</div>
	</div>
    <div id="server-navmenu-panel" class="jqm-navmenu-panel" data-position="left" data-display="overlay">
    	<ul data-role="listview" id="server-nav" class="jqm-list ui-alt-icon ui-nodisc-icon">
			<li  class="nav-locate" style="background-color: #F6F6F6;">添加ServerId&nbsp;&nbsp;&rArr;</li>
			<li data-theme="b" data-icon="home"><a href="#menu-panel">导航【服务器】</a></li>
			<li ><a href="#" class="nav-btn-active" data-type="base">基本</a></li>
			<li ><a href="#" data-type="blacknosay">禁言黑名单</a></li>
			<li ><a href="#" data-type="blackuser">角色黑名单</a></li>
			<li ><a href="#" data-type="blackaccount">帐号黑名单</a></li>
			<li ><a href="#" data-type="0">其他</a></li>
			<li data-theme="b"><a href="#user-page">用户</a></li>
			<li data-theme="b"><a href="#config-page">配置</a></li>

	     </ul>
	</div><!-- /navmenu -->

	<div role="main" class="ui-content jqm-content">
		<div data-role="controlgroup" id="server-controlgroup" class="ui-btn-inline">
			<a href="#popupNewServer" data-rel="popup" data-position-to="window" data-transition="pop" id="new-servertab" class="new-tab ui-btn ui-btn-inline ui-btn-icon-notext ui-icon-plus ui-nodisc-icon ui-alt-icon">Menu</a>
		</div>
		
		<div id="black-data" style="display:none;" >
			<div class="ui-field-contain">
			    <label for="user-id">UserId:</label>
			    <input name="userid" type="text">
			</div>
      	<div class="ui-field-contain">
	        <label>UserName:</label>
	        <input type="text" name="username">
   		</div>
   		<div class="ui-field-contain">
	        <label>LastTime(小时):</label>
	        <input type="text" name="lasttime" value="" placeholder="lasttime">
			</div>
			<div class="ui-grid-a ui-responsive">
    			<div class="ui-block-a"><a href="#" class="ui-btn ui-btn-b ui-corner-all ui-shadow" onclick="doBlack();">send black</a></div>
   		</div>
   		<div data-role="controlgroup" data-type="horizontal">
		    	<label><input type="checkbox" id="selectAllData" onchange="selectAllData(this)">所有匹配的Key</label>
				<a href="#" class="ui-btn ui-btn-inline ui-shadow ui-corner-all" onclick="delBlackDatas()">删除选中</a>
			</div>
		    <div data-role="controlgroup" id="blackdata-controlgroup"><!-- items will be injected here --></div>
		    </div>
		    
		    <div id="server-editor">
			</div>
		</div>	
		
	</div><!-- /content -->

	<%@ include file="footer.jsp" %>

</div><!-- /server-page -->