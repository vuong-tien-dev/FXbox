package com.vtstudio.fxbox.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.vtstudio.fxbox.listeners.OnEventFromActivityListener;
import com.vtstudio.fxbox.listeners.OnEventFromFragmentListener;

public class FxBaseFragment extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
