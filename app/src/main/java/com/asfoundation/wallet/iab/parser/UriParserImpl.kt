package com.asfoundation.wallet.iab.parser

import android.net.Uri
import com.asfoundation.wallet.iab.di.OSPUriParser
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import javax.inject.Inject

class UriParserImpl @Inject constructor(
  @OSPUriParser private val ospUriParser: UriParser,
) : UriParser {

  override fun parse(uri: Uri?): PurchaseData {
    return ospUriParser.parse(uri)
  }
}
