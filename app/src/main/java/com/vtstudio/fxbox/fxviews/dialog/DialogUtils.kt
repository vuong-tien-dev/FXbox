package com.vtstudio.fxbox.fxviews.dialog

import android.content.Context
import com.saadahmedsoft.popupdialog.CreateDialog
import com.saadahmedsoft.popupdialog.PopupDialog
import com.saadahmedsoft.popupdialog.Styles
import com.vtstudio.fxbox.R

object DialogUtils {
    @JvmStatic
     fun buildTemplate(context: Context, heading: String, content: String): CreateDialog {
        return PopupDialog.getInstance(context)
            .setStyle(Styles.IOS)
            .setHeading(heading)
            .setDescription(content)
            .setCancelable(false)
            .setDescriptionTextColor(R.color.white)
            .setHeadingTextColor(R.color.white)
            .setPositiveButtonBackground(R.drawable.dialog_background)
            .setNegativeButtonBackground(R.drawable.dialog_background)
            .setDialogBackground(R.drawable.dialog_background)
            .setPositiveButtonTextColor(R.color.colorAccent)
            .setNegativeButtonTextColor(R.color.colorAccent)
            .setNegativeButtonText(context.getString(R.string.cancel))
            .setPositiveButtonText("OK")
    }
}