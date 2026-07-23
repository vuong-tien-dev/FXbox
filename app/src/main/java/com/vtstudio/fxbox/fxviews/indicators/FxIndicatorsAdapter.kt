package com.vtstudio.fxbox.fxviews.indicators

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.ShapeDrawable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.vtstudio.fxbox.R


class FxIndicatorsAdapter @JvmOverloads constructor(
    private var count: Int = 0,
    private var size: Int = 6,
    private var spacing: Int = 2
) :
    RecyclerView.Adapter<FxIndicatorsAdapter.FxIndicatorsHolder>() {
    class FxIndicatorsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    private var currentSelected: Int = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FxIndicatorsAdapter.FxIndicatorsHolder {
        val view = View(parent.context)
        val params = ViewGroup.LayoutParams(size.toPx(), size.toPx())
        view.setBackgroundResource(R.drawable.circle_shape)
        view.setPadding(spacing.toPx())
        return FxIndicatorsHolder(view)
    }

    override fun onBindViewHolder(holder: FxIndicatorsAdapter.FxIndicatorsHolder, position: Int) {
        if (position == currentSelected) {
            val shape = holder.itemView.background
            if (shape is ShapeDrawable) {
                shape.paint.color = Color.WHITE
            }
        } else {
            val shape = holder.itemView.background
            if (shape is ShapeDrawable) {
                shape.paint.color = Color.WHITE
            }
        }
    }

    fun onIndicatorSelected (position: Int) {
        if(position != currentSelected) {
            notifyItemChanged(currentSelected)
            currentSelected = position
            notifyItemChanged(currentSelected)
        }
    }

    override fun getItemCount(): Int {
        return count
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCount(count: Int) {
        if (count >= 0 && count != this.count) {
            this.count = count
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSize (size: Int) {
        if (size >= 0 && size != this.size) {
            this.size = size
            notifyItemRangeChanged(0, count)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSpacing (spacing: Int) {
        if (spacing >= 0 && spacing != this.spacing) {
            this.spacing = spacing
            notifyItemRangeChanged(0, count)
        }
    }
}