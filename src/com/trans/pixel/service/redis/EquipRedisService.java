package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.EquipProto.Armor;
import com.trans.pixel.protoc.EquipProto.ArmorList;
import com.trans.pixel.protoc.EquipProto.Chip;
import com.trans.pixel.protoc.EquipProto.ChipList;
import com.trans.pixel.protoc.EquipProto.Equip;
import com.trans.pixel.protoc.EquipProto.EquipList;
import com.trans.pixel.protoc.EquipProto.Equiptucao;
import com.trans.pixel.protoc.EquipProto.EquiptucaoList;
import com.trans.pixel.protoc.EquipProto.Equipup;
import com.trans.pixel.protoc.EquipProto.EquipupList;
import com.trans.pixel.protoc.EquipProto.Material;
import com.trans.pixel.protoc.EquipProto.MaterialList;

@Service
public class EquipRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(EquipRedisService.class);
	private static final String CHIP_FILE_NAME = "ld_chip.xml";
	private static final String EQUIP_FILE_NAME = "ld_equip.xml";
	private static final String EQUIPUP_FILE_NAME = "ld_equipup.xml";
	private static final String EQUIPTUCAO_FILE_NAME = "lol_equiptucao.xml";
	private static final String ARMOR_FILE_NAME = "ld_armor.xml";
	private static final String MATERIAL_FILE_NAME = "ld_material.xml";
	
	public Chip getChip(int itemId) {
		String value = hget(RedisKey.CHIP_CONFIG, "" + itemId);
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
		Map<String, String> keyvalue = hget(RedisKey.CHIP_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Chip> map = buildChipConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Chip> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.CHIP_CONFIG, redismap);
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
		for(Chip.Builder chip : builder.getDataBuilderList()){
			map.put("" + chip.getItemid(), chip.build());
		}
		return map;
	}
	
	//equiptucao
	public Equiptucao getEquiptucao(int itemId) {
		String value = hget(RedisKey.EQUIP_TUCAO_CONFIG, "" + itemId);
		if (value == null) {
			Map<String, Equiptucao> config = getEquiptucaoConfig();
			return config.get("" + itemId);
		} else {
			Equiptucao.Builder builder = Equiptucao.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Equiptucao> getEquiptucaoConfig() {
		Map<String, String> keyvalue = hget(RedisKey.EQUIP_TUCAO_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Equiptucao> map = buildEquiptucaoConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Equiptucao> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.EQUIP_TUCAO_CONFIG, redismap);
			return map;
		}else{
			Map<String, Equiptucao> map = new HashMap<String, Equiptucao>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Equiptucao.Builder builder = Equiptucao.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Equiptucao> buildEquiptucaoConfig(){
		String xml = ReadConfig(EQUIPTUCAO_FILE_NAME);
		EquiptucaoList.Builder builder = EquiptucaoList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + EQUIPTUCAO_FILE_NAME);
			return null;
		}
		
		Map<String, Equiptucao> map = new HashMap<String, Equiptucao>();
		for(Equiptucao.Builder euiqptucao : builder.getEquipBuilderList()){
			map.put("" + euiqptucao.getItemid(), euiqptucao.build());
		}
		return map;
	}
	
	//equip
	public Equip getEquip(int itemId) {
		String value = hget(RedisKey.EQUIP_CONFIG, "" + itemId);
		if (value == null) {
			Map<String, Equip> equipConfig = getEquipConfig();
			return equipConfig.get("" + itemId);
		} else {
			Equip.Builder builder = Equip.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Equip> getEquipConfig() {
		Map<String, String> keyvalue = hget(RedisKey.EQUIP_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Equip> map = buildEquipConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Equip> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.EQUIP_CONFIG, redismap);
			return map;
		}else{
			Map<String, Equip> map = new HashMap<String, Equip>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Equip.Builder builder = Equip.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Equip> buildEquipConfig(){
		String xml = ReadConfig(EQUIP_FILE_NAME);
		EquipList.Builder builder = EquipList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + EQUIP_FILE_NAME);
			return null;
		}
		
		Map<String, Equip> map = new HashMap<String, Equip>();
		for(Equip.Builder chip : builder.getDataBuilderList()){
			map.put("" + chip.getId(), chip.build());
		}
		return map;
	}
	
	//armor
	public Armor getArmor(int itemId) {
		String value = hget(RedisKey.ARMOR_CONFIG, "" + itemId);
		if (value == null) {
			Map<String, Armor> config = getArmorConfig();
			return config.get("" + itemId);
		} else {
			Armor.Builder builder = Armor.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Armor> getArmorConfig() {
		Map<String, String> keyvalue = hget(RedisKey.ARMOR_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Armor> map = buildArmorConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Armor> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.ARMOR_CONFIG, redismap);
			return map;
		}else{
			Map<String, Armor> map = new HashMap<String, Armor>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Armor.Builder builder = Armor.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Armor> buildArmorConfig(){
		String xml = ReadConfig(ARMOR_FILE_NAME);
		ArmorList.Builder builder = ArmorList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + ARMOR_FILE_NAME);
			return null;
		}
		
		Map<String, Armor> map = new HashMap<String, Armor>();
		for(Armor.Builder armor : builder.getDataBuilderList()){
			map.put("" + armor.getId(), armor.build());
		}
		return map;
	}
	
	//equip
	public Material getMaterial(int itemId) {
		String value = hget(RedisKey.MATERIAL_CONFIG, "" + itemId);
		if (value == null) {
			Map<String, Material> config = getMaterialConfig();
			return config.get("" + itemId);
		} else {
			Material.Builder builder = Material.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Material> getMaterialConfig() {
		Map<String, String> keyvalue = hget(RedisKey.MATERIAL_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Material> map = buildMaterialConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Material> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.MATERIAL_CONFIG, redismap);
			return map;
		}else{
			Map<String, Material> map = new HashMap<String, Material>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Material.Builder builder = Material.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Material> buildMaterialConfig(){
		String xml = ReadConfig(MATERIAL_FILE_NAME);
		MaterialList.Builder builder = MaterialList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + MATERIAL_FILE_NAME);
			return null;
		}
		
		Map<String, Material> map = new HashMap<String, Material>();
		for(Material.Builder material : builder.getDataBuilderList()){
			map.put("" + material.getItemid(), material.build());
		}
		return map;
	}
	
	//equipup
	public Equipup getEquipup(int id) {
		String value = hget(RedisKey.EQUIPUP_CONFIG, "" + id);
		if (value == null) {
			Map<String, Equipup> equipConfig = getEquipupConfig();
			return equipConfig.get("" + id);
		} else {
			Equipup.Builder builder = Equipup.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Equipup> getEquipupConfig() {
		Map<String, String> keyvalue = hget(RedisKey.EQUIPUP_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, Equipup> map = buildEquipupConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Equipup> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.EQUIPUP_CONFIG, redismap);
			return map;
		}else{
			Map<String, Equipup> map = new HashMap<String, Equipup>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Equipup.Builder builder = Equipup.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Equipup> buildEquipupConfig(){
		String xml = ReadConfig(EQUIPUP_FILE_NAME);
		EquipupList.Builder builder = EquipupList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + EQUIPUP_FILE_NAME);
			return null;
		}
		
		Map<String, Equipup> map = new HashMap<String, Equipup>();
		for(Equipup.Builder equipup : builder.getDataBuilderList()){
			map.put("" + equipup.getId(), equipup.build());
		}
		return map;
	}
}
