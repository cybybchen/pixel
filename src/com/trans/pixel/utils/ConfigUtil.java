package com.trans.pixel.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.model.ServerBean;

public class ConfigUtil {
	private static final Logger logger = LoggerFactory
			.getLogger(ConfigUtil.class);
	private static final HttpUtil<String> strhttp = new HttpUtil<String>(new HTTPStringResolver());
	
	private static Properties readProps(String filename, Properties props) {// 绝对路径
		InputStream is = null;
		try {
			is = new FileInputStream(filename);
			props.load(is);
		} catch (Exception e) {
			logger.error("Error loading ldyxz.properties", e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
			}
		}
		return props;
	}

	public static Properties readProps2(String fileName, Properties props) {// 相对路径
		InputStream is = null;
		try {
			is = DirConst.class.getResourceAsStream(fileName);
			props.load(is);
		} catch (Exception e) {
			logger.error("Error loading advancer.properties", e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
			}
		}
		return props;
	}

	public static  String SERVER;
	public static  boolean CRONTAB_STATUS;
	public static  boolean IS_MASTER;
	
	public static  String MASTER_SERVER;
	public static  String LOGIN_SERVER;
	
	public static  Map<Integer, ServerBean> SERVER_MAP;
	static {
		try {
			Properties props = new Properties();
			readProps("/var/www/html/Pixel/ld/ld.properties", props);
	
			SERVER = props.getProperty("server");
			CRONTAB_STATUS = Boolean.valueOf(props.getProperty("crontab_status"));
			IS_MASTER = Boolean.valueOf(props.getProperty("is_master"));
			logger.warn("crontab_status is:" + CRONTAB_STATUS);
		} catch (Throwable t) {
			logger.error("parse ld.properties failed");
		}
	}

	static {
		try {
			Properties props = new Properties();
			readProps2("/config/advancer.properties", props);
	
			MASTER_SERVER = props.getProperty("masterserver");
			LOGIN_SERVER = props.getProperty("loginserver");
			logger.debug("masterserver is:" + MASTER_SERVER);
			logger.debug("LOGIN_SERVER is:" + LOGIN_SERVER);
		} catch (Throwable t) {
			logger.error("parse advancer.properties failed");
		}
	}

	static {
		try {
			SERVER_MAP = new HashMap<Integer, ServerBean>();
			String serverContent = strhttp.get(LOGIN_SERVER);
			JSONObject json = JSONObject.fromObject(serverContent);
			logger.debug("json is:" + json.toString());
			
			JSONArray array = json.getJSONArray("server");
			logger.debug("server is:" + array);
			
			for (int i = 0; i < array.size(); ++ i) {
				ServerBean serverBean = ServerBean.build(array.getString(i));
				SERVER_MAP.put(serverBean.getServerId(), serverBean);
			}
			
			logger.debug("server map is" + SERVER_MAP);
		} catch (Throwable t) {
			logger.error("get serverlist failed");
		}
	}
	
	public static ServerBean getServer(int serverId) {
		return SERVER_MAP.get(serverId);
	}
}
