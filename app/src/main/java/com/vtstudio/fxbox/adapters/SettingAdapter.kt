package com.vtstudio.fxbox.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.activity.SettingActivity
import com.vtstudio.fxbox.databinding.SettingItemBinding
import com.vtstudio.fxbox.listeners.OnItemListClickListener

class SettingAdapter(var settings: ArrayList<String> = SettingActivity.settingOptions) :
    RecyclerView.Adapter<SettingAdapter.SettingHolder>() {

    class SettingHolder(val binding: SettingItemBinding) : RecyclerView.ViewHolder(binding.root) {}

    private var onItemClickListener: OnItemListClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemListClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingHolder {
        val binding = SettingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SettingHolder(binding)
    }

    override fun getItemCount(): Int {
        return settings.size
    }

    override fun onBindViewHolder(holder: SettingHolder, position: Int) {

        val setting = settings[position]
        val context = holder.binding.root.context

        when (setting) {
            SettingActivity.SETTING_GENERAL -> {
                holder.binding.settingImage.setImageResource(R.drawable.general)
                holder.binding.settingOption.text = context.getString(R.string.setting_general)
                holder.binding.settingSummary.text = context.getString(R.string.setting_summary_general)
            }

            SettingActivity.SETTING_PLAYER -> {
                holder.binding.settingImage.setImageResource(R.drawable.play)
                holder.binding.settingOption.text = context.getString(R.string.setting_player)
                holder.binding.settingSummary.text = context.getString(R.string.setting_summary_player)
            }

            SettingActivity.SETTING_SHORTS -> {
                holder.binding.settingImage.setImageResource(R.drawable.shorts_nav_checked_true)
                holder.binding.settingOption.text = context.getString(R.string.setting_shorts)
                holder.binding.settingSummary.text = context.getString(R.string.setting_summary_shorts)
            }

            SettingActivity.SETTING_INFORMATION -> {
                holder.binding.settingImage.setImageResource(R.drawable.action_see_properties)
                holder.binding.settingOption.text = context.getString(R.string.setting_information)
                holder.binding.settingSummary.text = context.getString(R.string.setting_summary_information)
            }
        }
    }
}