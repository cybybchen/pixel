package com.trans.pixel.utils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

public class MailUtils {
	private static final String HOST = "smtp.qq.com";
	private static final Integer PORT = 25;
	private static final String USERNAME = "yanbin.chen@transmension.com";
	private static final String PASSWORD = "chenyanbin";
	private static final String EMAILFORM = "yanbin.chen@transmension.com";
	private static JavaMailSenderImpl mailSender = createMailSender();

	public static void main(String[] args) {
		MailUtils util = new MailUtils();
		System.out.println("start test");
		util.sendMail("xinji.wang@transmension.com", "帐号异常通知测试", "test from java!");
		System.out.println("end test");
	}
	/**
	 * 邮件发送器
	 *
	 * @return 配置好的工具
	 */
	private static JavaMailSenderImpl createMailSender() {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(HOST);
		sender.setPort(PORT);
		sender.setUsername(USERNAME);
		sender.setPassword(PASSWORD);
		sender.setDefaultEncoding("Utf-8");
		Properties p = new Properties();
		p.setProperty("mail.smtp.timeout", "25000");
		p.setProperty("mail.smtp.auth", "false");
		sender.setJavaMailProperties(p);
		return sender;
	}

	/**
	 * 发送邮件
	 *
	 * @param to
	 *            接受人
	 * @param subject
	 *            主题
	 * @param html
	 *            发送内容
	 * @throws MessagingException
	 *             异常
	 * @throws UnsupportedEncodingException
	 *             异常
	 */
	public void sendMail(final String to, final String subject, final String content) {
		Thread t = new Thread(){
			public void  run(){
				MimeMessage mimeMessage = mailSender.createMimeMessage();
				// 设置utf-8或GBK编码，否则邮件会有乱码
				MimeMessageHelper messageHelper;
				try {
					messageHelper = new MimeMessageHelper(mimeMessage,
							true, "UTF-8");
					try {
						messageHelper.setFrom(EMAILFORM, "系统名称");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					messageHelper.setTo(USERNAME);
					messageHelper.addCc(to);
		//			messageHelper.addCc("lolzj@transmension.com");
					messageHelper.setSubject(subject);
					messageHelper.setText(content, true);
					mailSender.send(mimeMessage);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
	}
}
