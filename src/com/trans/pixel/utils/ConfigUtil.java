package com.trans.pixel.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    private static Properties readProps(String filename, Properties props) {
//        InputStream is = ConfigUtil.class.getResourceAsStream(filename);
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

    public static final String SERVER;
    public static final Boolean CRONTAB_STATUS;
    
    static {
        Properties props = new Properties();
        readProps("/opt/ld/ld.properties", props);

        SERVER = props.getProperty("server");
        CRONTAB_STATUS = Boolean.valueOf(props.getProperty("crontab_status"));
        logger.debug("crontab_status is:" + CRONTAB_STATUS);
    }
}
