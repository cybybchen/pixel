package com.trans.pixel.test;

import java.util.Date;

import org.apache.log4j.Logger;

import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.HTTPProtobufResolver;
import com.trans.pixel.utils.HttpUtil;

public class BaseTest {
	private static Logger logger = Logger.getLogger(BaseTest.class);
  
    //define device user
    protected static final int GAME_VERSION = 1;
    protected static final int VERSION = 1;
    protected static final int SERVER_ID = 1;
    protected static final String ACCOUNT = "ybchen";
    protected static final String NICKNAME = "TESTER";
    protected static final String DEVICE_ID = "iphone5";
    protected static final String SESSION = "aa68d03cb47b25b797ba4e06269c3079";
    protected static final String TOKEN = "";
    protected static final long USER_ID = 28;
    protected static final String USER_NAME = "hahaha";
    protected static final String SCORE = "100";
    protected static final String ROOM = "2";
    protected static final String CARID = "carid";
    protected static final String MAPID = "event_hl_01";
    protected static final String RET = "1";
    protected static final String STARNUM = "10";
    protected static final String HEADID = "3";
    protected static final String CARPERFORM = "10";
    protected static final String PAINTID = "5";
    protected static final String FILE = "notice_1329";
    
    protected static final String defaultUrl = "http://118.192.77.33:8082/Lol450/gamedata";
    protected static String url;
    protected static final HttpUtil<ResponseCommand> http = new HttpUtil<ResponseCommand>(new HTTPProtobufResolver());

    protected static void initTestData() {
        
    }
    
    protected static HeadInfo head() {
    	if(url == null)
    		url = RedisService.ReadProperties("serverurl");
    	if(url.length() == 0)
    		url = defaultUrl;
        HeadInfo.Builder head = HeadInfo.newBuilder();
        head.setGameVersion(GAME_VERSION);
        head.setAccount(ACCOUNT);
        head.setServerId(SERVER_ID);
        head.setUserId(USER_ID);
        head.setVersion(VERSION);
        head.setDatetime((new Date()).getTime());
        return head.build();
    }
}
