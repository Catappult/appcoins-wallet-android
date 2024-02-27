package com.appcoins.wallet.core.analytics.analytics.partners

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GamesHubContentProviderService @Inject constructor(
  @ApplicationContext private val context: Context,
) {

  fun isGameFromGamesHub(packageName: String): Boolean {
    return fetchGameFromGamesHub(
      Uri.parse("$CONTENT_PROVIDER_URI$CONTENT_PROVIDER_TABLE"),
      CONTENT_PROVIDER_COLUMN,
      packageName,
      context
    )
  }

  fun fetchGameFromGamesHub(
    contentProviderUri: Uri,
    packageNameColumn: String,
    packageName: String,
    context: Context
  ): Boolean {
    val selection = "$packageNameColumn = ?"
    val selectionArgs = arrayOf(packageName)
    val resolver = context.contentResolver
    resolver.query(
      contentProviderUri,
      arrayOf(packageNameColumn),
      selection,
      selectionArgs,
      null
    ).use { cursor ->
      if (cursor != null && cursor.moveToFirst()) {
        return true
      }
    }
    return false
  }


  companion object {
    private const val CONTENT_PROVIDER_TABLE = "appDetails"
    private const val CONTENT_PROVIDER_COLUMN = "packageName"
    private const val CONTENT_PROVIDER_URI = "content://com.dti.hub.appdetails/" // gameshub://com.dti.folderlauncher/
  }
}
