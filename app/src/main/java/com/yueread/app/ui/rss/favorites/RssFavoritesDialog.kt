package com.yueread.app.ui.rss.favorites

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.yueread.app.R
import com.yueread.app.base.BaseDialogFragment
import com.yueread.app.data.entities.RssArticle
import com.yueread.app.data.entities.RssStar
import com.yueread.app.databinding.DialogRssFavoriteConfigBinding
import com.yueread.app.lib.theme.primaryColor
import com.yueread.app.utils.setLayout
import com.yueread.app.utils.viewbindingdelegate.viewBinding

class RssFavoritesDialog() : BaseDialogFragment(R.layout.dialog_rss_favorite_config, true) {

    constructor(rssArticle: RssArticle) : this() {
        arguments = Bundle().apply {
            putString("title", rssArticle.title)
            putString("group", rssArticle.group)
        }
    }

    constructor(rssStar: RssStar) : this() {
        arguments = Bundle().apply {
            putString("title", rssStar.title)
            putString("group", rssStar.group)
        }
    }

    private val binding by viewBinding(DialogRssFavoriteConfigBinding::bind)

    override fun onStart() {
        super.onStart()
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolBar.setBackgroundColor(primaryColor)
        val arguments = arguments ?: let {
            dismiss()
            return
        }

        var title = arguments.getString("title")
        var group = arguments.getString("group")
        binding.run {
            editTitle.setText(title)
            editGroup.setText(group)
            tvCancel.setOnClickListener {
                dismiss()
            }
            tvOk.setOnClickListener {
                val editTitle = editTitle.text.toString()
                if (editTitle.isNotBlank()) {
                    title = editTitle
                }
                val editGroup = editGroup.text.toString()
                if (editGroup.isNotBlank()) {
                    group = editGroup
                }
                callback?.updateFavorite(title, group)
                dismiss()
            }
            tvFooterLeft.setOnClickListener {
                callback?.deleteFavorite()
                dismiss()
            }
        }
    }

    val callback get() = (parentFragment as? Callback) ?: (activity as? Callback)

    interface Callback {

        fun updateFavorite(title: String?, group: String?)

        fun deleteFavorite()

    }

}
