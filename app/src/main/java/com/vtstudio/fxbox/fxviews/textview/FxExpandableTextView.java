package com.vtstudio.fxbox.fxviews.textview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.utils.HashtagUtils;
import com.vtstudio.fxbox.utils.LinkFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Copyright (C) 2017 Cliff Ophalvens (Blogc.at)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Cliff Ophalvens (Blogc.at)
 * @Updater Vuong Tien Developer
 */
public class FxExpandableTextView extends androidx.appcompat.widget.AppCompatTextView {
    public static final int DEFAULT_ANIMATION_DURATION = 750;
    private final List<OnExpandListener> onExpandListeners;
    private TimeInterpolator expandInterpolator;
    private TimeInterpolator collapseInterpolator;
    private int maxLines;
    private long animationDuration;
    private boolean animating;
    private boolean expanded;
    private int collapsedHeight;
    private boolean expandable;
    private boolean isScrollChanged;
    private int collapseTextButtonColor;
    private int expandTextButtonColor;
    private int titleColor;
    private float x;
    private float y;
    private boolean isTouchCollapseText = false;
    private boolean isPaddingAdded = false;
    private int respannedCount = 0;
    private int currentPaddingAdded = 0;
    private int maxLinesForScrolling;
    private boolean scrollEnabled;
    private boolean scrollableComfirmed;
    private Paint paint;
    private final String collapseTextButton;
    /*
     * Đây là trường cơ bản cho collapse string
     * @collapseString sẽ bằng collapseRawString + ... + collapseTextButton
     * collapseRawString sẽ được phân tích và gán mỗi khi isOtherText
     */
    private String collapseRawString;
    private String expandTextButton = "";
    private SpannableString originalString;
    private SpannableString collapseString;
    private String title;
    private float collapseTextWidth;
    private float collapseTextHeight;

    public FxExpandableTextView(final Context context) {
        this(context, null);
    }

    public FxExpandableTextView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FxExpandableTextView(final Context context, @Nullable final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        // read attributes
        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.FxExpandableTextView, defStyle, 0);
        this.animationDuration = attributes.getInt(R.styleable.FxExpandableTextView_animation_duration, DEFAULT_ANIMATION_DURATION);
        this.maxLinesForScrolling = attributes.getInt(R.styleable.FxExpandableTextView_maxLinesToScroll, Integer.MAX_VALUE);
        this.expandTextButton = attributes.getString(R.styleable.FxExpandableTextView_expandTextButton);
        this.collapseTextButton = attributes.getString(R.styleable.FxExpandableTextView_collapseTextButton);
        this.collapseTextButtonColor = attributes.getColor(R.styleable.FxExpandableTextView_collapseTextButtonColor, ContextCompat.getColor(context, R.color.rgb_240));
        this.expandTextButtonColor = attributes.getColor(R.styleable.FxExpandableTextView_expandTextButtonColor, ContextCompat.getColor(context, R.color.rgb_240));
        this.titleColor = attributes.getColor(R.styleable.FxExpandableTextView_highlightTitleColor, getTextColors().getDefaultColor());
        this.title = attributes.getString(R.styleable.FxExpandableTextView_highlightTitle);
        attributes.recycle();

        setLinkTextColor(getTextColors());
        setMovementMethod(LinkMovementMethod.getInstance());
        setVerticalFadingEdgeEnabled(false);

        // make default text for button

        // keep the original value of maxLines
        this.maxLines = this.getMaxLines();

        // create bucket of OnExpandListener instances
        this.onExpandListeners = new ArrayList<>();

        // create default interpolators
        this.expandInterpolator = new DecelerateInterpolator();
        this.collapseInterpolator = new DecelerateInterpolator();

        // fadingEdgeLength
        setFadingEdgeLength((int) (context.getResources().getDisplayMetrics().density * 20));

        // init paint
        initPaint();

        // initTextBounds
        initTextBounds();

        // add textWatcher to get Expandable
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Log.d("CustomTextView", "onTextChanged" + s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                FxExpandableTextView.this.post(() -> {
                    // văn bản nguyên bản
                    StringBuilder textSb = new StringBuilder(getText().toString());

                    boolean isOtherText = !(textSb.toString().equals(String.valueOf(collapseString)) || textSb.toString().equals(String.valueOf(originalString)));
                    // đảm bảo lineCount luôn nhỏ hơn hoặc bằng maxlines
                    if (!textSb.toString().isEmpty() && textSb.toString().equals(String.valueOf(collapseString)) && expandable) {
                        if (getLineCount() > maxLines) {
                            Log.d("FxExpandableTextView", "Text must be respanned: " + collapseRawString);
                            respannedCount++; // Nếu có lỗi gì đó khiến phải thực hiện quá nhiều, đếm lên để toast
                            collapseString = paintCollapsedText(collapseRawString);
                            setCollapsedText();
                        }
                    }

                    if (isOtherText) {
                        expandable = getLineCount() > maxLines;
                        respannedCount = 0;
                        unEnableScroll();
                        superMaxLines(maxLines);

                        isScrollChanged = false;
                        isTouchCollapseText = false; // người duùng chạm vào chữ "thu gọn"
                        expanded = false;
                        originalString = null;
                        collapseString = null;
                        scrollableComfirmed = false;

                        if (expandable) {
                            // lấy văn bản dòng cuối cùng
                            int endSubIndex = getLayout().getLineEnd(maxLines - 1);
                            int startSubIndex = getLayout().getLineStart(maxLines - 1);

                            StringBuilder textAtMaxLine = new StringBuilder(textSb.substring(startSubIndex, endSubIndex).trim());
                            List<String> wordsInTextAtMaxLine = new ArrayList<>(Arrays.asList(textAtMaxLine.toString().split(" ")));

                            Paint mPaint = getPaint();
                            float maxTextWidth = getWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
                            float expandTextButtonWidth = paint.measureText(expandTextButton); // paint riêng cho nút ex/col

                            float modifiedWidth = mPaint.measureText(textAtMaxLine + "... ") + expandTextButtonWidth;

                            Log.d("FxExpandableTextView", "Chiều dài vùng chứa văn bản là: " + maxTextWidth);
                            Log.d("FxExpandableTextView", "sau khi tính toán xong, chiều dài văn bản dòng cuối là: " + modifiedWidth);

                            // Loại bỏ một số từ ở cuối dòng để chừa chỗ cho chữ "xem thêm" nếu không đủ chỗ
                            while (modifiedWidth >= maxTextWidth && !wordsInTextAtMaxLine.isEmpty()) {
                                wordsInTextAtMaxLine.remove(wordsInTextAtMaxLine.size() - 1); // xóa 1 từ cuối dòng
                                textAtMaxLine = new StringBuilder(); // tạo lại chuỗi mới
                                for (int i = 0; i < wordsInTextAtMaxLine.size(); i++) {
                                    textAtMaxLine.append(wordsInTextAtMaxLine.get(i));
                                    if (i < wordsInTextAtMaxLine.size() - 1) {
                                        textAtMaxLine.append(" ");
                                    }
                                }

                                // tính lại chiều rộng của văn bản khi loại bỏ một từ
                                modifiedWidth = mPaint.measureText(textAtMaxLine + "... ") + expandTextButtonWidth;
                            }

                            Log.d("FxExpandableTextView", "sau khi loại bớt ký tự, chiều dài văn bản dòng cuối là: " + modifiedWidth);

                            String textFromFirstLine = "";
                            // lấy dòng đầu tiên
                            if (maxLines > 1) {
                                textFromFirstLine = textSb.substring(0, getLayout().getLineEnd(maxLines - 2));
                            }

                            // thêm ký tự ngắt dòng cho dòng đầu tiên nếu chưa có
                            boolean isNotContainedBreakLine = !textFromFirstLine.contains("\n");
                            Log.d("FxExpandableTextView", "chiều dài văn bản dòng cuối: " + textAtMaxLine.length());
                            collapseRawString = textFromFirstLine + ((textAtMaxLine.length() > 0 && isNotContainedBreakLine) ? textAtMaxLine : "\n");

//                            if (textAtMaxLine.length() <= 0 && isNotContainedBreakLine)
//                                textAtMaxLine.append("\n");
                            textAtMaxLine.append("... ");

                            // Căn chỉnh chữ "xem thêm" ra ngoài rìa textview
                            // Sử dụng non-breaking space (\u00A0) thay vì space thường
                            // để tránh bị collapse trên Android 13+
                            final String NBSP = "\u00A0"; // Non-breaking space
                            final float nbspWidth = mPaint.measureText(NBSP);
                            final float textWidthRatio = 0.9f;
                            
                            // Tính số lượng NBSP cần thiết thay vì loop append
                            float remainingSpace = (maxTextWidth * textWidthRatio) - modifiedWidth;
                            int nbspCount = (int) (remainingSpace / nbspWidth);
                            
                            // Append NBSP một lần thay vì loop
                            if (nbspCount > 0) {
                                StringBuilder nbspBuilder = new StringBuilder();
                                for (int i = 0; i < nbspCount; i++) {
                                    nbspBuilder.append(NBSP);
                                }
                                textAtMaxLine.append(nbspBuilder);
                                modifiedWidth += nbspCount * nbspWidth;
                            }

                            // loại bỏ tilte của text khỏi original text nếu có
                            String textExceptTitle = textFromFirstLine + textAtMaxLine;

                            // Lấy title ra và tô đậm
                            SpannableString spannedTitle = null;
                            int titleIndex = -1;
                            if (title != null && !title.isEmpty()) {

                                if (!textExceptTitle.contains(title)) {
                                    textExceptTitle = title + (title.charAt(title.length() - 1) == '\n' ? "" : " ") + textExceptTitle;
                                }

                                titleIndex = textExceptTitle.indexOf(title) + title.length();
                                textExceptTitle = textExceptTitle.substring(titleIndex);
                                float desireTextScale = 1.3f;
                                spannedTitle = paintTitle(title, desireTextScale);
                            }

                            SpannableStringBuilder collapseBuilder = new SpannableStringBuilder();

                            if (spannedTitle != null) collapseBuilder.append(spannedTitle);

                            collapseBuilder.append(HashtagUtils.formatHashtags(textExceptTitle));
                            collapseBuilder.append(paintExpandableText(expandTextButton));

                            collapseString = new SpannableString(collapseBuilder);

                            String targetOriginal = textSb.toString();
                            SpannableStringBuilder originalBuilder = new SpannableStringBuilder();

                            if (spannedTitle != null) {
                                targetOriginal = targetOriginal.substring(titleIndex);
                                originalBuilder.append(spannedTitle);
                            }

                            originalBuilder.append(targetOriginal);
                            originalBuilder.append("\n\n");
                            originalString = HashtagUtils.formatHashtags(originalBuilder);
                            originalString = LinkFormatter.formatUrls(originalString, getContext().getColor(R.color.colorAccent));
                            setCollapsedText();
                        } else {

                            SpannableString spannedTitle = null;
                            SpannableStringBuilder originalBuilder = new SpannableStringBuilder();
                            String textWithTitle = textSb.toString();
                            int titleIndex = -1;
                            if (title != null && !title.isEmpty()) {

                                if (!textWithTitle.contains(title)) {
                                    textWithTitle = title + (title.charAt(title.length() - 1) == '\n' ? "" : " ") + textWithTitle;
                                }

                                titleIndex = textWithTitle.indexOf(title) + title.length();
                                textWithTitle = textWithTitle.substring(titleIndex);
                                float desireTextScale = 1.3f;
                                spannedTitle = paintTitle(title, desireTextScale);
                                originalBuilder.append(spannedTitle);
                            }

                            originalBuilder.append(textWithTitle);
                            originalString = HashtagUtils.formatHashtags(originalBuilder);
                            originalString = LinkFormatter.formatUrls(originalString, getContext().getColor(R.color.colorAccent));
                            setExpandedText();
                        }

                        setClickable(expandable);
                    }
                });
            }
        });

    }


    private SpannableString paintCollapsedText(String text) {
        if (respannedCount > 10) {
            post(() -> Toast.makeText(getContext(), "This desc has been spanned too much: " + collapseRawString, Toast.LENGTH_LONG).show());
            return new SpannableString("This desc has been spanned too much!");
        }
        //Log.d("FxExpandableTextView", "placeholder: " + collapseRawString.replace(" ", "_"));
        String reg = " ";
        List<String> words = Arrays.asList(text.split(reg));

        if (words.size() < 2) {
            reg = "#";
            words = Arrays.asList(collapseRawString.split(reg));
        }
        //words.remove(words.size() - 1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.size() - 1; i++) {
            builder.append(words.get(i));
            if (i < words.size() - 2) {
                builder.append(reg);
            }
        }

        String textExceptTitle = builder.toString();
        SpannableString spannedTitle = null;
        if (title != null && !title.isEmpty() && (textExceptTitle.contains(title) || title.contains(textExceptTitle))) {
            //Log.d("FxExpandableTextView", "text: " + textExceptTitle);
            if (textExceptTitle.contains(title)) {
                Log.d("FxExpandableTextView", "title be contained: " + getText());
                textExceptTitle = textExceptTitle.substring(textExceptTitle.indexOf(title) + title.length());
                spannedTitle = paintTitle(title, 1.3f);
            } else if (title.contains(textExceptTitle)) {
                Log.d("FxExpandableTextView", "title contains: " + getText());
                spannedTitle = paintTitle(textExceptTitle, 1.3f);
                textExceptTitle = "";
            }
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        collapseRawString = "";
        if (spannedTitle != null) {
            collapseRawString += spannedTitle.toString();
            spannableStringBuilder.append(spannedTitle);
        }


        collapseRawString += textExceptTitle;

        spannableStringBuilder.append(String.valueOf(HashtagUtils.formatHashtags(textExceptTitle))).append("... ");

        spannableStringBuilder.append(paintExpandableText(expandTextButton));

        return new SpannableString(spannableStringBuilder);
    }

    private void initTextBounds() {
        if (paint != null && collapseTextButton != null) {
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            collapseTextWidth = paint.measureText(collapseTextButton);
            collapseTextHeight = Math.abs(fontMetrics.bottom - fontMetrics.top);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (expanded) {
            collapse();
        }
    }

    public void notifyOnViewRecycled() {
        if (expanded) {
            collapse();
        }
        //
        collapseString = null;
        originalString = null;
        collapseRawString = null;
        expandable = false;
        expanded = false;
        scrollEnabled = false;
        title = null;
        x = 0;
        y = 0;
        superMaxLines(maxLines);
        scrollTo(0, 0);
        onExpandListeners.clear();
        this.setText("");
        setHeight(0);
        requestLayout();
    }

    private void setCollapsedText() {
        setText(collapseString);
    }

    private void setExpandedText() {
        setText(originalString);
    }

    private SpannableString paintExpandableText(String expandText) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);

        int start = 0;
        builder.append(expandText);
        int end = builder.length();
        builder.setSpan(boldSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(expandTextButtonColor);

        NoUnderlineURLSpan urlSpan = new NoUnderlineURLSpan("") {
            @Override
            public void onClick(View widget) {
                toggle();
            }
        };

        builder.setSpan(urlSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return new SpannableString(builder);
    }

    private SpannableString paintTitle(String title, float textScale) {
        SpannableStringBuilder builder = new SpannableStringBuilder(title);

        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(textScale);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(titleColor);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);

        builder.setSpan(boldSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(relativeSizeSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return new SpannableString(builder);
    }

    private void initPaint() {
        paint = new Paint();
        Paint mPaint = getPaint();
        paint.set(mPaint);
        paint.setColor(collapseTextButtonColor);
        paint.setFakeBoldText(true);
    }


    @Override
    protected void onMeasure(final int widthMeasureSpec, int heightMeasureSpec) {
        // if this TextView is collapsed and maxLines = 0,
        // than make its height equals to zero
        if (this.maxLines == 0 && !this.expanded && !this.animating) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (expanded) {

            final int action = event.getAction();

            float x = event.getX();
            float y = event.getY();

            // Log.d("FxExpanded", "onTouchEvent X: " + x + ", Y: " + y);

            boolean isTouchDown = action == MotionEvent.ACTION_DOWN;

            boolean isTouchUp = action == MotionEvent.ACTION_UP;

            if (isTouchUp) {
                if (isTouchCollapseText) {
                    onCollapseTextClick();
                }

                isTouchCollapseText = false;
                return true;
            }

            final float limitBottomY = getHeight();
            final float limitLeftX = getWidth() - collapseTextWidth;
            final float limitTopY = limitBottomY - collapseTextHeight;

            if ((x <= getWidth() && x >= limitLeftX && y <= limitBottomY && y >= limitTopY) && isTouchDown) {
                isTouchCollapseText = true;
                scrollableComfirmed = false;
            }

            if ((scrollEnabled && canScrollVertically(1)) || scrollableComfirmed) {
                getParent().requestDisallowInterceptTouchEvent(true);
                scrollableComfirmed = true;
            }
        }

        return super.onTouchEvent(event);
    }

    private void onCollapseTextClick() {
        toggle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawLine(0, 0, getWidth(), 0, getPaint());
        if (expandable) {
            if (expanded || animating) {
                if (!isScrollChanged) {
                    calculateXY();
                } else {
                    isScrollChanged = false;
                }

                if (!animating) {
                    canvas.drawText(collapseTextButton, x, y, paint);
                }

                canvas.clipRect(0, 0, getWidth(), getScrollY() + getHeight() - getLineHeight() * 2);
                super.onDraw(canvas);
                return;
            }
        }
        super.onDraw(canvas);
    }

    private void superMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
    }

    @Override
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
        super.setMaxLines(maxLines);
    }

    private void calculateXY() {
        int width = getWidth();
        int height = getHeight();

        x = width - collapseTextWidth;
        y = height - collapseTextHeight / 2;
    }

    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {

        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);

        isScrollChanged = true;

        final Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        // Tính toán tọa độ x và y dựa trên chiều rộng, chiều cao, kích thước văn bản và vị trí cuộn của TextView
        x = getWidth() - paint.measureText(collapseTextButton);
        y = getHeight() + vert - collapseTextHeight / 2;

    }

    //region public helper methods

    /**
     * Toggle the expanded state of this {@link FxExpandableTextView}.
     *
     * @return true if toggled, false otherwise.
     */
    public boolean toggle() {
        return this.expanded
                ? this.collapse()
                : this.expand();
    }

    /**
     * Expand this {@link FxExpandableTextView}.
     *
     * @return true if expanded, false otherwise.
     */
    public boolean expand() {

        if (!this.expanded && !this.animating && this.maxLines >= 0) {
            // notify listener
            this.notifyOnExpand();

            // measure collapsed height
            this.measure
                    (
                            MeasureSpec.makeMeasureSpec(this.getMeasuredWidth(), MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    );

            this.collapsedHeight = this.getMeasuredHeight();

            // indicate that we are now animating
            this.animating = true;

            // set maxLines to MAX Integer, so we can calculate the expanded height
            this.superMaxLines(maxLinesForScrolling);
            final int expandTextPadding = (int) (collapseTextHeight * 1.5f);
            // addPaddingBottom(expandTextPadding);
            setExpandedText();

            // measure expanded height
            this.measure
                    (
                            MeasureSpec.makeMeasureSpec(this.getMeasuredWidth(), MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    );


            final int expandedHeight = this.getMeasuredHeight();

            // animate from collapsed height to expanded height
            final ValueAnimator valueAnimator = ValueAnimator.ofInt(this.collapsedHeight, expandedHeight);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(final ValueAnimator animation) {
                    notifyOnExpanding();
                    FxExpandableTextView.this.setHeight((int) animation.getAnimatedValue());
                }
            });

            // wait for the animation to end
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    // reset min & max height (previously set with setHeight() method)
                    FxExpandableTextView.this.setMaxHeight(expandedHeight);
                    FxExpandableTextView.this.setMinHeight(0);

                    // make scrollable
                    enableScroll();

                    // if fully expanded, set height to WRAP_CONTENT, because when rotating the device
                    // the height calculated with this ValueAnimator isn't correct anymore
                    final ViewGroup.LayoutParams layoutParams = FxExpandableTextView.this.getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    FxExpandableTextView.this.setLayoutParams(layoutParams);

                    // keep track of current status
                    FxExpandableTextView.this.expanded = true;
                    FxExpandableTextView.this.animating = false;

                    isScrollChanged = false;

                    notifyOnExpanded();
                }
            });

            // set interpolator
            valueAnimator.setInterpolator(this.expandInterpolator);

            // start the animation
            valueAnimator
                    .setDuration(this.animationDuration)
                    .start();

            return true;
        }

        return false;
    }

    /**
     * Collapse this {@link TextView}.
     *
     * @return true if collapsed, false otherwise.
     */
    public boolean collapse() {
        if (this.expanded && !this.animating && this.maxLines >= 0) {
            // notify listener
            this.notifyOnCollapse();

            // measure expanded height
            final int expandedHeight = this.getMeasuredHeight();

            // indicate that we are now animating
            this.animating = true;

            // animate from expanded height to collapsed height
            final ValueAnimator valueAnimator = ValueAnimator.ofInt(expandedHeight, (int) (this.collapsedHeight - getLineHeight() - getTextSize()));
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(final ValueAnimator animation) {
                    notifyOnCollapsing();
                    FxExpandableTextView.this.setHeight((int) animation.getAnimatedValue());
                }
            });

            // wait for the animation to end
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {

                    // keep track of current status
                    FxExpandableTextView.this.expanded = false;
                    FxExpandableTextView.this.animating = false;

                    Log.d("Animator", "OnAnimationEnd");

                    // set maxLines back to original value
                    FxExpandableTextView.this.superMaxLines(FxExpandableTextView.this.maxLines);

                    // cancel scrollable
                    unEnableScroll();


                    // if fully collapsed, set height back to WRAP_CONTENT, because when rotating the device
                    // the height previously calculated with this ValueAnimator isn't correct anymore
                    final ViewGroup.LayoutParams layoutParams = FxExpandableTextView.this.getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    FxExpandableTextView.this.setLayoutParams(layoutParams);

                    // removePaddingBottom();
                    setCollapsedText();

                    notifyOnCollapsed();

                }
            });

            // set interpolator
            valueAnimator.setInterpolator(this.collapseInterpolator);

            // start the animation
            valueAnimator
                    .setDuration(this.animationDuration)
                    .start();

            return true;
        }

        return false;
    }

    private void unEnableScroll() {
        if (scrollEnabled) {
            scrollTo(0, 0);
            setVerticalScrollBarEnabled(false);
            setVerticalFadingEdgeEnabled(false);
            setMovementMethod(new LinkMovementMethod());
            scrollEnabled = false;
        }
    }

    private void enableScroll() {
        if (getLineCount() > maxLinesForScrolling) {
            setVerticalFadingEdgeEnabled(true);
            setVerticalScrollBarEnabled(true);
            setMovementMethod(ScrollingMovementMethod.getInstance());
            setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
            scrollEnabled = true;
        }
    }

    //endregion

    //region public getters and setters

    /**
     * Sets the duration of the expand / collapse animation.
     *
     * @param animationDuration duration in milliseconds.
     */
    public void setAnimationDuration(final long animationDuration) {
        this.animationDuration = animationDuration;
    }

    /**
     * Adds a listener which receives updates about this {@link FxExpandableTextView}.
     *
     * @param onExpandListener the listener.
     */
    public void addOnExpandListener(final OnExpandListener onExpandListener) {
        this.onExpandListeners.add(onExpandListener);
    }

    /**
     * Removes a listener which receives updates about this {@link FxExpandableTextView}.
     *
     * @param onExpandListener the listener.
     */
    public void removeOnExpandListener(final OnExpandListener onExpandListener) {
        this.onExpandListeners.remove(onExpandListener);
    }

    /**
     * Sets a {@link TimeInterpolator} for expanding and collapsing.
     *
     * @param interpolator the interpolator
     */
    public void setInterpolator(final TimeInterpolator interpolator) {
        this.expandInterpolator = interpolator;
        this.collapseInterpolator = interpolator;
    }

    /**
     * Sets a {@link TimeInterpolator} for expanding.
     *
     * @param expandInterpolator the interpolator
     */
    public void setExpandInterpolator(final TimeInterpolator expandInterpolator) {
        this.expandInterpolator = expandInterpolator;
    }

    /**
     * Returns the current {@link TimeInterpolator} for expanding.
     *
     * @return the current interpolator, null by default.
     */
    public TimeInterpolator getExpandInterpolator() {
        return this.expandInterpolator;
    }

    /**
     * Sets a {@link TimeInterpolator} for collpasing.
     *
     * @param collapseInterpolator the interpolator
     */
    public void setCollapseInterpolator(final TimeInterpolator collapseInterpolator) {
        this.collapseInterpolator = collapseInterpolator;
    }

    /**
     * Returns the current {@link TimeInterpolator} for collapsing.
     *
     * @return the current interpolator, null by default.
     */
    public TimeInterpolator getCollapseInterpolator() {
        return this.collapseInterpolator;
    }

    /**
     * Is this {@link FxExpandableTextView} expanded or not?
     *
     * @return true if expanded, false if collapsed.
     */
    public boolean isExpanded() {
        return this.expanded;
    }

    //endregion

    /**
     * This method will notify the listener about this view being expanded.
     */
    private void notifyOnCollapse() {
        for (final OnExpandListener onExpandListener : this.onExpandListeners) {
            onExpandListener.onCollapse(this);
        }
    }

    private void notifyOnCollapsed() {
        for (final OnExpandListener onExpandListener : this.onExpandListeners) {
            onExpandListener.onCollapsed(this);
        }
    }

    private void notifyOnCollapsing() {
        for (final OnExpandListener onExpandListener : this.onExpandListeners) {
            onExpandListener.onCollapsing(this);
        }
    }

    /**
     * This method will notify the listener about this view being collapsed.
     */
    private void notifyOnExpand() {
        for (final OnExpandListener onExpandListener : this.onExpandListeners) {
            onExpandListener.onExpand(this);
        }
    }

    private void notifyOnExpanded() {
        for (final OnExpandListener onExpandListener : this.onExpandListeners) {
            onExpandListener.onExpanded(this);
        }
    }

    private void notifyOnExpanding() {
        for (final OnExpandListener onExpandListener : this.onExpandListeners) {
            onExpandListener.onExpanding(this);
        }
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    //region public interfaces

    /**
     * Interface definition for a callback to be invoked when
     * a {@link FxExpandableTextView} is expanded or collapsed.
     */
    public interface OnExpandListener {
        /**
         * The {@link FxExpandableTextView} is being expanded.
         *
         * @param view the textview
         */
        void onExpand(@NonNull FxExpandableTextView view);

        void onExpanding(@NonNull FxExpandableTextView view);

        void onExpanded(@NonNull FxExpandableTextView view);

        /**
         * The {@link FxExpandableTextView} is being collapsed.
         *
         * @param view the textview
         */
        void onCollapse(@NonNull FxExpandableTextView view);

        void onCollapsing(@NonNull FxExpandableTextView view);

        void onCollapsed(@NonNull FxExpandableTextView view);
    }

    /**
     * Simple implementation of the {@link OnExpandListener} interface with stub
     * implementations of each method. Extend this if you do not intend to override
     * every method of {@link OnExpandListener}.
     */
    public static class SimpleOnExpandListener implements OnExpandListener {
        @Override
        public void onExpand(@NonNull final FxExpandableTextView view) {
            // empty implementation
        }

        @Override
        public void onExpanding(@NonNull FxExpandableTextView view) {

        }

        @Override
        public void onExpanded(@NonNull FxExpandableTextView view) {

        }

        @Override
        public void onCollapse(@NonNull final FxExpandableTextView view) {
            // empty implementation
        }

        @Override
        public void onCollapsing(@NonNull FxExpandableTextView view) {

        }

        @Override
        public void onCollapsed(@NonNull FxExpandableTextView view) {

        }
    }
    //endregion

    public int getMaxLinesForScrolling() {
        return maxLinesForScrolling;
    }

    public void setMaxLinesForScrolling(int maxLinesToScroll) {
        this.maxLinesForScrolling = maxLinesToScroll > maxLines ? maxLinesToScroll : Integer.MAX_VALUE;
    }

    /**
     * This method will return status of expandable
     */
    public boolean expandable() {
        return expandable;
    }

    /**
     * This method will request Layout when we wanto collapse but not animation
     */
    private void addPaddingBottom(int padding) {
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), (int) (getPaddingBottom() + padding));
        isPaddingAdded = true;
        currentPaddingAdded = padding;
    }

    private void removePaddingBottom() {

        if (!isPaddingAdded) return;

        int paddingBottom = (int) (getPaddingBottom() - currentPaddingAdded);
        int targetPaddingBottom = Math.min(paddingBottom, getPaddingBottom());
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), targetPaddingBottom);
        isPaddingAdded = false;
    }

}
