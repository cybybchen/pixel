package com.trans.pixel.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileUtil {
	private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
	 /**
     * 功能：Java读取txt文件的内容
     * 步骤：1：先获得文件句柄
     * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
     * 3：读取到输入流后，需要读取生成字节流
     * 4：一行一行的输出。readline()。
     * 备注：需要考虑的是异常情况
     * @param filePath
     */
	public static List<String> readTxtFile(String filePath) {
		List<String> fileTetList = new ArrayList<String>();
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					fileTetList.add(lineTxt);
				}
				read.close();
			} else {
				log.error("找不到指定的文件:" + filePath);
			}
		} catch (Exception e) {
			log.error("读取文件内容出错" + filePath);
		}

		return fileTetList;
	}
     
//    public static void main(String argv[]){
//        String filePath = "/home/ybchen/workspace/pixel/test.txt";
//        readTxtFile(filePath);
//    }
}
