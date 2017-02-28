package com.trans.pixel.constants;

import java.io.InputStream;
import java.util.Properties;

public class DirConst {
	public static String CONFIG_FILE_BASE_DIR = null;//"/var/www/html/Pixel/conf_xml_test/";//ck:_ck newversion:_newversion
	
	public static String getConfigXmlPath(String fileName) {
		if(CONFIG_FILE_BASE_DIR == null){
			Properties props = new Properties();
			try {
				InputStream in = DirConst.class.getResourceAsStream("/config/advancer.properties");
				props.load(in);
				CONFIG_FILE_BASE_DIR = props.getProperty("configpath");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return CONFIG_FILE_BASE_DIR + fileName;
	}
}
