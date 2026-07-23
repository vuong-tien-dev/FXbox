package com.vtstudio.fxbox.utils;

import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.media.models.tiktok.ShortsDetails;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.utils.ModelUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchCommandHelper {

    /**
     * Checks if the query starts with a special search command and processes it.
     * Supported commands:
     * 1. =playlist: playlistName, start, end
     *    - =playlist: playlistName, limit -> gets first limit items
     *    - =playlist: playlistName, start, end -> gets items in range
     * 2. =shorts: start, end
     *    - =shorts: limit -> gets first limit items
     *    - =shorts: start, end -> gets items in range
     *
     * @param query    The search query.
     * @param database Room database instance.
     * @return List of Media if matches a command, otherwise null.
     */
    public static List<Media> tryExecuteCommand(String query, FxRoomDB database) {
        if (query == null) return null;
        query = query.trim();

        if (query.startsWith("=shorts:")) {
            return executeShortsCommand(query, database);
        } else if (query.startsWith("=playlist:")) {
            return executePlaylistCommand(query, database);
        }

        return null;
    }

    private static List<Media> executeShortsCommand(String query, FxRoomDB database) {
        String content = query.substring("=shorts:".length()).trim();
        String[] parts = content.split(",");
        int startPosition = 1;
        int endPosition = -1;
        boolean isLimitMode = false;

        if (parts.length == 1) {
            try {
                endPosition = Integer.parseInt(parts[0].trim());
                isLimitMode = true;
            } catch (NumberFormatException ignored) {}
        } else if (parts.length == 2) {
            try {
                startPosition = Integer.parseInt(parts[0].trim());
                String endPart = parts[1].trim();
                if ("-".equals(endPart) || "-1".equals(endPart)) {
                    endPosition = Integer.MAX_VALUE;
                } else if (endPart.startsWith("+")) {
                    try {
                        int count = Integer.parseInt(endPart.substring(1).trim());
                        endPosition = startPosition + count - 1;
                    } catch (NumberFormatException ignored) {}
                } else {
                    endPosition = Integer.parseInt(endPart);
                }
            } catch (NumberFormatException ignored) {}
        }

        if (endPosition != -1) {
            int listSize = database.shortsDetailsDao().getShortsCount();
            if (endPosition == Integer.MAX_VALUE) {
                endPosition = listSize;
            }

            boolean reversed = false;
            if (!isLimitMode && startPosition > endPosition) {
                int temp = startPosition;
                startPosition = endPosition;
                endPosition = temp;
                reversed = true;
            }

            if (isLimitMode) {
                startPosition = 1;
            } else {
                startPosition = Math.max(1, startPosition);
            }

            int offset = startPosition - 1;
            int limit = endPosition - startPosition + 1;

            if (limit > 0 && offset >= 0) {
                List<ShortsDetails> subList = database.shortsDetailsDao().getShortsDetailsWithLimitOffset(limit, offset);
                if (subList != null && !subList.isEmpty()) {
                    List<Media> shortsVideos = new ArrayList<>(ModelUtils.mediaFromDetails(subList));
                    if (reversed) {
                        java.util.Collections.reverse(shortsVideos);
                    }
                    return shortsVideos;
                }
            }
        }
        return new ArrayList<>();
    }

    private static List<Media> executePlaylistCommand(String query, FxRoomDB database) {
        String content = query.substring("=playlist:".length()).trim();
        String[] parts = content.split(",");
        if (parts.length >= 2) {
            String playlistName = parts[0].trim();
            int startPosition = 1;
            int endPosition = -1;
            boolean isLimitMode = false;

            if (parts.length == 2) {
                try {
                    endPosition = Integer.parseInt(parts[1].trim());
                    isLimitMode = true;
                } catch (NumberFormatException ignored) {}
            } else {
                try {
                    startPosition = Integer.parseInt(parts[1].trim());
                    String endPart = parts[2].trim();
                    if ("-".equals(endPart) || "-1".equals(endPart)) {
                        endPosition = Integer.MAX_VALUE;
                    } else if (endPart.startsWith("+")) {
                        try {
                            int count = Integer.parseInt(endPart.substring(1).trim());
                            endPosition = startPosition + count - 1;
                        } catch (NumberFormatException ignored) {}
                    } else {
                        endPosition = Integer.parseInt(endPart);
                    }
                } catch (NumberFormatException ignored) {}
            }

            if (endPosition != -1) {
                List<Playlist> allPlaylists = database.playlistDao().getAllPlaylists();
                Playlist targetPlaylist = null;
                for (Playlist p : allPlaylists) {
                    if (p.getName() != null && p.getName().equalsIgnoreCase(playlistName)) {
                        targetPlaylist = p;
                        break;
                    }
                }

                if (targetPlaylist == null) {
                    for (Playlist p : allPlaylists) {
                        if (p.getName() != null && p.getName().toLowerCase().contains(playlistName.toLowerCase())) {
                            targetPlaylist = p;
                            break;
                        }
                    }
                }

                if (targetPlaylist != null) {
                    List<String> videoIds = targetPlaylist.getVideoIdList();
                    if (videoIds != null && !videoIds.isEmpty()) {
                        int listSize = videoIds.size();
                        if (endPosition == Integer.MAX_VALUE) {
                            endPosition = listSize;
                        }

                        boolean reversed = false;
                        if (!isLimitMode && startPosition > endPosition) {
                            int temp = startPosition;
                            startPosition = endPosition;
                            endPosition = temp;
                            reversed = true;
                        }

                        if (isLimitMode) {
                            startPosition = 1;
                            endPosition = Math.min(listSize, endPosition);
                        } else {
                            startPosition = Math.max(1, startPosition);
                            endPosition = Math.min(listSize, endPosition);
                        }

                        List<Media> playlistVideos = new ArrayList<>();
                        if (startPosition <= endPosition && startPosition <= listSize) {
                            List<Long> fxIdList = ParserUtils.fromList(videoIds);
                            for (int i = startPosition - 1; i < endPosition; ++i) {
                                if (i < fxIdList.size()) {
                                    Long fxId = fxIdList.get(i);
                                    FxMediaVideo video = database.fxMediaVideoDao().getVideoByFxId(fxId);
                                    if (video == null) {
                                        video = ModelUtils.fromDetails(database.shortsDetailsDao().getDetailsByFxId(fxId));
                                    }
                                    if (video == null) {
                                        video = ModelUtils.fromDetails(database.ytvDetailsDao().getDetailsByFxId(fxId));
                                    }
                                    if (video != null) {
                                        playlistVideos.add(video);
                                    }
                                }
                            }
                            if (reversed) {
                                java.util.Collections.reverse(playlistVideos);
                            }
                        }
                        return playlistVideos;
                    }
                }
            }
        }
        return new ArrayList<>();
    }
}
