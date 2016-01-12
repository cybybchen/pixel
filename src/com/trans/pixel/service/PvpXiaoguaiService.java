package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.pvp.FieldBean;
import com.trans.pixel.model.pvp.PvpXiaoguaiBean;
import com.trans.pixel.service.redis.PvpXiaoguaiRedisService;

@Service
public class PvpXiaoguaiService {
	private static final int RANDOM_NUM = 5;
	private static final float XIYOU_PERCENT = 0.1f;
	@Resource
	private PvpXiaoguaiRedisService pvpXiaoguaiRedisService;
	public List<FieldBean> refreshXiaoguai(int mapId) {
		PvpXiaoguaiBean xiaoguai = pvpXiaoguaiRedisService.getPvpXiaoguai(mapId);
		if (xiaoguai == null) {
			parseAndSaveConfig();
			xiaoguai = pvpXiaoguaiRedisService.getPvpXiaoguai(mapId);
		}
		
		List<FieldBean> fieldList = xiaoguai.getFieldList();
		return randomFieldList(fieldList);
	}
	
	private List<FieldBean> randomFieldList(List<FieldBean> fieldList) {
		List<FieldBean> returnFieldList = new ArrayList<FieldBean>();
		Random random = new Random();
		while(returnFieldList.size() < RANDOM_NUM) {
			int randomNum = random.nextInt(fieldList.size());
			FieldBean field = fieldList.get(randomNum);
			if (!returnFieldList.contains(field)) {
				if (random.nextFloat() <= XIYOU_PERCENT)
					field.setLevel(1);
				
				returnFieldList.add(field);
			}
		}
		
		return returnFieldList;
	}
	
	private void parseAndSaveConfig() {
		List<PvpXiaoguaiBean> pvpXiaoguaiList = PvpXiaoguaiBean.xmlParse();
		if (pvpXiaoguaiList != null && pvpXiaoguaiList.size() > 0)
			pvpXiaoguaiRedisService.setPvpXiaoguaiList(pvpXiaoguaiList);
	}
}
