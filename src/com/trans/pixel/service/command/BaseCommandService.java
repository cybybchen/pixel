package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.model.userinfo.UserMineBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserMineCommand;
import com.trans.pixel.protoc.Commands.ResponseLootResultCommand;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.UserHero;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.UserMine;

@Service
public class BaseCommandService {
	protected void buildUserInfo(ResponseUserInfoCommand.Builder builder, UserBean user) {
		UserInfo.Builder userBuilder = UserInfo.newBuilder();
		userBuilder.setAccount(user.getAccount());
		userBuilder.setId(user.getId());
		userBuilder.setServerId(user.getServerId());
		userBuilder.setUserName(user.getUserName());
		
		builder.setUser(userBuilder.build());
	}
	
	protected ErrorCommand buildErrorCommand(ErrorConst errorConst) {
        ErrorCommand.Builder erBuilder = ErrorCommand.newBuilder();
        erBuilder.setCode(String.valueOf(errorConst.getCode()));
        erBuilder.setMessage(errorConst.getMesssage());
        return erBuilder.build();
    }
	
	protected int calUserFighting() {
		return 100;
	}
	
	protected ResponseLootResultCommand builderLootResultCommand(UserBean user) {
		ResponseLootResultCommand.Builder builder = ResponseLootResultCommand.newBuilder();
		builder.setExp(user.getExp());
		builder.setGold(user.getCoin());
		builder.setLastLootTime(user.getLastLootTime());
		
		return builder.build();
	}
	
	protected ResponseGetUserMineCommand builderGetUserMineCommand(List<UserMineBean> userMineList) {
		ResponseGetUserMineCommand.Builder builder = ResponseGetUserMineCommand.newBuilder();
		List<UserMine> userMineBuilderList = new ArrayList<UserMine>();
		for (UserMineBean userMine : userMineList) {
			userMineBuilderList.add(userMine.toUserMineBuilder());
		}
		builder.addAllUserMine(userMineBuilderList);
		
		return builder.build();
	}
	
	protected List<UserHero> getUserHeroList(List<UserHeroBean> userHeroList) {
		List<UserHero> userHeroBuilderList = new ArrayList<UserHero>();
		for (UserHeroBean userHero : userHeroList) {
			userHeroBuilderList.add(userHero.toUserHeroBuilder());
		}
		
		return userHeroBuilderList;
	}
}
