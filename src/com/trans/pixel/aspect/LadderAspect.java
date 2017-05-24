package com.trans.pixel.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trans.pixel.service.UserLadderService;

@Aspect
@Component
public class LadderAspect {

	private static final Logger log = LoggerFactory.getLogger(LadderAspect.class);
	@Autowired
	private UserLadderService userLadderService;
	
	@Pointcut("execution(* com.trans.pixel.service.command.LadderModeCommandService.*(..))")
//	@Pointcut("execution(* com.trans.pixel.service.command.LadderModeCommandService.ladderInfo(..))")
	public void laddermode() {
		
	}
	
	@Before("laddermode()")
	public void updateLadderSeason() {
		log.debug("before laddermode");
		userLadderService.seasonUpdate();
	}
}
