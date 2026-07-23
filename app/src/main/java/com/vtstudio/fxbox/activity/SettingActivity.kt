package com.vtstudio.fxbox.activity

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.adapters.SettingAdapter
import com.vtstudio.fxbox.databinding.ActivitySettingBinding
import com.vtstudio.fxbox.fragments.settings.General

class SettingActivity : AppCompatActivity() {
    companion object {
        const val SETTING_GENERAL = "general"
        const val SETTING_PLAYER = "player"
        const val SETTING_SHORTS = "shorts"
        const val SETTING_INFORMATION = "information"
        @JvmField
        val settingOptions = arrayListOf(SETTING_GENERAL, SETTING_PLAYER, SETTING_SHORTS, SETTING_INFORMATION)
    }

    private var _binding: ActivitySettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    fun initViews() {

        // config for support actionbar
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        supportActionBar!!.title = getString(R.string.setting)

        binding.toolbar.setNavigationOnClickListener { _ -> finish() }

        // config for list setting options
        val adapter = SettingAdapter()
        binding.settingsList.adapter = adapter
        binding.settingsList.layoutManager = LinearLayoutManager(baseContext)

        // config for event listeners
        adapter.setOnItemClickListener {
            position ->
            addFragment(adapter.settings[position])
        }
    }

    private fun addFragment(settingOption: String) {
        when (settingOption) {
            SETTING_GENERAL -> {
//                val fragment = General()
//                supportFragmentManager.beginTransaction().replace(R.id.settings_fragment_container, fragment as Fragment).commit()
            }
            SETTING_PLAYER -> {

            }
        }
    }

}