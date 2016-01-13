package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.model.userinfo.UserMineBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserMineCommand;
import com.trans.pixel.protoc.Commands.ResponseLootResultCommand;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.UserHero;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.UserMine;
import com.trans.pixel.protoc.Commands.UserRank;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.LadderService;

@Service
public class BaseCommandService {
	@Resource
	private LadderService ladderService;
	
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
	
	protected ResponseGetUserMineCommand buildGetUserMineCommand(List<UserMineBean> userMineList) {
		ResponseGetUserMineCommand.Builder builder = ResponseGetUserMineCommand.newBuilder();
		List<UserMine> userMineBuilderList = new ArrayList<UserMine>();
		for (UserMineBean userMine : userMineList) {
			userMineBuilderList.add(userMine.buildUserMine());
		}
		builder.addAllUserMine(userMineBuilderList);
		
		return builder.build();
	}
	
	protected List<UserHero> buildUserHeroList(List<UserHeroBean> userHeroList) {
		List<UserHero> userHeroBuilderList = new ArrayList<UserHero>();
		for (UserHeroBean userHero : userHeroList) {
			userHeroBuilderList.add(userHero.buildUserHero());
		}
		
		return userHeroBuilderList;
	}
	
	protected List<UserRank> buildUserRankList(List<UserRankBean> userRankList) {
		List<UserRank> userRankBuilderList = new ArrayList<UserRank>();
		for (UserRankBean userRank : userRankList) {
			userRankBuilderList.add(userRank.buildUserRank());
		}
		
		return userRankBuilderList;
	}
	
	protected void handleGetUserLadderRankListCommand(Builder responseBuilder, UserBean user) {	
		ResponseGetUserLadderRankListCommand.Builder builder = ResponseGetUserLadderRankListCommand.newBuilder();
		List<UserRankBean> rankList = ladderService.getRankListByUserId(user.getServerId(), user.getId());
		List<UserRank> userRankBuilderList = buildUserRankList(rankList);
		builder.addAllUserRank(userRankBuilderList);
		
		responseBuilder.setGetUserLadderRankListCommand(builder.build());
	}
}
