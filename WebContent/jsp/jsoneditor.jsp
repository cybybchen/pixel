<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>

<div id="jsoneditor" style="display:none;">
	<jsoneditor>
		<div class="json-editor-title"><span>Introduction</span>
			<a href="#" class="reload-btn editor-btn ui-btn ui-btn-inline">加载</a>
			<a href="#" style="padding:4px;border-style:none;display:none;" class="ui-state-disabled editor-btn ui-btn ui-btn-inline"><img style='opacity: 0.5;width: 2em;height: 2em;border-radius: 1.5em;' src="css/themes/default/images/ajax-loader.gif" /></a>
			<div style="right:0;float:right;">
				<a href="#"  class="del-btn editor-btn ui-btn ui-btn-inline">删除</a>
				<a href="#"  class="update-btn editor-btn ui-btn ui-btn-inline">更新</a>
				<a href="#"  class="reset-btn editor-btn ui-btn ui-btn-inline">重置</a>
			</div>
		</div>
	    <div class="json-editor"></div>
	    <!-- <p class="json-note">Note.</p> -->
	    <textarea class="json ui-input-text" onchange="updateJSON(this);"></textarea><br/>
	</jsoneditor>
</div>