package com.vtstudio.fxbox.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.vtstudio.fxbox.fragments.Download;
import com.vtstudio.fxbox.fragments.Home;
import com.vtstudio.fxbox.fragments.Library;
import com.vtstudio.fxbox.fragments.Shorts;
import com.vtstudio.fxbox.fragments.ShortsContainer;

public class MainPagerAdapter extends FragmentStateAdapter {
    public MainPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @Override
    public int getItemCount() {
        return 4; // Số lượng trang
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return Home.newInstance();
                case 1:
                    return ShortsContainer.newInstance();
            case 2:
                return Download.newInstance();
            case 3:
                return Library.newInstance();
        }
        throw new RuntimeException("This position do not match the item in navigation");
    }
}