package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestLoginCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.utils.HTTPProtobufResolver;
import com.trans.pixel.utils.HttpUtil;

public class BaseTest {
	private static Logger logger = Logger.getLogger(BaseTest.class);
	public UserInfo user = null;
  
    //define device user
    protected static final int GAME_VERSION = 1;
    protected static final int VERSION = 1;
    protected static final int SERVER_ID = 1;
    protected static final String ACCOUNT = "chli4";
    protected static final String USER_NAME = ACCOUNT;
    protected static long USER_ID = 38;
//    protected static final String DEVICE_ID = "iphone5";
//    protected static final String SESSION = "aa68d03cb47b25b797ba4e06269c3079";
//    protected static final String TOKEN = "";
//    protected static final String SCORE = "100";
//    protected static final String ROOM = "2";
//    protected static final String CARID = "carid";
//    protected static final String MAPID = "event_hl_01";
//    protected static final String RET = "1";
//    protected static final String STARNUM = "10";
//    protected static final String HEADID = "3";
//    protected static final String CARPERFORM = "10";
//    protected static final String PAINTID = "5";
//    protected static final String FILE = "notice_1329";
    
    protected static final String defaultUrl = "http://118.192.77.33:8082/Lol450/gamedata";
    protected static String url;
    protected static final HttpUtil<ResponseCommand> http = new HttpUtil<ResponseCommand>(new HTTPProtobufResolver());

    protected static void initTestData() {
        
    }
    
    protected HeadInfo head() {
    	Properties props = new Properties();
        try {
         InputStream in = getClass().getResourceAsStream("/config/advancer.properties");
         props.load(in);
         if(url == null){
        	 url = props.getProperty ("serverurl");
        	 System.out.println("test server:"+url);
         }
        } catch (Exception e) {
         e.printStackTrace();
        }
//    	if(url == null)
//    		url = RedisService.ReadProperties("serverurl");
		if (url == null) {
			url = defaultUrl;
			System.out.println("test server:" + url);
		}
        HeadInfo.Builder head = HeadInfo.newBuilder();
        head.setGameVersion(GAME_VERSION);
        head.setAccount(ACCOUNT);
        head.setServerId(SERVER_ID);
        head.setUserId(USER_ID);
        head.setVersion(VERSION);
        head.setDatetime((new Date()).getTime());
        return head.build();
    }

	public void login() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestLoginCommand.Builder b = RequestLoginCommand.newBuilder();
		builder.setLoginCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        if(response.hasErrorCommand() && response.getErrorCommand().getCode().equals("1000")){
        	logger.warn(response.getErrorCommand().getMessage()+"，重新注册");
    		RequestRegisterCommand.Builder registerbuilder = RequestRegisterCommand.newBuilder();
    		registerbuilder.setUserName(head().getAccount());
    		builder.clearLoginCommand().setRegisterCommand(registerbuilder.build());
    		
    		reqcmd = builder.build();
    		reqData = reqcmd.toByteArray();
            input = new ByteArrayInputStream(reqData);
            response = http.post(url, input);
        }
        if(response.hasUserInfoCommand()){
        	user = response.getUserInfoCommand().getUser();
        	USER_ID = user.getId();
        	logger.warn(response.getUserInfoCommand().getUser().getName()+"登陆成功");
        }else if(response.hasErrorCommand()){
        	logger.error(response.getErrorCommand().getMessage());
        }else{
        	logger.error("登陆错误");
        }
	}
}
