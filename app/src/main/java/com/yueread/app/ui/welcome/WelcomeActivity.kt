package com.yueread.app.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.postDelayed
import com.yueread.app.base.BaseActivity
import com.yueread.app.constant.PreferKey
import com.yueread.app.constant.Theme
import com.yueread.app.data.appDb
import com.yueread.app.databinding.ActivityWelcomeBinding
import com.yueread.app.help.config.AppConfig
import com.yueread.app.help.config.ThemeConfig
import com.yueread.app.lib.theme.accentColor
import com.yueread.app.lib.theme.backgroundColor
import com.yueread.app.ui.book.read.ReadBookActivity
import com.yueread.app.ui.main.MainActivity
import com.yueread.app.utils.BitmapUtils
import com.yueread.app.utils.fullScreen
import com.yueread.app.utils.getPrefBoolean
import com.yueread.app.utils.getPrefInt
import com.yueread.app.utils.getPrefString
import com.yueread.app.utils.setStatusBarColorAuto
import com.yueread.app.utils.startActivity
import com.yueread.app.utils.viewbindingdelegate.viewBinding
import com.yueread.app.utils.visible
import com.yueread.app.utils.windowSize

open class WelcomeActivity : BaseActivity<ActivityWelcomeBinding>() {

    override val binding by viewBinding(ActivityWelcomeBinding::inflate)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            // 避免从桌面启动程序后，会重新实例化入口类的activity
            finish()
        } else {
            val welcomeShowTime = getPrefInt(PreferKey.welcomeShowTime, 500)
            if (welcomeShowTime == 0) {
                startMainActivity()
            } else {
                binding.root.postDelayed(welcomeShowTime.toLong()) { startMainActivity() }
            }
        }
        binding.ivBook.setColorFilter(accentColor)
        binding.vwTitleLine.setBackgroundColor(accentColor)
    }

    override fun setupSystemBar() {
        fullScreen()
        setStatusBarColorAuto(backgroundColor, true, fullScreen)
        upNavigationBarColor()
    }

    override fun upBackgroundImage() {
        if (getPrefBoolean(PreferKey.customWelcome)) {
            kotlin.runCatching {
                when (ThemeConfig.getTheme()) {
                    Theme.Dark -> {
                        getPrefString(PreferKey.welcomeImageDark)?.let { path ->
                            if (path.endsWith(".9.png")) {
                                BitmapUtils.decodeNinePatchDrawable(path)?.let {
                                    window.decorView.background = it
                                }
                            } else {
                                val size = windowManager.windowSize
                                BitmapUtils.decodeBitmap(path, size.widthPixels, size.heightPixels)?.let {
                                    window.decorView.background = it.toDrawable(resources)
                                }
                            }
                        }
                        binding.tvLegado.visible(AppConfig.welcomeShowTextDark)
                        binding.ivBook.visible(AppConfig.welcomeShowIconDark)
                        binding.tvGzh.visible(AppConfig.welcomeShowTextDark)
                        return
                    }
                    else -> {
                        getPrefString(PreferKey.welcomeImage)?.let { path ->
                            if (path.endsWith(".9.png")) {
                                BitmapUtils.decodeNinePatchDrawable(path)?.let {
                                    window.decorView.background = it
                                }
                            } else {
                                val size = windowManager.windowSize
                                BitmapUtils.decodeBitmap(path, size.widthPixels, size.heightPixels)?.let {
                                    window.decorView.background = it.toDrawable(resources)
                                }
                            }
                        }
                        binding.tvLegado.visible(AppConfig.welcomeShowText)
                        binding.ivBook.visible(AppConfig.welcomeShowIcon)
                        binding.tvGzh.visible(AppConfig.welcomeShowText)
                        return
                    }
                }
            }
        }
        super.upBackgroundImage()
    }

    private fun startMainActivity() {
        startActivity<MainActivity>()
        if (getPrefBoolean(PreferKey.defaultToRead) && appDb.bookDao.lastReadBook != null) {
            startActivity<ReadBookActivity>()
        }
        finish()
    }

}

class Launcher1 : WelcomeActivity()
class Launcher2 : WelcomeActivity()
class Launcher3 : WelcomeActivity()
class Launcher4 : WelcomeActivity()
class Launcher5 : WelcomeActivity()
class Launcher6 : WelcomeActivity()
class Launcher7 : WelcomeActivity()