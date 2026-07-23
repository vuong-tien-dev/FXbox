package com.vtstudio.fxbox.fxviews.textview;

import android.text.TextPaint;
import android.text.style.URLSpan;

public class NoUnderlineURLSpan extends URLSpan {
    public NoUnderlineURLSpan(String url) {
        super(url);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        // Không gọi super.updateDrawState(ds) để tránh underline và background highlight mặc định
        // Đặc biệt cần thiết cho Android 13+ có click feedback mới cho URLSpan
        ds.setColor(ds.linkColor); // Giữ màu link
        ds.setUnderlineText(false); // Không gạch chân
        // Background sẽ không được set vì không gọi super
    }
}