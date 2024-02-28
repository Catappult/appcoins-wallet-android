package com.appcoins.wallet.core.analytics.analytics.partners

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GamesHubContentProviderService @Inject constructor(
  @ApplicationContext private val context: Context,
) {

  fun isGameFromGamesHub(packageName: String): Boolean {
    return try {
      fetchGameFromGamesHub(
        Uri.parse("$CONTENT_PROVIDER_URI$CONTENT_PROVIDER_TABLE"),
        CONTENT_PROVIDER_COLUMN,
        packageName,
        context
      )
    } catch (e: Exception) {
      Log.e(
        "GamesHubContentProvider",
        "Error fetching gamesHub contentProvider: ${e.message}"
        )
      false
    }
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
        val index = cursor.getColumnIndex(CONTENT_PROVIDER_COLUMN_VALUE)
        if (index >= 0) {
          return cursor.getInt(index) == 1
        } else {
          return false
        }
      }
    }
    return false
  }


  companion object {
    private const val CONTENT_PROVIDER_TABLE = "appDetails"
    private const val CONTENT_PROVIDER_COLUMN = "packageName"
    private const val CONTENT_PROVIDER_COLUMN_VALUE = "isInstalledByGH"
    private const val CONTENT_PROVIDER_URI = "content://com.dti.folderlauncher.appdetails/"
  }
}
