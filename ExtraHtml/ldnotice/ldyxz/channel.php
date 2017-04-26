<?php
	$path = "/var/www/html/ldyxz/notice/".$_POST['select'];
	mkFolder($path);
	$filename = $path.'/content.txt';
	$file = fopen($filename, "w");
	fwrite($file,var_export($_POST['content'],"true"));
	fclose($file);
	//mkFolder($filename);
		
	


	function mkFolder($path)  
	{
		if(!is_readable($path))  
		{
			is_file($path) or mkdir($path, 0700);  
		}  
	}  
?>
