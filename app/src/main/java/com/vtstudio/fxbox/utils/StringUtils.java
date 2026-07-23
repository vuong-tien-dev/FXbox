package com.vtstudio.fxbox.utils;

public class StringUtils {
    public static boolean containsIn (String str, String... keys){
        for(String key : keys){
            if(str.contains(key)) return true;
        }
        return false;
    }
}
