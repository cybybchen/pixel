package com.trans.pixel.test;

import java.util.Date;

import org.junit.Test;

public class TimeTest extends BaseTest {
	
	@Test
	public void test() {
		testTime(1460715544);
	}
	
	public void testTime(long sec) {
		Date date = new Date(sec*1000);
		System.out.println(date);
	}
}
