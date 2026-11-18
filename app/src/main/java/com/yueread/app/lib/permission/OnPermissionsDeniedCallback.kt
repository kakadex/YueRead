package com.yueread.app.lib.permission

interface OnPermissionsDeniedCallback {

    fun onPermissionsDenied(deniedPermissions: Array<String>)

}
