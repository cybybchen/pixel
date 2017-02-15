<?php
require_once('lib/db.class.php');
class BaseController {
	public $dao;

    	public function __construct()
    	{
        	$this->dao = DAO::getinstance('lol450_ck_log');
    	}
}
?>
