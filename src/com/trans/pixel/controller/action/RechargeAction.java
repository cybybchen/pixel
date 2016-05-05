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

import com.trans.pixel.service.RechargeService;
import com.trans.pixel.utils.HmacSHA1Encryption;


@Controller
@Scope("prototype")
public class RechargeAction {
	private static final Logger logger = LoggerFactory.getLogger(RechargeAction.class);
    
	@Resource
	private RechargeService rechargeService;
    @RequestMapping("/recharge")
    @ResponseBody
    public void rechrage(HttpServletRequest request, HttpServletResponse response) {
    	Map<String, String> params = getParamsMap(request);
		logger.warn("recall params is:" + params);
		
		rechargeService.doRecharge(params);
    }
    
    private Map<String, String> getParamsMap(HttpServletRequest request) {
		Map<String, String> paramsMap = HmacSHA1Encryption.parseRequest(request);
		logger.info("params " + paramsMap);
		
		return paramsMap;
	}

}
