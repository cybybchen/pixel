package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestLoginCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.test.pressure.TimeBean;
import com.trans.pixel.utils.HTTPProtobufResolver;
import com.trans.pixel.utils.HTTPStringResolver;
import com.trans.pixel.utils.HttpUtil;

public class BaseTest {
  
    //define device user
    protected static int GAME_VERSION = 1;
    protected static int VERSION = 1;
    protected static int SERVER_ID = 1;
    protected static String ACCOUNT = "chli4";
    protected static String USER_NAME = ACCOUNT;
    protected static long USER_ID = 57;
    protected static String DEVICE_ID = "iphone5";
    protected static String SESSION = "";
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
    
    protected static final String defaultUrl = "http://123.59.144.200:8082/Lol450/gamedata";
    protected static String url;
    protected static boolean extraLog = true;
    protected HeadInfo head = null;
    protected UserInfo user = null;
    protected static final HttpUtil<ResponseCommand> http = new HttpUtil<ResponseCommand>(new HTTPProtobufResolver());
    protected static final HttpUtil<String> strhttp = new HttpUtil<String>(new HTTPStringResolver());

    protected static void initTestData() {
        
    }

    protected String headurl() {
    	String serverurl = null;
    	Properties props = new Properties();
        try {
         InputStream in = getClass().getResourceAsStream("/config/advancer.properties");
         props.load(in);
         serverurl = props.getProperty ("serverurl");
        } catch (Exception e) {
         e.printStackTrace();
        }
		if (serverurl == null) 
			serverurl = defaultUrl;
		return serverurl;
    }
    
    protected String manageurl() {
    	String serverurl = headurl();
    	url = serverurl.replace("/gamedata", "/datamanager");
    	return url;
    }
    
    protected HeadInfo head() {
    	if(head != null)
    		return head;
    	else
    		return head(ACCOUNT, SERVER_ID);
    }

    protected HeadInfo head(String account, int serverId) {
		if (url == null) {
			url = headurl();
			System.out.println("test server:" + url);
		}
        HeadInfo.Builder builder = HeadInfo.newBuilder();
        builder.setGameVersion(GAME_VERSION);
        builder.setAccount(account);
        builder.setServerId(serverId);
        builder.setUserId(USER_ID);
        builder.setVersion(VERSION);
        builder.setSession(SESSION);
        builder.setDatetime(new Date().getTime());
        head = builder.build();
        return head;
    }

    public RequestCommand getRequestCommand(){
    	return getRequestCommand(head());
    }
    
    public RequestCommand getRequestCommand(HeadInfo head){
    	RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head);
		return builder.build();
    }
    
	public ResponseCommand login() {
		return login(getRequestCommand());
	}

	public ResponseCommand login(RequestCommand request) {
		RequestLoginCommand.Builder b = RequestLoginCommand.newBuilder();
        ResponseCommand response = request("loginCommand", b.build(), request);
        if(response.hasErrorCommand() && response.getErrorCommand().getCode().equals("1000")){
        	if(extraLog)	System.out.println(request.getHead().getAccount()+":"+response.getErrorCommand().getMessage()+"，重新注册");
    		RequestRegisterCommand.Builder registerbuilder = RequestRegisterCommand.newBuilder();
    		registerbuilder.setUserName(head().getAccount());
    		registerbuilder.setHeroId(42);
    		response = request("registerCommand", registerbuilder.build(), request);
        }
        if(response.hasUserInfoCommand()){
//        	ResponseCommand.Builder responsebuilder = ResponseCommand.newBuilder(response);
        	HeadInfo.Builder headbuilder  = HeadInfo.newBuilder(response.getHead());
        	user = response.getUserInfoCommand().getUser();
        	headbuilder.setUserId(USER_ID = user.getId());
        	headbuilder.setSession(SESSION = user.getSession());
        	head = headbuilder.build();
//        	responsebuilder.setHead(headbuilder);
//        	response = responsebuilder.build();
        	if(extraLog)	System.out.println(response.getUserInfoCommand().getUser().getName()+"登陆成功");
        }else if(response.hasErrorCommand()){
        	System.out.println(response.getErrorCommand().getMessage());
        }else{
        	System.out.println("登陆错误");
        }
        return response;
	}
	
	public static Map<String, TimeBean> timemap = new ConcurrentHashMap<String, TimeBean>();
	public void addRequestTime(String key, long msec){
		TimeBean bean = timemap.get(key);
		if(bean == null){
			bean = new TimeBean();
			bean.setKey(key);
		}
		bean.setTime(bean.getTime()+1);
		if(msec > 0){
			bean.setSuccess(bean.getSuccess()+1);
			bean.setMsec(bean.getMsec()+msec);
		}
		timemap.put(key, bean);
	}

	public ResponseCommand request(String name, Object value) {
		return request(name, value, getRequestCommand());
	}
	
	public ResponseCommand request(String name, Object value, RequestCommand request) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(request);
		setField(name, value, builder);
		
		long time = System.currentTimeMillis();
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        if(response == null)
        	addRequestTime(name, 0);
        else if(response.hasErrorCommand()){
        	if(!response.getErrorCommand().getCode().equals("1000")){
	        	addRequestTime(name, 0);
	        	System.out.println(name+":"+response.getErrorCommand().getMessage());
	        }
        }else{
        	addRequestTime(name, System.currentTimeMillis()-time);
        	if(extraLog){
        		System.out.println(response.getAllFields());
        		System.out.println("request time:"+(System.currentTimeMillis()-time));
        	}
        }
        
        return response;
	}
	
	public static boolean setField(String name, Object value, Message.Builder builder) {
		try {
			JsonFormat.setField(name, value, builder);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
