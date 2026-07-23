package com.vtstudio.fxbox.fxviews.listview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.adapters.ShortsImageAdapter;

import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class ImagePagerView extends FrameLayout {
    private TextView mTextView;
    private ViewPager2 mViewPager2;
    private View mRootView;
    private CircleIndicator3 mDotsIndicator;
    private ShortsImageAdapter mAdapter;
    private List<String> mImageListPath;
    private int mIndicatorsCount;
    private Runnable mCallback;

    public ImagePagerView(@NonNull Context context, @NonNull List<String> imageListPath, @NonNull CircleIndicator3 dotsIndicator) {
        super(context);

        mImageListPath = imageListPath;
        mAdapter = new ShortsImageAdapter(mImageListPath);
        mDotsIndicator = dotsIndicator;

        initViews(context);
        configViews();
    }

    @SuppressLint("SetTextI18n")
    private void configViews() {
        mViewPager2.setOffscreenPageLimit(2);
        mViewPager2.setAdapter(mAdapter);

        mTextView.setVisibility(View.INVISIBLE);
        mTextView.setText(1 + " / " + mAdapter.getItemCount());

        // Nếu số lượng ảnh lớn hơn 1 mới hiển thị indicators
        if(mAdapter.getItemCount() > 1) {
            mDotsIndicator.setVisibility(View.VISIBLE);
            if (mAdapter.getItemCount() < 6) {
                mIndicatorsCount = 5;
                mDotsIndicator.createIndicators(mAdapter.getItemCount(), 0);
            } else {
                final int count = mAdapter.getItemCount();
                mIndicatorsCount = count;

                for (int i = 11; i >= 3; --i) {
                    if (mIndicatorsCount % i == 0) {
                        mIndicatorsCount = i;
                        break;
                    }
                }

                if (mIndicatorsCount == count && count > 11) {
                    mIndicatorsCount = 5;
                }
                mDotsIndicator.createIndicators(mIndicatorsCount, 0);
            }

            // lắng nghe sự kiện chuyển trang ảnh để hiện thị textview chứa vị trí ảnh hiện tại
            mCallback = buildCallback();

            mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    mTextView.setText(position + 1 + " / " + mAdapter.getItemCount());
                    mDotsIndicator.animatePageSelected(position % mIndicatorsCount);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        mTextView.postDelayed(mCallback, 1000);
                    } else {
                        mTextView.removeCallbacks(mCallback);
                        mTextView.setVisibility(View.VISIBLE);
                    }
                }
            });
            // --------------------- //
        } else {
            mDotsIndicator.setVisibility(View.GONE);
        }
    }

    private Runnable buildCallback() {
        return () -> {
            if (mTextView != null) {
                mTextView.setVisibility(View.INVISIBLE);
            }
        };
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (mAdapter != null) {
            mAdapter.setOnClickListener(l);
        }
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        if (mAdapter != null) {
            mAdapter.setOnLongClickListener(l);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mViewPager2 != null) {
            mViewPager2.setCurrentItem(0);
        }
    }

    private void initViews(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mRootView = inflater.inflate(R.layout.shorts_list_image_lay, this, true);
        mTextView = (TextView) mRootView.findViewById(R.id.shorts_image_count);
        mViewPager2 = (ViewPager2) mRootView.findViewById(R.id.shorts_image_list_view);
    }
}
