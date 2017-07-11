package com.trans.pixel.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//@Aspect
//@Component
//public class TimeAspect {
//
//	private static final Logger log = LoggerFactory.getLogger(TimeAspect.class);
//	
//	@Pointcut("execution(* com.trans.pixel.service.command.*.*(..)) || execution(* com.trans.pixel.service.*.*(..)) || execution(* com.trans.pixel.service.redis.*.*(..))")
//	public void anyService() {
//		
//	}
//	
//	// @Before("anyService()")
//	// public void printTime() {
//	// 	log.debug("before anyService");
//	// 	userTimeService.seasonUpdate();
//	// }
//
//	long a=0;  
//	// @Before("anyService()")
//    public void doBefore(JoinPoint jp) {              
//        Object[] o=jp.getArgs();  
//        for(int i=0;i<o.length;i++){  
//        	log.debug("输入参数为"+o[i]);  
//        }  
//        a=System.currentTimeMillis();  
//        log.debug("当前方法执行时间为: "  
//        + jp.getTarget().getClass().getName() + "."  
//        + jp.getSignature().getName());  
//    }  
//    
//    // @After("anyService()")
//    public void doAfter(JoinPoint jp) {  
//    	log.debug("方法结束时间为: "  
//        + jp.getTarget().getClass().getName() + "."  
//        + jp.getSignature().getName());  
//
//    	log.debug("\r\n执行耗时 : "+(System.currentTimeMillis()-a)+" 秒 ");  
//    }  
//	
//	@Around("anyService()")
//    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {  
//
//        long time = System.currentTimeMillis();  
//        Object retVal = pjp.proceed();  
//        time = System.currentTimeMillis() - time;  
//       Object[] o=pjp.getArgs();
//       String arg = "";
//       for(int i=0;i<o.length;i++){  
//    	   if(o[i] != null){
//    		   String str = o[i].toString().replace("\n","").replace(" ", "");
//    		   arg += str.substring(0, Math.min(15, str.length()))+"#";  
//    	   }
//       }       
//        if(time > 2)
//        log.debug("执行时间为\t"+time+"\t"
//        + pjp.getTarget().getClass().getName() + "."  
//        + pjp.getSignature().getName()+"\t"+arg);  
//        return retVal;  
//    }       
//
//    public void doThrowing(JoinPoint jp, Throwable ex) {  
//    	log.debug("method " + jp.getTarget().getClass().getName()  
//        + "." + jp.getSignature().getName() + " throw exception");  
//    	log.debug(ex.getMessage());  
//    }  
//}
