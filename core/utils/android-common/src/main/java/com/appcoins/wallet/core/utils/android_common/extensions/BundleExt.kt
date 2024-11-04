package com.appcoins.wallet.core.utils.android_common.extensions

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import java.io.Serializable

inline fun <reified T : Serializable> Fragment.getSerializableExtra(name: String): T? =
  requireArguments().getSerializableExtra(name)

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.getSerializableExtra(name: String): T? =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    getSerializable(name, T::class.java)
  else
    getSerializable(name) as T?

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Fragment.getParcelableExtra(name: String): T? =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    requireArguments().getParcelable(name, T::class.java)
  else
    requireArguments().getParcelable(name) as T?

inline fun <reified T : Parcelable> Activity.getParcelable(name: String): T? =
  intent.getParcelable(name)

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelable(name: String): T? =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    getParcelableExtra(name, T::class.java)
  else
    getParcelableExtra(name) as T?
