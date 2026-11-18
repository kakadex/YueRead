package com.yueread.app.lib.theme.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import com.yueread.app.lib.theme.accentColor
import com.yueread.app.utils.applyTint

class ThemeCheckBox(context: Context, attrs: AttributeSet) : AppCompatCheckBox(context, attrs) {

    private var isUserAction = false

    init {
        if (!isInEditMode) {
            applyTint(context.accentColor)
        }
    }

    override fun performClick(): Boolean {
        isUserAction = true
        val result = super.performClick()
        isUserAction = false
        return result
    }

    fun setOnUserCheckedChangeListener(listener: ((Boolean) -> Unit)?) {
        if (listener == null) {
            return super.setOnCheckedChangeListener(null)
        }
        super.setOnCheckedChangeListener { _, isChecked ->
            if (isUserAction) {
                listener.invoke(isChecked)
            }
        }
    }

}
