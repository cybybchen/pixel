package com.trans.pixel.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.TimeConst;

/**
 * 日期操作类
 * 
 * @author ma.ruofei@ea.com
 * @version 1.0 2009-06-23 15:12:23
 * @since 1.0
 */
public class DateUtil {
	private static final Logger log = LoggerFactory.getLogger(DateUtil.class);
    /** 单例 */
    private static final DateUtil instance = new DateUtil();

    private DateUtil() {
    }

    /**
     * 取时间的增量
     */
    public static String getAddDate(Date date, int addDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, addDay);
        return formatDate(cal.getTime());
    }

    /**
     * 取得该类唯一实例
     * 
     * @return 该类唯实例
     */
    public static DateUtil instance() {
        return instance;
    }

    /**
     * 获得当天的格式化日期。
     * 
     * @return
     */
    public static String getCurrentDayDateString() {
        return new SimpleDateFormat(TimeConst.DEFAULT_DATE_FORMAT).format(new Date());
    }
    
    /**
     * 获得当天的格式化日期。
     * 
     * @return
     */
    public static String getCurrentDateString() {
        return new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).format(new Date());
    }

    /**
     * 获得当天的格式化时间。
     * 
     * @return
     */
    public static String getCurrentTimeString() {
        return new SimpleDateFormat(TimeConst.DEFAULT_TIME_FORMAT).format(new Date());
    }
    
    public static int getDateId() {
        return Integer.parseInt(new SimpleDateFormat(TimeConst.DATE_FORMAT_INT).format(new Date()));
    }

    /**
     * 将yyyy-MM-dd格式的字符串转换为日期对象
     * 
     * @param date
     *            待转换字符串
     * @return 转换后日期对象
     * @see #getDate(String, String, Date)
     */
    public static Date getDate(String date) {
        return getDate(date, TimeConst.DEFAULT_DATETIME_FORMAT, null);
    }

    /**
     * 获取当前的日期对象
     */
    public static Date getDate() {
    	return getDate(getCurrentDateString(), TimeConst.DEFAULT_DATETIME_FORMAT);
    }
    
    /**
     * 将yyyy-MM-dd HH:mm:ss格式的字符串转换为日期对象
     * 
     * @param date
     *            待转换字符串
     * @return 转换后日期对象
     * @see #getDate(String, String, Date)
     */
    public static Date getDateTime(String date) {
    	if (date == null || date.equals(""))
    		date = getCurrentDate(TimeConst.DEFAULT_DATETIME_FORMAT);
        return getDate(date, TimeConst.DEFAULT_DATETIME_FORMAT, null);
    }

    /**
     * 将指定格式的字符串转换为日期对象
     * 
     * @param date
     *            待转换字符串
     * @param format
     *            日期格式
     * @return 转换后日期对象
     * @see #getDate(String, String, Date)
     */
    public static Date getDate(String date, String format) {
        return getDate(date, format, null);
    }

    /**
     * 将指定格式的字符串转换为日期对象
     * 
     * @param date
     *            日期对象
     * @param format
     *            日期格式
     * @param defVal
     *            转换失败时的默认返回值
     * @return 转换后的日期对象
     */
    public static Date getDate(String date, String format, Date defVal) {
        if (StringUtil.isEmpty(date) || StringUtil.isEmpty(format))
            return null;
        Date d;
        try {
            d = new SimpleDateFormat(format).parse(date);
        } catch (ParseException e) {
        	log.debug("parse error={}", e);
            d = defVal;
        }
        return d;
    }

    /**
     * 将日期对象格式化成yyyy-MM-dd格式的字符串
     * 
     * @param date
     *            待格式化日期对象
     * @return 格式化后的字符串
     * @see #formatDate(Date, String, String)
     */
    public static String formatDate(Date date) {
        return formatDate(date, TimeConst.DEFAULT_DATE_FORMAT, null);
    }

    /**
     * 将日期对象格式化成yyyy-MM-dd HH:mm:ss格式的字符串
     * 
     * @param date
     *            待格式化日期对象
     * @return 格式化后的字符串
     * @see #formatDate(Date, String, String)
     */
    public static String forDatetime(Date date) {
        return formatDate(date, TimeConst.DEFAULT_DATETIME_FORMAT, null);
    }

    /**
     * 将日期对象格式化成HH:mm:ss格式的字符串
     * 
     * @param date
     *            待格式化日期对象
     * @return 格式化后的字符串
     * @see #formatDate(Date, String, String)
     */
    public static String formatTime(Date date) {
        return formatDate(date, TimeConst.DEFAULT_TIME_FORMAT, null);
    }

    /**
     * 将日期对象格式化成指定类型的字符串
     * 
     * @param date
     *            待格式化日期对象
     * @param format
     *            格式化格式
     * @return 格式化后的字符串
     * @see #formatDate(Date, String, String)
     */
    public static String formatDate(Date date, String format) {
        return formatDate(date, format, null);
    }

    /**
     * 将日期对象格式化成指定类型的字符串
     * 
     * @param date
     *            待格式化日期对象
     * @param format
     *            格式化格式
     * @param defVal
     *            格式化失败时的默认返回值
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date, String format, String defVal) {
        if (date == null || StringUtil.isEmpty(format))
            return defVal;
        String ret;
        try {
            ret = new SimpleDateFormat(format).format(date);
        } catch (Exception e) {
            ret = defVal;
        }
        return ret;
    }

    /**
     * 获得两个时间之间相差的分钟数(返回值去掉了小数部分，即只返回)。（time1 - time2）
     * 
     * @param time1
     * @param time2
     * @return 返回两个时间之间的分钟数，如果time1晚于time1，则返回正数，否则返回负数或者0
     */
    public static int intervalMinute(long time1, long time2) {
        long intervalMillSecond = time1 - time2;

        // 相差的分钟数 = 相差的毫秒数 / 每分钟的毫秒数 (小数位采用去尾制)
        return (int) (intervalMillSecond / TimeConst.MILLION_SECOND_PER_MINUTE);
    }
    
    /**
     * 获得两个日期之间相差的天数(返回值去掉了小数部分，即只返回)。（date1 - date2）
     * 
     * @param date1
     * @param date2
     * @return 返回两个日期之间的天数差，如果date1晚于date2，则返回正数，否则返回负数或者0
     */
    public static int intervalDays(Date date1, Date date2) {
        long intervalMillSecond = setToDayStartTime(date1).getTime() - setToDayStartTime(date2).getTime();

        // 相差的天数 = 相差的毫秒数 / 每天的毫秒数 (小数位采用去尾制)
        return (int) (intervalMillSecond / TimeConst.MILLION_SECOND_PER_DAY);
    }

    /**
     * 将时间调整到当天的0：0：0
     * 
     * @param date
     * @return
     */
    public static Date setToDayStartTime(Date date) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(date.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 将时间调整到当天的0：0：0
     * 
     * @param date
     * @return
     */
    public static void updateToDayStartTime(Date date) {
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            date.setTime(calendar.getTimeInMillis());
        }

    }
    
    /**
     * 将时间调整到当天的24：0：0
     * 
     * @param date
     * @return
     */
    public static Date setToDayEndTime(Date date) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(date.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
    
    /**
     * 将时间调整到当天的NN：0：0
     * 
     * @param date
     * @return
     */
    public static Date setToDayTime(Date date, int hour) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(date.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static int intervalHours(long time1, long time2) {
        return (int) ((time2 - time1) / TimeConst.MILLIONSECONDS_PER_HOUR);
    }

    /**
     * 获得两个日期之间相差的小时数。（date1 - date2）
     * 
     * @param date1
     * @param date2
     * @return 返回两个日期之间相差的小时数。
     */
    public static int intervalHours(Date date1, Date date2) {
        long intervalMillSecond = date1.getTime() - date2.getTime();

        // 相差的小时数 = 相差的毫秒数 / 每小时的毫秒数 (抛弃剩余的分钟数)
        return (int) (intervalMillSecond / TimeConst.MILLIONSECONDS_PER_HOUR);
    }

    /**
     * 获得两个日期之间相差的分钟数。（date1 - date2）
     * 
     * @param date1
     * @param date2
     * @return 返回两个日期之间相差的分钟数。
     */
    public static int intervalMinutes(Date date1, Date date2) {
        return intervalMinutes(date1.getTime(), date2.getTime());
    }

    /**
     * 获得两个日期之间相差的分钟数。（timeMillis1 - timeMillis2）
     * 
     * @param timeMillis1
     * @param timeMillis2
     * @return
     */
    public static int intervalMinutes(long timeMillis1, long timeMillis2) {
        long intervalMillSecond = timeMillis1 - timeMillis2;

        // 相差的分钟数 = 相差的毫秒数 / 每分钟的毫秒数 (小数位采用进位制处理，即大于0则加1)
        return (int) (intervalMillSecond / TimeConst.MILLIONSECONDS_PER_MINUTE + (intervalMillSecond % TimeConst.MILLIONSECONDS_PER_MINUTE > 0 ? 1
                : 0));
    }

    /**
     * 获得两个日期之间相差的秒数。（date1 - date2）
     * 
     * @param date1
     * @param date2
     * @return
     */
    public static int intervalSeconds(Date date1, Date date2) {
        long intervalMillSecond = date1.getTime() - date2.getTime();

        return (int) (intervalMillSecond / TimeConst.MILLIONSECONDS_PER_SECOND + (intervalMillSecond % TimeConst.MILLIONSECONDS_PER_SECOND > 0 ? 1
                : 0));
    }

    public static int intervalSeconds(long date1, long date2) {
        long intervalMillSecond = date1 - date2;
        return (int) (intervalMillSecond / TimeConst.MILLIONSECONDS_PER_SECOND + (intervalMillSecond % TimeConst.MILLIONSECONDS_PER_SECOND > 0 ? 1
                : 0));
    }

    /**
     * 取得过去的某个云上的日子
     * 
     * @param mark
     *            基准时间点
     * @param interval
     *            离传入时间之前的天数
     * @return
     */
    public static Date getPastDay(Date mark, int interval) {
        Calendar c = Calendar.getInstance();
        c.setTime(mark);
        c.add(Calendar.DAY_OF_YEAR, -interval);

        return c.getTime();
    }

    /**
     * 取得未来某个绚丽的日子
     * 
     * @param mark
     * @param interval
     * @return
     */
    public static Date getFutureDay(Date mark, int interval) {
        Calendar c = Calendar.getInstance();
        c.setTime(mark);
        c.add(Calendar.DAY_OF_YEAR, interval);

        return c.getTime();
    }

    /**
     * 取得未来几个小时后的时间
     * 
     * @param mark
     * @param interval
     * @return
     */
    public static Date getFutureHour(Date mark, int interval) {
        Calendar c = Calendar.getInstance();
        c.setTime(mark);
        c.add(Calendar.HOUR, interval);

        return c.getTime();
    }

    /**
     * 时间检测器
     * 
     * @param date
     *            被检测时间
     * @return
     */
    public static String timeCheck(Date date) {
        String time_desc;
        int num;
        long check = Math.abs(System.currentTimeMillis() - date.getTime());
        if (check < TimeConst.M) {
            num = (int) (check / TimeConst.S);
            time_desc = String.valueOf(num) + "秒";
        } else if (check >= TimeConst.M && check < TimeConst.H) {
            num = (int) (check / TimeConst.M);
            time_desc = String.valueOf(num) + "分钟";
        } else if (check >= TimeConst.H && check < TimeConst.D) {
            num = (int) (check / TimeConst.H);
            time_desc = String.valueOf(num) + "小时";
            num = (int) (check % TimeConst.H / TimeConst.M);
            time_desc += String.valueOf(num) + "分钟";
        } else if (check >= TimeConst.D && check < 8 * TimeConst.D) {
            num = (int) (check / TimeConst.D);
            time_desc = String.valueOf(num) + "天";
        } else {
            time_desc = new SimpleDateFormat(TimeConst.DEFAULT_DATE_FORMAT).format(date);
        }

        return time_desc;
    }

    /**
     * 时间检测器哟
     * 
     * @param date
     *            被检测时间
     * @return
     */
    public static String timeDiffCheck(Date date) {
        String time_desc = timeCheck(date);
        long diff = System.currentTimeMillis() - date.getTime();
        if (diff != 0) {
            time_desc += diff > 0 ? "前" : "后";
        }
        return time_desc;
    }

    public static String getTimeRemainString(long remain) {
        String desc;
        int num;
        if (remain < TimeConst.M) {
            num = (int) (remain / TimeConst.S);
            desc = String.valueOf(num) + "秒后";
        } else if (remain >= TimeConst.M && remain < TimeConst.H) {
            int min = (int) (remain / TimeConst.M);
            desc = String.valueOf(min) + "分";
            long sec = remain % TimeConst.M;
            desc += String.valueOf(sec / TimeConst.S) + "秒后";
        } else {
            int hour = (int) (remain / TimeConst.H);
            desc = String.valueOf(hour) + "小时";
            long min = remain % TimeConst.H;
            desc += String.valueOf(min / TimeConst.M) + "分后";
        }

        return desc;
    }

    /**
     * 根据偏移量计算一个变更后的date时间
     * 
     * @param date
     * @param amount
     * @return
     */
    public static Date changeDate(Date date, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, amount);
        return cal.getTime();
    }

    /**
     * 计算两个日期段相交的秒数
     * 
     * @param s1
     *            第一段日期的开始时间
     * @param e1
     *            第一段日期的结束时间
     * @param s2
     *            第二段日期的开始时间
     * @param e2
     *            第二段日期的结束时间
     * @return
     */
    public static int intersectionSeconds(Date s1, Date e1, Date s2, Date e2) {
        if (s1.after(e1) || s2.after(e2)) {
            throw new IllegalArgumentException("Date s1(s2) must before e1(e2).");
        }

        Date s = s1.before(s2) ? s2 : s1;
        Date e = e1.before(e2) ? e1 : e2;

        return e.before(s) ? 0 : intervalSeconds(e, s);
    }
    
    public static String getCurrentDate(String dateFormat) {
		SimpleDateFormat df = new SimpleDateFormat(dateFormat);
		String now = df.format(new Date());
		return now;
	}
    
    public static Date getCurrentDayDate(String time) {
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT);
		String now = df.format(new Date());
		String day = now.substring(0, 10);
		String resultTime = "";
		if (time == null || time.isEmpty()) {
			resultTime = day + " " + TimeConst.PVP_REFRESH_XIAOGUAI_TIME_3;
		} else {
			resultTime = day + " " + time;
		}

		Date date = null;
		try {
			date = df.parse(resultTime);
		} catch (ParseException e) {
			
		}
		
//		logger.debug("date after is:"+date);
		
		return date;
	}
    
    public static boolean isInvalidMail(String time) {
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT);
		String currentTimeStr = df.format(new Date());
		Date currentDate = null;
		Calendar calendar = Calendar.getInstance();   
	    try {
			calendar.setTime(df.parse(time));
			currentDate = df.parse(currentTimeStr);
		} catch (ParseException e) {
			
		}  
	    calendar.set(Calendar.DAY_OF_MONTH , calendar.get(Calendar.DAY_OF_MONTH) + RedisExpiredConst.EXPIRED_USERINFO_DAYS);
	    Date lastDate = calendar.getTime();
		
		if (lastDate.after(currentDate))
			return false;
		return true;
	}
    
    public static boolean timeIsAvailable(String startTimeStr, String endTimeStr) {
		return timeIsAvailable(startTimeStr, endTimeStr, TimeConst.DEFAULT_DATETIME_FORMAT);
	}
	
	public static boolean timeIsAvailable(String startTimeStr, String endTimeStr, String simpleDate) {
		SimpleDateFormat df = new SimpleDateFormat(simpleDate);
		Date startDate = null;
		Date endDate = null;
		Date currentDate = null;
		String currentTimeStr = df.format(new Date());
		try {
			startDate= df.parse(startTimeStr);
			endDate = df.parse(endTimeStr);
			currentDate = df.parse(currentTimeStr);
		} catch (ParseException e) {
			
		}  
		
		if (currentDate.after(startDate) && currentDate.before(endDate))
			return true;
		
		return false;
	}
	
	public static boolean timeIsBefore(String time1, String time2) {
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT);
		Date date1 = null;
		Date date2 = null;
		try {
			date1 = df.parse(time1);
			date2 = df.parse(time2);
		} catch (ParseException e) {
			return true;
		}  catch (NullPointerException e) {
			return true;
		}  
		
		return date1.before(date2);
	}
	
	public static boolean timeIsOver(String endTimeStr) {
		return timeIsOver(endTimeStr, TimeConst.DEFAULT_DATETIME_FORMAT);
	}
	
	public static boolean timeIsOver(String endTimeStr, String simpleDate) {
		SimpleDateFormat df = new SimpleDateFormat(simpleDate);
		Date endDate = null;
		Date currentDate = null;
		String currentTimeStr = df.format(new Date());
		try {
			endDate = df.parse(endTimeStr);
			currentDate = df.parse(currentTimeStr);
		} catch (ParseException e) {
			
		}  
		
		if (endDate == null)
			return false;
		if (currentDate.before(endDate))
			return false;
		
		return true;
	}
	
	public static boolean timeIsOver(Date endTime) {
		return timeIsOver(endTime, TimeConst.DEFAULT_DATETIME_FORMAT);
	}
	
	public static boolean timeIsOver(Date endTime, String simpleDate) {
		SimpleDateFormat df = new SimpleDateFormat(simpleDate);
		Date currentDate = null;
		String currentTimeStr = df.format(new Date());
		try {
			currentDate = df.parse(currentTimeStr);
		} catch (ParseException e) {
			
		}  
		
		if (currentDate.before(endTime))
			return false;
		
		return true;
	}
	
	public static int getWeekDay() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int day = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (day == 0) {
			day = 7;
		}
		
		return day;
	}
	
	public static boolean isNextDay(String lastLoginTime) {
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATE_FORMAT);
		boolean nextDay = false;
		if (lastLoginTime == null || lastLoginTime.trim().equals("")) {
			return true;
		}
		try {
			String now = df.format(new Date());
			String last = df.format(df.parse(lastLoginTime));
			if (now.equals(last)) {
				nextDay = false;
			} else {
				nextDay = true;
			}
		} catch (ParseException e) {
			nextDay = false;
		}
		
		return nextDay;
	}
	
	public static boolean isNextWeek(String lastLoginTime) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int now = c.get(Calendar.WEEK_OF_YEAR);
		
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATE_FORMAT);
		try {
			c.setTime(df.parse(lastLoginTime));
		} catch (ParseException e) {
			return false;
		}
		int last = c.get(Calendar.WEEK_OF_YEAR);
		
		if (now != last)
			return true;
		
		return false;
	}
	
	public static Date getEndDateOfD() {
		return setToDayEndTime(getDate());
	}
	
	/**
     * 取当前月份的某天
     */
    public static int getDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getDate());

        return cal.get(Calendar.DAY_OF_MONTH);
    }
}
