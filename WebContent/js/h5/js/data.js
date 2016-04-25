function showPage(selector){
	if(!$(selector).hasClass("ui-page-active")){
		$(".ui-page-active").removeClass("ui-page-active");
		$(selector).addClass("ui-page-active");
	}
}

var guidejson = null;
var teamjson = null;
var herojson = null;
var datajson = null;
function updateListData(action, json){
	if(action.startsWith("action=A1001")){
		guidejson = json;
		action = "action=A1002";
		showPage("#listview-page");
		$("#listview-page .ui-header h1>img").attr("src", "css/images/title_guide.png");
		$("#listview-page .ul-wapper .ui-listview").empty();
		$.each(json, function (key, value) {
			var $el = $( '<li><a href="#" action="'+action+'&id='+value["id"]+'"><img src="css/images/'+value["icon"]+'.png"><h2>'+value["title"]+'</h2><p>'+value["summary"]+'</p></a></li>' );
			$("#listview-page .ul-wapper .ui-listview").append($el);
		});
	}else if(action.startsWith("action=A1002")){
		showPage("#detailview-page");
		$("#detailview-page .ul-wapper .ui-detailview").empty();
		$.each(json, function (key, value) {
			var article = value["content"] == null ? "" : value["content"];
			var content = article.split("\n").join("</p><p>");
			var $el = $( '<h2>'+value["title"]+'</h2><span class="date">2016.1.1</span><p>'+content+'</p>' );
			$("#detailview-page .ul-wapper .ui-detailview").append($el);
		});
	}else if(action.startsWith("action=A1003")){
		teamjson = json;
		action = "action=A1004";
		showPage("#listview-page");
		$("#listview-page .ui-header h1>img").attr("src", "css/images/title_team.png");
		$("#listview-page .ul-wapper .ui-listview").empty();
		$.each(json, function (key, value) {
			var $el = $( '<li><a href="#" action="'+action+'&id='+value["id"]+'"><img src="css/images/'+value["icon"]+'.png"><h2>'+value["title"]+'</h2><p>'+value["summary"]+'</p></a></li>' );
			$("#listview-page .ul-wapper .ui-listview").append($el);
		});
	}else if(action.startsWith("action=A1004")){
		showPage("#detailview-page");
		action = "action=A1006";
		$("#detailview-page .ul-wapper .ui-detailview").empty();
		$.each(json, function (key, value) {
			var article = value["content"] == null ? "" : value["content"];
			var content = article.split("\n").join("</p><p>");
			content = '<h2>'+value["title"]+'</h2><span class="date">2016.1.1</span><p>'+content
					+'</p><h3 style="margin-top: 15px;"><hr width="100px" color="#333" noshade="noshade" size="1" style="float:left;"/>核心英雄<hr width="100px" color="#333" noshade="noshade" size="1" style="float:right;"/></h3>'
					+'<div class="ui-navbar"><ul class="ui-grid-d">';
			content += (value["core1"] != "0"?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+value["core1"]+'"><img src="css/images/'+value["core1"]+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (value["core2"] != "0"?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+value["core2"]+'"><img src="css/images/'+value["core2"]+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (value["core3"] != "0"?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+value["core3"]+'"><img src="css/images/'+value["core3"]+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (value["core4"] != "0"?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+value["core4"]+'"><img src="css/images/'+value["core4"]+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (value["core5"] != "0"?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+value["core5"]+'"><img src="css/images/'+value["core5"]+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += '</ul></div><h3><hr width="100px" color="#333" noshade="noshade" size="1" style="float:left;"/>推荐阵容<hr width="100px" color="#333" noshade="noshade" size="1" style="float:right;"/></h3>'
					+'<div class="ui-navbar"><ul class="ui-grid-d">';
			content += (value["hero1"] != "0"?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+value["hero1"]+'"><img src="css/images/'+value["hero1"]+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (value["hero2"] != "0"?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+value["hero2"]+'"><img src="css/images/'+value["hero2"]+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (value["hero3"] != "0"?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+value["hero3"]+'"><img src="css/images/'+value["hero3"]+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (value["hero4"] != "0"?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+value["hero4"]+'"><img src="css/images/'+value["hero4"]+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (value["hero5"] != "0"?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+value["hero5"]+'"><img src="css/images/'+value["hero5"]+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += '</ul></div><div class="ui-navbar"><ul class="ui-grid-d">';
			content += (value["hero6"] != "0"?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+value["hero6"]+'"><img src="css/images/'+value["hero6"]+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (value["hero7"] != "0"?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+value["hero7"]+'"><img src="css/images/'+value["hero7"]+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (value["hero8"] != "0"?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+value["hero8"]+'"><img src="css/images/'+value["hero8"]+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (value["hero9"] != "0"?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+value["hero9"]+'"><img src="css/images/'+value["hero9"]+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (value["hero10"] != "0"?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+value["hero10"]+'"><img src="css/images/'+value["hero10"]+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += '</ul></div><div class="ui-navbar"><ul class="ui-grid-d">';
			content += (value["hero11"] != "0"?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+value["hero11"]+'"><img src="css/images/'+value["hero11"]+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (value["hero12"] != "0"?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+value["hero12"]+'"><img src="css/images/'+value["hero12"]+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (value["hero13"] != "0"?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+value["hero13"]+'"><img src="css/images/'+value["hero13"]+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (value["hero14"] != "0"?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+value["hero14"]+'"><img src="css/images/'+value["hero14"]+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (value["hero15"] != "0"?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+value["hero15"]+'"><img src="css/images/'+value["hero15"]+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += '</ul></div><div class="ui-navbar"><ul class="ui-grid-d">';
			content += (value["hero16"] != "0"?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+value["hero16"]+'"><img src="css/images/'+value["hero16"]+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (value["hero17"] != "0"?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+value["hero17"]+'"><img src="css/images/'+value["hero17"]+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (value["hero18"] != "0"?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+value["hero18"]+'"><img src="css/images/'+value["hero18"]+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (value["hero19"] != "0"?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+value["hero19"]+'"><img src="css/images/'+value["hero19"]+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (value["hero20"] != "0"?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+value["hero20"]+'"><img src="css/images/'+value["hero20"]+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += '</ul></div>';
			var $el = $( content );
			$("#detailview-page .ul-wapper .ui-detailview").append($el);
		});
	}else if(action.startsWith("action=A1005")){
		herojson = json;
		action = "action=A1006";
		showPage("#listview-page");
		$("#listview-page .ui-header h1>img").attr("src", "css/images/title_hero.png");
		$("#listview-page .ul-wapper .ui-listview").empty();
		$.each(json, function (key, value) {
			var $el = $( '<li><a href="#" action="'+action+'&id='+value["id"]+'"><img src="css/images/'+value["id"]+'.png"><h2>'+value["name"]+'</h2><p>'+value["position"]+'</p></a></li>' );
			$("#listview-page .ul-wapper .ui-listview").append($el);
		});
	}else if(action.startsWith("action=A1006")){
		showPage("#detailview-page");
		action = "action=A1007";
		$("#detailview-page .ul-wapper .ui-detailview").empty();
		var hero_detail = json["hero_detail"][0];
		var article = hero_detail["intro"] == null ? "" : hero_detail["intro"];
		var content = article.split("\n").join("</p><p>");
		content = '<h2 style="border-bottom:0px">'+hero_detail["name"]+'</h2><h3><hr width="100px" color="#333" noshade="noshade" size="1" style="float:left;"/>'+hero_detail["position"]+'<hr width="100px" color="#333" noshade="noshade" size="1" style="float:right;"/></h3>'
				+'<div><img src="css/images/'+hero_detail["id"]+'.png"><div>攻：'+formatScore(hero_detail["ad"])+'</div><div>法：'+formatScore(hero_detail["ap"])+'</div><div>防：'+formatScore(hero_detail["def"])+'</div><div>上手：'+formatScore(hero_detail["handle"])
				+'</div></div><p>'+content+'</p><h3 style="margin-top: 15px;"><hr width="100px" color="#333" noshade="noshade" size="1" style="float:left;"/>出装思路<hr width="100px" color="#333" noshade="noshade" size="1" style="float:right;"/></h3><p>';
		var equip_detail = json["equip_detail"][0];
		article = equip_detail["content"] == null ? "" : equip_detail["content"];
		content += article.split("\n").join("</p><p>")+'</p>'
				+'<div class="ui-navbar"><div>平民出装</div><ul class="ui-grid-e">'
				+'<li class="ui-block-a"><a href="#" action="'+action+'&id='+equip_detail["equip1"]+'"><img src="css/images/'+equip_detail["equip1"]+'.png"></a></li>'
				+'<li class="ui-block-b"><a href="#" action="'+action+'&id='+equip_detail["equip2"]+'"><img src="css/images/'+equip_detail["equip2"]+'.png"></a></li>'
				+'<li class="ui-block-c"><a href="#" action="'+action+'&id='+equip_detail["equip3"]+'"><img src="css/images/'+equip_detail["equip3"]+'.png"></a></li>'
				+'<li class="ui-block-d"><a href="#" action="'+action+'&id='+equip_detail["equip4"]+'"><img src="css/images/'+equip_detail["equip4"]+'.png"></a></li>'
				+'<li class="ui-block-e"><a href="#" action="'+action+'&id='+equip_detail["equip5"]+'"><img src="css/images/'+equip_detail["equip5"]+'.png"></a></li>'
				+'<li class="ui-block-f"><a href="#" action="'+action+'&id='+equip_detail["equip6"]+'"><img src="css/images/'+equip_detail["equip6"]+'.png"></a></li>'
				+'</ul></div><div class="ui-navbar"><div>土豪出装</div><ul class="ui-grid-e">'
				+'<li class="ui-block-a"><a href="#" action="'+action+'&id='+equip_detail["equip7"]+'"><img src="css/images/'+equip_detail["equip7"]+'.png"></a></li>'
				+'<li class="ui-block-b"><a href="#" action="'+action+'&id='+equip_detail["equip8"]+'"><img src="css/images/'+equip_detail["equip8"]+'.png"></a></li>'
				+'<li class="ui-block-c"><a href="#" action="'+action+'&id='+equip_detail["equip9"]+'"><img src="css/images/'+equip_detail["equip9"]+'.png"></a></li>'
				+'<li class="ui-block-d"><a href="#" action="'+action+'&id='+equip_detail["equip10"]+'"><img src="css/images/'+equip_detail["equip10"]+'.png"></a></li>'
				+'<li class="ui-block-e"><a href="#" action="'+action+'&id='+equip_detail["equip11"]+'"><img src="css/images/'+equip_detail["equip11"]+'.png"></a></li>'
				+'<li class="ui-block-f"><a href="#" action="'+action+'&id='+equip_detail["equip12"]+'"><img src="css/images/'+equip_detail["equip12"]+'.png"></a></li>'
				+'</ul></div>';
		var $el = $( content );
		$("#detailview-page .ul-wapper .ui-detailview").append($el);
	}else if(action.startsWith("action=A1007")){
		showPage("#detailview-page");
		action = "action=A1007";
		$("#detailview-page .ul-wapper .ui-detailview").empty();
		$.each(json, function (key, value) {
			var article = value["des"] == null ? "" : value["des"];
			var content = article.split("\n").join("</p><p>");
			content = '<h2>'+value["name"]+'</h2><div class="ui-field"><img src="css/images/'+value["id"]+'.png"><p>'+content
					+'</p></div><div>可合成物品</div><div class="ui-navbar"><ul class="ui-grid-e">';
			article = value["target"] == null ? "" : value["target"];
			var equip = article.split(",");
			content += (equip[0] != null && equip[0] != ""?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+equip[0]+'"><img src="css/images/'+equip[0]+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (equip[1] != null && equip[1] != ""?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+equip[1]+'"><img src="css/images/'+equip[1]+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (equip[2] != null && equip[2] != ""?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+equip[2]+'"><img src="css/images/'+equip[2]+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (equip[3] != null && equip[3] != ""?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+equip[3]+'"><img src="css/images/'+equip[3]+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (equip[4] != null && equip[4] != ""?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+equip[4]+'"><img src="css/images/'+equip[4]+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += (equip[5] != null && equip[5] != ""?'<li class="ui-block-f"><a href="#" action="'+action+'&id='+equip[5]+'"><img src="css/images/'+equip[5]+'.png"></a></li>':'<li class="ui-block-f"><a></a></li>');
			content += '</ul></div><div>合成所需</div><div class="ui-navbar"><ul class="ui-grid-e">';
			article = value["origin"] == null ? "" : value["origin"];
			equip = article.split(",");
			content += (equip[0] != null && equip[0] != ""?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+equip[0]+'"><img src="css/images/'+equip[0]+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (equip[1] != null && equip[1] != ""?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+equip[1]+'"><img src="css/images/'+equip[1]+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (equip[2] != null && equip[2] != ""?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+equip[2]+'"><img src="css/images/'+equip[2]+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (equip[3] != null && equip[3] != ""?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+equip[3]+'"><img src="css/images/'+equip[3]+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (equip[4] != null && equip[4] != ""?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+equip[4]+'"><img src="css/images/'+equip[4]+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += (equip[5] != null && equip[5] != ""?'<li class="ui-block-f"><a href="#" action="'+action+'&id='+equip[5]+'"><img src="css/images/'+equip[5]+'.png"></a></li>':'<li class="ui-block-f"><a></a></li>');
			content += '</ul></div>';
			var $el = $( content );
			$("#detailview-page .ul-wapper .ui-detailview").append($el);
		});
	}else if(action.startsWith("action=A1008")){
		datajson = json;
		action = "action=A1009";
		showPage("#listview-page");
		$("#listview-page .ui-header h1>img").attr("src", "css/images/title_data.png");
		$("#listview-page .ul-wapper .ui-listview").empty();
		$.each(json, function (key, value) {
			var $el = $( '<li><a href="#" action="'+action+'&id='+value["id"]+'"><img src="css/images/'+value["id"]+'.png"><h2>'+value["title"]+'</h2><p>'+value["summary"]+'</p></a></li>' );
			$("#listview-page .ul-wapper .ui-listview").append($el);
		});
	}else if(action.startsWith("action=A1009")){
		showPage("#detailview-page");
		action = "action=A1006";
		$("#detailview-page .ul-wapper .ui-detailview").empty();
		var content = '<h2>'+ranktitle+'</h2><span class="date">2016.1.1</span><div>出场率</div><div>胜率</div><div style="float:right;right:0;"><div>出场率</div><div>胜率</div></div><ul class="ui-listview">';
		$.each(json, function (key, value) {
			content += '<li><a href="#" action="'+action+'&id='+value["heroid"]+'"><img src="css/images/'+value["heroid"]+'.png"></a><div class="ui-progress"><div class="ui-progress-r">40%</div></div></li>';
		});
		content += '</ul>';
		var $el = $( content );
		$("#detailview-page .ul-wapper .ui-detailview").append($el);
// 		[
//     {
//         "id": "1",
//         "heroid": "1",
//         "herorate": "1",
//         "herohot": "1"
//     }
// ]
	}
}

function formatScore(score) {
	var result = "";
	for (var i = 2; i <= score && i <= 10; i+=2) {
		result += "★";
	};
	if(score % 2 == 1)
		result += "☆";
	return result;
}

function updateListJson(action, isback) {
	if(action == null)
		action = "action=A1001";
	if(action.startsWith("action=A1001") && guidejson != null){
		updateListData(action, guidejson);
	}else if(action.startsWith("action=A1003") && teamjson != null){
		updateListData(action, teamjson);
	}else if(action.startsWith("action=A1005") && herojson != null){
		updateListData(action, herojson);
	}else if(action.startsWith("action=A1008") && datajson != null){
		updateListData(action, datajson);
	}else{
		$.getJSON("http://123.59.144.200/loldataserver/index.php", action, function(json){
			updateListData(action, json);
			// hideLoading();
		});
		if(!isback && action.indexOf("&") >= 0)
			backurls.push(currenturl);
	}
	currenturl = action;
}

var currenturl = "action=A1001";
var backurls = new Array();
function goBack(){
	updateListJson(backurls.pop(), true);
}

function showLoading(){
    $(".loading:hidden").show();
}

function hideLoading(){
    $(".loading").hide();
}

var ranktitle = "";
$(document).ready(function() {
	resizeContent();
	updateListJson("action=A1007&id=11003");
	$(".ui-content, .ui-footer").on('click', "a", function() {
		var action = $(this).attr("action");
		if(action != null){
			if(action.startsWith("action=A1009"))
				ranktitle = $(this).find("h2").text();
			updateListJson(action);
		}
	});
});
$(window).resize(function() {
	resizeContent();
});

function resizeContent() {
	var height = $(window).height() - 192;
	//alert(height);
	$(".ul-wapper").height(height);
	$(".ul-wapper .ui-listview").height(height);
}


