package com.vtstudio.fxbox.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlExtractor {

    // Hàm để trích xuất ID từ URL YouTube
    public static String getYouTubeId(String youTubeUrl) {
        String pattern = "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*";

        Pattern compiledPattern = Pattern.compile(pattern,
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            int indexEnd = youTubeUrl.indexOf("?");
            int indexStart = youTubeUrl.indexOf("/");

            if (indexEnd == -1 || indexStart == -1) return null;

            return youTubeUrl.substring(indexStart + 1, indexEnd);
        }
    }
}