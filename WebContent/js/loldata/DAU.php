<?php
require_once('BaseController.php');
error_reporting(E_ERROR);

class DAU extends BaseController {
	function getDAU($time) {
		$sql = "select * from heartbeat_log where date( time ) = \"$time\";";
		return $this->dao->db->query($sql);
	}
}

function caltime($time) {
	$data = strtotime($time);
	if($data == false || $data == -1)
		return time()*1000;
	else
		return $data*1000;
}

$time = $_GET["time"];
$dau = new DAU();
$db = $dau->getDAU($time);
$result = array();
$servertime = caltime($db[0]["time"]);
$count = 0;
foreach($db as $value){
	$data["x"] = caltime($value["time"]);
	$data["y"] = (int)$value["count"];
	$result[$value["serverid"]]["name"] = $value["serverid"];
	$result[$value["serverid"]]["data"][] = $data;
	if($data["x"] == $servertime){
		$count += $data["y"];
	}else{
		$data["x"] = $servertime;
		$data["y"] = $count;
		$result["10000"]["name"] = "all";
		$result["10000"]["data"][] = $data;
		$servertime = caltime($value["time"]);
		$count = (int)$value["count"];
	}
}
$data["x"] = $servertime;
$data["y"] = $count;
$result["10000"]["name"] = "all";
$result["10000"]["data"][] = $data;
ksort($result);
$array = array();
foreach($result as $value){
	$array[] = $value;
}

header('Access-Control-Allow-Origin: *');
print_r(json_encode($array));

?>
