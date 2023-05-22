package com.asfoundation.wallet.update_required

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.wallet.appcoins.core.legacy_base.legacy.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateRequiredActivity : com.wallet.appcoins.core.legacy_base.legacy.BaseActivity() {
  companion object {
    @JvmStatic
    fun newIntent(context: Context) = Intent(context, UpdateRequiredActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_update_required)
  }
}
