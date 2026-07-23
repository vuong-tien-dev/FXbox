package com.vtstudio.fxbox.adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.vtstudio.fxbox.fragments.Shorts;

public class ShortsPagerAdapter extends FragmentStateAdapter {

    public ShortsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @Override
    public int getItemCount() {
        return 2; // Số lượng trang
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
       // Log.d("ShortsContainer", "createFragment shorts");
        switch (position) {
            case 0:
              //  Log.d("ShortsContainer", "createFragment shorts");
                return Shorts.newInstance(1);
            case 1:
                return Shorts.newInstance(0);
        }
        throw new RuntimeException("This position do not match the item in navigation");
    }
}