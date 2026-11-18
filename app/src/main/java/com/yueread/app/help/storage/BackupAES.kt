package com.yueread.app.help.storage

import cn.hutool.crypto.symmetric.AES
import com.yueread.app.help.config.LocalConfig
import com.yueread.app.utils.MD5Utils

class BackupAES : AES(
    MD5Utils.md5Encode(LocalConfig.password ?: "").encodeToByteArray(0, 16)
)