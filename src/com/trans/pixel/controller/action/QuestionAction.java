package com.trans.pixel.controller.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Controller
@Scope("prototype")
public class QuestionAction {
	private Logger logger = LoggerFactory.getLogger(QuestionAction.class);
	@Resource
	private LogService logService;
	@Resource
	private UserService userService;
	
	@RequestMapping(value = "/question")
	@ResponseBody
	public void dispatchRequest(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, String[]> params = request.getParameterMap();
//		logger.warn("params is:" + params);
		Iterator<Entry<String, String[]>> it = params.entrySet().iterator();
		Map<String, String> logMap = new HashMap<String, String>();
		while (it.hasNext()) {
			Entry<String, String[]> entry = it.next();
//			logger.warn("key is:" + entry.getKey());
			logMap.put(entry.getKey(), composeQuestion(entry.getValue()));
		}
		UserBean user = userService.getUser(TypeTranslatedUtil.stringToLong(logMap.get("userid")));
		if (user != null && user.getQuestStatus() == 0) {
			logMap.put(LogString.SERVERID, "" + user.getServerId());
			logService.sendLog(logMap, LogString.LOGTYPE_QUESTION);
			
			user.setQuestStatus(1);
			userService.updateUser(user);
		}
		
		logger.warn("logmap is:" + logMap);
	}
	
	private String composeQuestion(String[] questions) {
		String question = "";
		for (String quest : questions) {
			question += quest;
		}
		
		return question;
	}
}
