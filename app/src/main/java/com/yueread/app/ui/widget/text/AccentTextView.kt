package com.yueread.app.ui.widget.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.yueread.app.R
import com.yueread.app.lib.theme.accentColor
import com.yueread.app.utils.getCompatColor

class AccentTextView(context: Context, attrs: AttributeSet?) :
    AppCompatTextView(context, attrs) {

    init {
        if (!isInEditMode) {
            setTextColor(context.accentColor)
        } else {
            setTextColor(context.getCompatColor(R.color.accent))
        }
    }

}
