package com.vtstudio.fxbox.utils;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.media.models.FxMediaVideo;

import java.util.ArrayList;
import java.util.List;

public class ParserUtils {
    public static List<Long> fromList(@NonNull List<String> list) {
        List<Long> result = new ArrayList<>();

        for(String e : new ArrayList<>(list)) {
            result.add(Long.parseLong(e));
        }

        return result;
    }
    public static List<Long> fromListMedia(List<FxMediaVideo> videos) {
        List<Long> result = new ArrayList<>();

        if(videos == null || videos.isEmpty()) return result;

        for (FxMediaVideo video : new ArrayList<>(videos)) {
            result.add(video.getFxId());
        }

        return result;
    }
}
