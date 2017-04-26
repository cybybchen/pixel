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
  	<h1>公告查询</h1>
  </div>

  <div data-role="content">
	<form id="formid" name="form" method="post">
		<div class="channels" style="text-align:center;">
		<?php
			include("select.html");
		?>
		</div>
		<div style="margin:30px;text-align:center"> 
	     		<input type="submit" style="font-size:18px;" data-inline="true" value="查询" >
		</div>

<?php
        require_once("common.php");
        if (isset($_POST["select"]) && $_POST['select'] != "") {
                $content = readFileContent($_POST['select']);
                echo "<div class='content' style='text-align:center;margin:100px'>";
                echo "        <h2>公告内容</h2>";
                echo "        <textarea type='text' style='height:158px;width:500px' name='content'>$content</textarea>";
                echo "</div>";
                echo "<div style='margin:30px;text-align:center;'>";
                echo "        <input type='button' style='font-size:18px;' data-inline='true' value='修改' onclick='sub()'>";
                echo "</div>";
        }
?>

    	</form>
  </div>
</div>
</body>
</html>
