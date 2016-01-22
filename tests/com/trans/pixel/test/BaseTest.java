package com.trans.pixel.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.log4j.Logger;

import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.utils.HTTPProtobufResolver;
import com.trans.pixel.utils.HttpUtil;

public class BaseTest {
	final public static String PROPERTIES = "Properties/";
	private static Logger logger = Logger.getLogger(BaseTest.class);
  
    //define device user
    protected static final int GAME_VERSION = 1;
    protected static final int VERSION = 1;
    protected static final int SERVER_ID = 5;
    protected static final String ACCOUNT = "cyb";
    protected static final String NICKNAME = "TESTER";
    protected static final String DEVICE_ID = "iphone5";
    protected static final String SESSION = "aa68d03cb47b25b797ba4e06269c3079";
    protected static final String TOKEN = "";
    protected static final long USER_ID = 28;
    protected static final String USER_NAME = "cyb";
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
    
    protected static final String url = "http://118.192.77.33:8082/Lol450/gamedata";
//    protected static final String url = "http://127.0.0.1:8080/pixel/gamedata";
    protected static final HttpUtil<ResponseCommand> http = new HttpUtil<ResponseCommand>(new HTTPProtobufResolver());

    protected static void initTestData() {
        
    }
    
    protected static HeadInfo head() {
        HeadInfo.Builder head = HeadInfo.newBuilder();
        head.setGameVersion(GAME_VERSION);
        head.setAccount(ACCOUNT);
        head.setServerId(SERVER_ID);
        head.setUserId(USER_ID);
        head.setVersion(VERSION);
        head.setDatetime((new Date()).getTime());
        return head.build();
    }
    
    protected static void WriteToFile(String msg, String fileName){
		File file = new File(PROPERTIES+fileName);
		if (!file.exists()) {
			try {
				file.createNewFile(); // 创建文件
			} catch (IOException e) {
				logger.error("Fail to create file:"+fileName);
				return;
			}
		}

		// 向文件写入内容(输出流)
//		byte bt[] = new byte[1024];
//		bt = msg.getBytes();
		try {
			FileOutputStream in = new FileOutputStream(file);
			try {
				in.write(msg.getBytes(), 0, msg.length());
				in.close();
				// System.out.println("写入文件成功");
			} catch (IOException e) {
				logger.error("Fail to write file:"+fileName);
			}
		} catch (FileNotFoundException e) {
			logger.error("Fail to find file:"+fileName);
		}
		try {
			// 读取文件内容 (输入流)
			FileInputStream out = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(out);
			int ch = 0;
			while ((ch = isr.read()) != -1) {
				System.out.print((char) ch);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
    }
    protected static void WriteToFile(String msg){
    	WriteToFile(msg, "default");
    }
    
    protected static String ReadFromFile(String fileName){
		String msg = "";
		File file = new File(PROPERTIES + fileName);
		if (!file.exists()) {
			logger.error("Fail to find file:" + fileName);
			return msg;
		}
		try {
			// 读取文件内容 (输入流)
			FileInputStream out = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(out);
			int ch = 0;
			while ((ch = isr.read()) != -1) {
				// System.out.print((char) ch);
				msg += (char) ch;
			}
		} catch (Exception e) {
			logger.error("Fail to read file:" + fileName);
		}
		return msg;
    }
    protected static String ReadFromFile(){
    	return ReadFromFile("default");
    }
}
