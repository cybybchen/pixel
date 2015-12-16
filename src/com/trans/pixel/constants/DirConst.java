package com.trans.pixel.constants;

public class DirConst {
	public static final String CONFIG_FILE_BASE_DIR = "/var/www/html/pixel/conf_xml/";
	
	public static String getConfigXmlPath(String fileName) {
		return CONFIG_FILE_BASE_DIR + fileName;
	}
}
