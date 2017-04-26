<!DOCTYPE html>
<html>
<head>
	<?php
	include("head.html");
	?>
</head>

<body>
<div data-role="page" class="ui-page">
  <div data-role="header" style="text-align:center;">
  	<h1>公告配置</h1>
  </div>

  <div data-role="content">
	<form id="formid" name="form" method="post">
		<div class="channels" style="text-align:center;">
		<?php
			include("select.html");
		?>	
		</div>
		<div class="content" style="text-align:center;margin:100px">
			<h2>内容</h2>
 			<textarea type="text" style="height:158px;width:500px" name="content" value=""></textarea>
		</div>
		<div style="margin:0 auto;text-align:center"> 
	     		<input type="button" style="font-size:18px;" data-inline="true" value="提交"  onclick="sub()">
		</div>
    	</form>
  </div>
</div>
</body>
</html>
