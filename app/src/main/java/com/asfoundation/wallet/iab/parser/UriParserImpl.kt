package com.asfoundation.wallet.iab.parser

import android.net.Uri
import com.asfoundation.wallet.iab.di.OSPUriParser
import com.asfoundation.wallet.iab.di.SDKUriParser
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import javax.inject.Inject

class UriParserImpl @Inject constructor(
  @OSPUriParser private val ospUriParser: UriParser,
  @SDKUriParser private val sdkUriParser: UriParser,
) : UriParser {

  override fun parse(uri: Uri?): PurchaseData {
    if (uri == null) throw NullPointerException("Integration error: URI is null")

    return when (uri.scheme) {
      PURCHASE_URI_OSP_SCHEME -> ospUriParser.parse(uri)
      PURCHASE_URI_SDK_SCHEME -> sdkUriParser.parse(uri)
      else -> throw RuntimeException("URI not supported: $uri")
    }
  }
}
