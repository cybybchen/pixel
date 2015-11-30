package com.trans.pixel.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Scope("prototype")
public class TestAction {
	private Logger logger = Logger.getLogger(TestAction.class);
	@RequestMapping(value = "/base")
	@ResponseBody
	public void dispatchRequest(HttpServletRequest request,
			HttpServletResponse response) {
		logger.debug("1111");
	}
}
