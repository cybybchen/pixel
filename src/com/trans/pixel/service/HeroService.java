package com.trans.pixel.service;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.protoc.HeroProto.HeroFetter;
import com.trans.pixel.protoc.HeroProto.HeroFetters;
import com.trans.pixel.protoc.HeroProto.HeroFettersOrder;
import com.trans.pixel.protoc.HeroProto.StarMaterial;
import com.trans.pixel.protoc.HeroProto.Upgrade;
import com.trans.pixel.service.redis.HeroRedisService;

@Service
public class HeroService {

	@Resource
	private HeroRedisService heroRedisService;
	@Resource
	private UserPokedeService userPokedeService;
	
	public Hero getHero(int heroId) {
		Hero hero = heroRedisService.getHero(heroId);
		
		return hero;
	}
	
	public Map<Integer, Hero> getHeroMap() {
		return heroRedisService.getHeroConfig();
	}
	
	public long getLevelUpExp(int levelId) {
		Upgrade next = heroRedisService.getUpgrade(levelId);
//		HeroUpgradeBean current = getHeroUpgrade(levelId - 1);
		
		return next.getExp();
	}
	
	public long getLevelUpExp(int start, int add) {
		Map<Integer, Upgrade> upgradeMap = heroRedisService.getUpgradeConfig();
		long exp = 0;
		Iterator<Entry<Integer, Upgrade>> it = upgradeMap.entrySet().iterator();
		while (it.hasNext()) {
		Entry<Integer, Upgrade> entry = it.next();
		Upgrade upgrade = entry.getValue();
			if (upgrade.getLevel() > start && upgrade.getLevel() <= start + add) {
				exp += upgrade.getExp();
			}
		}
		
		return exp;
	}
	
	public Upgrade getUpgrade(int level) {
		return heroRedisService.getUpgrade(level);
	}
	
	public long getDeleteExp(int level) {
		long addExp = 0;
		Map<Integer, Upgrade> map = heroRedisService.getUpgradeConfig();
		while (level > 0) {
			Upgrade upgrade = map.get(level);
			addExp += upgrade.getExp();
			level--;
		}
		
		return addExp;
	}
	
	public ResultConst openFetter(UserBean user, UserPokedeBean userPokede, int fetterId) {
		if(userPokede == null)
			return ErrorConst.OPEN_FETTER_ERROR;
		if (userPokede.hasOpenedFetter(fetterId))
			return ErrorConst.HAS_OPEN_FETTER_ERROR;
		HeroFettersOrder heroFettersOrder = heroRedisService.getHeroFettersOrder(userPokede.getHeroId());
		for (HeroFetters heroFetters : heroFettersOrder.getFettersList()) {
			if (heroFetters.getFettersid() == fetterId) {
				for (HeroFetter heroFetter : heroFetters.getHeroList()) {
					UserPokedeBean fetterPokede = userPokedeService.selectUserPokede(user, heroFetter.getHeroid());
					if (fetterPokede == null || heroFetter.getHerorank() > fetterPokede.getRank() || heroFetter.getHerostar() > fetterPokede.getStar())
						return ErrorConst.OPEN_FETTER_ERROR;
					
					userPokede.addFetterId(fetterId);
					return SuccessConst.OPEN_FETTER_SUCCESS;
				}
			}
		}
		
		return ErrorConst.OPEN_FETTER_ERROR;
	}
	
//	public List<RewardBean> getHeroRareEquip(HeroInfoBean heroInfo) {
//		List<RewardBean> rewardList = new ArrayList<RewardBean>();
//		HeroBean hero = getHero(heroInfo.getHeroId());
//		HeroRareLevelup heroRareLevelup = heroRedisService.getHeroRareLevelup(hero.getPosition());
//		for (HeroRareLevelupRank rank : heroRareLevelup.getRankList()) {
//			if (rank.getRank() <= heroInfo.getRank()) {
//				rewardList.add(RewardBean.init(rank.getEquip1(), rank.getCount1()));
//				if (hero.getQuality() >= 2)
//					rewardList.add(RewardBean.init(rank.getEquip2(), rank.getCount2()));
//				
//				if (hero.getQuality() >= 3)
//					rewardList.add(RewardBean.init(rank.getEquip3(), rank.getCount3()));
//				
//				if (hero.getQuality() >= 4)
//					rewardList.add(RewardBean.init(rank.getEquip4(), rank.getCount4()));
//				
//				if (hero.getQuality() >= 6)
//					rewardList.add(RewardBean.init(rank.getEquip5(), rank.getCount5()));
//			}
//		}
//		
//		return rewardList;
//	}
	
	public StarMaterial getStarMaterial(int star, int position) {
		Map<Integer, StarMaterial> map = heroRedisService.getStarMaterialConfig();
		for (StarMaterial sm : map.values()) {
			if (sm.getStar() == star && sm.getPosition() == position)
				return sm;
		}
		
		return null;
	}
	
	public Map<Integer, StarMaterial> getStarMaterialMap() {
		return heroRedisService.getStarMaterialConfig();
	}
	
	public StarMaterial getStarMaterial(int id) {
		return heroRedisService.getStarMaterial(id);
	}
}
