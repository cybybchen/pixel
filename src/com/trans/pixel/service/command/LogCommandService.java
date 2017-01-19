package com.trans.pixel.service.command;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestLogCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.IdfaService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.utils.StringUtil;

@Service
public class LogCommandService extends BaseCommandService {

	@Resource
	private LogService logService;
	@Resource
	private UserService userService;
	@Resource
	private IdfaService idfaService;
	
	public void log(RequestLogCommand cmd, Builder responseBuilder, UserBean user) {
		int serverId = 0;
		long userId = 0;
		if (user != null) {
			serverId = user.getServerId();
			userId = user.getId();
		}
		Map<String, String> params = initParams(serverId, userId, cmd);
		logService.sendLog(params, cmd.getLogtype());
		
		if (!params.get(LogString.IDFA).isEmpty())
			idfaService.updateIdfaStatus(params.get(LogString.IDFA));
			
		if (user != null) {
			if (params.get(LogString.IDFA) != null && !params.get(LogString.IDFA).isEmpty()) {
				user.setIdfa(params.get(LogString.IDFA));
				userService.updateUser(user);
			}
		}
		// responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.LOG_SEND_SUCCESS));
	}
	
	private Map<String, String> initParams(int serverId, long userId, RequestLogCommand cmd) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.PHONEUUID, cmd.getPhoneuuid());
		params.put(LogString.LOGINTYPE, "" + cmd.getLogintype());
		params.put(LogString.PLATFORM, cmd.getPlatform());
		params.put(LogString.VENDOR, cmd.getVendor());
		params.put(LogString.VERSION, cmd.getVersion());
		params.put(LogString.MODEL, cmd.getModel());
		params.put(LogString.CURRENCY, "" + cmd.getCurrency());
		params.put(LogString.CURRENCYAMOUNT, "" + cmd.getCurrencyamount() * 100);	
		params.put(LogString.STAGE, "" + cmd.getStage());
		params.put(LogString.ITEMID, "" + cmd.getItemid());
		params.put(LogString.ACTION, "" + cmd.getAction());
		params.put(LogString.CHANNEL, cmd.getChannel());
		params.put(LogString.RECHARGE_TYPE, "" + cmd.getRechargetype());
		params.put(LogString.IDFA, StringUtil.filter(cmd.getIdfa()));
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.USERID, "" + userId);
		
		return params;
	}
}
