package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

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
	
	public TaskRedisService() {
		buildTask1TargetConfig();
		buildTask2TargetConfig();
		buildTask3OrderConfig();
	}
	
	//task1
	public TaskTarget getTask1Target(int targetId) {
		Map<Integer, TaskTarget> map = hgetcache(RedisKey.TASK1_CONFIG_KEY);
			return map.get(targetId);
	}
	
	public Map<Integer, TaskTarget> getTask1TargetConfig() {
		Map<Integer, TaskTarget> map = hgetcache(RedisKey.TASK1_CONFIG_KEY);
		return map;
	}
	
	private void buildTask1TargetConfig(){
		String xml = RedisService.ReadConfig(TASK1_FILE_NAME);
		TaskTargetList.Builder builder = TaskTargetList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, TaskTarget> map = new HashMap<Integer, TaskTarget>();
		Map<Integer, TaskOrder> ordermap = new HashMap<Integer, TaskOrder>();
		for(TaskTarget.Builder task : builder.getDataBuilderList()){
			map.put(task.getTargetid(), task.build());
			for (TaskOrder.Builder order : task.getOrderBuilderList()) {
				order.setTargetid(task.getTargetid());
				ordermap.put(order.getOrder(), order.build());
			}
		}
		hputcacheAll(RedisKey.TASK1_ORDER_CONFIG_KEY, ordermap);
		hputcacheAll(RedisKey.TASK1_CONFIG_KEY, map);
	}
	
	//task1 order
	public TaskOrder getTask1Order(int order) {
		Map<Integer, TaskOrder> map = hgetcache(RedisKey.TASK1_ORDER_CONFIG_KEY);
		return map.get(order);
	}
	
	//task3
	public TaskOrder getTask3Order(int order) {
		Map<Integer, TaskOrder> map = hgetcache(RedisKey.TASK3_CONFIG_KEY);
		return map.get(order);
	}
	
	public Map<Integer, TaskOrder> getTask3OrderConfig() {
		Map<Integer, TaskOrder> map = hgetcache(RedisKey.TASK3_CONFIG_KEY);
		return map;
	}
	
	private Map<Integer, TaskOrder> buildTask3OrderConfig(){
		String xml = RedisService.ReadConfig(TASK3_FILE_NAME);
		Task3OrderList.Builder builder = Task3OrderList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TASK3_FILE_NAME);
			return null;
		}
		
		Map<Integer, TaskOrder> map = new HashMap<Integer, TaskOrder>();
		for(TaskOrder.Builder task : builder.getDataBuilderList()){
			map.put(task.getOrder(), task.build());
		}
		hputcacheAll(RedisKey.TASK3_CONFIG_KEY, map);
		
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
	
	public Map<Integer, Task2TargetHero> getTask2TargetConfig() {
		Map<Integer, Task2TargetHero> map = hgetcache(RedisKey.TASK2_CONFIG_KEY);
		return map;
	}
	
	private Map<Integer, Task2TargetHero> buildTask2TargetConfig(){
		String xml = RedisService.ReadConfig(TASK2_FILE_NAME);
		Task2TargetList.Builder builder = Task2TargetList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + TASK2_FILE_NAME);
			return null;
		}
		
		Map<Integer, Task2TargetHero> map = new HashMap<Integer, Task2TargetHero>();
		for (Task2TargetHero.Builder hero : builder.getDataBuilderList()) {
			map.put(hero.getHeroid(), hero.build());
			Map<Integer, TaskOrder> ordermap = new HashMap<Integer, TaskOrder>();
			for (TaskTarget.Builder task : hero.getTargetBuilderList()) {
				for (TaskOrder.Builder order : task.getOrderBuilderList()) {
					order.setTargetid(task.getTargetid());
					ordermap.put(order.getOrder(), order.build());
				}
			}
			hputcacheAll(RedisKey.TASK2_ORDER_CONFIG_PREFIX + hero.getHeroid(), ordermap);
		}
		hputcacheAll(RedisKey.TASK2_CONFIG_KEY, map);
		
		return map;
	}
	
	//task2 order
	public TaskOrder getTask2Order(int order, int heroId) {
		Map<Integer, TaskOrder> map = hgetcache(RedisKey.TASK2_ORDER_CONFIG_PREFIX + heroId);
		TaskOrder value = map.get(order);
		if (value == null) {
			buildTask2TargetConfig();
			map = hgetcache(RedisKey.TASK2_ORDER_CONFIG_PREFIX + heroId);
			value = map.get(order);
		} 
		
//		TaskOrder.Builder builder = TaskOrder.newBuilder();
//		if(RedisService.parseJson(value, builder))
//			return builder.build();
		
		return value;
	}
}
