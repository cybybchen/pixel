package com.trans.pixel.service.command;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestLogCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.LogService;

@Service
public class LogCommandService extends BaseCommandService {

	@Resource
	private LogService logService;
	
	public void log(RequestLogCommand cmd, Builder responseBuilder, UserBean user) {
		Map<String, String> params = initParams(user.getServerId(), user.getId(), cmd);
		logService.sendLog(params, cmd.getLogtype());
		
		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.LOG_SEND_SUCCESS));
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
		params.put(LogString.CURRENCYAMOUNT, "" + cmd.getCurrencyamount());	
		params.put(LogString.STAGE, "" + cmd.getStage());
		params.put(LogString.ITEMID, "" + cmd.getItemid());
		params.put(LogString.ACTION, "" + cmd.getAction());
		params.put(LogString.CHANNEL, cmd.getChannel());
		params.put(LogString.RECHARGE_TYPE, "" + cmd.getRechargetype());
		
		return params;
	}
}