package com.asfoundation.wallet.router

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.ui.WalletsActivity

class ManageWalletsRouter {

  fun open(context: Context) {
    val intent =
        Intent(context, WalletsActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    context.startActivity(intent)
  }
}