package com.asfoundation.wallet.navigator

import android.content.Intent
import com.wallet.appcoins.core.legacy_base.BaseActivity


abstract class ActivityNavigator : BaseActivity(), ActivityNavigatorContract {

  override fun startActivityForResult(intent: Intent, intentTitle: Int,
                                      fileIntentCode: Int) {
    startActivityForResult(Intent.createChooser(intent, getString(intentTitle)), fileIntentCode)
  }
}