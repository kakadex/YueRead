package com.yueread.app.lib.theme.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.yueread.app.lib.theme.accentColor
import com.yueread.app.utils.applyTint

/**
 * @author Aidan Follestad (afollestad)
 */
class ThemeSeekBar(context: Context, attrs: AttributeSet) : AppCompatSeekBar(context, attrs) {

    init {
        if (!isInEditMode) {
            applyTint(context.accentColor)
        }
    }
}
