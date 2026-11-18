package com.yueread.app.ui.association

import android.os.Bundle
import com.yueread.app.base.BaseActivity
import com.yueread.app.constant.SourceType
import com.yueread.app.databinding.ActivityTranslucenceBinding
import com.yueread.app.utils.showDialogFragment
import com.yueread.app.utils.viewbindingdelegate.viewBinding

class OpenUrlConfirmActivity :
    BaseActivity<ActivityTranslucenceBinding>() {

    override val binding by viewBinding(ActivityTranslucenceBinding::inflate)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        intent.getStringExtra("uri")?.let {
            val mimeType = intent.getStringExtra("mimeType")
            val sourceOrigin = intent.getStringExtra("sourceOrigin")
            val sourceName = intent.getStringExtra("sourceName")
            val sourceType = intent.getIntExtra("sourceType", SourceType.book)
            showDialogFragment(OpenUrlConfirmDialog(it, mimeType, sourceOrigin, sourceName, sourceType))
        } ?: finish()
    }

}
