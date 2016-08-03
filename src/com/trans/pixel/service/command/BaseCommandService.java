package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.MessageBoardBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserAchieveBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserClearBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserFoodBean;
import com.trans.pixel.model.userinfo.UserFriendBean;
import com.trans.pixel.model.userinfo.UserHeadBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Commands.ClearInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.HeroInfo;
import com.trans.pixel.protoc.Commands.Mail;
import com.trans.pixel.protoc.Commands.MailList;
import com.trans.pixel.protoc.Commands.MessageBoard;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.ResponseLootResultCommand;
import com.trans.pixel.protoc.Commands.ResponseMessageCommand;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.UserAchieve;
import com.trans.pixel.protoc.Commands.UserEquip;
import com.trans.pixel.protoc.Commands.UserFood;
import com.trans.pixel.protoc.Commands.UserFriend;
import com.trans.pixel.protoc.Commands.UserHead;
import com.trans.pixel.protoc.Commands.UserProp;
import com.trans.pixel.protoc.Commands.UserRank;
import com.trans.pixel.protoc.Commands.UserTeam;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.UserClearService;
import com.trans.pixel.utils.DateUtil;

@Service
public class BaseCommandService {
	@Resource
	private LadderService ladderService;
	@Resource
	private UserClearService userClearService;
	
	protected void buildUserInfo(ResponseUserInfoCommand.Builder builder, UserBean user) {
		builder.setUser(user.build());
	}
	
	// protected void buildMessageOrErrorCommand(Builder responseBuilder, ResultConst result) {
	// 	if(result instanceof SuccessConst)
	// 		responseBuilder.setMessageCommand(buildMessageCommand(result));
	// 	else
	// 		responseBuilder.setErrorCommand(buildErrorCommand(result));
 //    }
	
	protected ErrorCommand buildErrorCommand(ResultConst errorConst) {
        ErrorCommand.Builder erBuilder = ErrorCommand.newBuilder();
        erBuilder.setCode(String.valueOf(errorConst.getCode()));
        erBuilder.setMessage(errorConst.getMesssage());
        return erBuilder.build();
    }
	
	protected ErrorCommand buildErrorCommand(ResultConst errorConst, String msg) {
        ErrorCommand.Builder erBuilder = ErrorCommand.newBuilder();
        erBuilder.setCode(String.valueOf(errorConst.getCode()));
        erBuilder.setMessage(msg);
        return erBuilder.build();
    }

	protected ErrorConst getNotEnoughError(int rewardId) {
		ErrorConst errorConst = ErrorConst.NOT_ENOUGH;
		if (rewardId > RewardConst.HERO) {
			errorConst = ErrorConst.NOT_ENOUGH_HERO;
		} else if (rewardId > RewardConst.PROP) {
			errorConst = ErrorConst.NOT_ENOUGH_PROP;
		} else if (rewardId > RewardConst.PACKAGE) {
			
		} else if (rewardId > RewardConst.CHIP) {
			errorConst = ErrorConst.NOT_ENOUGH_CHIP;
		} else if (rewardId > RewardConst.EQUIPMENT) {
			errorConst = ErrorConst.NOT_ENOUGH_EQUIP;
		} else {
			switch (rewardId) {
				case RewardConst.EXP:
					break;
				case RewardConst.COIN:
					errorConst = ErrorConst.NOT_ENOUGH_COIN;
					break;
				case RewardConst.JEWEL:
					errorConst = ErrorConst.NOT_ENOUGH_JEWEL;
					break;
				case RewardConst.PVPCOIN:
					errorConst = ErrorConst.NOT_ENOUGH_MAGICCOIN;
					break;
				case RewardConst.EXPEDITIONCOIN:
					errorConst = ErrorConst.NOT_ENOUGH_EXPEDITIONCOIN;
					break;
				case RewardConst.LADDERCOIN:
					errorConst = ErrorConst.NOT_ENOUGH_LADDERCOIN;
					break;
				case RewardConst.UNIONCOIN:
					errorConst = ErrorConst.NOT_ENOUGH_UNIONCOIN;
					break;
				default:
					break;
			}
		}
        // ErrorCommand.Builder erBuilder = ErrorCommand.newBuilder();
        // erBuilder.setCode(String.valueOf(errorConst.getCode()));
        // erBuilder.setMessage(errorConst.getMesssage());
        // return erBuilder.build();
        return errorConst;
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
	
	protected List<HeroInfo> buildUserHeroList(List<HeroInfoBean> userHeroList) {
		List<HeroInfo> userHeroBuilderList = new ArrayList<HeroInfo>();
		for (HeroInfoBean userHero : userHeroList) {
			userHeroBuilderList.add(userHero.buildHeroInfo());
		}
		
		return userHeroBuilderList;
	}
	
	protected List<UserEquip> buildUserEquipList(List<UserEquipBean> userEquipList) {
		List<UserEquip> userEquipBuilderList = new ArrayList<UserEquip>();
		for (UserEquipBean userEquip : userEquipList) {
			userEquipBuilderList.add(userEquip.buildUserEquip());
		}
		
		return userEquipBuilderList;
	}
	
	protected List<UserFood> buildUserFoodList(List<UserFoodBean> userFoodList) {
		List<UserFood> userFoodBuilderList = new ArrayList<UserFood>();
		for (UserFoodBean userFood : userFoodList) {
			userFoodBuilderList.add(userFood.buildUserFood());
		}
		
		return userFoodBuilderList;
	}
	
	protected List<UserProp> buildUserPropList(List<UserPropBean> userPropList) {
		List<UserProp> userPropBuilderList = new ArrayList<UserProp>();
		for (UserPropBean userProp : userPropList) {
			userPropBuilderList.add(userProp.buildUserProp());
		}
		
		return userPropBuilderList;
	}
	
	protected List<UserRank> buildUserRankList(List<UserRankBean> userRankList) {
		List<UserRank> userRankBuilderList = new ArrayList<UserRank>();
		for (UserRankBean userRank : userRankList) {
			if(userRank != null)
				userRankBuilderList.add(userRank.buildUserRank());
		}
		
		return userRankBuilderList;
	}
	
	protected MailList buildMailList(int type, List<MailBean> mailList) {
		MailList.Builder mailListBuilder = MailList.newBuilder();
		List<Mail> mailBuilderList = new ArrayList<Mail>();
		for (MailBean mail : mailList) {
			mailBuilderList.add(mail.buildMail());
		}
		mailListBuilder.setType(type);
		mailListBuilder.addAllMail(mailBuilderList);
		
		return mailListBuilder.build();
	}
	
	protected List<UserFriend> buildUserFriendList(List<UserFriendBean> userFriendList) {
		List<UserFriend> userFriendBuilderList = new ArrayList<UserFriend>();
		for (UserFriendBean userFriend : userFriendList) {
			userFriendBuilderList.add(userFriend.buildUserFriend());
		}
		
		return userFriendBuilderList;
	}
	
	protected void handleGetUserLadderRankListCommand(Builder responseBuilder, UserBean user) {	
		ResponseGetUserLadderRankListCommand.Builder builder = ResponseGetUserLadderRankListCommand.newBuilder();
		List<UserRankBean> rankList = ladderService.getRankListByUserId(user.getServerId(), user);
		List<UserRank> userRankBuilderList = buildUserRankList(rankList);
		builder.addAllUserRank(userRankBuilderList);
		
		responseBuilder.setGetUserLadderRankListCommand(builder.build());
	}
	
	protected MailBean buildMail(long userId, String content, int type, List<RewardBean> rewardList) {
		MailBean mail = new MailBean();
		mail.setUserId(userId);
		mail.setContent(content);
		mail.setType(type);
		mail.setRewardList(rewardList);
		mail.setStartDate(DateUtil.getCurrentDateString());
		
		return mail;
	}
	
	protected MailBean buildMail(long userId, long friendId, int vip, int icon, String friendName, String content, int type) {
		MailBean mail = new MailBean();
		mail.setUserId(userId);
		mail.setFromUserId(friendId);
		mail.setFromUserName(friendName);
		mail.setContent(content);
		mail.setType(type);
		mail.setStartDate(DateUtil.getCurrentDateString());
		mail.setVip(vip);
		mail.setIcon(icon);
		
		return mail;
	}
	
	protected MailBean buildMail(long userId, long friendId, int vip, int icon, String usreName, String content, int type, int relatedId) {
		MailBean mail = new MailBean();
		mail.setUserId(userId);
		mail.setFromUserId(friendId);
		mail.setContent(content);
		mail.setType(type);
		mail.setRelatedId(relatedId);
		mail.setFromUserName(usreName);
		mail.setStartDate(DateUtil.getCurrentDateString());
		mail.setVip(vip);
		mail.setIcon(icon);
		
		return mail;
	}
	
	protected ResponseMessageCommand buildMessageCommand(ResultConst success) {
		ResponseMessageCommand.Builder builder = ResponseMessageCommand.newBuilder();
		builder.setCode(success.getCode());
		builder.setMsg(success.getMesssage());
		
		return builder.build();
	}
	
	protected List<MessageBoard> buildMessageBoardList(List<MessageBoardBean> messageBoardList) {
		List<MessageBoard> messageBoardBuilderList = new ArrayList<MessageBoard>();
		for (MessageBoardBean messageBoard : messageBoardList) {
			messageBoardBuilderList.add(messageBoard.buildMessageBoard());
		}
		
		return messageBoardBuilderList;
	}
	
	protected List<UserTeam> buildUserTeamList(List<UserTeamBean> userTeamList) {
		List<UserTeam> userTeamBuilderList = new ArrayList<UserTeam>();
		for (UserTeamBean userTeam : userTeamList) {
			userTeamBuilderList.add(userTeam.buildUserTeam());
		}
		
		return userTeamBuilderList;
	}
	
	protected List<UserAchieve> buildUserAchieveList(List<UserAchieveBean> userAchieveList) {
		List<UserAchieve> userAchieveBuilderList = new ArrayList<UserAchieve>();
		for (UserAchieveBean userAchieve : userAchieveList) {
			userAchieveBuilderList.add(userAchieve.buildUserAchieve());
		}
		
		return userAchieveBuilderList;
	}
	
	protected List<UserHead> buildUserHeadList(List<UserHeadBean> userHeadList) {
		List<UserHead> builderList = new ArrayList<UserHead>();
		for (UserHeadBean userHead : userHeadList) {
			builderList.add(userHead.buildUserHead());
		}
		
		return builderList;
	}
	
	protected List<HeroInfo> buildHeroInfo(List<UserPokedeBean> userPokedeList, UserBean user) {
		List<HeroInfo> heroInfoList = new ArrayList<HeroInfo>();
		for (UserPokedeBean userPokede : userPokedeList) {
			heroInfoList.add(userPokede.buildUserPokede(userClearService.selectUserClear(user, userPokede.getHeroId())));
		}
		
		return heroInfoList;
	}
	
	protected List<ClearInfo> buildClearInfo(List<UserClearBean> userClearList) {
		List<ClearInfo> clearInfoList = new ArrayList<ClearInfo>();
		for (UserClearBean userClear : userClearList) {
			clearInfoList.add(userClear.buildClearInfo());
		}
		
		return clearInfoList;
	}
}
