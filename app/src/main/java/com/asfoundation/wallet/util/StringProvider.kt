package com.asfoundation.wallet.util

import android.content.res.Resources
import androidx.annotation.StringRes

class StringProvider(val resources: Resources) {

  fun getString(@StringRes stringRes: Int, vararg params: Any): String {
    return resources.getString(stringRes, *params)
  }

  fun getString(@StringRes stringRes: Int): String {
    return resources.getString(stringRes)
  }
}