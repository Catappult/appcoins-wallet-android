package com.appcoins.wallet.ui.common

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StringProvider @Inject constructor(@ApplicationContext val context: Context) {

  fun getString(@StringRes stringRes: Int, vararg params: Any): String {
    return context.resources.getString(stringRes, *params)
  }

  fun getString(@StringRes stringRes: Int): String {
    return context.resources.getString(stringRes)
  }
}