package com.trans.pixel.service.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.PackageConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.Armor;
import com.trans.pixel.protoc.EquipProto.Equip;
import com.trans.pixel.protoc.EquipProto.Prop;
import com.trans.pixel.protoc.EquipProto.RequestEquipPokedeCommand;
import com.trans.pixel.protoc.EquipProto.RequestEquipStrenthenCommand;
import com.trans.pixel.protoc.EquipProto.RequestEquipupCommand;
import com.trans.pixel.protoc.EquipProto.ResponseEquipPokedeCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.EquipPokedeService;
import com.trans.pixel.service.EquipService;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.PropService;
import com.trans.pixel.service.UserClearService;
import com.trans.pixel.service.UserEquipPokedeService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class EquipPokedeCommandService extends BaseCommandService {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(EquipPokedeCommandService.class);
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private LogService logService;
	@Resource
	private CostService costService;
	@Resource
	private UserClearService userClearService;
	@Resource
	private EquipPokedeService equipPokedeService;
	@Resource
	private ActivityService activityService;
	@Resource
	private EquipService equipService;
	@Resource
	private PropService propService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private UserService userService;
	@Resource
	private LadderService ladderService;
	
	public void getUserEquipPokedeList(RequestEquipPokedeCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseEquipPokedeCommand.Builder builder = ResponseEquipPokedeCommand.newBuilder();
		List<UserEquipPokedeBean> userPokedeList = userEquipPokedeService.selectUserEquipPokedeList(user.getId());
		builder.addAllUserEquipPokede(buildEquipPokede(userPokedeList));
		responseBuilder.setEquipPokedeCommand(builder.build());
	}
	
	public void pokedeStrengthen(RequestEquipStrenthenCommand cmd, Builder responseBuilder, UserBean user) {
		int equipId = cmd.getEquipId();
		int itemId = 0;
		if (cmd.hasItemId())
			itemId = cmd.getItemId();
		
		if (!equipPokedeService.canUse(user, equipId, itemId)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.PROP_USE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.PROP_USE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		MultiReward.Builder rewards = MultiReward.newBuilder();
		UserEquipPokedeBean pokede = userEquipPokedeService.selectUserEquipPokede(user, equipId);
		int prelevel = pokede.getLevel();
		ResultConst result = null;
		if (itemId >= PackageConst.RANDOM_STRENTHEN_EQUIP_ID && itemId <= PackageConst.EQUIP_STRENTHEN_PROTECTED_ID) {
			if (!costService.canCost(user, itemId, 1))
				result = ErrorConst.NOT_ENOUGH_PROP;
			else if (itemId >= PackageConst.RANDOM_STRENTHEN_EQUIP_ID && itemId < PackageConst.EQUIP_STRENTHEN_PROTECTED_ID) {
				Prop prop = propService.getProp(itemId);
				result = equipPokedeService.equipStrenthen(user, pokede, prop.getBossid() == 0 ? 6 : prop.getBossid(), prop.getBossid() == 0 ? 6 : 0);
			} else
				result = equipPokedeService.equipStrenthen(pokede, user, rewards, true);
		} else {
			result = equipPokedeService.equipStrenthen(pokede, user, rewards, false);
			userService.updateUser(user);
		}
		
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			ErrorCommand errorCommand = buildErrorCommand(result);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		if (itemId >= PackageConst.RANDOM_STRENTHEN_EQUIP_ID && itemId <= PackageConst.EQUIP_STRENTHEN_PROTECTED_ID) {
			costService.costAndUpdate(user, itemId, 1);
			
			UserPropBean userProp = userPropService.selectUserProp(user.getId(), itemId);
			if (userProp != null){
				pushCommandService.pushUserPropCommand(responseBuilder, user, userProp);
			}
		}
			
		activityService.levelupEquip(user, pokede.getLevel());
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + user.getServerId());
		params.put(LogString.USERID, "" + user.getId());
		params.put(LogString.HEROID, "" + 0);
		params.put(LogString.EQUIPID, "" + equipId);
		if(pokede.getLevel() == 0)
			params.put(LogString.RESULT, "1");
		else if(pokede.getLevel() > prelevel)
			params.put(LogString.RESULT, "2");
		else
			params.put(LogString.RESULT, "0");
		params.put(LogString.PRELEVEL, "" + prelevel);
		params.put(LogString.LEVEL, "" + pokede.getLevel());
		if(equipId < RewardConst.ARMOR){
			Equip equip = equipService.getEquip(equipId);
			params.put(LogString.EQUIPRARE, "" + equip.getIrare());
		}else{
			Armor equip = equipService.getArmor(equipId);
			params.put(LogString.EQUIPRARE, "" + equip.getIrare());
		}
		params.put(LogString.COINCOST, "" + (rewards.getLootCount()>0 ? rewards.getLoot(0).getCount() :0));
		
		logService.sendLog(params, LogString.LOGTYPE_EQUIPUP);
		
		ResponseEquipPokedeCommand.Builder builder = ResponseEquipPokedeCommand.newBuilder();
		builder.addUserEquipPokede(pokede.build());
		responseBuilder.setEquipPokedeCommand(builder.build());
		pushCommandService.pushRewardCommand(responseBuilder, user, rewards.build(), false);//cost
	}
	
	public void equipup(RequestEquipupCommand cmd, Builder responseBuilder, UserBean user) {
		int currentLadderEquipId = ladderService.getCurrentSeasonEquipId();
		if (currentLadderEquipId == cmd.getItemid()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.LADDER_SEASON_EQUIP_NOTUP_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.LADDER_SEASON_EQUIP_NOTUP_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		UserEquipPokedeBean pokede = userEquipPokedeService.selectUserEquipPokede(user, cmd.getItemid());
		if (pokede == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.EQUIP_NOT_GET_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_NOT_GET_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		ResultConst ret = equipPokedeService.equipup(pokede, user);
		if (ret instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			ErrorCommand errorCommand = buildErrorCommand(ret);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		ResponseEquipPokedeCommand.Builder builder = ResponseEquipPokedeCommand.newBuilder();
		builder.addUserEquipPokede(pokede.build());
		responseBuilder.setEquipPokedeCommand(builder.build());
	}
}
