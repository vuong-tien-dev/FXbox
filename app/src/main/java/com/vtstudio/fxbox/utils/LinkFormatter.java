package com.vtstudio.fxbox.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.View;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkFormatter {

    public static SpannableString formatUrls(CharSequence text, int linkColor) {
        if (text == null) return null;

        SpannableStringBuilder spannableBuilder;

        if (text instanceof Spannable) {
            // Nếu text đã là một SpannableString, sử dụng nó trực tiếp
            spannableBuilder = new SpannableStringBuilder((Spannable) text);
        } else {
            // Nếu text là một chuỗi bình thường, tạo một SpannableString mới từ nó
            spannableBuilder = new SpannableStringBuilder(text);
        }

        // Mẫu regex để tìm các URL
        Pattern pattern = Pattern.compile(
                "(http|https|ftp)://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(/[a-zA-Z0-9\\-\\.\\?\\,\\'/\\\\+&%\\$#_]*)?");
        Matcher matcher = pattern.matcher(text);

        // Tìm tất cả các URL và áp dụng CustomURLSpan và ForegroundColorSpan
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String url = matcher.group();

            ForegroundColorSpan colorSpan = new ForegroundColorSpan(linkColor);

           // spannableBuilder.setSpan(customURLSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableBuilder.setSpan(colorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return new SpannableString(spannableBuilder);
    }

    public static class CustomURLSpan extends URLSpan {
        private Context context;
        public CustomURLSpan(String url, Context context) {
            super(url);
            this.context = context;
        }

        @Override
        public void onClick(View widget) {
            // Xử lý sự kiện click vào URL
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getURL()));
            context.startActivity(intent);
        }
    }

}
