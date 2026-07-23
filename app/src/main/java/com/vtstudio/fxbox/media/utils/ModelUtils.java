package com.vtstudio.fxbox.media.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.database.dao.ShortsUserDao;
import com.vtstudio.fxbox.media.models.tiktok.Comment;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.tiktok.ShortsDetails;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.youtube.YTDetails;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;

import java.util.ArrayList;
import java.util.List;

public class ModelUtils {
    public static List<ShortsVideo> fromDetailsList(List<ShortsDetails> details3s) {
        List<ShortsVideo> shorts3s = new ArrayList<>();

        if (details3s != null) {
            for (ShortsDetails d : details3s) {
                shorts3s.add(fromDetails(d));
            }
        }
        return shorts3s;
    }

    public static List<YTVideo> fromYTDetailsList(List<YTDetails> details3s) {
        List<YTVideo> shorts3s = new ArrayList<>();

        if (details3s != null) {
            for (YTDetails d : details3s) {
                shorts3s.add(fromDetails(d));
            }
        }
        return shorts3s;
    }

    public static List<String> awemeIdsFrom(List<ShortsVideo> videos) {
        List<String> awemeIds = new ArrayList<>();
        for (ShortsVideo v : videos) {
            awemeIds.add(v.getAwemeId());
        }
        return awemeIds;
    }

    public static List<String> fxIdsFromFxMediaVideo(List<FxMediaVideo> videos) {
        List<String> fxIds = new ArrayList<>();
        for (FxMediaVideo v : videos) {
            fxIds.add(String.valueOf(v.getFxId()));
        }
        return fxIds;
    }
    public static List<String> awemeIdsFromFxMediaVideo(List<FxMediaVideo> videos) {
        List<String> awemeIds = new ArrayList<>();
        for (FxMediaVideo v : videos) {
            if(v instanceof ShortsVideo) awemeIds.add(((ShortsVideo) v).getAwemeId());
        }
        return awemeIds;
    }

    public static ShortsVideo fromDetails(ShortsDetails details3) {
        if (details3 == null) return null;

        ShortsVideo v = details3.getShortsVideo();
        v.setShortsMusic(details3.getShortsMusic());
        v.setShortsUser(details3.getShortsUser());

        return v;
    }

    public static YTVideo fromDetails(YTDetails details3) {
        if (details3 == null) return null;

        YTVideo v = details3.getVideo();
        v.setUser(details3.getUser());

        return v;
    }

    public static List<FxMediaVideo> fxFromDetails(List<ShortsDetails> details3s) {
        List<FxMediaVideo> shorts3s = new ArrayList<>();
        if (details3s != null) {
            for (ShortsDetails d : details3s) {
                ShortsVideo v = d.getShortsVideo();
                if (v != null) {
                    v.setShortsUser(d.getShortsUser());
                    v.setShortsMusic(d.getShortsMusic());
                    shorts3s.add(v);
                }
            }
        }
        return shorts3s;
    }

    public static List<Media> mediaFromDetails(List<ShortsDetails> details3s) {
        List<Media> shorts3s = new ArrayList<>();
        if (details3s != null) {
            for (ShortsDetails d : details3s) {
                ShortsVideo v = d.getShortsVideo();
                v.setShortsUser(d.getShortsUser());
                v.setShortsMusic(d.getShortsMusic());
                shorts3s.add(v);
            }
        }
        return shorts3s;
    }

    public static List<Media> mediaFromFxMediaVideo(List<FxMediaVideo> details3s) {
        List<Media> shorts3s = new ArrayList<>();
        if (details3s != null) {
            shorts3s.addAll(details3s);
        }
        return shorts3s;
    }

    public static void fetchUserToComment(@NonNull List<Comment> commentList, @NonNull ShortsUserDao userDao) {
        for (Comment comment : new ArrayList<>(commentList)) {
            ShortsUser user = userDao.getUserById(comment.getUid());
            comment.setShortsUser(user);
        }
    }

    public static List<FxMediaVideo> filterFrom (@NonNull List<Media> mediaList) {
        List<FxMediaVideo> result = new ArrayList<>();
        for (int i = 0; i < mediaList.size(); ++i) {
            Media media = mediaList.get(i);
            if(media instanceof FxMediaVideo) {
                result.add((FxMediaVideo) media);
            }
        }
        return result;
    }

    public static long retrieveFxIdFromNameIfCan(@NonNull String name) {
        try {
            int indexOf_ = name.lastIndexOf("_");
            long id;
            if (indexOf_ == -1) {
                if (name.contains("TiktokPackage Downloader")) {
                    indexOf_ = name.lastIndexOf(" ");
                    String idString = name.substring(indexOf_ + 1);
                    id = Long.parseLong(idString);
                } else {
                    id = System.currentTimeMillis();
                    Log.d("ContentService", "name: " + name + " id: " + id);
                }
            } else {
                id = Long.parseLong(name.substring(indexOf_ + 1));
            }

            return id;
        } catch (NumberFormatException formatException) {
            throw new NumberFormatException(name + " cannot retrieve fxId please fix it name");
        }
    }
}
