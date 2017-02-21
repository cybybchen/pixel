package com.trans.pixel.constants;

public class DirConst {
	public static final String CONFIG_FILE_BASE_DIR = "/var/www/html/Pixel/conf_xml_test/";//ck:_ck newversion:_newversion
	
	public static String getConfigXmlPath(String fileName) {
		return CONFIG_FILE_BASE_DIR + fileName;
	}
}
