package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.Task3OrderList;
import com.trans.pixel.protoc.Commands.TaskOrder;
import com.trans.pixel.protoc.Commands.TaskTarget;
import com.trans.pixel.protoc.Commands.TaskTargetList;

@Service
public class TaskRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(TaskRedisService.class);
	private static final String TASK1_FILE_NAME = "lol_task1.xml";
	private static final String TASK2_FILE_NAME = "lol_task2.xml";
	private static final String TASK3_FILE_NAME = "lol_task3.xml";
	
	//task1
	public TaskTarget getTask1Target(int targetId) {
		String value = hget(RedisKey.TASK1_CONFIG_KEY, "" + targetId);
		if (value == null) {
			Map<String, TaskTarget> config = getTask1TargetConfig();
			return config.get("" + targetId);
		} else {
			TaskTarget.Builder builder = TaskTarget.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, TaskTarget> getTask1TargetConfig() {
		Map<String, String> keyvalue = hget(RedisKey.TASK1_CONFIG_KEY);
		if(keyvalue.isEmpty()){
			Map<String, TaskTarget> map = buildTask1TargetConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, TaskTarget> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.TASK1_CONFIG_KEY, redismap);
			return map;
		}else{
			Map<String, TaskTarget> map = new HashMap<String, TaskTarget>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				TaskTarget.Builder builder = TaskTarget.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, TaskTarget> buildTask1TargetConfig(){
		String xml = ReadConfig(TASK1_FILE_NAME);
		TaskTargetList.Builder builder = TaskTargetList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + TASK1_FILE_NAME);
			return null;
		}
		
		Map<String, TaskTarget> map = new HashMap<String, TaskTarget>();
		Map<String, String> redismap = new HashMap<String, String>();
		for(TaskTarget.Builder task : builder.getTargetBuilderList()){
			map.put("" + task.getTargetid(), task.build());
			for (TaskOrder.Builder order : task.getOrderBuilderList()) {
				order.setTargetid(task.getTargetid());
				redismap.put("" + order.getOrder(), formatJson(order.build()));
			}
		}
		hputAll(RedisKey.TASK1_ORDER_CONFIG_KEY, redismap);
		return map;
	}
	
	//task1 order
	public TaskOrder getTask1Order(int order) {
		String value = hget(RedisKey.TASK1_ORDER_CONFIG_KEY, "" + order);
		if (value == null) {
			buildTask1TargetConfig();
			value = hget(RedisKey.TASK1_ORDER_CONFIG_KEY, "" + order);
		} 
		
		TaskOrder.Builder builder = TaskOrder.newBuilder();
		if(parseJson(value, builder))
			return builder.build();
		
		return null;
	}
	
	//task3
	public TaskOrder getTask3Order(int order) {
		String value = hget(RedisKey.TASK3_CONFIG_KEY, "" + order);
		if (value == null) {
			Map<String, TaskOrder> config = getTask3OrderConfig();
			return config.get("" + order);
		} else {
			TaskOrder.Builder builder = TaskOrder.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, TaskOrder> getTask3OrderConfig() {
		Map<String, String> keyvalue = hget(RedisKey.TASK3_CONFIG_KEY);
		if(keyvalue.isEmpty()){
			Map<String, TaskOrder> map = buildTask3OrderConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, TaskOrder> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.TASK3_CONFIG_KEY, redismap);
			return map;
		}else{
			Map<String, TaskOrder> map = new HashMap<String, TaskOrder>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				TaskOrder.Builder builder = TaskOrder.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, TaskOrder> buildTask3OrderConfig(){
		String xml = ReadConfig(TASK3_FILE_NAME);
		Task3OrderList.Builder builder = Task3OrderList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + TASK3_FILE_NAME);
			return null;
		}
		
		Map<String, TaskOrder> map = new HashMap<String, TaskOrder>();
		for(TaskOrder.Builder task : builder.getOrderBuilderList()){
			map.put("" + task.getOrder(), task.build());
		}
		return map;
	}
}
