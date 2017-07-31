package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.mapper.CdkeyConfigMapper;
import com.trans.pixel.model.mapper.CdkeyMapper;
import com.trans.pixel.model.mapper.UserCipherMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.ActivityProto.Cipher;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.ShopProto.Cdkey;
import com.trans.pixel.service.redis.CdkeyRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class CdkeyService {
	@Resource
	private CdkeyRedisService redis;
	@Resource
	private CdkeyMapper cdkeyMapper;
	@Resource
	private CdkeyConfigMapper configMapper;
	@Resource
	private UserCipherMapper userCipherMapper;
	
	public List<String> getCdkeyRewarded(UserBean user){
		String value =  redis.getCdkeyRewarded(user);
		List<String> rewarded =  new ArrayList<String>();
		if(value == null){
			value = cdkeyMapper.selectById(user.getId());
		}
		if(value != null)
		for(String key : value.split(",")){
			rewarded.add(key);
		}
		return rewarded;
	}

	public String getCdkeyRewardedStr(UserBean user){
		String value =  redis.getCdkeyRewarded(user);
		if(value == null)
			value = cdkeyMapper.selectById(user.getId());
		if(value == null)
			value = "";
		return value;
	}
	
	public void saveCdkeyRewarded(UserBean user, List<String> rewarded){
		String value = rewarded.size() > 0 ? rewarded.get(0) : "";
		for(int i = 1; i < rewarded.size(); i++) {
			value += ","+rewarded.get(i);
		}
		cdkeyMapper.update(user.getId(), value);
		redis.saveCdkeyRewarded(user, value);
	}
	
	public void updateToDB(long userId) {
		String value = redis.getCdkeyRewarded(userId);
		if(value != null)
			cdkeyMapper.update(userId, value);
	}
	
	public String popDBKey(){
		return redis.popDBKey();
	}
	
	/*on master server*/
	public String getCdkey(String key){
		return redis.getCdkey(key);
	}

	public String getCdkeyOld(String key){
		return redis.getCdkeyOld(key);
	}

	public void delCdkey(String key, String value){
		redis.delCdkey(key, value);
	}
	
	public Cdkey getCdkeyConfig(String id){
		return redis.getCdkeyConfig(id);
	}
	
	public void delCdkeyConfig(String id){
		redis.delCdkeyConfig(id);
		configMapper.delete(Integer.parseInt(id));
	}
	
	List<String> getAvaiCdkeys(String id){
		return redis.getAvaiCdkeys(id);
	}
	
	List<String> addCdkeyConfig(Cdkey.Builder cdkey, int length){
		List<String> keys = redis.addCdkeyConfig(cdkey, length);
		configMapper.update(cdkey.getId(), RedisService.formatJson(cdkey.build()));
		return keys;
	}
	
	Map<Integer, String> getCdkeyConfigs(){
		Map<String, String> map = redis.getCdkeyConfigs();
		if(map.isEmpty()){
			List<String> cdkeys = configMapper.selectAll();
			Map<String, String> cdkeymap = new TreeMap<String, String>();
			for(String value : cdkeys){
				Cdkey.Builder builder = Cdkey.newBuilder();
				RedisService.parseJson(value, builder);
				cdkeymap.put(builder.getId()+"", RedisService.formatJson(builder.build()));
			}
			redis.saveCdkeyConfigs(cdkeymap);
			return redis.getCdkeyConfigs(cdkeys);
		}else
			return redis.getCdkeyConfigs(map.values());
	}
	
	public ResultConst cipherReward(UserBean user, String des, MultiReward.Builder rewards) {
		Cipher cipher = redis.getCipher(des.toUpperCase());
		if (cipher == null 
				|| (!cipher.getStarttime().isEmpty() && !cipher.getEndtime().isEmpty() && !DateUtil.timeIsAvailable(cipher.getStarttime(), cipher.getEndtime()))) {
			return ErrorConst.CIPHER_IS_UNUSE_ERROR;
		}
		
		if (hasCipherReward(user, cipher.getId())) {
			return ErrorConst.CIPHER_HAS_REWARD_ERROR;
		}
		
		rewards.addAllLoot(cipher.getRewardList());
		
		return SuccessConst.CDKEY_SUCCESS;
	}
	
	private boolean hasCipherReward(UserBean user, int cipherId) {
		boolean hasReward = redis.hasCipherReward(user, cipherId);
		if (hasReward)
			return hasReward;
		
		if (!redis.hasCipherKey(cipherId)) {
			List<Long> userIds = userCipherMapper.getUserIds(cipherId);
			if (userIds != null && !userIds.isEmpty()) {
				redis.setCipherUserIds(cipherId, userIds);
				
				hasReward = redis.hasCipherReward(user, cipherId);
			}
		}
		
		if (!hasReward) {
			redis.setCipherUserId(cipherId, user.getId());
			userCipherMapper.addCipher(cipherId, user.getId());
		}
		
		return hasReward;
	}
}
