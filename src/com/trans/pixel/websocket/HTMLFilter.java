package com.trans.pixel.websocket;
/** 
 * HTML 工具类  
 * 
 * @author XXX 
 */  
public final class HTMLFilter {  
    public static String filter(String message) {  
        if (message == null)  
            return (null);  
        char content[] = new char[message.length()];  
        message.getChars(0, message.length(), content, 0);  
        StringBuilder result = new StringBuilder(content.length + 50);  
        //控制对尖括号等特殊字符进行转义
        for (int i = 0; i < content.length; i++) {  
            switch (content[i]) {  
            case '<':
                result.append("&lt;");
                break;
            case '>':
                result.append("&gt;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '"':
                result.append("&quot;");
                break;
            default:
                result.append(content[i]);
            }
        }  
        return (result.toString());  
    }  
}  