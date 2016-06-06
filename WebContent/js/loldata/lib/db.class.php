<?php
class DAO{
    public $db;
    private static $instance;
    public function __construct($selectdb){
        $this->db=new mysqldb($selectdb);
    }
    public static function getinstance($selectdb){
        if(!self::$instance){
            $c=__CLASS__;
            self::$instance=new $c($selectdb);
        }
        return self::$instance;
    }
	public static function switchdb($selectdb) {
		$c=__CLASS__;
		self::$instance=new $c($selectdb);

		return self::$instance;
	}
}

class mysqldb{
    private $m_instance;
    public function __construct($selectdb){
      return   $this->connect($selectdb);
    }
    private function connect($database_name){
        $conn=mysql_connect('10.9.24.74','root','alazhu@2908');
//        $conn=mysql_connect('localhost','root','');
	mysql_query("set names 'utf8' ");
	//mysql_query("set character_set_client=utf8");
	//mysql_query("set character_set_results=utf8");
        mysql_select_db($database_name,$conn);
        return $this->m_instance=$conn;
    }

    public function query($sql){
        $result=mysql_query($sql,$this->m_instance);
        return $this->fetch($result);
    }
    private function fetch($result){
        $arr=array();
        while($row = mysql_fetch_array($result,MYSQL_ASSOC)){
            $arr[]=$row;
        }
        return $arr;
    }
    public  function close(){
        mysql_close($this->m_instance);
    }

}
?>
