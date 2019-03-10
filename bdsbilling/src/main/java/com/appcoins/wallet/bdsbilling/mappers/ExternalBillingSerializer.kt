package com.appcoins.wallet.billing.mappers

import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.PurchaseSignatureSerializer
import com.appcoins.wallet.bdsbilling.repository.entity.SKU
import com.appcoins.wallet.billing.repository.entity.Product
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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

  fun mapProduct(product: Product): SKU {
      return SKU(product.sku, "inapp", getBasePrice(product), getBaseCurrency(product),
              getBasePriceInMicro(product), getAppcPrice(product), APPC,
              getAppcPriceInMicro(product), getFiatPrice(product), product.price.currency,
              getFiatPriceInMicro(product), product.title, product.description)
  }

  private fun getBasePrice(product: Product): String {
    return if ((APPC.equals(product.price.base, true)) && product.price.base != null)
      getAppcPrice(product)
    else
      getFiatPrice(product)
  }

  private fun getBasePriceInMicro(product: Product): Int {
    return if ((APPC.equals(product.price.base, true)) && product.price.base != null)
      getAppcPriceInMicro(product)
    else
      getFiatPriceInMicro(product)
  }

  private fun getBaseCurrency(product: Product): String {
    return if ((APPC.equals(product.price.base, true)) && product.price.base != null)
      APPC
    else
      product.price.currency
  }

  private fun getFiatPrice(product: Product): String {
    return String.format(Locale.US, "%s %s", product.price
        .currencySymbol, product.price
        .amount)
  }

  private fun getFiatPriceInMicro(product: Product): Int {
    return (product.price.amount * 1000000).toInt()
  }

  private fun getAppcPrice(product: Product): String {
    return String.format("%s %s", APPC, product.price.appcoinsAmount)
  }

  private fun getAppcPriceInMicro(product: Product): Int {
    return (product.price.appcoinsAmount * 1000000).toInt()
  }

  fun serializeSignatureData(purchase: Purchase): String {
    val gson =
        GsonBuilder().registerTypeAdapter(Purchase::class.java, PurchaseSignatureSerializer())
            .disableHtmlEscaping()
            .create()
    return gson.toJson(purchase)
  }

}
