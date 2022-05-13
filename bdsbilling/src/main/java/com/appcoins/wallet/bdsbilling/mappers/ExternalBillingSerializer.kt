package com.appcoins.wallet.bdsbilling.mappers

import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.SKU
import com.google.gson.Gson
import java.util.*

class ExternalBillingSerializer {

  companion object {
    internal const val APPC = "APPC"
  }

  fun serializeProducts(products: List<Product>): List<String> {
    val serializedProducts = ArrayList<String>()
    for (product in products) {
      serializedProducts.add(Gson().toJson(mapProduct(product)))
    }
    return serializedProducts
  }

  private fun mapProduct(product: Product): SKU = SKU(
    product.sku,
    product.billingType,
    getBasePrice(product),
    getBaseCurrency(product),
    getBasePriceInMicro(product),
    getAppcPrice(product),
    APPC,
    getAppcPriceInMicro(product),
    getFiatPrice(product),
    product.transactionPrice.currency,
    getFiatPriceInMicro(product),
    product.title,
    product.description,
    product.subscriptionPeriod,
    product.trialPeriod
  )

  private fun getBasePrice(product: Product): String =
    if ((APPC.equals(
        product.transactionPrice.base,
        true
      )) && product.transactionPrice.base != null
    ) {
      getAppcPrice(product)
    } else {
      getFiatPrice(product)
    }

  private fun getBasePriceInMicro(product: Product): Long =
    if ((APPC.equals(
        product.transactionPrice.base,
        true
      )) && product.transactionPrice.base != null
    ) {
      getAppcPriceInMicro(product)
    } else {
      getFiatPriceInMicro(product)
    }

  private fun getBaseCurrency(product: Product): String =
    if ((APPC.equals(
        product.transactionPrice.base,
        true
      )) && product.transactionPrice.base != null
    ) {
      APPC
    } else {
      product.transactionPrice.currency
    }

  private fun getFiatPrice(product: Product): String =
    String.format(
      Locale.US,
      "%s %s",
      product.transactionPrice.currencySymbol,
      product.transactionPrice.amount
    )

  private fun getFiatPriceInMicro(product: Product): Long =
    (product.transactionPrice.amount * 1000000).toLong()

  private fun getAppcPrice(product: Product): String =
    String.format("%s %s", APPC, product.transactionPrice.appcoinsAmount)

  private fun getAppcPriceInMicro(product: Product): Long =
    (product.transactionPrice.appcoinsAmount * 1000000).toLong()

}
