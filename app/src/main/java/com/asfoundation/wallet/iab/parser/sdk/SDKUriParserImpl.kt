package com.asfoundation.wallet.iab.parser.sdk

import android.net.Uri
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.appcoins.wallet.core.analytics.analytics.partners.InstallerService
import com.appcoins.wallet.core.analytics.analytics.partners.OemIdExtractorService
import com.appcoins.wallet.core.utils.properties.MiscProperties.DEFAULT_TOKEN_ADDRESS
import com.appcoins.wallet.core.utils.properties.MiscProperties.DEFAULT_TOKEN_DECIMALS
import com.appcoins.wallet.core.utils.properties.MiscProperties.DEFAULT_TOKEN_NAME
import com.appcoins.wallet.core.utils.properties.MiscProperties.DEFAULT_TOKEN_SYMBOL
import com.asfoundation.wallet.entity.TokenInfo
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.parser.ETH
import com.asfoundation.wallet.iab.parser.PAYMENT_TYPE_INAPP
import com.asfoundation.wallet.iab.parser.UriParser
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.bouncycastle.util.encoders.Hex
import org.kethereum.erc681.ERC681
import org.kethereum.erc681.parseERC681
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.inject.Inject

class SDKUriParserImpl @Inject constructor(
  private val oemIdExtractor: OemIdExtractorService,
  private val oemPackageExtractor: InstallerService,
) : UriParser {

  enum class TransactionType {
    APPC, TOKEN, ETH
  }

  private val tokenInfo by lazy {
    TokenInfo(
      DEFAULT_TOKEN_ADDRESS,
      DEFAULT_TOKEN_NAME,
      DEFAULT_TOKEN_SYMBOL.lowercase(Locale.getDefault()),
      DEFAULT_TOKEN_DECIMALS
    )
  }

  override fun parse(uri: Uri?): PurchaseData {
    if (uri == null) throw NullPointerException("Integration error: URI is null")

    val erc681 = parseERC681(uri.toString())

    return when (getTransactionType(erc681)) {
      TransactionType.APPC -> buildAppcTransaction(uri, erc681)
      TransactionType.TOKEN -> buildTokenTransaction(uri, erc681)
      else -> buildEthTransaction(uri, erc681)
    }
  }

  private fun buildEthTransaction(uri: Uri, erc681: ERC681): PurchaseData {
    val data = retrieveData(payment = erc681)
    val oemData = getOemData(domain = data.domain)

    val value = convertToMainMetric(BigDecimal(erc681.value), 18)

    return PurchaseData(
      referrerUrl = uri.toString(),
      type = PAYMENT_TYPE_INAPP,
      purchaseValue = value.toString(),
      oemId = oemData.second,
      oemPackage = oemData.first,
      domain = data.domain,
      symbol = ETH
    )
  }

  private fun buildTokenTransaction(uri: Uri, erc681: ERC681): PurchaseData {
    val data = retrieveData(payment = erc681)
    val oemData = getOemData(domain = data.domain)

    val amount = getAmount(erc681)

    return PurchaseData(
      referrerUrl = uri.toString(),
      oemId = oemData.second,
      oemPackage = oemData.first,
      domain = data.domain,
      erc681Amount = amount,
      symbol = tokenInfo.symbol,
    )
  }

  private fun buildAppcTransaction(uri: Uri, erc681: ERC681): PurchaseData {
    val data = retrieveData(payment = erc681)
    val oemData = getOemData(domain = data.domain)

    val amount = getAmount(erc681)

    return PurchaseData(
      referrerUrl = uri.toString(),
      type = data.type,
      origin = data.origin,
      skuId = data.skuId,
      domain = data.domain,
      orderReference = data.orderReference,
      oemId = oemData.second,
      oemPackage = oemData.first,
      payload = data.payload,
      erc681Amount = amount,
      symbol = tokenInfo.symbol,
    )
  }

  private fun getAmount(erc681: ERC681) =
    erc681.functionParams["uint256"]
      .takeIf { it != null }
      ?.let { convertToMainMetric(BigDecimal(it), tokenInfo.decimals) }
      ?: BigDecimal.ZERO

  private fun convertToMainMetric(value: BigDecimal, decimals: Int): BigDecimal {
    try {
      val divider = StringBuilder(18)
      divider.append("1")
      for (i in 0 until decimals) {
        divider.append("0")
      }
      return value.divide(BigDecimal(divider.toString()), decimals, RoundingMode.DOWN)
    } catch (ex: NumberFormatException) {
      return BigDecimal.ZERO
    }
  }

  private fun retrieveData(payment: ERC681): TransactionData {
    val data = String(
      Hex.decode(
        payment.functionParams["data"]
          ?.substring(2)
          ?.toByteArray(StandardCharsets.UTF_8)
      )
    )

    return try {
      Gson().fromJson(data, TransactionData::class.java)
    } catch (e: JsonSyntaxException) {
      TransactionData(_skuId = data)
    }
  }

  private fun getTransactionType(payment: ERC681): TransactionType {
    if (payment.function == null) return TransactionType.ETH
    if (payment.function.equals("buy", ignoreCase = true)) return TransactionType.APPC

    return TransactionType.TOKEN
  }

  private fun getOemData(domain: String): Pair<String, String> =
    oemPackageExtractor.getInstallerPackageName(domain).blockingGet() to
        oemIdExtractor.extractOemId(domain).blockingGet()
}
