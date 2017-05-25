package com.trans.pixel.controller.action;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.trans.pixel.model.ServerBean;
import com.trans.pixel.service.ServerService;
import com.trans.pixel.utils.Common;
import com.trans.pixel.utils.TypeTranslatedUtil;


@Controller
@Scope("prototype")
public class ServeropenAction {
	private static final Logger logger = LoggerFactory.getLogger(ServeropenAction.class);
    
	@Resource
	private ServerService serverService;
    @RequestMapping("/serveropen")
    @ResponseBody
    public String serveropen(HttpServletRequest request, HttpServletResponse response) {
    	Map<String, String> params = Common.getParamsMap(request);
    	logger.debug("11:" + request.getParameterMap());
		logger.debug("recall params is:" + params);
		
		ServerBean server = builderServerBean(params);
		serverService.setServer(server);
//		rechargeService.doRecharge(params, false);
		
		return "SUCCESS";
    }
    
    private ServerBean builderServerBean(Map<String, String> params) {
    	ServerBean server = new ServerBean();
    	server.setServerId(TypeTranslatedUtil.stringToInt(params.get("id")));
    	server.setKaifuTime(params.get("date"));
    	server.setStatus(TypeTranslatedUtil.stringToInt(params.get("status")));
    	
    	return server;
    }
}
