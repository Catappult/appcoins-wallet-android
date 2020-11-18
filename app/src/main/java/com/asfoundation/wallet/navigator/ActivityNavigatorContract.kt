package com.asfoundation.wallet.navigator

import android.content.ActivityNotFoundException
import android.content.Intent

interface ActivityNavigatorContract {

  @Throws(ActivityNotFoundException::class)
  fun startActivityForResult(intent: Intent, intentTitle: Int, fileIntentCode: Int)
}
