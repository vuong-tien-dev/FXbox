package com.vtstudio.fxbox.fxviews.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.databinding.DialogLoadingLayoutBinding
import com.vtstudio.fxbox.databinding.LoadingDialogWithTextBinding
import com.vtstudio.fxbox.databinding.LoadingLayoutBinding

open class LoadingDialog constructor(context: Context) : Dialog (context){
    private var textView: TextView? = null


    fun setText(text: String) {
        textView?.let {
            it.text = text
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.loading_dialog_with_text)

        textView = findViewById<TextView>(R.id.loading_text)

        window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        val decorView = window?.decorView
        decorView?.let {
            val view = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
    }

}