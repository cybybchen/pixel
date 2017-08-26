package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RankConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.ActivityProto.RequestRankCommand;
import com.trans.pixel.protoc.ActivityProto.ResponseRankCommand;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.UserRank;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.LadderProto.ResponseFightInfoCommand;
import com.trans.pixel.service.FightInfoService;
import com.trans.pixel.service.RankService;
import com.trans.pixel.service.UserService;

@Service
public class RankCommandService extends BaseCommandService {

	@Resource
	private RankService rankService;
	@Resource
	private FightInfoService fightInfoService;
	@Resource
	private UserService userService;
	
	public void getRankList(RequestRankCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseRankCommand.Builder builder = ResponseRankCommand.newBuilder();
		int type = cmd.getType();
		if (type == RankConst.TYPE_FIGHTINFO) {
			List<FightInfo> infos = rankService.getFightInfoList();
			List<FightInfo> infoList = new ArrayList<FightInfo>();
			for (FightInfo fight : infos) {
				FightInfo.Builder infoBuilder = FightInfo.newBuilder(fight);
				if(infoBuilder.hasEnemy())
					infoBuilder.setEnemy(userService.getCache(user.getServerId(), infoBuilder.getEnemy().getId()));
				
				infoList.add(infoBuilder.build());
			}
			builder.addAllFightInfo(infoList);
			ResponseFightInfoCommand.Builder fightinfoBuilder = ResponseFightInfoCommand.newBuilder();
			fightinfoBuilder.addAllId(fightInfoService.getSaveFightInfoIds(user));
			responseBuilder.setFightInfoCommand(fightinfoBuilder.build());
		} else {
			List<UserRankBean> rankList = rankService.getRankList(user.getServerId(), type);
			List<UserRank> userRankBuilderList = buildUserRankList(rankList);
			builder.addAllUserRank(userRankBuilderList);
		}
		
		responseBuilder.setRankCommand(builder.build());
	}
}
