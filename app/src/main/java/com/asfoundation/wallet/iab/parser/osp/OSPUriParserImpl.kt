package com.asfoundation.wallet.iab.parser.osp

import android.net.Uri
import com.asfoundation.wallet.iab.parser.osp.exception.MissingDataParseException
import com.appcoins.wallet.core.analytics.analytics.partners.InstallerService
import com.appcoins.wallet.core.analytics.analytics.partners.OemIdExtractorService
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.parser.APPC
import com.asfoundation.wallet.iab.parser.BDS_ORIGIN
import com.asfoundation.wallet.iab.parser.PAYMENT_TYPE_INAPP_UNMANAGED
import com.asfoundation.wallet.iab.parser.PurchaseUriParameters
import com.asfoundation.wallet.iab.parser.UriParser
import java.math.BigDecimal
import javax.inject.Inject

class OSPUriParserImpl @Inject constructor(
  private val oemIdExtractor: OemIdExtractorService,
  private val oemPackageExtractor: InstallerService,
) : UriParser {

  override fun parse(uri: Uri?): PurchaseData {
    uri ?: throw NullPointerException("Integration error: URI is null")
    val parameters = mutableListOf<Pair<String, String>>().also { list ->
      uri.queryParameterNames.forEach { name ->
        uri.getQueryParameter(name)?.let { param -> list.add(name to param) }
      }
    }
    val domain = parameters.find(PurchaseUriParameters.DOMAIN)
      ?: throw MissingDataParseException("URI must contain the ${PurchaseUriParameters.DOMAIN} name")

    val currency = parameters.find(PurchaseUriParameters.CURRENCY)
    val value = parameters.find(PurchaseUriParameters.VALUE)
    val type = parameters.find(PurchaseUriParameters.TYPE) ?: PAYMENT_TYPE_INAPP_UNMANAGED
    val origin = parameters.find(PurchaseUriParameters.ORIGIN) ?: BDS_ORIGIN
    val skuId = parameters.find(PurchaseUriParameters.PRODUCT)
      ?: throw MissingDataParseException("URI must contain the ${PurchaseUriParameters.PRODUCT} name")
    val callbackUrl = parameters.find(PurchaseUriParameters.CALLBACK_URL)
    val orderReference = parameters.find(PurchaseUriParameters.ORDER_REFERENCE)
    val payload = parameters.find(PurchaseUriParameters.METADATA)
    val productToken = parameters.find(PurchaseUriParameters.PRODUCT_TOKEN)
    val signature = parameters.find(PurchaseUriParameters.SIGNATURE)

    val oemId = oemIdExtractor.extractOemId(domain).blockingGet()
    val oemPackage = oemPackageExtractor.getInstallerPackageName(domain).blockingGet()

    return PurchaseData(
      referrerUrl = uri.toString(),
      type = type,
      origin = origin,
      skuId = skuId,
      domain = domain,
      callbackUrl = callbackUrl,
      orderReference = orderReference,
      value = value,
      signature = signature,
      currency = currency,
      oemId = oemId,
      oemPackage = oemPackage,
      payload = payload,
      productToken = productToken,
    )
  }
}

private fun <T> List<Pair<String, T>>.find(key: String): T? = find { it.first == key }?.second
