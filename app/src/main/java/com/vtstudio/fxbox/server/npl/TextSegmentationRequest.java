package com.vtstudio.fxbox.server.npl;

import com.vtstudio.fxbox.media.models.TextSegment;
import com.vtstudio.fxbox.server.OnResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TextSegmentationRequest extends NPLBaseRequest<TextSegmentationRequest, TextSegment> {
    private String rawText;
    public TextSegmentationRequest rawText(String rawText) {
        this.rawText = rawText;
        return this;
    }
    protected TextSegmentationRequest(NPLApi api) {
        super(api);
    }

    @Override
    protected void executeRequest(NPLApi api) {

        OkHttpClient okHttpClient = api.getOkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        String requestBodyString = "{\"sourceType\":\"TEXT\",\"source\":\"" + rawText + "\"}";
        RequestBody body = RequestBody.create(mediaType, requestBodyString);

        Request request = new Request.Builder()
                .url("https://api.ai21.com/studio/v1/segmentation")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("Authorization", "Bearer 3m3bbUHm3cnfqBPySIoeUF6VmD4reo4w")
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    JSONObject result = new JSONObject(response.body().string());

                    TextSegment textSegment = new TextSegment();
                    List<String> segments = new ArrayList<>();

                    JSONArray array = result.optJSONArray("segments");
                    if (array != null) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject o = array.getJSONObject(i);
                            if (o != null) {
                                String segment = o.optString("segmentText");
                                segments.add(segment);
                            }
                        }
                        textSegment.setSegments(segments);
                        notifyOnResponse(new com.vtstudio.fxbox.server.Response<>(textSegment, true, "A21", null));

                    } else {
                        notifyOnResponse(new com.vtstudio.fxbox.server.Response<>(null, false, "A21", new OnResponseListener.Errol(OnResponseListener.Errol.DATA_ERROL)));
                    }
                } else {
                    notifyOnResponse(new com.vtstudio.fxbox.server.Response<>(null, false, "A21", new OnResponseListener.Errol(OnResponseListener.Errol.DATA_ERROL)));
                }
            }
        } catch (IOException | JSONException e) {
            notifyOnResponse(new com.vtstudio.fxbox.server.Response<>(null, false, "A21", new OnResponseListener.Errol(OnResponseListener.Errol.SERVER_ERROL)));
        }
        finally {
            release();
        }
    }

}
