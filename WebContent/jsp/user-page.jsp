<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>
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
	<div data-role="popup" id="users-panel" data-theme="b" data-position="right" data-display="overlay">
		<ul data-role="listview" data-split-icon="delete" class="jqm-list ui-alt-icon ui-nodisc-icon ui-listview">
			<li><a href="#" class="ui-btn">Empty</a></li>
		</ul>
	</div><!-- /panel -->
	<!-- <div data-role="popup" id="popupReward" data-theme="a" class="ui-corner-all">
		<div style="padding:10px 20px;">
			<h3>Please send reward</h3>
	        <label for="rewardid" class="ui-hidden-accessible">rewardId:</label>
	        <input type="text" name="rewardid" value="" placeholder="rewardId">

	        <label for="rewardcount" class="ui-hidden-accessible">rewardCount:</label>
	        <input type="text" name="rewardcount" value="" placeholder="rewardCount">

	    	<a href="#" class="ui-btn ui-corner-all ui-shadow ui-btn-b ui-btn-icon-left ui-icon-check" data-rel="back" data-transition="flow" onclick="doReward()">send reward</a>
		</div>
	</div> -->
    <div id="user-navmenu-panel" class="jqm-navmenu-panel" data-position="left" data-display="overlay">
    	<ul data-role="listview" id="user-nav" class="jqm-list ui-alt-icon ui-nodisc-icon">
			<li class="nav-locate" style="background-color: #F6F6F6;">添加用户&nbsp;&nbsp;&rArr;&nbsp;&nbsp;<span id="current-userid"></span></li>
			<li data-theme="b" data-icon="home"><a href="#menu-panel">导航【用户】</a></li>
			<li ><a href="#" onclick='$("#rewardForm").toggle();'>发奖</a></li>
			<li ><a href="#" class="nav-btn-active" data-type="base">基本</a></li>
			<li ><a href="#" data-type="hero">英雄</a></li>
			<li ><a href="#" data-type="equip">装备</a></li>
			<li ><a href="#" data-type="pvp">PVP</a></li>
			<li ><a href="#" data-type="0">其他</a></li>
			<li data-theme="b"><a href="#server-page">服务器</a></li>
			<li data-theme="b"><a href="#config-page">配置</a></li>
	     </ul>
	</div><!-- /navmenu -->

	<div role="main" class="ui-content jqm-content">
		<a href="#" class="ui-btn ui-shadow ui-corner-all ui-icon-carat-d ui-btn-icon-notext ui-btn-inline" style="position:absolute;right:5px;z-index:999;" onclick="popupUsersPanel();">UsersPanel</a>
		<div data-role="controlgroup" id="user-controlgroup" class="ui-btn-inline">
			<a href="#popupNewUser" data-rel="popup" data-position-to="window" data-transition="pop" id="new-usertab" class="new-tab ui-btn ui-btn-inline ui-btn-icon-notext ui-icon-plus ui-nodisc-icon ui-alt-icon">Menu</a>
			<!-- <a href="#" class="nav-btn ui-btn ui-btn-inline" userid="2">Anchor</a> -->
		</div>
		<form id="rewardForm" class="ui-grid-a" style="display:none;">
			<div class="ui-block-a">
	        	<label>RewardId:</label>
			    <div class="ui-input-search ui-body-inherit ui-corner-all ui-shadow-inset ui-input-has-clear">
			        <input name="rewardid" data-type="search" data-enhanced="true" data-inset="false" id="rewardid-input" placeholder="rewardId">
			    </div>
			    <div data-role="controlgroup" data-enhanced="true" data-filter="true" data-filter-reveal="true" data-input="#rewardid-input" class="ui-controlgroup ui-controlgroup-vertical ui-corner-all">
			        <div class="ui-controlgroup-controls">
			        	<%@ include file="reward.jsp" %>
			        </div>
			    </div>
		    </div>
	        <div class="ui-block-b">
		        <label>RewardCount:</label>
		        <input type="text" name="rewardcount" value="" placeholder="rewardCount">
	
	    		<a href="#" class="ui-btn ui-btn-b ui-corner-all ui-shadow" onclick="doReward();">send reward</a>
    		</div>
		</form>
		<div id="user-editor" style="float:clear;">
		</div>
	</div><!-- /content -->

	<%@ include file="footer.jsp" %>

</div><!-- /user-page -->