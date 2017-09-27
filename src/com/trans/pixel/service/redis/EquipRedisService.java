package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.EquipProto.Armor;
import com.trans.pixel.protoc.EquipProto.ArmorList;
import com.trans.pixel.protoc.EquipProto.Chip;
import com.trans.pixel.protoc.EquipProto.ChipList;
import com.trans.pixel.protoc.EquipProto.Engine;
import com.trans.pixel.protoc.EquipProto.EngineList;
import com.trans.pixel.protoc.EquipProto.Equip;
import com.trans.pixel.protoc.EquipProto.EquipList;
import com.trans.pixel.protoc.EquipProto.Equiptucao;
import com.trans.pixel.protoc.EquipProto.EquiptucaoList;
import com.trans.pixel.protoc.EquipProto.Equipup;
import com.trans.pixel.protoc.EquipProto.EquipupList;
import com.trans.pixel.protoc.EquipProto.Material;
import com.trans.pixel.protoc.EquipProto.MaterialList;
import com.trans.pixel.protoc.EquipProto.Pet;
import com.trans.pixel.protoc.EquipProto.PetList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class EquipRedisService extends CacheService {
	private static Logger logger = Logger.getLogger(EquipRedisService.class);
	private static final String CHIP_FILE_NAME = "ld_chip.xml";
	private static final String EQUIP_FILE_NAME = "ld_equip.xml";
	private static final String EQUIPUP_FILE_NAME = "ld_equipup.xml";
	private static final String EQUIPTUCAO_FILE_NAME = "lol_equiptucao.xml";
	private static final String ARMOR_FILE_NAME = "ld_armor.xml";
	private static final String MATERIAL_FILE_NAME = "ld_material.xml";
	private static final String ENGINE_FILE_NAME = "ld_engine.xml";
	private static final String PET_FILE_NAME = "ld_pet.xml";
	
	public EquipRedisService() {
		buildChipConfig();
		buildEquipConfig();
		buildEquipupConfig();
//		buildEquiptucaoConfig();
		buildArmorConfig();
		buildMaterialConfig();
		buildEngineConfig();
		buildPetConfig();
	}
	
	public Chip getChip(int itemId) {
		Map<Integer, Chip> map = hgetcache(RedisKey.CHIP_CONFIG);
		return map.get(itemId);
	}
	
	private Map<Integer, Chip> buildChipConfig(){
		String xml = RedisService.ReadConfig(CHIP_FILE_NAME);
		ChipList.Builder builder = ChipList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + CHIP_FILE_NAME);
			return null;
		}
		
		Map<Integer, Chip> map = new HashMap<Integer, Chip>();
		for(Chip.Builder chip : builder.getDataBuilderList()){
			map.put(chip.getItemid(), chip.build());
		}
		hputcacheAll(RedisKey.CHIP_CONFIG, map);
		
		return map;
	}
	
	public Pet getPet(int itemId) {
		Map<Integer, Pet> map = hgetcache(RedisKey.PET_CONFIG);
		return map.get(itemId);
	}
	
	private Map<Integer, Pet> buildPetConfig(){
		String xml = RedisService.ReadConfig(PET_FILE_NAME);
		PetList.Builder builder = PetList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + PET_FILE_NAME);
			return null;
		}
		
		Map<Integer, Pet> map = new HashMap<Integer, Pet>();
		for(Pet.Builder config : builder.getDataBuilderList()){
			map.put(config.getId(), config.build());
		}
		hputcacheAll(RedisKey.PET_CONFIG, map);
		
		return map;
	}
	
	public Engine getEngine(int itemId) {
		Map<Integer, Engine> map = hgetcache(RedisKey.ENGINE_CONFIG);
		return map.get(itemId);
	}
	
	private Map<Integer, Engine> buildEngineConfig(){
		String xml = RedisService.ReadConfig(ENGINE_FILE_NAME);
		EngineList.Builder builder = EngineList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + ENGINE_FILE_NAME);
			return null;
		}
		
		Map<Integer, Engine> map = new HashMap<Integer, Engine>();
		for(Engine.Builder chip : builder.getDataBuilderList()){
			map.put(chip.getId(), chip.build());
		}
		hputcacheAll(RedisKey.ENGINE_CONFIG, map);
		
		return map;
	}
	
	//equiptucao
	public Equiptucao getEquiptucao(int itemId) {
		Map<Integer, Equiptucao> map = hgetcache(RedisKey.EQUIP_TUCAO_CONFIG);
		return map.get(itemId);
	}
	
	private Map<Integer, Equiptucao> buildEquiptucaoConfig(){
		String xml = RedisService.ReadConfig(EQUIPTUCAO_FILE_NAME);
		EquiptucaoList.Builder builder = EquiptucaoList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + EQUIPTUCAO_FILE_NAME);
			return null;
		}
		
		Map<Integer, Equiptucao> map = new HashMap<Integer, Equiptucao>();
		for(Equiptucao.Builder euiqptucao : builder.getEquipBuilderList()){
			map.put(euiqptucao.getItemid(), euiqptucao.build());
		}
		hputcacheAll(RedisKey.EQUIP_TUCAO_CONFIG, map);
		
		return map;
	}
	
	//equip
	public Equip getEquip(int itemId) {
		Map<Integer, Equip> map = hgetcache(RedisKey.EQUIP_CONFIG);
		return map.get(itemId);
	}
	
	public Map<Integer, Equip> getEquipConfig() {
		Map<Integer, Equip> map = hgetcache(RedisKey.EQUIP_CONFIG);
		return map;
	}
	
	private Map<Integer, Equip> buildEquipConfig(){
		String xml = RedisService.ReadConfig(EQUIP_FILE_NAME);
		EquipList.Builder builder = EquipList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + EQUIP_FILE_NAME);
			return null;
		}
		
		Map<Integer, Equip> map = new HashMap<Integer, Equip>();
		for(Equip.Builder chip : builder.getDataBuilderList()){
			map.put(chip.getId(), chip.build());
		}
		hputcacheAll(RedisKey.EQUIP_CONFIG, map);
		
		return map;
	}
	
	//armor
	public Armor getArmor(int itemId) {
		Map<Integer, Armor> map = hgetcache(RedisKey.ARMOR_CONFIG);
		return map.get(itemId);
	}
	
	public Map<Integer, Armor> getArmorConfig() {
		Map<Integer, Armor> map = hgetcache(RedisKey.ARMOR_CONFIG);
		return map;
	}
	
	private Map<Integer, Armor> buildArmorConfig(){
		String xml = RedisService.ReadConfig(ARMOR_FILE_NAME);
		ArmorList.Builder builder = ArmorList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + ARMOR_FILE_NAME);
			return null;
		}
		
		Map<Integer, Armor> map = new HashMap<Integer, Armor>();
		for(Armor.Builder armor : builder.getDataBuilderList()){
			map.put(armor.getId(), armor.build());
		}
		hputcacheAll(RedisKey.ARMOR_CONFIG, map);
		
		return map;
	}
	
	//equip
	public Material getMaterial(int itemId) {
		Map<Integer, Material> map= hgetcache(RedisKey.MATERIAL_CONFIG);
		return map.get(itemId);
	}
	
	private Map<Integer, Material> buildMaterialConfig(){
		String xml = RedisService.ReadConfig(MATERIAL_FILE_NAME);
		MaterialList.Builder builder = MaterialList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + MATERIAL_FILE_NAME);
			return null;
		}
		
		Map<Integer, Material> map = new HashMap<Integer, Material>();
		for(Material.Builder material : builder.getDataBuilderList()){
			map.put(material.getItemid(), material.build());
		}
		hputcacheAll(RedisKey.MATERIAL_CONFIG, map);
		
		return map;
	}
	
	//equipup
	public Equipup getEquipup(int id) {
		Map<Integer, Equipup> map = hgetcache(RedisKey.EQUIPUP_CONFIG);
		return map.get(id);
	}
	
	private Map<Integer, Equipup> buildEquipupConfig(){
		String xml = RedisService.ReadConfig(EQUIPUP_FILE_NAME);
		EquipupList.Builder builder = EquipupList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + EQUIPUP_FILE_NAME);
			return null;
		}
		
		Map<Integer, Equipup> map = new HashMap<Integer, Equipup>();
		for(Equipup.Builder equipup : builder.getDataBuilderList()){
			map.put(equipup.getId(), equipup.build());
		}
		hputcacheAll(RedisKey.EQUIPUP_CONFIG, map);
		
		return map;
	}
}
