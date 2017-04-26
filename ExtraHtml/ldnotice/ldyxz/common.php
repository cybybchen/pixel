<?php
	const FILE_PATH = "/var/www/html/ldyxz/notice/";
	function readFileContent($channel, $method="r") {
		$path = FILE_PATH.$channel;
		$filename = $path.'/content.txt';
		$file = fopen($filename, $method);
		$content = fread($file,filesize($filename));
		fclose($file);

		return $content;
	}
?>
