package com.folioreader.ui.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.AppBarLayout

class FolioAppBarLayout : AppBarLayout {

    companion object {
        @JvmField
        val LOG_TAG: String = FolioAppBarLayout::class.java.simpleName
    }

    var navigationBarHeight: Int = 0
    var insets: Rect? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->

            Log.v(LOG_TAG, "-> onApplyWindowInsets")

            val sysBars: Insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            this.insets = Rect(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)

            navigationBarHeight = sysBars.bottom

            setMargins(sysBars.left, sysBars.top, sysBars.right)

            windowInsets
        }
    }

    @Suppress("DEPRECATION")
    override fun fitSystemWindows(insets: Rect?): Boolean {
        Log.v(LOG_TAG, "-> fitSystemWindows")

        if (insets != null) {
            this.insets = Rect(insets)
            navigationBarHeight = insets.bottom
            setMargins(insets.left, insets.top, insets.right)
        }

        return super.fitSystemWindows(insets)
    }

    private fun setMargins(left: Int, top: Int, right: Int) {
        val params = layoutParams as MarginLayoutParams
        params.leftMargin = left
        params.topMargin = top
        params.rightMargin = right
        layoutParams = params
    }

    fun setTopMargin(top: Int) {
        val params = layoutParams as MarginLayoutParams
        params.topMargin = top
        layoutParams = params
    }
}
