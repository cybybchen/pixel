<?php

	const REDIS_HOST = "10.66.138.124";
        const REDIS_PORT = "6379";
        const REDIS_PASSWORD = "crs-eyly0vmq:trans123";
	const REDIS_DB = 0;
	const REDIS_KEY = "channel_key";
	
	function store($array) {
		$redis = new Redis();
        	$redis->connect(REDIS_HOST, REDIS_PORT);
        	$redis->auth(REDIS_PASSWORD);
		$redis->select(REDIS_DB);

		$redis->hincrby(REDIS_KEY, $array['channel'], 1);	
		
		$redis->close();
	}
?>
