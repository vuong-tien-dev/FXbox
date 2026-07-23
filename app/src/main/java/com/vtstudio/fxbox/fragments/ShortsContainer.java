package com.vtstudio.fxbox.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.activity.FxBaseActivity;
import com.vtstudio.fxbox.activity.Search;
import com.vtstudio.fxbox.adapters.ShortsPagerAdapter;
import com.vtstudio.fxbox.databinding.FragmentShortsContainerBinding;
import com.vtstudio.fxbox.listeners.OnServiceConnectionListener;
import com.vtstudio.fxbox.media.player.FxPlayer;
import com.vtstudio.fxbox.utils.ViewsUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShortsContainer#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShortsContainer extends Fragment {
    private FragmentShortsContainerBinding mBinding;
    public ShortsContainer() {
        // Required empty public constructor
    }

    public static ShortsContainer newInstance() {
        return new ShortsContainer();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentShortsContainerBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    private void initViews () {
        TabLayout tabLayout = mBinding.tabs;

        ViewPager2 pager = mBinding.fragmentShortsList;

        ShortsPagerAdapter mAdapter = new ShortsPagerAdapter(this);
        pager.setAdapter(mAdapter);

        TabLayoutMediator mMediator = new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText(getString(R.string.cloud));
                        break;
                    case 1:
                        tab.setText(getString(R.string.for_you));
                        break;
                }
            }
        });
        mMediator.attach();

        ViewsUtils.setTouchScaleEffect(mBinding.searchBar, 0.8f);
        ViewsUtils.setTouchScaleEffect(mBinding.pictureInPicture, 0.8f);

        // đặt trình lắng nghe sự kiện cho search bar
        mBinding.searchBar.setOnClickListener((v) -> {
            if (getActivity() == null) return;
            Intent intent = new Intent(getActivity(), Search.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        Activity activity =  getActivity();

        if(activity instanceof FxBaseActivity) {
            FxBaseActivity baseActivity = (FxBaseActivity) activity;
            if(!baseActivity.isFxPlayerBound()) {
                baseActivity.boundFxPlayer(new OnServiceConnectionListener() {
                    @Override
                    public void onServiceConnected() {
                       setTargetPageIfShortsIsPlayedBefore(baseActivity, pager);
                    }

                    @Override
                    public void onServiceDisconnected() {

                    }
                });
            } else {
                setTargetPageIfShortsIsPlayedBefore(baseActivity, pager);
            }

            if(baseActivity.isPipSupported()) {
                mBinding.pictureInPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Settings.canDrawOverlays(baseActivity)) {
                            baseActivity.getFxPlayer().setVideoWindowModeEnabled(true, baseActivity.getClass());
                            baseActivity.moveTaskToBack(true);
                        } else {
                            baseActivity.requestOverlayPermission();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setTargetPageIfShortsIsPlayedBefore (@NonNull FxBaseActivity baseActivity, @NonNull ViewPager2 pager){
        FxPlayer fxPlayer = baseActivity.getFxPlayer();
        if (fxPlayer != null) {
            if(fxPlayer.getCurrentRequestId() == Shorts.BASE_MEDIA_REQUEST_ID + Shorts.TYPE_VIDEO_FROM_CLOUD) {
                pager.setCurrentItem(0); // trang kho lưu trữ
            } else if (fxPlayer.getCurrentRequestId() == Shorts.BASE_MEDIA_REQUEST_ID + Shorts.TYPE_VIDEO_FOR_YOU) {
                pager.setCurrentItem(1); // trang dành cho bạn
            }
        }
    }
}