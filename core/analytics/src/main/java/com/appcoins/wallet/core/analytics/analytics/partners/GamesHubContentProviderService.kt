package com.appcoins.wallet.core.analytics.analytics.partners

import android.content.Context
import android.net.Uri
import android.util.Log
import com.appcoins.wallet.core.network.backend.api.PartnerAttributionApi
import com.appcoins.wallet.core.utils.properties.MiscProperties
import com.appcoins.wallet.sharedpreferences.OemIdPreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
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

  // TODO test contentProvider access
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
    private const val CONTENT_PROVIDER_TABLE = "games"  //TODO
    private const val CONTENT_PROVIDER_COLUMN = "package_name"  //TODO
    private const val CONTENT_PROVIDER_URI = "gameshub://com.dti.folderlauncher/"  //TODO
  }
}
