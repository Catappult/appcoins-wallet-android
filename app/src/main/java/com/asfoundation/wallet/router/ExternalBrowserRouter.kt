package com.asfoundation.wallet.router

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.asf.wallet.R
import javax.inject.Inject

class ExternalBrowserRouter @Inject constructor() {

  fun open(context: Context, uri: Uri) {
    try {
      val launchBrowser = Intent(Intent.ACTION_VIEW, uri)
      launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(launchBrowser)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT)
          .show()
    }
  }

}
