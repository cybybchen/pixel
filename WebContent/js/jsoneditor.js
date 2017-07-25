var json = {
    "data1":{
    "string": "foo",
    "number": 5,
    "array": [1, 2, 3],
    "object": {
        "property": "value",
        "subobj": {
            "arr": ["foo", "ha"],
            "numero": 1
        }
    }}
};

function showReloadBtn(){
    dom = $(".reload-btn:hidden");
    dom.show();
    dom.next().hide();
}

function hideReloadBtn(dom){
    dom = dom.find(".reload-btn");
    dom.hide();
    dom.next().show();
}

function updateJSON(dom, data) {
    var val;
    if(data == null){//update json view
        val = $(dom).val();
        if (val) {
            try { json = JSON.parse(val); }
            catch (e) { alert('Error in parsing json. ' + e); }
        } else {
            json = {};
        }
        $(dom).prev().jsonEditor(json, { change: updateJSON, propertyclick: showPath });
    }else{//update json text
        var div = $(dom);
        var count = 10;
        while(!div.hasClass("json-editor")){
            if(count-- < 0)
                return;
            div = $(div).parent();
        }
        div = div.next();
        val = JSON.stringify(data);
        div.val(val);
    }
}

function showPath(path) {
    $('#path').text(path);
}

var userid = 0;
var username = "";
var serverid = 0;
//////////////user/////////////////
function addNewUserTab() {
    userid = $('input[name="userid"]:visible').val();
    username = $('input[name="username"]:visible').val();
    serverid = $('input[name="serverid"]:visible').val();
    if(userid){
    	username = 0;
    	serverid = 0;
    }
    requestUserJson(userid, username, serverid);
}

/*set global userid before called*/
function addUserTab(){
    var checked = false;
    var navs =  $(".nav-userbtn");
    for (var i =navs.length - 1; i >= 0; i--) {
     if($(navs[i]).attr("userid") == userid){
         checked = true;
         if(!$(navs[i]).hasClass("nav-btn-active")){
             navs.removeClass("nav-btn-active");
             $(navs[i]).addClass("nav-btn-active");
         }
         break;
     }
    }
    if(!checked){
        var text = "S"+serverid+"."+username;
        var $el = $( '<a href="#" userid="'+userid+'" title="userId:'+userid+'" onclick="requestUserJson('+userid+', \''+username+'\', '+serverid+')" class="nav-userbtn nav-btn ui-btn ui-btn-inline">' + text + '</a>' );
        $("#new-usertab").after($el);
        $el.buttonMarkup();
        $( "#user-controlgroup" ).controlgroup( "refresh" );
        $(".nav-userbtn").removeClass("nav-btn-active");
        $($(".nav-userbtn")[0]).addClass("nav-btn-active");
    }

    $("#current-userid").text("userId: "+userid);
}

function closeUserTab(id){
    var navs =  $(".nav-userbtn");
    for (var i =navs.length - 1; i >= 0; i--) {
     if($(navs[i]).attr("userid") == id){
         $(navs[i]).remove();
         break;
     }
    }
}

function popupUsersPanel(){
    var content = "";
    $.each($(".nav-userbtn"), function () {
        var id = $(this).attr("userid");
        content += '<li class="ui-li-has-alt"><a href="#" userid="'+id+'" title="userId:'+id+'" onclick="requestUserJson('+id+')" class="ui-btn">'+$(this).text()+'</a><a href="#" userid="'+id+'" onclick="closeUserTab('+id+')" class="ui-btn ui-btn-icon-notext ui-icon-delete" title="close '+id+'"></a></li>'
    });
    if(content == "")
        content = '<li><a href="#" class="ui-btn">Empty</a></li>';
    else{
        $("#users-panel ul").html(content);
        $("#users-panel").popup('open');
    }
}

function appendUserData(key, value){
    var editor;
    if($(".json-editor-title[key="+key+"]").length > 0){
        editor = $(".json-editor-title[key="+key+"]").parent();
    }else{
        editor = $("#jsoneditor jsoneditor").clone();
        editor.appendTo("#user-editor");
    }
    editor.find(".json-editor-title span").text(key);
    editor.find(".json-editor-title").attr("key", key);
    editor.find(".json-editor").jsonEditor(value, { change: updateJSON, propertyclick: showPath });
    editor.find(".json").val(JSON.stringify(value));
}

function updateUserJson(jsondata) {
    $.ajax({
        type: "POST",
        url: "datamanager",
        contentType: "application/json; charset=utf-8",
        data: /*JSON.stringify(GetJsonData())*/
            JSON.stringify(jsondata),
        dataType: "json",
        success: function (message) {
            // json = message;
            appendUserDatas(message);
            showReloadBtn();
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            alert("提交数据失败："+XMLHttpRequest.status);
            showReloadBtn();
        }
    });
}

function quickReward(itemid, count) {
    $('input[name="rewardid"]:visible').val(itemid);
    $('input[name="rewardcount"]:visible').val(count);
}

function fillRewardId(item){
    $("#rewardid-input").val($(item).attr("itemid"));
}

function doReward(){
	var rewardId = $('input[name="rewardid"]:visible').val();
	var rewardCount = $('input[name="rewardcount"]:visible').val();
    var mailContent = $.trim($('input[name="mailcontent"]:visible').val());
	var data = buildUserJson("rewardId", rewardId);
	data["rewardCount"] = rewardCount;
    data["mailContent"] = mailContent;
	var datatype = $("#user-nav .nav-btn-active").attr("data-type");
    if(datatype == "base"){
    	data["UserData"] = 1;
    }
    updateUserJson(data);
}

function reloadUserJson(jsondata) {
    updateUserJson(jsondata);
}

function deleteUserJson(jsondata) {
    updateUserJson(jsondata);
}

function resetUserJson(jsondata) {
    updateUserJson(jsondata);
}

function requestUserJson(myuserid, myusername, myserverid) {
    if(myuserid != null || myserverid != null){
        userid = myuserid;
        username = myusername;
        serverid = myserverid;
    }
    // if(userid == 0 && serverid == 0)
    //     return;
    $.ajax({
        type: "POST",
        url: "datamanager",
        contentType: "application/json; charset=utf-8",
        data: /*JSON.stringify(GetJsonData())*/
            JSON.stringify(buildUserJson()),
        dataType: "json",
        success: function (message) {
            json = message;
            appendUserDatas(message, true);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
        	alert("提交数据失败："+XMLHttpRequest.status);
        }
    });
}

function buildUserJson(key, value){
    var json = {};
    json["userId"] = userid;
    if(username)
    	json["userName"] = username;
    json["serverId"] = serverid;
    json["session"] = session;
    if(key != null){
        if(value != null)
            json[key] = value;
        else
            json[key] = 1;
    }else{
        var datatype = $("#user-nav .nav-btn-active").attr("data-type");
        if(datatype == "base"){
            json["userData"] = 1;
            // json["UserLevel"] = 1;
            json["Event"] = 1;
            json["team"] = 1;
            json["teamCache"] = 1;
            json["achieve"] = 1;
            json["LibaoCount"] = 1;
        }else if(datatype == "hero"){
            json["hero"] = 1;
            json["pokede"] = 1;
        }else if(datatype == "equip"){
            json["equip"] = 1;
            json["prop"] = 1;
            json["equippokede"] = 1;
        }else if(datatype == "pvp"){
            json["rewardTask"] = 1;
            json["pvpMonster"] = 1;
            json["pvpMine"] = 1;
            json["pvpBuff"] = 1;
            json["areaMonster"] = 1;
            json["areaBossTime"] = 1;
            json["areaEquip"] = 1;
            json["areaBuff"] = 1;
        } else if (datatype == "talent") {
        		json["talent"] = 1;
            json["talentskill"] = 1;
        } else{
            json["mailList0"] = 1;
            json["mailList1"] = 1;
            json["mailList2"] = 1;
            json["mailList3"] = 1;
            json["mailList4"] = 1;
            json["mailList5"] = 1;
            json["mailList6"] = 1;
            json["mailList7"] = 1;
            json["mailList8"] = 1;
            json["mailList9"] = 1;
            json["friendList"] = 1;
            // json["DAILYSHOP"] = 1;
            // json["BLACKSHOP"] = 1;
            // json["UNIONSHOP"] = 1;
            // json["PVPSHOP"] = 1;
            // json["EXPEDITIONSHOP"] = 1;
            // json["LADDERSHOP"] = 1; 
        }
    }
    return json;
}

function appendUserDatas(message, dirty){
    if(message["error"]!=null){
        alert("ERROR:"+message["error"]);
        if(message["error"].match("Please login") != null)
        	document.location.href='login.jsp';
        return;
    }
    if(dirty)
        $("#user-editor").empty();
    if(message["success"]!=null){
    	$("#msg-popup").html(message["success"]);
	    $("#msg-popup").popup('open');
	}
    userid = message["userId"];
    username = message["userName"];
    serverid = message["serverId"];
    addUserTab();
    if(message["UserData"]!=null){
        appendUserData("UserData", message["UserData"]);
    }
    if(message["UserLevel"]!=null){
        appendUserData("UserLevel", message["UserLevel"]);
    }
    if(message["Event"]!=null){
        appendUserData("Event", message["Event"]);
    }
    if(message["hero"]!=null){
        appendUserData("hero", message["hero"]);
    }
    if(message["pokede"]!=null){
        appendUserData("pokede", message["pokede"]);
    }
    if(message["equip"]!=null){
        appendUserData("equip", message["equip"]);
    }
    if(message["prop"]!=null){
        appendUserData("prop", message["prop"]);
    }
    if(message["equippokede"]!=null){
        appendUserData("equippokede", message["equippokede"]);
    }
    if(message["LevelRecord"]!=null){
        appendUserData("LevelRecord", message["LevelRecord"]);
    }
    if(message["LootLevel"]!=null){
        appendUserData("LootLevel", message["LootLevel"]);
    }
    if(message["teamCache"]!=null){
        appendUserData("teamCache", message["teamCache"]);
    }
    if(message["team"]!=null){
        appendUserData("team", message["team"]);
    }
    if(message["LibaoCount"]!=null){
        appendUserData("LibaoCount", message["LibaoCount"]);
    }
    if(message["achieve"]!=null){
        appendUserData("achieve", message["achieve"]);
    }
    if(message["rewardTask"]!=null){
        appendUserData("rewardTask", message["rewardTask"]);
    }
    if(message["pvpMonster"]!=null){
        appendUserData("pvpMonster", message["pvpMonster"]);
    }
    if(message["pvpMine"]!=null){
        appendUserData("pvpMine", message["pvpMine"]);
    }
    if(message["pvpBuff"]!=null){
        appendUserData("pvpBuff", message["pvpBuff"]);
    }
    if(message["areaMonster"]!=null){
        appendUserData("areaMonster", message["areaMonster"]);
    }
    if(message["areaBossTime"]!=null){
        appendUserData("areaBossTime", message["areaBossTime"]);
    }
    if(message["areaEquip"]!=null){
        appendUserData("areaEquip", message["areaEquip"]);
    }
    if(message["areaBuff"]!=null){
        appendUserData("areaBuff", message["areaBuff"]);
    }
    if(message["talent"]!=null){
        appendUserData("talent", message["talent"]);
    }
    if(message["talentskill"]!=null){
        appendUserData("talentskill", message["talentskill"]);
    }
    if(message["mailList0"]!=null){
        appendUserData("mailList0", message["mailList0"]);
    }
    if(message["mailList1"]!=null){
        appendUserData("mailList1", message["mailList1"]);
    }
    if(message["mailList2"]!=null){
        appendUserData("mailList2", message["mailList2"]);
    }
    if(message["mailList3"]!=null){
        appendUserData("mailList3", message["mailList3"]);
    }
    if(message["mailList4"]!=null){
        appendUserData("mailList4", message["mailList4"]);
    }
    if(message["mailList5"]!=null){
        appendUserData("mailList5", message["mailList5"]);
    }
    if(message["mailList6"]!=null){
        appendUserData("mailList6", message["mailList6"]);
    }
    if(message["mailList7"]!=null){
        appendUserData("mailList7", message["mailList7"]);
    }
    if(message["mailList8"]!=null){
        appendUserData("mailList8", message["mailList8"]);
    }
    if(message["mailList9"]!=null){
        appendUserData("mailList9", message["mailList9"]);
    }
    if(message["friendList"]!=null){
        appendUserData("friendList", message["friendList"]);
    }
    if(message["DAILYSHOP"]!=null){
        appendUserData("DAILYSHOP", message["DAILYSHOP"]);
    }
    if(message["BLACKSHOP"]!=null){
        appendUserData("BLACKSHOP", message["BLACKSHOP"]);
    }
    if(message["UNIONSHOP"]!=null){
        appendUserData("UNIONSHOP", message["UNIONSHOP"]);
    }
    if(message["PVPSHOP"]!=null){
        appendUserData("PVPSHOP", message["PVPSHOP"]);
    }
    if(message["EXPEDITIONSHOP"]!=null){
        appendUserData("EXPEDITIONSHOP", message["EXPEDITIONSHOP"]);
    }
    if(message["LADDERSHOP"]!=null){
        appendUserData("LADDERSHOP", message["LADDERSHOP"]);
    }
}

//////////////server/////////////////
function addNewServerTab() {
    serverid = $('input[name="serverid"]:visible').val();
    requestServerJson(serverid);
}

/*set global serverid before called*/
function addServerTab(){
    var checked = false;
    var navs =  $(".nav-serverbtn");
    for (var i =navs.length - 1; i >= 0; i--) {
     if($(navs[i]).attr("serverid") == serverid){
         checked = true;
         if(!$(navs[i]).hasClass("nav-btn-active")){
             navs.removeClass("nav-btn-active");
             $(navs[i]).addClass("nav-btn-active");
         }
         break;
     }
    }
    if(!checked){
        var text = "S"+serverid;
        var $el = $( '<a href="#" serverid="'+serverid+'" onclick="requestServerJson('+serverid+')" class="nav-serverbtn nav-btn ui-btn ui-btn-inline">' + text + "</a>" );
        $("#new-servertab").after($el);
        $el.buttonMarkup();
        $( "#server-controlgroup" ).controlgroup( "refresh" );
        $(".nav-serverbtn").removeClass("nav-btn-active");
        $($(".nav-serverbtn")[0]).addClass("nav-btn-active");
    }
}

function appendServerData(key, value){
    var editor;
    if($(".json-editor-title[key="+key+"]").length > 0){
        editor = $(".json-editor-title[key="+key+"]").parent();
    }else{
        editor = $("#jsoneditor jsoneditor").clone();
        editor.appendTo("#server-editor");
    }
    editor.find(".json-editor-title span").text(key);
    editor.find(".json-editor-title").attr("key", key);
    editor.find(".json-editor").jsonEditor(value, { change: updateJSON, propertyclick: showPath });
    editor.find(".json").val(JSON.stringify(value));
}

function addBlackDatas(json){
//	json.sort();
	$("#blackdata-controlgroup").empty();
	$.each(json, function (key, value) {
		var $el = $( "<label for='data-" + value + "'>" + key + ":" + value + "</label><input type='checkbox' id='data-" + value + "' name='" + key + "'></input>" );
		$("#blackdata-controlgroup").append($el);
		$( $el[ 1 ] ).checkboxradio();
	});
    $( "#blackdata-controlgroup" ).controlgroup( "refresh" );
}

function delBlackDatas(){
	var keys = [];
	$.each($("#blackdata-controlgroup input:checked"), function () {
		keys[keys.length]=$(this).attr("name");
	});
	var json = buildServerJson("del-blackDatas", keys);
	var datatype = $("#server-nav .nav-btn-active").attr("data-type");
	json["blackType"] = datatype;
	if(keys.length > 0)
		updateServerJson(json);
}

function updateServerJson(jsondata) {
    $.ajax({
        type: "POST",
        url: "datamanager",
        contentType: "application/json; charset=utf-8",
        data: /*JSON.stringify(GetJsonData())*/
            JSON.stringify(jsondata),
        dataType: "json",
        success: function (message) {
            // json = message;
            appendServerDatas(message);
            showReloadBtn();
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            alert("提交数据失败："+XMLHttpRequest.status);
            showReloadBtn();
        }
    });
}

function reloadServerJson(jsondata) {
    updateServerJson(jsondata);
}

function deleteServerJson(jsondata) {
    updateServerJson(jsondata);
}

function requestServerJson(myserverid) {
    if(myserverid != null)
        serverid = myserverid;
    $.ajax({
        type: "POST",
        url: "datamanager",
        contentType: "application/json; charset=utf-8",
        data: /*JSON.stringify(GetJsonData())*/
            JSON.stringify(buildServerJson()),
        dataType: "json",
        success: function (message) {
            json = message;
            appendServerDatas(message, true);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            alert("提交数据失败："+XMLHttpRequest.status);
        }
    });
}

function buildServerJson(key, value){
    var json = {};
    json["serverId"] = serverid;
    json["session"] = session;
    if(key != null){
        if(value != null)
            json[key] = value;
        else
            json[key] = 1;
    }else{
        var datatype = $("#server-nav .nav-btn-active").attr("data-type");
        if(datatype == "base"){
            json["areaBoss"] = 1;
            json["areaResource"] = 1;
            json["areaResourceMine"] = 1;
            json["unionList"] = 1;
          } else if (datatype == "blacknosay") {
                json["blacknosay"] = 1;
          } else if (datatype == "blackuser") {
            json["blackuser"] = 1;
          } else if (datatype == "blackaccount") {
            json["blackaccount"] = 1;
          } else{
            json["messageBoard"] = 1;
        }
    }
    return json;
}

function appendServerDatas(message, dirty){
    if(message["error"]!=null){
        alert("ERROR:"+message["error"]);
        if(message["error"].match("Please login") != null)
        	document.location.href='login.jsp';
        return;
    }
    if(dirty)
        $("#server-editor").empty();
    if(message["success"]!=null){
    	$("#msg-popup").html(message["success"]);
	    $("#msg-popup").popup('open');
	}
    serverid = message["serverId"];
    addServerTab();
    if(message["areaBoss"]!=null){
        appendServerData("areaBoss", message["areaBoss"]);
    }
    if(message["areaResource"]!=null){
        appendServerData("areaResource", message["areaResource"]);
    }
    if(message["areaResourceMine"]!=null){
        appendServerData("areaResourceMine", message["areaResourceMine"]);
    }
    if(message["unionList"]!=null){
        appendServerData("unionList", message["unionList"]);
    }
    if(message["messageBoard"]!=null){
        appendServerData("messageBoard", message["messageBoard"]);
    }
    $("#black-data").show();
    if (message["blacknosay"] != null) {
    	addBlackDatas(message["blacknosay"]);
    } else if (message["blackuser"] != null) {
    	addBlackDatas(message["blackuser"]);
    } else if (message["blackaccount"] != null) {
    	addBlackDatas(message["blackaccount"]);
    } else{
    	$("#black-data").hide();
    }
}
//////////////config/////////////////
function appendConfigData(key, value, visible){
    var editor;
    if($(".json-editor-title[key="+key+"]").length > 0){
        editor = $(".json-editor-title[key="+key+"]").parent();
    }else{
        editor = $("#jsoneditor jsoneditor").clone();
        editor.appendTo("#config-editor");
    }
    editor.find(".json-editor-title span").text(key);
    editor.find(".json-editor-title").attr("key", key);
    editor.find(".update-btn").hide();
    if(visible){
        editor.find(".json-editor").show();
        if(key == "GmRight" || key == "VersionController") editor.find(".update-btn").show();
        editor.find(".json").show();
        editor.find(".json-editor").jsonEditor(value, { change: updateJSON, propertyclick: showPath });
        editor.find(".json").val(JSON.stringify(value));
    }else{
        editor.find(".json-editor").hide();
        editor.find(".json").hide();
    }
}

function updateConfigJson(jsondata) {
    $.ajax({
        type: "POST",
        url: "datamanager",
        contentType: "application/json; charset=utf-8",
        data: /*JSON.stringify(GetJsonData())*/
            JSON.stringify(jsondata),
        dataType: "json",
        success: function (message) {
            // json = message;
            appendConfigDatas(message);
            showReloadBtn();
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
        	//XMLHttpRequest.responseText
            alert("提交数据失败"+XMLHttpRequest.status+":"+textStatus);
            showReloadBtn();
        }
    });
}

function reloadConfigJson(jsondata) {
    updateConfigJson(jsondata);
}

function deleteConfigJson(jsondata) {
    updateConfigJson(jsondata);
}

// function requestConfigJson() {
//     $.ajax({
//         type: "POST",
//         url: "datamanager",
//         contentType: "application/json; charset=utf-8",
//         data: /*JSON.stringify(GetJsonData())*/
//             JSON.stringify(buildConfigJson()),
//         dataType: "json",
//         success: function (message) {
//             json = message;
//             $("#config-editor").empty();
//             appendConfigDatas(message);
//         },
//         error: function (XMLHttpRequest, textStatus, errorThrown) {
//             alert("提交数据失败："+XMLHttpRequest.status);
//         }
//     });
// }

function buildConfigJson(key, value){
    var json = {};
    if(key != null){
        json["serverId"] = 1;
        json["session"] = session;
        if(value != null)
            json[key] = value;
        else
            json[key] = "{}";
    }else{
        var datatype = $("#config-nav .nav-btn-active").attr("data-type");
        if(datatype == "common"){
            json["RmbConfig"] = "{}";
            json["Rmb1Config"] = "{}";
            json["VipConfig"] = "{}";
            json["ActivityRichangConfig"] = "{}";
        }else if(datatype == "base"){
            json["DailyShopConfig"] = "{}";
            json["ShopConfig"] = "{}";
            json["BlackShopConfig"] = "{}";
            json["UnionShopConfig"] = "{}";
            json["PvpShopConfig"] = "{}";
            json["ExpeditionShopConfig"] = "{}";
            json["LadderShopConfig"] = "{}";
            json["AreaMonsterReward"] = "{}";
            json["LadderRankingConfig"] = "{}";
            json["LadderDailyConfig"] = "{}";
            json["LevelConfig"] = "{}";
            json["LevelDiffConfig1"] = "{}";
            json["LevelDiffConfig2"] = "{}";
            json["LevelDiffConfig3"] = "{}";
            json["DaguanConfig"] = "{}";
            json["WinLevelConfig"] = "{}";
            json["LootLevelConfig"] = "{}";
            json["HeroUpgradeConfig"] = "{}";
            json["HeroConfig"] = "{}";
            json["HeroRareConfig"] = "{}";
        }else if(datatype == "cdkey"){
            json["Cdkey"] = "{}";
        }else if(datatype == "blacklist"){
            json["BlackList"] = "{}";
        }else if(datatype == "delete"){
            json["DeleteData"] = "{}";
        }else if(datatype == "gmright"){
            json["GmRight"] = "{}";
            json["VersionController"] = "{}";
        }else{
            json["HeroStarConfig"] = "{}";
            json["LotteryConfig1001"] = "{}";
            json["LotteryConfig1002"] = "{}";
            json["LotteryEquipConfig1001"] = "{}";
            json["LotteryEquipConfig1002"] = "{}";
            json["EquipConfig"] = "{}";
            json["ChipConfig"] = "{}";
            json["SkillConfig"] = "{}";
            json["SkillLevelConfig"] = "{}";
            json["PropConfig"] = "{}";
            json["FenJieConfig"] = "{}";
            json["TotalSignConfig"] = "{}";
            json["SignConfig"] = "{}";
            json["Sign2Config"] = "{}";
            json["VipLibaoConfig"] = "{}";
            json["MoHuaConfig"] = "{}";
            json["MoHuaCardConfig"] = "{}";
            json["MoHuaJieDuanConfig"] = "{}";
            json["MoHuaLootConfig"] = "{}";
            json["AchieveConfig"] = "{}";
            json["ActivityRiChangConfig"] = "{}";
            json["ActivityKaiFu2Config"] = "{}";
            json["ActivityKaiFuConfig"] = "{}";
            json["AreaEquipConfig"] = "{}";
            json["AreaMonsterRewardConfig"] = "{}";
            json["AreaConfig"] = "{}";
            json["AreaBossConfig"] = "{}";
            json["AreaBossRandConfig"] = "{}";
            json["AreaBossRewardConfig"] = "{}";
            json["AreaMonsterConfig"] = "{}";
            json["AreaMonsterRandConfig"] = "{}";
            json["AreaPositionConfig"] = "{}";
            json["AreaResourceConfig"] = "{}";
            json["PvpMonsterRewardConfig"] = "{}";
            json["PvpMonsterConfig"] = "{}";
            json["PvpBossConfig"] = "{}";
            json["PvpPositionConfig"] = "{}";
            json["PvpMapConfig"] = "{}";
            json["PurchaseCoinConfig"] = "{}";
            json["PurchaseCoinRewardConfig"] = "{}";
        }
    }
    return json;
}

function appendConfigDatas(message, visible){
    if(visible == null)
        visible = true;
    if(message["error"]!=null){
        alert("ERROR:"+message["error"]);
        if(message["error"].match("Please login") != null)
        	document.location.href='login.jsp';
        return;
    }
    if(message["success"]!=null){
    	$("#msg-popup").html(message["success"]);
	    $("#msg-popup").popup('open');
	}
    if(message["RmbConfig"]!=null){
        appendConfigData("RmbConfig", message["RmbConfig"], visible);
    }
    if(message["Rmb1Config"]!=null){
        appendConfigData("Rmb1Config", message["Rmb1Config"], visible);
    }
    if(message["VipConfig"]!=null){
        appendConfigData("VipConfig", message["VipConfig"], visible);
    }
    if(message["ActivityRichangConfig"]!=null){
        appendConfigData("ActivityRichangConfig", message["ActivityRichangConfig"], visible);
    }
    if(message["DailyShopConfig"]!=null){
        appendConfigData("DailyShopConfig", message["DailyShopConfig"], visible);
    }
    if(message["ShopConfig"]!=null){
        appendConfigData("ShopConfig", message["ShopConfig"], visible);
    }
    if(message["BlackShopConfig"]!=null){
        appendConfigData("BlackShopConfig", message["BlackShopConfig"], visible);
    }
    if(message["UnionShopConfig"]!=null){
        appendConfigData("UnionShopConfig", message["UnionShopConfig"], visible);
    }
    if(message["PvpShopConfig"]!=null){
        appendConfigData("PvpShopConfig", message["PvpShopConfig"], visible);
    }
    if(message["ExpeditionShopConfig"]!=null){
        appendConfigData("ExpeditionShopConfig", message["ExpeditionShopConfig"], visible);
    }
    if(message["LadderShopConfig"]!=null){
        appendConfigData("LadderShopConfig", message["LadderShopConfig"], visible);
    }
    if(message["AreaMonsterReward"]!=null){
        appendConfigData("AreaMonsterReward", message["AreaMonsterReward"], visible);
    }
    if(message["LadderRankingConfig"]!=null){
        appendConfigData("LadderRankingConfig", message["LadderRankingConfig"], visible);
    }
    if(message["LadderDailyConfig"]!=null){
        appendConfigData("LadderDailyConfig", message["LadderDailyConfig"], visible);
    }
    if(message["LevelConfig"]!=null){
        appendConfigData("LevelConfig", message["LevelConfig"], visible);
    }
    if(message["LevelDiffConfig1"]!=null){
        appendConfigData("LevelDiffConfig1", message["LevelDiffConfig1"], visible);
    }
    if(message["LevelDiffConfig2"]!=null){
        appendConfigData("LevelDiffConfig2", message["LevelDiffConfig2"], visible);
    }
    if(message["LevelDiffConfig3"]!=null){
        appendConfigData("LevelDiffConfig3", message["LevelDiffConfig3"], visible);
    }
    if(message["DaguanConfig"]!=null){
        appendConfigData("DaguanConfig", message["DaguanConfig"], visible);
    }
    if(message["WinLevelConfig"]!=null){
        appendConfigData("WinLevelConfig", message["WinLevelConfig"], visible);
    }
    if(message["LootLevelConfig"]!=null){
        appendConfigData("LootLevelConfig", message["LootLevelConfig"], visible);
    }
    if(message["HeroUpgradeConfig"]!=null){
        appendConfigData("HeroUpgradeConfig", message["HeroUpgradeConfig"], visible);
    }
    if(message["HeroConfig"]!=null){
        appendConfigData("HeroConfig", message["HeroConfig"], visible);
    }
    if(message["HeroRareConfig"]!=null){
        appendConfigData("HeroRareConfig", message["HeroRareConfig"], visible);
    }
    if(message["HeroStarConfig"]!=null){
        appendConfigData("HeroStarConfig", message["HeroStarConfig"], visible);
    }
    if(message["LotteryConfig1001"]!=null){
        appendConfigData("LotteryConfig1001", message["LotteryConfig1001"], visible);
    }
    if(message["LotteryConfig1002"]!=null){
        appendConfigData("LotteryConfig1002", message["LotteryConfig1002"], visible);
    }
    if(message["LotteryEquipConfig1001"]!=null){
        appendConfigData("LotteryEquipConfig1001", message["LotteryEquipConfig1001"], visible);
    }
    if(message["LotteryEquipConfig1002"]!=null){
        appendConfigData("LotteryEquipConfig1002", message["LotteryEquipConfig1002"], visible);
    }
    if(message["EquipConfig"]!=null){
        appendConfigData("EquipConfig", message["EquipConfig"], visible);
    }
    if(message["ChipConfig"]!=null){
        appendConfigData("ChipConfig", message["ChipConfig"], visible);
    }
    if(message["SkillConfig"]!=null){
        appendConfigData("SkillConfig", message["SkillConfig"], visible);
    }
    if(message["SkillLevelConfig"]!=null){
        appendConfigData("SkillLevelConfig", message["SkillLevelConfig"], visible);
    }
    if(message["PropConfig"]!=null){
        appendConfigData("PropConfig", message["PropConfig"], visible);
    }
    if(message["FenJieConfig"]!=null){
        appendConfigData("FenJieConfig", message["FenJieConfig"], visible);
    }
    if(message["TotalSignConfig"]!=null){
        appendConfigData("TotalSignConfig", message["TotalSignConfig"], visible);
    }
    if(message["SignConfig"]!=null){
        appendConfigData("SignConfig", message["SignConfig"], visible);
    }
    if(message["Sign2Config"]!=null){
        appendConfigData("Sign2Config", message["Sign2Config"], visible);
    }
    if(message["VipLibaoConfig"]!=null){
        appendConfigData("VipLibaoConfig", message["VipLibaoConfig"], visible);
    }
    if(message["MoHuaConfig"]!=null){
        appendConfigData("MoHuaConfig", message["MoHuaConfig"], visible);
    }
    if(message["MoHuaCardConfig"]!=null){
        appendConfigData("MoHuaCardConfig", message["MoHuaCardConfig"], visible);
    }
    if(message["MoHuaJieDuanConfig"]!=null){
        appendConfigData("MoHuaJieDuanConfig", message["MoHuaJieDuanConfig"], visible);
    }
    if(message["MoHuaLootConfig"]!=null){
        appendConfigData("MoHuaLootConfig", message["MoHuaLootConfig"], visible);
    }
    if(message["AchieveConfig"]!=null){
        appendConfigData("AchieveConfig", message["AchieveConfig"], visible);
    }
    if(message["ActivityRiChangConfig"]!=null){
        appendConfigData("ActivityRiChangConfig", message["ActivityRiChangConfig"], visible);
    }
    if(message["ActivityKaiFu2Config"]!=null){
        appendConfigData("ActivityKaiFu2Config", message["ActivityKaiFu2Config"], visible);
    }
    if(message["ActivityKaiFuConfig"]!=null){
        appendConfigData("ActivityKaiFuConfig", message["ActivityKaiFuConfig"], visible);
    }
    if(message["AreaEquipConfig"]!=null){
        appendConfigData("AreaEquipConfig", message["AreaEquipConfig"], visible);
    }
    if(message["AreaMonsterRewardConfig"]!=null){
        appendConfigData("AreaMonsterRewardConfig", message["AreaMonsterRewardConfig"], visible);
    }
    if(message["AreaConfig"]!=null){
        appendConfigData("AreaConfig", message["AreaConfig"], visible);
    }
    if(message["AreaBossConfig"]!=null){
        appendConfigData("AreaBossConfig", message["AreaBossConfig"], visible);
    }
    if(message["AreaBossRandConfig"]!=null){
        appendConfigData("AreaBossRandConfig", message["AreaBossRandConfig"], visible);
    }
    if(message["AreaBossRewardConfig"]!=null){
        appendConfigData("AreaBossRewardConfig", message["AreaBossRewardConfig"], visible);
    }
    if(message["AreaMonsterConfig"]!=null){
        appendConfigData("AreaMonsterConfig", message["AreaMonsterConfig"], visible);
    }
    if(message["AreaMonsterRandConfig"]!=null){
        appendConfigData("AreaMonsterRandConfig", message["AreaMonsterRandConfig"], visible);
    }
    if(message["AreaPositionConfig"]!=null){
        appendConfigData("AreaPositionConfig", message["AreaPositionConfig"], visible);
    }
    if(message["AreaResourceConfig"]!=null){
        appendConfigData("AreaResourceConfig", message["AreaResourceConfig"], visible);
    }
    if(message["PvpMonsterRewardConfig"]!=null){
        appendConfigData("PvpMonsterRewardConfig", message["PvpMonsterRewardConfig"], visible);
    }
    if(message["PvpMonsterConfig"]!=null){
        appendConfigData("PvpMonsterConfig", message["PvpMonsterConfig"], visible);
    }
    if(message["PvpBossConfig"]!=null){
        appendConfigData("PvpBossConfig", message["PvpBossConfig"], visible);
    }
    if(message["PvpPositionConfig"]!=null){
        appendConfigData("PvpPositionConfig", message["PvpPositionConfig"], visible);
    }
    if(message["PvpMapConfig"]!=null){
        appendConfigData("PvpMapConfig", message["PvpMapConfig"], visible);
    }
    if(message["PurchaseCoinConfig"]!=null){
        appendConfigData("PurchaseCoinConfig", message["PurchaseCoinConfig"], visible);
    }
    if(message["PurchaseCoinRewardConfig"]!=null){
        appendConfigData("PurchaseCoinRewardConfig", message["PurchaseCoinRewardConfig"], visible);
    }
    if(message["GmRight"]!=null){
        appendConfigData("GmRight", message["GmRight"], visible);
    }
    if(message["VersionController"]!=null){
        appendConfigData("VersionController", message["VersionController"], visible);
    }
    if(message["Cdkey"]!=null){
    	$("#config-cdkey").show();
        showCdkeyTable(message["Cdkey"]);
        
        $("#table-cdkey-popup-popup :checkbox").on('click', function() {
        	toggleCdkeyTable(this);
        });
    }else{
    	$("#config-cdkey").hide();
    }
    if(message["BlackList"]!=null){
        $("#config-blacklist").show();
        showBlackListTable(message["BlackList"]);
        
        $("#table-blacklist-popup-popup :checkbox").on('click', function() {
            toggleBlackListTable(this);
        });
    }else{
        $("#config-blacklist").hide();
    }
    if(message["RedisData"]!=null){
    	$("#config-redisdata").show();
    	addRedisDatas(message["RedisData"]);
    }else{
    	$("#config-redisdata").hide();
    }
}

function addRedisDatas(json){
    if(json == "keep")
        return;
	json.sort();
	$("#data-controlgroup").empty();
	$.each(json, function (key, value) {
		var $el = $( "<label for='data-" + value + "'>" + value + "</label><input type='checkbox' id='data-" + value + "' name='" + value + "'></input>" );
		$("#data-controlgroup").append($el);
		$( $el[ 1 ] ).checkboxradio();
	});
    $( "#data-controlgroup" ).controlgroup( "refresh" );
}

function selectAllData(dom){
	var checked = $(dom).is(':checked');
	$(dom).parent().parent().next().find("input").attr("checked", checked).checkboxradio("refresh");
}

function showCdkeyTable(json){
	var table = $("#table-cdkey tbody");
    var dom = table.find("tr");
    var jsonlength = Object.keys(json).length;
    for (var i = dom.length - 1; i > 0; i--) {
        dom[i].remove();
    };
    for (var i = 1; i < jsonlength; i++) {
        table.append($(dom[0]).clone());
    };
    dom = table.find("tr");
    var index = jsonlength-1;
    $.each(json, function (key, value) {
        var children = $(dom[index]).children();
        $(children[0]).html(key);
        $(children[1]).html(value["name"]);
        $(children[2]).html(JSON.stringify(value["reward"]));
        $(children[3]).html(value["currentCount"]+"/"+value["count"]);
        if(index == 0){
        	$("#cdkey-form input[name='id']").val(Number(key)+1);
        	$("#cdkey-form input[name='id']").change();
        }
        index--;
	});
}

function toggleCdkeyTable(checkbox){
	var name = $(checkbox).prev().text();
	var tr = $("#table-cdkey thead tr th");
	var th = 3;
	var ischecked = $(checkbox).is(":checked");
	for(var i = 0; i < tr.length; i++){
		if($(tr[i]).text() == name)
			th = i;
	}
    $.each($("#table-cdkey tbody tr"), function () {
        var children = $(this).children();
        if(ischecked){
        	$(children[th]).removeClass("ui-table-cell-hidden");
        	$(children[th]).addClass("ui-table-cell-visible");
        }else{
        	$(children[th]).removeClass("ui-table-cell-visible");
        	$(children[th]).addClass("ui-table-cell-hidden");
        }
	});
}

//function addCdkey(){
//	var json = {};
//	json["id"] = Number($("#cdkey-form input[name='id']").val());
//	json["name"] = $("#cdkey-form input[name='name']").val();
//	json["reward"] = $("#cdkey-form input[name='reward']").val();
//	json["count"] = Number($("#cdkey-form input[name='count']").val());
//	updateConfigJson(buildConfigJson("add-Cdkey", json));
//}

//function delCdkey(dom){
//	var key = $(dom).parent().parent().find("th:first").text();
//	updateConfigJson(buildConfigJson("del-Cdkey", key));
//}

function configCdkey(dom){
	var children = $(dom).children();
	$("#cdkey-form input[name='id']").val($(children[0]).text());
	$("#cdkey-form input[name='name']").val($(children[1]).text());
	$("#cdkey-form input[name='reward']").val($(children[2]).text());
	$("#cdkey-form input[name='count']").val("-1");
	$("#cdkey-form input[name='id']").change();
}

function updateCdkeyUrl(dom){
	var value = $(dom).val();
	var form = $(dom).parent().parent().parent();
	form.attr("action", "cdkey"+value+".txt");
}


function showBlackListTable(json){
    var table = $("#table-blacklist tbody");
    var dom = table.find("tr");
    var jsonlength = Object.keys(json).length;
    for (var i = dom.length - 1; i > 0; i--) {
        dom[i].remove();
    };
    for (var i = 1; i < jsonlength; i++) {
        table.append($(dom[0]).clone());
    };
    dom = table.find("tr");
    var index = jsonlength-1;
    $.each(json, function (key, value) {
        var children = $(dom[index]).children();
        $(children[0]).html(key);
        $(children[1]).html(value["userName"]);
        $(children[2]).html(value["serverId"]);
        $(children[3]).html(value["account"]);
        $(children[4]).html(value["idfa"]);
        $(children[5]).html(value["notalk"] ? "√" : "×");
        $(children[6]).html(value["noranklist"] ? "√" : "×");
        $(children[7]).html(value["nologin"] ? "√" : "×");
        $(children[8]).html(value["noaccount"] ? "√" : "×");
        $(children[9]).html(value["noidfa"] ? "√" : "×");
        index--;
    });
}

function toggleBlackListTable(checkbox){
    var name = $(checkbox).prev().text();
    var tr = $("#table-blacklist thead tr th");
    var th = 3;
    var ischecked = $(checkbox).is(":checked");
    for(var i = 0; i < tr.length; i++){
        if($(tr[i]).text() == name)
            th = i;
    }
    $.each($("#table-blacklist tbody tr"), function () {
        var children = $(this).children();
        if(ischecked){
            $(children[th]).removeClass("ui-table-cell-hidden");
            $(children[th]).addClass("ui-table-cell-visible");
        }else{
            $(children[th]).removeClass("ui-table-cell-visible");
            $(children[th]).addClass("ui-table-cell-hidden");
        }
    });
}

//function addBlackList(){
//  var json = {};
//  json["id"] = Number($("#blacklist-form input[name='id']").val());
//  json["name"] = $("#blacklist-form input[name='name']").val();
//  json["reward"] = $("#blacklist-form input[name='reward']").val();
//  json["count"] = Number($("#blacklist-form input[name='count']").val());
//  updateConfigJson(buildConfigJson("add-BlackList", json));
//}

//function delBlackList(dom){
//  var key = $(dom).parent().parent().find("th:first").text();
//  updateConfigJson(buildConfigJson("del-BlackList", key));
//}

function configBlackList(dom){
    var children = $(dom).children();
    $("#blacklist-form input[name='userid']").val($(children[0]).text());
    $("#blacklist-form input[name='username']").val($(children[1]).text());
    $("#blacklist-form input[name='serverid']").val("");//$(children[2]).text());
    // $("#blacklist-form input[name='notalk']").attr("checked", $(children[5]).text() == "×" ? false : true).checkboxradio("refresh");
    // $("#blacklist-form input[name='noranklist']").attr("checked", $(children[6]).text() == "×" ? false : true).checkboxradio("refresh");
    // $("#blacklist-form input[name='nologin']").attr("checked", $(children[7]).text() == "×" ? false : true).checkboxradio("refresh");
    // $("#blacklist-form input[name='noaccount']").attr("checked", $(children[8]).text() == "×" ? false : true).checkboxradio("refresh");
    // $("#blacklist-form input[name='noidfa']").attr("checked", $(children[9]).text() == "×" ? false : true).checkboxradio("refresh");
}

function delConfigData(type){
	var value = $("#redisdata-keys").val();
	var json = buildConfigJson("del-ConfigData", type);
	json["RedisData"] = value;
	updateConfigJson(json);
}

function getRedisData(){
	var value = $("#redisdata-keys").val();
	var json = buildConfigJson("RedisData", value);
	updateConfigJson(json);
}

function delRedisData(){
	var value = $("#redisdata-keys").val();
	var json = buildConfigJson("del-RedisData", value);
	updateConfigJson(json);
}

function delRedisDatas(){
	var keys = [];
	$.each($("#data-controlgroup input:checked"), function () {
		keys[keys.length]=$(this).attr("name");
	});
	var json = buildConfigJson("del-RedisDatas", keys);
	var value = $("#redisdata-keys").val();
	json["RedisData"] = value;
	if(keys.length > 0)
		updateConfigJson(json);
}

$(document).ready(function() {
    $( "#menu-panel" ).panel({
      animate: false
    });
    $( "#msg-popup" ).enhanceWithin().popup();
    $( "#users-panel" ).enhanceWithin().popup();
    //////////////user///////////////////
    $("#user-editor").on('click', ".reload-btn", function() {
        var data = buildUserJson($( this ).parent().attr("key"));
        // date[key] = 1;
        reloadUserJson(data);
        hideReloadBtn($(this).parent());
    });
    $("#user-editor").on('click', ".update-btn", function() {
        var dom = $( this ).parent().parent();
        var val = dom.parent().find(".json").val();
        if(val.length < 5){ 
            alert('Error in updating empty json. ');return;
        }
        try { json = JSON.parse(val); }
        catch (e) { alert('Error in parsing json. ' + e);return; }
        var data = buildUserJson("update-"+dom.attr("key"), val);
        // date["update-"+key] = 1;
        updateUserJson(data);
        hideReloadBtn($(this).parent().parent());
    });
    $("#user-editor").on('click', ".del-btn", function() {
        var dom = $( this ).parent().parent();
        var jsondom = dom.parent().find(".json");
        jsondom.val("{}");
        updateJSON(jsondom);
        var data = buildUserJson("del-"+dom.attr("key"));
        // date["del-"+key] = 1;
        deleteUserJson(data);
        hideReloadBtn($(this).parent().parent());
    });
    $("#user-editor").on('click', ".reset-btn", function() {
        var dom = $( this ).parent().parent();
        var jsondom = dom.parent().find(".json");
        jsondom.val("{}");
        updateJSON(jsondom);
        var data = buildUserJson("reset-"+dom.attr("key"));
        // date["del-"+key] = 1;
        deleteUserJson(data);
        hideReloadBtn($(this).parent().parent());
    });
    $("#user-navmenu-panel a[data-type]").on('click', function() {
        $(this).parent().parent().find(".nav-btn-active").removeClass("nav-btn-active");
        $(this).addClass("nav-btn-active");
        if(userid == 0 && serverid == 0)
        	return;
        requestUserJson();
    });
    ///////////////server///////////////
    $("#server-editor").on('click', ".reload-btn", function() {
        var data = buildServerJson($( this ).parent().attr("key"));
        // date[key] = 1;
        reloadServerJson(data);
        hideReloadBtn($(this).parent());
    });
    $("#server-editor").on('click', ".update-btn", function() {
        var dom = $( this ).parent().parent();
        var val = dom.parent().find(".json").val();
        if(val.length < 5){ 
            alert('Error in updating empty json. ');return;
        }
        try { json = JSON.parse(val); }
        catch (e) { alert('Error in parsing json. ' + e);return; }
        var data = buildServerJson("update-"+dom.attr("key"), val);
        // date["update-"+key] = 1;
        updateServerJson(data);
        hideReloadBtn($(this).parent().parent());
    });
    $("#server-editor").on('click', ".del-btn", function() {
        var dom = $( this ).parent().parent();
        var jsondom = dom.parent().find(".json");
        jsondom.val("{}");
        updateJSON(jsondom);
        var data = buildServerJson("del-"+dom.attr("key"));
        // date["del-"+key] = 1;
        deleteServerJson(data);
        hideReloadBtn($(this).parent().parent());
    });
    $("#server-navmenu-panel a[data-type]").on('click', function() {
        $(this).parent().parent().find(".nav-btn-active").removeClass("nav-btn-active");
        $(this).addClass("nav-btn-active");
        if(serverid == 0)
            return;
        requestServerJson();
    });
    /////////////config////////////////////
    $("#config-editor").on('click', ".reload-btn", function() {
        var data = buildConfigJson($( this ).parent().attr("key"));
        // date[key] = 1;
        reloadConfigJson(data);
        hideReloadBtn($(this).parent());
    });
    $("#config-editor").on('click', ".update-btn", function() {
        var dom = $( this ).parent().parent();
        var val = dom.parent().find(".json").val();
        if(val.length < 5){ 
            alert('Error in updating empty json. ');return;
        }
        try { json = JSON.parse(val); }
        catch (e) { alert('Error in parsing json. ' + e);return; }
        var data = buildConfigJson("update-"+dom.attr("key"), val);
        // date["update-"+key] = 1;
        updateConfigJson(data);
        hideReloadBtn($(this).parent().parent());
    });
    $("#config-editor").on('click', ".del-btn", function() {
        var dom = $( this ).parent().parent();
        var jsondom = dom.parent().find(".json");
        jsondom.val("{}");
        updateJSON(jsondom);
        var data = buildConfigJson("del-"+dom.attr("key"));
        // date["del-"+key] = 1;
        deleteConfigJson(data);
        hideReloadBtn($(this).parent().parent());
    });
    $("#config-navmenu-panel a[data-type]").on('click', function() {
        $(this).parent().parent().find(".nav-btn-active").removeClass("nav-btn-active");
        $(this).addClass("nav-btn-active");
        $("#config-editor").empty();
        var json = buildConfigJson();
        if(json.hasOwnProperty("Cdkey")){
        	json = buildConfigJson("Cdkey", "{}");
        	updateConfigJson(json);
        }else if(json.hasOwnProperty("BlackList")){
            json = buildConfigJson("BlackList", "{}");
            updateConfigJson(json);
        }else if(json.hasOwnProperty("DeleteData")){
        	$("#selectAllData").attr("checked",false).checkboxradio("refresh");
        	$("#redisdata-keys").val("pixel:config*");
        	json = buildConfigJson("RedisData", "pixel:config*");
        	updateConfigJson(json);
        }else if(json.hasOwnProperty("GmRight")){
            json = buildConfigJson("GmRight", "1");
            json["VersionController"] = 1;
            updateConfigJson(json);
        }else
        	appendConfigDatas(json, false);
    });
    $("#table-cdkey").on('click', ".del-btn", function() {
    	var cdkeyid = $(this).parent().parent().find("th:first").text();
    	var json = buildConfigJson("del-Cdkey", cdkeyid);
    	updateConfigJson(json);
    });
    $("#cdkey-session").val(session);
    $("#table-blacklist").on('click', ".del-btn", function() {
        var userid = $(this).parent().parent().find("th:first").text();
        var json = buildConfigJson("del-BlackList", userid);
        updateConfigJson(json);
    });
     $('#blacklist-form').submit(function(e) {
        e.preventDefault();
        var data = {};
        data["notalk"] =  $('input[name="notalk"]:visible').is(':checked');
        data["noranklist"] =  $('input[name="noranklist"]:visible').is(':checked');
        data["nologin"] =  $('input[name="nologin"]:visible').is(':checked');
        data["noaccount"] =  $('input[name="noaccount"]:visible').is(':checked');
        data["noidfa"] =  $('input[name="noidfa"]:visible').is(':checked');
        var json = buildConfigJson("update-BlackList", data);
        json["userId"] =  Number($('input[name="userid"]:visible').val());
        json["userName"] =  $('input[name="username"]:visible').val();
        json["serverId"] =  Number($('input[name="serverid"]:visible').val());
        updateConfigJson(json);
        // $.ajax({
        // type: "POST",
        // url: "datamanager",
        // contentType: "application/json; charset=utf-8",
        // data: $('#blacklist-form').serialize(),
        // dataType: "json",
        // success: function (message) {
        //     // json = message;
        //     showBlackListTable(message);
        // },
        // error: function (XMLHttpRequest, textStatus, errorThrown) {
        //     alert("提交数据失败："+XMLHttpRequest.status);
        //     showReloadBtn();
        // }
    // });
     });
//    $('.json').val(JSON.stringify(json));
//    $('.json-editor').jsonEditor(json, { change: updateJSON, propertyclick: showPath });
});

    function isObject(o) { return Object.prototype.toString.call(o) == '[object Object]'; }
    function relogin(){ document.location.href='login.jsp'; }
    
	function doBlack(){
		var userId = $('input[name="userid"]:visible').val();
		var userName = $('input[name="username"]:visible').val();
		var lastTime = $('input[name="lasttime"]:visible').val();
		var data={};
		data["session"] = session;
		data["serverId"] = serverid;
		data["userId"] = userId;
		data["userName"] = userName;
		data["lastTime"] = lastTime;
		var datatype = $("#server-nav .nav-btn-active").attr("data-type");
    	data["blackType"] = datatype;
    	updateServerJson(data);
	}


