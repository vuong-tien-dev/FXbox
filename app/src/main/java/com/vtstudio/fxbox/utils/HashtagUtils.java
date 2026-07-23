package com.vtstudio.fxbox.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;

public class HashtagUtils {
    public static SpannableString formatHashtags(CharSequence text) {

        if(text == null) return null;

        SpannableStringBuilder spannableBuilder;

        if (text instanceof Spannable) {
            // Nếu text đã là một SpannableString, sử dụng nó trực tiếp
            spannableBuilder = new SpannableStringBuilder((Spannable) text);
        } else {
            // Nếu text là một chuỗi bình thường, tạo một SpannableString mới từ nó
            spannableBuilder = new SpannableStringBuilder(text);
        }

        Pattern pattern = Pattern.compile("#\\w+"); // Pattern để tìm các từ hashtag
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            spannableBuilder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        return new SpannableString(spannableBuilder);
    }

    public static SpannableString formatHashtags(CharSequence text, @ColorInt int color) {

        if (text == null) return null;

        SpannableStringBuilder spannableBuilder;

        if (text instanceof Spannable) {
            // Nếu text đã là một SpannableString, sử dụng nó trực tiếp
            spannableBuilder = new SpannableStringBuilder((Spannable) text);
        } else {
            // Nếu text là một chuỗi bình thường, tạo một SpannableString mới từ nó
            spannableBuilder = new SpannableStringBuilder(text);
        }

        Pattern pattern = Pattern.compile("#\\w+"); // Pattern để tìm các từ hashtag
        Matcher matcher = pattern.matcher(spannableBuilder); // Sử dụng spannableBuilder thay vì text

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Tạo mới các instance của StyleSpan và ForegroundColorSpan
            StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(color);

            // Áp dụng style và color cho từng hashtag
            spannableBuilder.setSpan(styleSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableBuilder.setSpan(foregroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return new SpannableString(spannableBuilder);
    }

    public static SpannableString setTags(String pTagString, @ColorInt int color, @ColorInt int touchColor ) {
        SpannableString string = new SpannableString(pTagString);
        int start = -1;
        for (int i = 0; i < pTagString.length(); i++) {
            if (pTagString.charAt(i) == '#') {
                start = i;
            } else if (pTagString.charAt(i) == ' ' || pTagString.charAt(i) == '\n' || (i == pTagString.length() - 1 && start != -1)) {
                if (start != -1) {
                    if (i == pTagString.length() - 1) {
                        i++; // case for if hash is last word and there is no
                        // space after word
                    }

                    // Tạo mới các instance của StyleSpan và ForegroundColorSpan
                    StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
                    ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(color);

                    // Áp dụng style và color cho từng hashtag
                    string.setSpan(styleSpan, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    string.setSpan(foregroundColorSpan, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    string.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {

                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            Log.d("FxExpandable", "updateDrawState");
                        }
                    }, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    start = -1;
                }
            }
        }

        return string;
    }

}