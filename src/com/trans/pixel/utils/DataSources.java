package com.trans.pixel.utils;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DataSources extends AbstractRoutingDataSource {
    private static final ThreadLocal<String> contextHolder=new ThreadLocal<String>();  
    
    public static void setDataSourceType(String dataSourceType){  
        contextHolder.set(dataSourceType);  
    }  
      
    public static String getDataSourceType(){  
        return (String) contextHolder.get();  
    }  
      
    public static void clearDataSourceType(){  
        contextHolder.remove();  
    }
	@Override
	protected Object determineCurrentLookupKey() {
		return getDataSourceType();  
	}
}
