package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.EquipmentBean;
import com.trans.pixel.protoc.Commands.Chip;
import com.trans.pixel.protoc.Commands.ChipList;

@Service
public class EquipRedisService extends RedisService {
	private static final String CHIP_FILE_NAME = "lol_chip.xml";
	@Resource
	public RedisTemplate<String, String> redisTemplate;
	
	public List<EquipmentBean> getEquipList() {
		return redisTemplate.execute(new RedisCallback<List<EquipmentBean>>() {
			@Override
			public List<EquipmentBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.EQUIP_KEY);
				
				List<EquipmentBean> list = new ArrayList<EquipmentBean>();
				Set<Entry<String, String>> set = bhOps.entries().entrySet();
				Iterator<Entry<String, String>> itr = set.iterator();
				while(itr.hasNext()) {
					Entry<String, String> entry = itr.next();
					EquipmentBean bean = EquipmentBean.fromJson(entry.getValue());
					if (bean != null) {
						list.add(bean);
					}
				}
				
				return list;
			}
		
		});
	}
	
	public void setEquipList(final List<EquipmentBean> list) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.EQUIP_KEY);
				
				for (EquipmentBean bean : list) {
					bhOps.put("" + bean.getItemid(), bean.toJson());
				}
				
				return null;
			}
		
		});
	}
	
	public EquipmentBean getEquip(final int id) {
		return redisTemplate.execute(new RedisCallback<EquipmentBean>() {
			@Override
			public EquipmentBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.EQUIP_KEY);
				
				EquipmentBean bean = EquipmentBean.fromJson(bhOps.get("" + id));
				return bean;
			}
		
		});
	}
	
	public Chip getChip(int itemId) {
		String value = hget(RedisKey.CHIP_KEY, "" + itemId);
		if (value == null) {
			Map<String, Chip> chipConfig = getChipConfig();
			return chipConfig.get("" + itemId);
		} else {
			Chip.Builder builder = Chip.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Chip> getChipConfig() {
		Map<String, String> keyvalue = hget(RedisKey.CHIP_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Chip> map = buildChipConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Chip> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.CHIP_KEY, redismap);
			return map;
		}else{
			Map<String, Chip> map = new HashMap<String, Chip>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Chip.Builder builder = Chip.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Chip> buildChipConfig(){
		String xml = ReadConfig(CHIP_FILE_NAME);
		ChipList.Builder builder = ChipList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + CHIP_FILE_NAME);
			return null;
		}
		
		Map<String, Chip> map = new HashMap<String, Chip>();
		for(Chip.Builder chip : builder.getChipBuilderList()){
			map.put("" + chip.getItemid(), chip.build());
		}
		return map;
	}
}
