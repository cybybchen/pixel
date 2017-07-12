package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.TaskProto.Task2TargetHero;
import com.trans.pixel.protoc.TaskProto.Task2TargetList;
import com.trans.pixel.protoc.TaskProto.Task3OrderList;
import com.trans.pixel.protoc.TaskProto.TaskOrder;
import com.trans.pixel.protoc.TaskProto.TaskTarget;
import com.trans.pixel.protoc.TaskProto.TaskTargetList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class TaskRedisService extends CacheService {
	private static Logger logger = Logger.getLogger(TaskRedisService.class);
	private static final String TASK1_FILE_NAME = "ld_task1.xml";
	private static final String TASK2_FILE_NAME = "ld_task2.xml";
	private static final String TASK3_FILE_NAME = "ld_task3.xml";
	
	//task1
	public TaskTarget getTask1Target(int targetId) {
		String value = hget(RedisKey.TASK1_CONFIG_KEY, "" + targetId);
		if (value == null) {
			Map<String, TaskTarget> config = getTask1TargetConfig();
			return config.get("" + targetId);
		} else {
			TaskTarget.Builder builder = TaskTarget.newBuilder();
			if(RedisService.parseJson(value, builder))
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
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.TASK1_CONFIG_KEY, redismap);
			return map;
		}else{
			Map<String, TaskTarget> map = new HashMap<String, TaskTarget>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				TaskTarget.Builder builder = TaskTarget.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, TaskTarget> buildTask1TargetConfig(){
		String xml = RedisService.ReadConfig(TASK1_FILE_NAME);
		TaskTargetList.Builder builder = TaskTargetList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TASK1_FILE_NAME);
			return null;
		}
		
		Map<String, TaskTarget> map = new HashMap<String, TaskTarget>();
		Map<String, String> redismap = new HashMap<String, String>();
		for(TaskTarget.Builder task : builder.getDataBuilderList()){
			map.put("" + task.getTargetid(), task.build());
			for (TaskOrder.Builder order : task.getOrderBuilderList()) {
				order.setTargetid(task.getTargetid());
				redismap.put("" + order.getOrder(), RedisService.formatJson(order.build()));
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
		if(RedisService.parseJson(value, builder))
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
			if(RedisService.parseJson(value, builder))
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
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.TASK3_CONFIG_KEY, redismap);
			return map;
		}else{
			Map<String, TaskOrder> map = new HashMap<String, TaskOrder>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				TaskOrder.Builder builder = TaskOrder.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, TaskOrder> buildTask3OrderConfig(){
		String xml = RedisService.ReadConfig(TASK3_FILE_NAME);
		Task3OrderList.Builder builder = Task3OrderList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TASK3_FILE_NAME);
			return null;
		}
		
		Map<String, TaskOrder> map = new HashMap<String, TaskOrder>();
		for(TaskOrder.Builder task : builder.getDataBuilderList()){
			map.put("" + task.getOrder(), task.build());
		}
		return map;
	}
	
	//task2
//	private Task2TargetHero getTask2TargetHero(int targetId) {
//		String value = hget(RedisKey.TASK2_CONFIG_KEY, "" + targetId);
//		if (value == null) {
//			Map<String, Task2TargetHero> config = getTask2TargetConfig();
//			return config.get("" + targetId);
//		} else {
//			Task2TargetHero.Builder builder = Task2TargetHero.newBuilder();
//			if(parseJson(value, builder))
//				return builder.build();
//		}
//		
//		return null;
//	}
	
	public Map<String, Task2TargetHero> getTask2TargetConfig() {
		Map<String, String> keyvalue = hget(RedisKey.TASK2_CONFIG_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Task2TargetHero> map = buildTask2TargetConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Task2TargetHero> entry : map.entrySet()){
				redismap.put(entry.getKey(), RedisService.formatJson(entry.getValue()));
			}
			hputAll(RedisKey.TASK2_CONFIG_KEY, redismap);
			return map;
		}else{
			Map<String, Task2TargetHero> map = new HashMap<String, Task2TargetHero>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Task2TargetHero.Builder builder = Task2TargetHero.newBuilder();
				if(RedisService.parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Task2TargetHero> buildTask2TargetConfig(){
		String xml = RedisService.ReadConfig(TASK2_FILE_NAME);
		Task2TargetList.Builder builder = Task2TargetList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TASK2_FILE_NAME);
			return null;
		}
		
		Map<String, Task2TargetHero> map = new HashMap<String, Task2TargetHero>();
		for (Task2TargetHero.Builder hero : builder.getDataBuilderList()) {
			map.put("" + hero.getHeroid(), hero.build());
			Map<String, String> redismap = new HashMap<String, String>();
			for (TaskTarget.Builder task : hero.getTargetBuilderList()) {
				for (TaskOrder.Builder order : task.getOrderBuilderList()) {
					order.setTargetid(task.getTargetid());
					redismap.put("" + order.getOrder(), RedisService.formatJson(order.build()));
				}
			}
			hputAll(RedisKey.TASK2_ORDER_CONFIG_PREFIX + hero.getHeroid(), redismap);
		}
		
		return map;
	}
	
	//task2 order
	public TaskOrder getTask2Order(int order, int heroId) {
		String value = hget(RedisKey.TASK2_ORDER_CONFIG_PREFIX + heroId, "" + order);
		if (value == null) {
			buildTask2TargetConfig();
			value = hget(RedisKey.TASK2_ORDER_CONFIG_PREFIX + heroId, "" + order);
		} 
		
		TaskOrder.Builder builder = TaskOrder.newBuilder();
		if(RedisService.parseJson(value, builder))
			return builder.build();
		
		return null;
	}
}
