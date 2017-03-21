package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.ActivityProto.RequestRankCommand;
import com.trans.pixel.protoc.ActivityProto.ResponseRankCommand;
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
		List<UserRankBean> rankList = rankService.getRankList(user.getServerId(), type);
		List<UserRank> userRankBuilderList = buildUserRankList(rankList);
		builder.addAllUserRank(userRankBuilderList);
		
		responseBuilder.setRankCommand(builder.build());
	}
}
