package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.EquipmentBean;
import com.trans.pixel.service.redis.EquipRedisService;

@Service
public class EquipService {

	@Resource
	private EquipRedisService equipRedisService;
	public EquipmentBean getEquip(int itemId) {
		EquipmentBean equip = equipRedisService.getEquip(itemId);
		if (equip == null) {
			parseAndSaveEquipConfig();
			equip = equipRedisService.getEquip(itemId);
		}
		
		return equip;
	}
	
	private void parseAndSaveEquipConfig() {
		List<EquipmentBean> list = EquipmentBean.xmlParse();
		equipRedisService.setEquipList(list);;
	}
}
