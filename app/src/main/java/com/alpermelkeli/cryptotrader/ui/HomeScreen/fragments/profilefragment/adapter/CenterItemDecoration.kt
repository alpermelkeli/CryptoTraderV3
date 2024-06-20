package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.profilefragment.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CenterItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount
        val spacing = (parent.width - view.layoutParams.width) / 2

        if (position == 0) {
            outRect.left = spacing
        } else if (position == itemCount - 1) {
            outRect.right = spacing
        }
    }
}
