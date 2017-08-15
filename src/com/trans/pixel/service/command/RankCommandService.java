package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RankConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.ActivityProto.RequestRankCommand;
import com.trans.pixel.protoc.ActivityProto.ResponseRankCommand;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.UserRank;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.RankService;

@Service
public class RankCommandService extends BaseCommandService {

	@Resource
	private RankService rankService;
	
	public void getRankList(RequestRankCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseRankCommand.Builder builder = ResponseRankCommand.newBuilder();
		int type = cmd.getType();
		if (type == RankConst.TYPE_FIGHTINFO) {
			List<FightInfo> fightinfoList = rankService.getFightInfoList();
			List<FightInfo> randomFightinfo = new ArrayList<FightInfo>();
			while (fightinfoList.size() >= RankConst.FIGHTINFO_RANK_RETURN_LIMIT &&
					randomFightinfo.size() < RankConst.FIGHTINFO_RANK_RETURN_LIMIT) {
				int rand = RandomUtils.nextInt(fightinfoList.size());
				FightInfo fight = fightinfoList.get(rand);
				randomFightinfo.add(fight);
				fightinfoList.remove(rand);
			}
			if (randomFightinfo.isEmpty())
				builder.addAllFightInfo(fightinfoList);
			else
				builder.addAllFightInfo(randomFightinfo);
		} else {
			List<UserRankBean> rankList = rankService.getRankList(user.getServerId(), type);
			List<UserRank> userRankBuilderList = buildUserRankList(rankList);
			builder.addAllUserRank(userRankBuilderList);
		}
		
		responseBuilder.setRankCommand(builder.build());
	}
}
