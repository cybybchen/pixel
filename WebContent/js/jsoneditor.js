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
    }},"data2":{
    "string": "foo",
    "number": 5,
    "array": [1, 2, 3],
    "object": {
        "property": "value",
        "subobj": {
            "arr": ["foo", "ha"],
            "numero": 1
        }
    }},"data3":{
    "string": "foo",
    "number": 5,
    "array": [1, 2, 3],
    "object": {
        "property": "value",
        "subobj": {
            "arr": ["foo", "ha"],
            "numero": 1
        }
    }
}};

var userid = 0;
var username = "";
var serverid = 0;
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
        var $el = $( '<a href="#" userid="'+userid+'" onclick="requestUserJson(this, '+userid+', '+username+', '+serverid+')" class="nav-userbtn nav-btn ui-btn ui-btn-inline">' + text + "</a>" );
        $("#new-usertab").after($el);
        $el.buttonMarkup();
        $( "#user-controlgroup" ).controlgroup( "refresh" );
        $(".nav-userbtn").removeClass("nav-btn-active");
        $($(".nav-userbtn")[0]).addClass("nav-btn-active");
        // $($(".nav-userbtn")[0]).click()
    }
}

function appendUserData(key, value){
    var editor = $("#jsoneditor jsoneditor").clone();
    if($(".json-editor-title[key="+key+"]").length > 0)
        editor = $(".json-editor-title[key="+key+"]").parent();
    editor.appendTo("#user-editor");
    editor.find(".json-editor-title span").text(key);
    editor.find(".json-editor-title").attr("key", key);
    editor.find(".json-editor").jsonEditor(value, { change: updateJSON, propertyclick: showPath });
    editor.find(".json").val(JSON.stringify(value));
}

function appendUserDatas(message){
    userid = message["userId"];
    username = message["userName"];
    serverid = message["serverId"];
    addUserTab();
    if(message["UserData"]!=null){
        appendUserData("UserData", message["UserData"]);
    }
    if(message["LevelRecord"]!=null){
        appendUserData("LevelRecord", message["LevelRecord"]);
    }
    if(message["LootLevel"]!=null){
        appendUserData("LootLevel", message["LootLevel"]);
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
        },
        error: function (message) {
            alert("提交数据失败！\n"+message);
        }
    });
}

function reloadUserJson(jsondata) {
    updateUserJson(jsondata);
}

function deleteUserJson(jsondata) {
    updateUserJson(jsondata);
}

function requestUserJson(userid, username, serverid) {
    $.ajax({
        type: "POST",
        url: "datamanager",
        contentType: "application/json; charset=utf-8",
        data: /*JSON.stringify(GetJsonData())*/
            JSON.stringify(buildUserJson()),
        dataType: "json",
        success: function (message) {
            json = message;
            $("#user-editor").empty();
            appendUserDatas(message);
            // for(var key in message){//message
            // if(isObject(message[key]))
            //     appendUserData(key, message[key]);
            // }
//            if (message > 0) {
            // alert(message);
//            }
        },
        error: function (message) {
            alert("提交数据失败！\n"+message);
        }
    });
}

function buildUserJson(key){
	var json = {};
	json["userId"] = userid;
	json["userName"] = username;
    json["serverId"] = serverid;
    if(key != null){
        json[key] = 1;
    }else{
        json["userData"] = 1;
        json["userDailyData"] = 1;
        json["areaMonster"] = 1;
        json["team"] = 1;
        json["teamCache"] = 1;
        json["hero"] = 1;
        json["equip"] = 1;
        json["pvpMap"] = 1;
        json["userMine"] = 1;
        json["mailList"] = 1;
        json["friendList"] = 1;
    }
    return json;
}

// function printJSON() {
//     $('#json').val(JSON.stringify(json));

// }

function updateJSON(dom, data) {
    if(data == null){//update json view
        var val = $(dom).val();
        if (val) {
            try { json = JSON.parse(val); }
            catch (e) { alert('Error in parsing json. ' + e); }
        } else {
            json = {};
        }
        $(dom).prev().jsonEditor(json, { change: updateJSON, propertyclick: showPath });
    }else{//update json text
        var div = $(dom).parent();
        while(!div.hasClass("json-editor")){
            div = $(div).parent();
        }
        div = div.next();
        div.val(JSON.stringify(data));
    }
}

function showPath(path) {
    $('#path').text(path);
}

$(document).ready(function() {
    // $(".nav-userbtn").click(function() {
    //     if(!$(this).hasClass("nav-btn-active")){
    //         requestUserJson(userid, username, serverid);
    //     }
    //     $(this).addClass("nav-btn-active");
    // });
    $( "#menu-panel" ).panel({
      animate: false
    });
    $(".reload-btn").on('click', function() {
        var data = buildUserJson("reload-"+$( this ).parent().attr(key));
        // date["reload-"+key] = 1;
        reloadUserJson(data);
    });
    $(".update-btn").on('click', function() {
        var data = buildUserJson("update-"+$( this ).parent().attr(key));
        // date["update-"+key] = 1;
        updateUserJson(data);
    });
    $(".del-btn").on('click', function() {
        var data = buildUserJson("del-"+$( this ).parent().attr(key));
        // date["del-"+key] = 1;
        deleteUserJson(data);
    });
    

    // $('#rest > button').click(function() {
    //     var url = $('#rest-url').val();
    //     $.ajax({
    //         url: url,
    //         dataType: 'jsonp',
    //         jsonp: $('#rest-callback').val(),
    //         success: function(data) {
    //             json = data;
    //             $('#editor').jsonEditor(json, { change: updateJSON, propertyclick: showPath });
    //             printJSON();
    //         },
    //         error: function() {
    //             alert('Something went wrong, double-check the URL and callback parameter.');
    //         }
    //     });
    // });

    // $('.json').change(function() {
    //     var val = $(this).val();

    //     if (val) {
    //         try { json = JSON.parse(val); }
    //         catch (e) { alert('Error in parsing json. ' + e); }
    //     } else {
    //         json = {};
    //     }
        
    //     $(this).prev().jsonEditor(json, { change: updateJSON, propertyclick: showPath });
    // });

    // $('#expander').click(function() {
    //     var editor = $('#editor');
    //     editor.toggleClass('expanded');
    //     $(this).text(editor.hasClass('expanded') ? 'Collapse' : 'Expand all');
    // });

    // $('.json').val(JSON.stringify(json));
    // $('.json-editor').jsonEditor(json, { change: updateJSON, propertyclick: showPath });
});

    function isObject(o) { return Object.prototype.toString.call(o) == '[object Object]'; }


