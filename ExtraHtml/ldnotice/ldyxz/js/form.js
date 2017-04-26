/**
 * 
 */
$(function() {
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