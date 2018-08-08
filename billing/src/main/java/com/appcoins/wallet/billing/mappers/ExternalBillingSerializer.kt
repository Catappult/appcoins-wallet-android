package com.appcoins.wallet.billing.mappers

import com.appcoins.wallet.billing.repository.entity.*
import com.google.gson.*
import org.jetbrains.annotations.NotNull
import java.lang.reflect.Type
import java.util.ArrayList
import java.util.Locale

class ExternalBillingSerializer {
  fun serializeProducts(products: List<Product>): List<String> {
    val serializedProducts = ArrayList<String>()
    for (product in products) {
      serializedProducts.add(Gson().toJson(mapProduct(product)))
    }
    return serializedProducts
  }

  fun mapProduct(product: Product): SKU {
    return SKU(product.sku, "inapp", getPrice(product),
        product.price
            .currency, (product.price
        .appcoinsAmount).toLong(), product.title, product.description)
  }

  private fun getPrice(product: Product): String {
    return String.format(Locale.US, "%s %.2f", product.price
        .currencySymbol, product.price
        .amount)
  }

  fun serializeSignatureData(purchase: Purchase): String {
    val gson = GsonBuilder().registerTypeAdapter(Purchase::class.java, PurchaseSignatureSerializer())
        .create()
    return gson.toJson(purchase)
  }

}
