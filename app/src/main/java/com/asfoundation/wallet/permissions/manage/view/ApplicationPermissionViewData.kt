package com.asfoundation.wallet.permissions.manage.view

import android.graphics.drawable.Drawable

data class ApplicationPermissionViewData(val packageName: String,
                                         val appName: String,
                                         val hasPermission: Boolean,
                                         val icon: Drawable,
                                         val apkSignature: String)
