package com.trans.pixel.controller.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.MailService;
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
	@Resource
	private MailService mailService;
	
	@RequestMapping(value = "/question")
	@ResponseBody
	public void dispatchRequest(HttpServletRequest request,
			HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", "*");
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
			
			sendReward(user.getId());
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
	
	private void sendReward(long userId) {
		String content = "问卷调查奖励";
		List<RewardBean> rewardList = RewardBean.initRewardList(RewardConst.JEWEL, 200);
		MailBean mail = MailBean.buildSystemMail(userId, content, RewardBean.buildRewardInfoList(rewardList));
		mailService.addMail(mail);
	}
}
