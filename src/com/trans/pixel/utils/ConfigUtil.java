package com.trans.pixel.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trans.pixel.constants.DirConst;

public class ConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    private static Properties readProps(String filename, Properties props) {//绝对路径
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

    public static Properties readProps2(String fileName, Properties props) {//相对路径
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
    
    public static final String SERVER;
    public static final boolean CRONTAB_STATUS;
    public static final boolean IS_MASTER;
    
    public static final String MASTER_SERVER;
    
    static {
        Properties props = new Properties();
        readProps("/var/www/html/Pixel/ld/ld.properties", props);

        SERVER = props.getProperty("server");
        CRONTAB_STATUS = Boolean.valueOf(props.getProperty("crontab_status"));
        IS_MASTER = Boolean.valueOf(props.getProperty("is_master"));
        logger.debug("crontab_status is:" + CRONTAB_STATUS);
    }
    
    static {
        Properties props = new Properties();
        readProps2("/config/advancer.properties", props);

        MASTER_SERVER = props.getProperty("masterserver");
        logger.debug("masterserver is:" + MASTER_SERVER);
    }
}
