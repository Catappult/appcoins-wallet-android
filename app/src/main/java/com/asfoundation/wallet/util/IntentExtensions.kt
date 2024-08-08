package com.asfoundation.wallet.util

import android.content.Intent
import android.os.Build
import android.os.Parcelable

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelable(name: String) =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    getParcelableExtra(name, T::class.java)
  else
    getParcelableExtra(name)