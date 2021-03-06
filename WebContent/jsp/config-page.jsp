<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>

<div data-role="page" id="config-page" class="jqm-demos" data-quicklinks="true">
    <div id="config-navmenu-panel" class="jqm-navmenu-panel" data-position="left" data-display="overlay">
    	<ul data-role="listview" id="config-nav" class="jqm-list ui-alt-icon ui-nodisc-icon">
			<li  class="nav-locate" style="background-color: #F6F6F6;">配置</li>
			<li data-theme="b" data-icon="home"><a href="#menu-panel">导航【配置】</a></li>
			<li ><a href="#" data-type="common">常用</a></li>
			<li ><a href="#" data-type="base">基本</a></li>
			<li ><a href="#" data-type="0">其他</a></li>
			<li ><a href="#" data-type="cdkey">cdkey</a></li>
			<li ><a href="#" data-type="delete">删除数据(慎用)</a></li>
			<li ><a href="#" data-type="gmright">管理GM账号</a></li>
			<li data-theme="b"><a href="#user-page">用户</a></li>
			<li data-theme="b"><a href="#server-page">服务器</a></li>

	     </ul>
	</div><!-- /navmenu -->

	<div role="main" class="ui-content jqm-content">
		<div id="config-cdkey" style="display:none;">
		<form id="cdkey-form" action='cdkey.txt' method="post" target="_blank" style="margin:0 5px;">
				<input name="session" id="cdkey-session" value="" type="hidden">
			<div class="ui-field-contain">
			    <label for="cdkey-id">ID:</label>
			    <input name="id" id="cdkey-id" value="" type="text" onchange="updateCdkeyUrl(this)">
			</div>
			<div class="ui-field-contain">
			    <label for="cdkey-name">礼包名称:</label>
			    <input name="name" id="cdkey-name" value="礼包" type="text">
			</div>
			<div class="ui-field-contain">
			    <label for="cdkey-length">cdkey长度:</label>
			    <input name="length" id="cdkey-length" value="8" type="text">
			</div>
			<div class="ui-field-contain">
			    <label for="cdkey-reward">奖励:</label>
			    <input name="reward" id="cdkey-reward" value='[{"itemid":1001,"count":1},{"itemid":1002,"count":1},{"itemid":1003,"count":1}]' type="text">
			</div>
			<div class="ui-field-contain">
			    <label for="cdkey-count">生成数量:</label>
			    <input name="count" id="cdkey-count" value="0" type="text">
			</div>
			<input type="submit" value="获取cdkey" class="ui-btn ui-corner-all ui-shadow ui-btn-icon-left ui-icon-check">
		</form>
		<table data-role="table" id="table-cdkey" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive" data-column-btn-text="选择列">
			<thead>
				<tr class="ui-bar-d">
					<th>ID</th>
					<th data-priority="2">礼包名称</th>
					<th data-priority="5">奖励</th>
					<th data-priority="3">已兑换</th>
					<th>操作</th>
				</tr>
			</thead>
			<tbody>
				<tr onclick="configCdkey(this)">
					<th>1</th>
					<td>cdkey</td>
					<td>{}</td>
					<td>0/0</td>
					<td><a href="#"  class="del-btn table-btn ui-btn ui-btn-inline">删除</a></td>
				</tr>
			</tbody>
		</table>
		</div>
		<div id="config-redisdata" style="display:none;margin:0 5px;">
			<div class="ui-field-contain">
			    <label for="redisdata-keys">表达式:</label>
			    <input name="keys" id="redisdata-keys" value="*" type="text">
			</div>
			<div class="ui-grid-a ui-responsive">
			    <div class="ui-block-a"><a href="#" class="ui-btn ui-shadow ui-corner-all ui-btn-icon-left ui-icon-check" onclick="getRedisData()">获取redisdata</a></div>
			    <div class="ui-block-b"><a href="#" class="ui-btn ui-shadow ui-corner-all ui-btn-icon-left ui-icon-delete" onclick="delRedisData()">删除redisdata</a></div>
			</div>
		    <div>
		    <div data-role="controlgroup" data-type="horizontal">
		    	<label><input type="checkbox" id="selectAllData" onchange="selectAllData(this)">所有匹配的Key</label>
				<a href="#" class="ui-btn ui-btn-inline ui-shadow ui-corner-all" onclick="delRedisDatas()">删除选中</a>
			</div>
		    <div data-role="controlgroup" id="data-controlgroup"><!-- items will be injected here --></div>
		    </div>
		</div>
		<div id="config-editor">
		</div>
	</div><!-- /content -->

	<%@ include file="footer.jsp" %>

</div><!-- /config-page -->
