package com.asfoundation.wallet.ui

import android.content.Intent

interface ActivityResultSharer {
  fun addOnActivityListener(listener: ActivityResultListener)
  fun remove(listener: ActivityResultListener)

  interface ActivityResultListener {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
  }
}
