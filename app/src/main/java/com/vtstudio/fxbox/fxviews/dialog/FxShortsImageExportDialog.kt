package com.vtstudio.fxbox.fxviews.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.adapters.ShortsImageExportAdapter
import com.vtstudio.fxbox.databinding.ShortsImageExportDialogBinding
import com.vtstudio.fxbox.media.MediaManger
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo
import xyz.hasnat.sweettoast.SweetToast

class FxShortsImageExportDialog(
    context: Context,
    private val shorts: ShortsVideo
) : BottomSheetDialog(context, R.style.TransparentBottomSheetDialog) {

    private val binding = ShortsImageExportDialogBinding.inflate(LayoutInflater.from(context))
    private val imageAdapter = ShortsImageExportAdapter(shorts.imageListPath ?: emptyList())

    init {
        binding.title.text = context.getString(R.string.export_slideshow)
        binding.exportMusic.setOnClickListener {
            MediaManger.exportMediaToPublicFolder(context, shorts)
            dismiss()
        }
        binding.exportImages.setOnClickListener { showImageSelection() }
        binding.backButton.setOnClickListener { showExportChoices() }
        binding.exportSelectedImages.setOnClickListener {
            val files = imageAdapter.selectedFiles
            if (files.isEmpty()) {
                SweetToast.warning(context, context.getString(R.string.select_at_least_one_image))
                return@setOnClickListener
            }
            MediaManger.exportImageFilesToPublicFolder(context, files)
            dismiss()
        }

        binding.imageList.layoutManager = LinearLayoutManager(context)
        binding.imageList.adapter = imageAdapter
        setContentView(binding.root)
    }

    private fun showImageSelection() {
        binding.title.setText(R.string.select_images_to_export)
        binding.exportChoices.visibility = View.GONE
        binding.imageSelection.visibility = View.VISIBLE
    }

    private fun showExportChoices() {
        binding.title.setText(R.string.export_slideshow)
        binding.imageSelection.visibility = View.GONE
        binding.exportChoices.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.decorView?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(R.color.transparent)
    }
}
