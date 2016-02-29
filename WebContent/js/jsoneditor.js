var json = {
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
};

var userid = 0;
var username = "";
var serverid = 0;
function addUserTab() {
    userid = $('input[name="userid"]:visible').val();
    username = $('input[name="username"]:visible').val();
    serverid = $('input[name="serverid"]:visible').val();
    requestUserJson(userid, username, serverid)
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
             userid = message["userId"];
             username = message["userName"];
             serverid = message["serverId"];
             //add user tab
             var checked = false;
             var navs =  $(".nav-userbtn");
             for (var i =navs.length - 1; i >= 0; i--) {
                 if($(navs[i]).attr("userid") == userid){
                     checked = true;
                     if(!$(navs[i]).hasClass("")){
                         $(navs[i]).click();
                     }
                     break;
                 }
             }
             if(!checked){
                 var text = "user"+userid;
                 var $el = $( '<a href="#" userid="'+userid+'" onclick="requestUserJson(this, '+userid+', '+username+', '+serverid+')" class="nav-userbtn nav-btn ui-btn ui-btn-inline">' + text + "</a>" );
                 $("#new-usertab").after($el);
                 $el.buttonMarkup();
                 $( "#user-controlgroup" ).controlgroup( "refresh" );
                 $(".nav-userbtn").removeClass("nav-btn-active");
                 $($(".nav-userbtn")[0]).addClass("nav-btn-active");
                 // $($(".nav-userbtn")[0]).click()
             }
             json = message;
             printJSON();
             $('#editor').jsonEditor(json, { change: updateJSON, propertyclick: showPath });
            ////
//            if (message > 0) {
                alert(message);
//            }
        },
        error: function (message) {
            alert("提交数据失败！\n"+message);
        }
    });
}

function buildUserJson(){
	var json = {};
	json["userId"] = userid;
	json["userName"] = username;
	json["serverId"] = serverid;
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
    return json;
}

function printJSON() {
    $('#json').val(JSON.stringify(json));

}

function updateJSON(data) {
    json = data;
    printJSON();
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

    $('#json').change(function() {
        var val = $('#json').val();

        if (val) {
            try { json = JSON.parse(val); }
            catch (e) { alert('Error in parsing json. ' + e); }
        } else {
            json = {};
        }
        
        $('#editor').jsonEditor(json, { change: updateJSON, propertyclick: showPath });
    });

    $('#expander').click(function() {
        var editor = $('#editor');
        editor.toggleClass('expanded');
        $(this).text(editor.hasClass('expanded') ? 'Collapse' : 'Expand all');
    });
    
    printJSON();
    $('#editor').jsonEditor(json, { change: updateJSON, propertyclick: showPath });
});


