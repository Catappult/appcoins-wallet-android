package com.asfoundation.wallet.iab.parser

import android.net.Uri
import com.asfoundation.wallet.iab.domain.model.PurchaseData

interface UriParser {
  fun parse(uri: Uri?): PurchaseData
}
