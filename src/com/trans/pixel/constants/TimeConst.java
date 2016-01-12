package com.trans.pixel.constants;

public class TimeConst {
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_INT = "yyyyMMdd";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public static final long S = 1000;
    public static final long M = 1000 * 60;
    public static final long H = 1000 * 60 * 60;
    public static final long D = 1000 * 60 * 60 * 24;

    // 时间相关参数
    public  static final long MINUTE_PER_RECOVER = 5;
    public static final long HOURS_PER_DAY = 24;
    public static final long MINUTES_PER_HOUR = 60;
    public static final long SECONDS_PER_MINUTE = 60;
    public static final long MILLIONSECONDS_PER_SECOND = 1000;
    public static final long MILLIONSECONDS_PER_MINUTE = MILLIONSECONDS_PER_SECOND * SECONDS_PER_MINUTE;
    public static final long MILLIONSECONDS_PER_HOUR = MILLIONSECONDS_PER_SECOND * SECONDS_PER_MINUTE
            * MINUTES_PER_HOUR;
    public static final long MILLION_SECOND_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE
            * MILLIONSECONDS_PER_SECOND;
    public static final long MILLION_SECOND_PER_MINUTE = SECONDS_PER_MINUTE
            * MILLIONSECONDS_PER_SECOND;
    public static final long MILLIONSECONDS_PER_RECOVER = MINUTE_PER_RECOVER * MILLION_SECOND_PER_MINUTE;
    
    public static final long SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    
    public static final String PVP_REFRESH_XIAOGUAI_TIME_1 = "00:00:00";
    public static final String PVP_REFRESH_XIAOGUAI_TIME_2 = "12:00:00";
    public static final String PVP_REFRESH_XIAOGUAI_TIME_3 = "18:00:00";
}
