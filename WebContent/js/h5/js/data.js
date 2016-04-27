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
					+'</p><h3 style="margin-top: 15px;"><hr width="100px" color="#333" noshade="noshade" size="1" style="float:left;margin-right:-100px;"/>核心英雄<hr width="100px" color="#333" noshade="noshade" size="1" style="float:right;margin-left:-100px;"/></h3>'
					+'<div class="ui-navbar"><ul class="ui-grid-d">';
			content += (value["core1"] != "0"?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+value["core1"]+'"><img src="css/images/char'+getHeroIcon(value["core1"])+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (value["core2"] != "0"?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+value["core2"]+'"><img src="css/images/char'+getHeroIcon(value["core2"])+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (value["core3"] != "0"?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+value["core3"]+'"><img src="css/images/char'+getHeroIcon(value["core3"])+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (value["core4"] != "0"?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+value["core4"]+'"><img src="css/images/char'+getHeroIcon(value["core4"])+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (value["core5"] != "0"?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+value["core5"]+'"><img src="css/images/char'+getHeroIcon(value["core5"])+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += '</ul></div><h3><hr width="100px" color="#333" noshade="noshade" size="1" style="float:left;margin-right:-100px;"/>推荐阵容<hr width="100px" color="#333" noshade="noshade" size="1" style="float:right;margin-left:-100px;"/></h3>'
					+'<div class="ui-navbar"><ul class="ui-grid-d">';
			content += (value["hero1"] != "0"?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+value["hero1"]+'"><img src="css/images/char'+getHeroIcon(value["hero1"])+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (value["hero2"] != "0"?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+value["hero2"]+'"><img src="css/images/char'+getHeroIcon(value["hero2"])+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (value["hero3"] != "0"?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+value["hero3"]+'"><img src="css/images/char'+getHeroIcon(value["hero3"])+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (value["hero4"] != "0"?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+value["hero4"]+'"><img src="css/images/char'+getHeroIcon(value["hero4"])+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (value["hero5"] != "0"?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+value["hero5"]+'"><img src="css/images/char'+getHeroIcon(value["hero5"])+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += '</ul></div><div class="ui-navbar"><ul class="ui-grid-d">';
			content += (value["hero6"] != "0"?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+value["hero6"]+'"><img src="css/images/char'+getHeroIcon(value["hero6"])+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (value["hero7"] != "0"?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+value["hero7"]+'"><img src="css/images/char'+getHeroIcon(value["hero7"])+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (value["hero8"] != "0"?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+value["hero8"]+'"><img src="css/images/char'+getHeroIcon(value["hero8"])+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (value["hero9"] != "0"?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+value["hero9"]+'"><img src="css/images/char'+getHeroIcon(value["hero9"])+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (value["hero10"] != "0"?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+value["hero10"]+'"><img src="css/images/char'+getHeroIcon(value["hero10"])+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += '</ul></div><div class="ui-navbar"><ul class="ui-grid-d">';
			content += (value["hero11"] != "0"?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+value["hero11"]+'"><img src="css/images/char'+getHeroIcon(value["hero11"])+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (value["hero12"] != "0"?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+value["hero12"]+'"><img src="css/images/char'+getHeroIcon(value["hero12"])+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (value["hero13"] != "0"?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+value["hero13"]+'"><img src="css/images/char'+getHeroIcon(value["hero13"])+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (value["hero14"] != "0"?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+value["hero14"]+'"><img src="css/images/char'+getHeroIcon(value["hero14"])+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (value["hero15"] != "0"?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+value["hero15"]+'"><img src="css/images/char'+getHeroIcon(value["hero15"])+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += '</ul></div><div class="ui-navbar"><ul class="ui-grid-d">';
			content += (value["hero16"] != "0"?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+value["hero16"]+'"><img src="css/images/char'+getHeroIcon(value["hero16"])+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (value["hero17"] != "0"?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+value["hero17"]+'"><img src="css/images/char'+getHeroIcon(value["hero17"])+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (value["hero18"] != "0"?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+value["hero18"]+'"><img src="css/images/char'+getHeroIcon(value["hero18"])+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (value["hero19"] != "0"?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+value["hero19"]+'"><img src="css/images/char'+getHeroIcon(value["hero19"])+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (value["hero20"] != "0"?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+value["hero20"]+'"><img src="css/images/char'+getHeroIcon(value["hero20"])+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
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
			var $el = $( '<li><a href="#" action="'+action+'&id='+value["id"]+'"><img src="css/images/char'+getHeroIcon(value["id"])+'.png"  alt="'+value["id"]+'"><h2>'+value["name"]+'</h2><p>'+value["position"]+'</p></a></li>' );
			$("#listview-page .ul-wapper .ui-listview").append($el);
		});
	}else if(action.startsWith("action=A1006")){
		showPage("#detailview-page");
		action = "action=A1007";
		$("#detailview-page .ul-wapper .ui-detailview").empty();
		var hero_detail = json["hero_detail"][0];
		var article = hero_detail["intro"] == null ? "" : hero_detail["intro"];
		var content = article.split("\n").join("</p><p>");
		content = '<h2 style="border-bottom:0px">'+hero_detail["name"]+'</h2><h3><hr width="100px" color="#333" noshade="noshade" size="1" style="float:left;margin-right:-100px;"/><span>'
				+hero_detail["position"]+'</span><hr width="100px" color="#333" noshade="noshade" size="1" style="float:right;margin-left:-100px;"/></h3>'
				+'<div class="ui-field"><img src="css/images/char'+getHeroIcon(hero_detail["id"])
				+'.png"><div class="ui-score">攻：'+formatScore(hero_detail["ad"])+'</div><div class="ui-score">法：'+formatScore(hero_detail["ap"])
				+'</div><div class="ui-score">防：'+formatScore(hero_detail["def"])+'</div><div class="ui-score">上手：'+formatScore(hero_detail["handle"])
				+'</div></div><p>'+content+'</p><h3 style="margin-top: 15px;"><hr width="100px" color="#333" noshade="noshade" size="1" style="float:left;margin-right:-100px;"/>出装思路<hr width="100px" color="#333" noshade="noshade" size="1" style="float:right;margin-left:-100px;"/></h3><p>';
		var equip_detail = json["equip_detail"][0];
		article = equip_detail["content"] == null ? "" : equip_detail["content"];
		content += article.split("\n").join("</p><p>")+'</p>'
				+'<div class="ui-navbar"><div>平民出装</div><ul class="ui-grid-e">'
				+'<li class="ui-block-a"><a href="#" action="'+action+'&id='+equip_detail["equip1"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip1"])+'.png"></a></li>'
				+'<li class="ui-block-b"><a href="#" action="'+action+'&id='+equip_detail["equip2"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip2"])+'.png"></a></li>'
				+'<li class="ui-block-c"><a href="#" action="'+action+'&id='+equip_detail["equip3"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip3"])+'.png"></a></li>'
				+'<li class="ui-block-d"><a href="#" action="'+action+'&id='+equip_detail["equip4"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip4"])+'.png"></a></li>'
				+'<li class="ui-block-e"><a href="#" action="'+action+'&id='+equip_detail["equip5"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip5"])+'.png"></a></li>'
				+'<li class="ui-block-f"><a href="#" action="'+action+'&id='+equip_detail["equip6"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip6"])+'.png"></a></li>'
				+'</ul></div><div class="ui-navbar"><div>土豪出装</div><ul class="ui-grid-e">'
				+'<li class="ui-block-a"><a href="#" action="'+action+'&id='+equip_detail["equip7"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip7"])+'.png"></a></li>'
				+'<li class="ui-block-b"><a href="#" action="'+action+'&id='+equip_detail["equip8"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip8"])+'.png"></a></li>'
				+'<li class="ui-block-c"><a href="#" action="'+action+'&id='+equip_detail["equip9"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip9"])+'.png"></a></li>'
				+'<li class="ui-block-d"><a href="#" action="'+action+'&id='+equip_detail["equip10"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip10"])+'.png"></a></li>'
				+'<li class="ui-block-e"><a href="#" action="'+action+'&id='+equip_detail["equip11"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip11"])+'.png"></a></li>'
				+'<li class="ui-block-f"><a href="#" action="'+action+'&id='+equip_detail["equip12"]+'"><img src="css/images/equipment/'+getIcon(equip_detail["equip12"])+'.png"></a></li>'
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
			content = '<h2>'+value["name"]+'</h2><div class="ui-field"><img src="css/images/equipment/'+getIcon(value["id"])+'.png"><p>'+content
					+'</p></div><div>可合成物品</div><div class="ui-navbar"><ul class="ui-grid-e">';
			article = value["target"] == null ? "" : value["target"];
			var equip = $.trim(article).split(",");
			content += (equip[0] != null && equip[0] != ""?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+equip[0]+'"><img src="css/images/equipment/'+getIcon(equip[0])+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (equip[1] != null && equip[1] != ""?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+equip[1]+'"><img src="css/images/equipment/'+getIcon(equip[1])+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (equip[2] != null && equip[2] != ""?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+equip[2]+'"><img src="css/images/equipment/'+getIcon(equip[2])+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (equip[3] != null && equip[3] != ""?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+equip[3]+'"><img src="css/images/equipment/'+getIcon(equip[3])+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (equip[4] != null && equip[4] != ""?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+equip[4]+'"><img src="css/images/equipment/'+getIcon(equip[4])+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += (equip[5] != null && equip[5] != ""?'<li class="ui-block-f"><a href="#" action="'+action+'&id='+equip[5]+'"><img src="css/images/equipment/'+getIcon(equip[5])+'.png"></a></li>':'<li class="ui-block-f"><a></a></li>');
			var i = 6;
			while(equip.length > i){
				content += '</ul></div><div class="ui-navbar"><ul class="ui-grid-e">';
				content += (equip[i+0] != null && equip[i+0] != ""?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+equip[i+0]+'"><img src="css/images/equipment/'+getIcon(equip[i+0])+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
				content += (equip[i+1] != null && equip[i+1] != ""?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+equip[i+1]+'"><img src="css/images/equipment/'+getIcon(equip[i+1])+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
				content += (equip[i+2] != null && equip[i+2] != ""?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+equip[i+2]+'"><img src="css/images/equipment/'+getIcon(equip[i+2])+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
				content += (equip[i+3] != null && equip[i+3] != ""?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+equip[i+3]+'"><img src="css/images/equipment/'+getIcon(equip[i+3])+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
				content += (equip[i+4] != null && equip[i+4] != ""?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+equip[i+4]+'"><img src="css/images/equipment/'+getIcon(equip[i+4])+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
				content += (equip[i+5] != null && equip[i+5] != ""?'<li class="ui-block-f"><a href="#" action="'+action+'&id='+equip[i+5]+'"><img src="css/images/equipment/'+getIcon(equip[i+5])+'.png"></a></li>':'<li class="ui-block-f"><a></a></li>');
				i += 6;
			}
			content += '</ul></div><div>合成所需</div><div class="ui-navbar"><ul class="ui-grid-e">';
			article = value["origin"] == null ? "" : value["origin"];
			equip = $.trim(article).split(",");
			content += (equip[0] != null && equip[0] != ""?'<li class="ui-block-a"><a href="#" action="'+action+'&id='+equip[0]+'"><img src="css/images/equipment/'+getIcon(equip[0])+'.png"></a></li>':'<li class="ui-block-a"><a></a></li>');
			content += (equip[1] != null && equip[1] != ""?'<li class="ui-block-b"><a href="#" action="'+action+'&id='+equip[1]+'"><img src="css/images/equipment/'+getIcon(equip[1])+'.png"></a></li>':'<li class="ui-block-b"><a></a></li>');
			content += (equip[2] != null && equip[2] != ""?'<li class="ui-block-c"><a href="#" action="'+action+'&id='+equip[2]+'"><img src="css/images/equipment/'+getIcon(equip[2])+'.png"></a></li>':'<li class="ui-block-c"><a></a></li>');
			content += (equip[3] != null && equip[3] != ""?'<li class="ui-block-d"><a href="#" action="'+action+'&id='+equip[3]+'"><img src="css/images/equipment/'+getIcon(equip[3])+'.png"></a></li>':'<li class="ui-block-d"><a></a></li>');
			content += (equip[4] != null && equip[4] != ""?'<li class="ui-block-e"><a href="#" action="'+action+'&id='+equip[4]+'"><img src="css/images/equipment/'+getIcon(equip[4])+'.png"></a></li>':'<li class="ui-block-e"><a></a></li>');
			content += (equip[5] != null && equip[5] != ""?'<li class="ui-block-f"><a href="#" action="'+action+'&id='+equip[5]+'"><img src="css/images/equipment/'+getIcon(equip[5])+'.png"></a></li>':'<li class="ui-block-f"><a></a></li>');
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
		raterank = json;
		rankRate(rankrate);
	}
}


var raterank = [];
function switchRate(){
	if(rankrate == "herohot")
		rankRate("herorate");
	else
		rankRate("herohot");
}

var rankrate = "herohot";
function rankRate(rate) {
	rankrate = rate;
	var action = "action=A1006";
	$("#detailview-page .ul-wapper .ui-detailview").empty();
	var content = '<h2>'+ranktitle+'</h2><span class="date">2016.1.1</span><div style="margin:12px 8px;font-size: 14px;">'
			+'<span style="margin-left:30px;">出场率<span style="display:inline-block;margin-left:5px;width:10px;height:10px;background-color: #F00;"></span></span>'
			+'<span style="margin-left: 20px;">胜率<span style="display:inline-block;margin-left:5px;width:10px;height:10px;background-color: #00F;"></span></span>'
			+'<a href="#" onclick="switchRate();" style="float:right;margin-right:10px;">';
	if(rate == "herorate"){
		raterank.sort(function(a, b){return b["herorate"]-a["herorate"];});
		content += '<span class="ui-hot">出场率</span><span class="ui-rate ui-rate-active">胜率</span></a></div><ul class="ui-ratelist ui-listview">';
	}else{
		raterank.sort(function(a, b){return b["herohot"]-a["herohot"];});
		content += '<span class="ui-hot ui-rate-active">出场率</span><span class="ui-rate">胜率</span></a></div><ul class="ui-ratelist ui-listview">';
	}
	
	$.each(raterank, function (key, value) {
		content += '<li style="background-image:none;"><a href="#" action="'+action+'&id='+value["heroid"]+'"><img src="css/images/char'+getHeroIcon(value["heroid"])
		+'.png"><div class="ui-progress" style="margin-top:10px;"><div class="ui-progress-r" style="width:'+(value["herohot"]*2)+'%;"></div><span>'+value["herohot"]
		+'%</span></div><div class="ui-progress"><div class="ui-progress-b" style="width:'+(value["herorate"]*0.75)+'%;"></div><span>'+value["herorate"]+'%</span></div></a></li>';
	});
	content += '</ul>';
	var $el = $( content );
	$("#detailview-page .ul-wapper .ui-detailview").append($el);
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
		if(!isback) $(".ul-wapper .ui-listview:visible").scrollTop(0);
	}else if(action.startsWith("action=A1003") && teamjson != null){
		updateListData(action, teamjson);
		if(!isback) $(".ul-wapper .ui-listview:visible").scrollTop(0);
	}else if(action.startsWith("action=A1005") && herojson != null){
		updateListData(action, herojson);
		if(!isback) $(".ul-wapper .ui-listview:visible").scrollTop(0);
	}else if(action.startsWith("action=A1008") && datajson != null){
		updateListData(action, datajson);
		if(!isback) $(".ul-wapper .ui-listview:visible").scrollTop(0);
	}else{
		$.getJSON("http://123.59.144.200/loldataserver/index.php", action, function(json){
			updateListData(action, json);
			$(".ul-wapper .ui-detailview:visible").scrollTop(0);
			if(!isback) $(".ul-wapper .ui-listview:visible").scrollTop(0);
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

function share(){
	
}

function showLoading(){
    $(".loading:hidden").show();
}

function hideLoading(){
    $(".loading").hide();
}

var ranktitle = "热度排行";
$(document).ready(function() {
	resizeContent();
	updateListJson("action=A1001");
	$(".ui-content, .ui-footer").on('click', "a", function() {
		var action = $(this).attr("action");
		if(action != null && !action.startsWith("action=A1007&id=2")){
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
	$(".ul-wapper .ui-listview, .ul-wapper .ui-detailview").height(height);
}

String.prototype.startsWith = function(str) {
	if (this.substr(0, str.length) == str)
		return true;
	return false;
};

function getHeroIcon(id){
	return heroicons[id];
}
var heroicons = {"1":46,
"5":9,
"8":47,
"9":28,
"11":1,
"13":2,
"17":29,
"27":38,
"32":48,
"35":19,
"36":20,
"38":12,
"39":11,
"40":30,
"41":39,
"42":31,
"43":4,
"48":40,
"50":41,
"51":13,
"52":42,
"55":21,
"58":14,
"59":22,
"61":15,
"63":16,
"70":32,
"72":5,
"74":6,
"75":44,
"76":7,
"78":45,
"79":33,
"82":17,
"84":49,
"86":23,
"88":24,
"96":50,
"97":34,
"105":26,
"108":8,
"117":36,
"121":27,
"10":18,
"12":10,
"24":3,
"60":43,
"94":25,
"102":51,
"104":52,
"116":35,
"122":37}

function getIcon(id){
	return icons[id];
}
var icons = {"10000":10017,
"10001":10006,
"10002":10001,
"10003":10002,
"10004":10003,
"10005":10013,
"10006":10015,
"10007":10014,
"10008":10006,
"10009":10001,
"10010":10002,
"10011":10003,
"10012":10013,
"10013":10015,
"10014":10014,
"11001":11029,
"11002":11029,
"11003":11014,
"11004":11014,
"11005":11008,
"11006":11008,
"11007":11028,
"11008":11028,
"11009":10003,
"11010":10003,
"11011":11019,
"11012":11019,
"11013":11020,
"11014":11020,
"11015":11021,
"11016":11021,
"11017":11022,
"11018":11022,
"11019":11023,
"11020":11023,
"11021":10001,
"11022":11011,
"11023":11024,
"11024":11024,
"11025":12002,
"11026":12002,
"11027":11032,
"11028":11032,
"11029":10002,
"11030":10002,
"11031":11015,
"11032":11015,
"11033":10015,
"11034":10015,
"11035":10001,
"11036":11037,
"11037":11038,
"11038":11038,
"11039":11039,
"11040":11039,
"11041":10014,
"11042":10014,
"11043":10006,
"11044":10006,
"11045":10013,
"11046":10013,
"11047":11040,
"11048":11040,
"11049":11037,
"12001":12011,
"12002":12011,
"12003":12011,
"12004":12018,
"12005":12018,
"12006":12018,
"12007":12016,
"12008":12016,
"12009":12016,
"12010":12017,
"12011":12017,
"12012":12017,
"12013":12020,
"12014":12020,
"12015":12020,
"12016":11028,
"12017":11028,
"12018":11013,
"12019":11013,
"12020":11013,
"12021":12001,
"12022":12001,
"12023":12001,
"12024":12010,
"12025":12010,
"12026":12010,
"12027":10003,
"12028":10003,
"12029":10003,
"12030":11030,
"12031":11030,
"12032":11030,
"12034":11031,
"12035":11031,
"12036":11031,
"12037":11019,
"12038":11019,
"12039":11019,
"12040":11020,
"12041":11020,
"12042":11020,
"12043":11021,
"12044":11021,
"12045":11021,
"12046":11022,
"12047":11022,
"12048":11022,
"12049":11023,
"12050":11023,
"12051":11023,
"12052":12029,
"12053":12029,
"12054":12029,
"12056":13041,
"12057":13041,
"12058":12014,
"12059":12014,
"12060":12012,
"12061":12012,
"12062":12012,
"12063":12009,
"12064":12009,
"12065":12009,
"12066":12002,
"12067":12002,
"12068":11032,
"12069":11032,
"12070":12005,
"12071":12005,
"12072":12005,
"12073":12021,
"12074":12021,
"12075":12021,
"12076":10002,
"12077":10002,
"12078":10002,
"12079":11015,
"12080":11015,
"12081":11015,
"12082":10015,
"12083":10015,
"12084":10015,
"12085":12043,
"12086":12043,
"12087":12043,
"12088":10001,
"12089":10001,
"12090":10001,
"12091":11037,
"12092":11037,
"12093":12044,
"12094":12044,
"12095":12044,
"12096":11038,
"12097":11038,
"12098":11039,
"12099":11039,
"12100":10014,
"12101":10014,
"12102":10014,
"12103":10006,
"12104":10006,
"12105":10006,
"12106":10013,
"12107":10013,
"12108":10013,
"12109":11040,
"12110":11040,
"12111":12045,
"12112":12045,
"12113":12045,
"12114":12046,
"12115":12046,
"12116":12046,
"12117":10003,
"12118":10006,
"12119":10006,
"12120":10002,
"12121":10003,
"12122":10003,
"12123":10003,
"12124":10003,
"12125":10006,
"12126":10002,
"12127":10002,
"12128":10013,
"12129":10002,
"12130":10014,
"12131":10001,
"12132":10013,
"12133":10003,
"12134":10002,
"12135":10006,
"12136":10015,
"13001":13027,
"13002":13027,
"13003":13051,
"13004":13051,
"13005":13032,
"13006":13032,
"13007":13032,
"13008":13031,
"13009":13031,
"13010":13031,
"13011":13031,
"13012":12017,
"13013":12017,
"13014":12017,
"13015":12020,
"13016":12020,
"13017":13013,
"13018":13013,
"13019":13013,
"13020":13052,
"13021":13052,
"13022":13052,
"13023":13008,
"13024":13008,
"13025":13008,
"13026":13011,
"13027":13011,
"13028":13011,
"13029":13001,
"13030":13001,
"13031":13025,
"13032":13025,
"13033":13025,
"13034":13017,
"13035":13017,
"13036":13017,
"13037":13015,
"13038":13015,
"13039":13015,
"13040":13007,
"13041":13007,
"13042":13007,
"13043":13012,
"13044":13012,
"13045":13012,
"13046":13018,
"13047":13018,
"13048":13018,
"13049":13003,
"13050":13003,
"13051":13005,
"13052":13005,
"13053":13005,
"13054":12036,
"13055":12036,
"13056":12036,
"13057":11019,
"13058":11019,
"13059":11019,
"13060":11020,
"13061":11020,
"13062":11020,
"13063":11021,
"13064":11021,
"13065":11021,
"13066":11022,
"13067":11022,
"13068":11022,
"13069":11023,
"13070":11023,
"13071":11023,
"13072":12029,
"13073":12029,
"13074":13034,
"13075":13034,
"13076":13034,
"13078":13041,
"13079":13041,
"13080":12014,
"13081":12014,
"13082":12014,
"13083":12012,
"13084":13014,
"13085":13014,
"13086":13014,
"13087":13004,
"13088":13004,
"13089":13016,
"13090":13016,
"13092":13002,
"13093":13009,
"13094":13009,
"13095":13009,
"13096":13020,
"13097":13020,
"13098":13028,
"13099":13028,
"13100":13028,
"13101":13010,
"13102":13010,
"13103":13029,
"13104":13029,
"13105":13019,
"13106":13019,
"13107":13019,
"13108":13023,
"13109":13023,
"13110":13023,
"13111":13006,
"13112":13006,
"13113":13006,
"13114":12037,
"13115":12037,
"13116":12037,
"13117":12015,
"13118":12015,
"13119":12015,
"13120":11025,
"13121":11025,
"13122":11025,
"13123":11026,
"13124":11026,
"13125":11026,
"13126":11027,
"13127":11027,
"13128":11027,
"13129":10003,
"13130":10003,
"13131":10003,
"13132":13068,
"13133":13068,
"13134":13068,
"13135":13069,
"13136":13069,
"13137":13069,
"13138":13070,
"13139":13070,
"13140":13070,
"13141":13071,
"13142":13071,
"13143":13072,
"13144":13072,
"13145":10001,
"13146":10001,
"13147":10001,
"13148":13073,
"13149":13073,
"13150":13074,
"13151":13074,
"13152":11037,
"13153":11037,
"13154":13075,
"13155":13075,
"13156":13075,
"13157":10002,
"13158":10002,
"13159":10002,
"13160":12044,
"13161":12044,
"13162":11038,
"13163":11038,
"13164":11039,
"13165":11039,
"13166":10014,
"13167":10014,
"13168":10014,
"13169":13076,
"13170":13076,
"13171":13076,
"13172":10006,
"13173":10006,
"13174":10006,
"13175":13077,
"13176":13077,
"13177":13077,
"13178":10013,
"13179":10013,
"13180":10013,
"13181":13078,
"13182":13078,
"13183":13078,
"13184":13079,
"13185":13079,
"13186":13079,
"13187":11040,
"13188":11040,
"13189":10015,
"13190":10015,
"13191":10015,
"13192":13080,
"13193":13080,
"13194":13080,
"13195":13081,
"13196":13081,
"13197":13082,
"13198":13082,
"13199":13082,
"13200":13083,
"13201":13083,
"13202":13083,
"13203":10014,
"13204":10001,
"13205":10013,
"13206":10002,
"13207":10003,
"13208":10002,
"13209":10003,
"13210":10002,
"13211":10002,
"13212":10006,
"13213":10015,
"13214":10003,
"13215":10014,
"13216":10006,
"13901":13002,
"14001":14024,
"14002":13051,
"14003":14031,
"14004":14030,
"14005":12020,
"14006":14001,
"14007":13052,
"14008":14007,
"14009":14006,
"14010":14002,
"14011":14022,
"14012":14016,
"14013":14014,
"14014":14014,
"14015":14010,
"14016":14011,
"14017":14017,
"14018":13003,
"14019":13005,
"14020":12036,
"14021":12036,
"14022":14044,
"14023":12024,
"14024":14045,
"14025":13036,
"14026":14046,
"14027":12025,
"14028":14047,
"14029":13037,
"14030":14042,
"14031":12026,
"14032":14043,
"14033":13038,
"14034":14048,
"14035":12027,
"14036":14049,
"14037":13039,
"14038":14050,
"14039":12028,
"14040":14051,
"14041":13040,
"14042":12029,
"14043":14029,
"14044":14064,
"14045":14028,
"14046":14027,
"14047":14013,
"14048":14009,
"14049":14009,
"14050":13016,
"14051":13002,
"14052":14008,
"14053":14008,
"14054":14019,
"14055":14025,
"14056":14015,
"14057":14012,
"14058":14004,
"14059":14020,
"14060":14020,
"14061":14005,
"14062":12037,
"14063":12015,
"14064":14052,
"14065":14053,
"14066":14054,
"14067":14055,
"14068":14056,
"14069":14057,
"14070":14058,
"14071":14059,
"14072":14060,
"14073":14061,
"14074":14062,
"14075":14063,
"14076":10003,
"14077":13068,
"14078":13068,
"14079":13069,
"14080":13069,
"14081":13070,
"14082":13070,
"14083":13071,
"14084":13071,
"14085":13072,
"14086":13072,
"14087":10001,
"14088":13073,
"14089":13073,
"14090":13074,
"14091":13074,
"14092":14071,
"14093":13075,
"14094":14072,
"14095":10002,
"14096":14073,
"14097":14074,
"14098":11039,
"14099":14075,
"14100":10014,
"14101":13076,
"14102":10006,
"14103":13077,
"14104":13077,
"14105":13077,
"14106":10013,
"14107":13078,
"14108":14076,
"14109":13079,
"14110":14077,
"14111":14078,
"14112":10015,
"14113":13080,
"14114":13080,
"14115":14079,
"14116":14080,
"14117":14080,
"14118":14081,
"14119":14082,
"14120":10006,
"14121":10002,
"14122":10002,
"14123":10002,
"14124":10002,
"14125":10002,
"14126":10001,
"14127":10006,
"14128":10001,
"14129":10006,
"14130":10014,
"14131":10015,
"14132":10014,
"14133":10001,
"14134":10014,
"14135":10002,
"14136":10001,
"14137":10014,
"14138":10006,
"14139":10014,
"14140":10001,
"15001":10016,
"15002":10012,
"15003":10004,
"15004":10005,
"15005":10007,
"16001":11002,
"16002":11003,
"16003":11004,
"16004":11005,
"16005":11006,
"16006":11012,
"16007":11009,
"16008":11010,
"16009":11016,
"16010":11017,
"16011":11018,
"17001":11007,
"17002":12004,
"17003":12006,
"17004":12007,
"17005":12008,
"17006":12013,
"17007":12022,
"17008":12030,
"17009":12031,
"17010":12032,
"17011":12033,
"17012":12034,
"18001":13053,
"18002":13042,
"18003":13043,
"18004":13044,
"18005":13045,
"18006":13046,
"18007":13047,
"18008":13048,
"18009":13049,
"18010":13050,
"19001":14032,
"19002":14033,
"19003":14034,
"19004":14035,
"19005":14036,
"19006":14037,
"19007":14038,
"19008":14039,
"19009":14040,
"19010":14041,
"19011":14068,
"19012":14066,
"19013":14065,
"19014":14067,
"27001":10025,
"27002":10026,
"27003":10027,
"27004":12007,
"27005":12008,
"27008":12030,
"27009":12031,
"27010":12032,
"27011":12033,
"27012":12034,
"27013":10018,
"27014":10019,
"27015":10020,
"27016":10021,
"27017":10022,
"27018":10023,
"27019":10024,
"28001":13053,
"28002":13042,
"28003":13043,
"28004":13044,
"28005":13045,
"28006":13046,
"28007":13047,
"28008":13048,
"28009":13049,
"28010":13050,
"29001":14032,
"29002":14033,
"29003":14034,
"29004":14035,
"29005":14036,
"29006":14037,
"29007":14038,
"29008":14039,
"29009":14040,
"29010":14041,
"29012":14068,
"29013":14066,
"29014":14065,
"29015":14067,
"29016":13054,
"29017":13055,
"29018":13056,
"29019":12038,
"29020":13057,
"29021":13058,
"29022":11033,
"29023":12039,
"29024":13059,
"29025":14069,
"29026":13060,
"29027":11034,
"29028":11035,
"29029":12040,
"29030":13061,
"29031":11041,
"29032":12041,
"29033":13062,
"29034":13063,
"29035":13064,
"29036":13065,
"29037":11036,
"29038":12042,
"29039":13066,
"29040":13067,
"29041":14070,
"20042":12047,
"20043":12048,
"20044":12049,
"20045":12050,
"20046":12051,
"20047":12052,
"20048":12053,
"20049":12054,
"20050":12055,
"20051":12056,
"20052":12057,
"20053":12058,
"20054":12059,
"20055":12060,
"20056":12061,
"20057":12062,
"20058":12063,
"20059":12064,
"20060":12065,
"20061":12066,
"20062":12067,
"20063":12068,
"20064":12069,
"20065":12070,
"20066":12071,
"20067":12072,
"29068":12073}
