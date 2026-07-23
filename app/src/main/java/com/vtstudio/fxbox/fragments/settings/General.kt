package com.vtstudio.fxbox.fragments.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import com.vtstudio.fxbox.R

class General : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.setting_general)
    }
}