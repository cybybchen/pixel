package com.trans.pixel.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.trans.pixel.model.ServerBean;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.HeroProto.RequestGetTeamCommand;
import com.trans.pixel.protoc.HeroProto.ResponseGetTeamCommand;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.protoc.ServerProto.HeadInfo;
import com.trans.pixel.utils.ConfigUtil;
import com.trans.pixel.utils.HTTPProtobufResolver;
import com.trans.pixel.utils.HTTPStringResolver;
import com.trans.pixel.utils.HttpUtil;

@Component
public class RequestService {
	
	private static final HttpUtil<ResponseCommand> http = new HttpUtil<ResponseCommand>(new HTTPProtobufResolver());
	private static final HttpUtil<String> strhttp = new HttpUtil<String>(new HTTPStringResolver());
	
	@Resource
	private ServerService serverService;
	
	public ResponseGetTeamCommand getTeamRequest(long userId, int serverId) {
		RequestCommand.Builder request = RequestCommand.newBuilder();
		request.setHead(buildHeadInfo(serverId));
		RequestGetTeamCommand.Builder teamBuilder = RequestGetTeamCommand.newBuilder();
		teamBuilder.setUserId(userId);
		request.setTeamCommand(teamBuilder.build());
		RequestCommand reqcmd = request.build();
		byte[] reqData = reqcmd.toByteArray();
		InputStream input = new ByteArrayInputStream(reqData);
		ResponseCommand response = http.post(!ConfigUtil.IS_MASTER ? ConfigUtil.MASTER_SERVER : ConfigUtil.getServer(serverId).getAddrIp(), input);//查询不同物理服务器的人物
		
		return response.getTeamCommand();
	}
	
	private HeadInfo buildHeadInfo(int serverId) {
		HeadInfo.Builder head = HeadInfo.newBuilder();
		ServerBean server = serverService.getServer(serverId);
		if (ConfigUtil.IS_MASTER)
			server = ConfigUtil.getServer(head.getServerId());

		if (server == null)
			return null;
		
		head.setAccount("");
		head.setLevel(1);//表示服务器转发
		head.setServerId(serverId);
		head.setVersion(0);
		head.setUserId(0);
		head.setSession("");
		head.setServerstarttime(server.getKaifuTime());
		head.setDatetime(System.currentTimeMillis() / 1000);
		head.setOnlineStatus(serverService.getOnlineStatus(head.getVersion()+""));
		head.setGameVersion(serverService.getGameVersion());
		head.setServerStatus(server.getStatus());
		
		return head.build();
	}
}
