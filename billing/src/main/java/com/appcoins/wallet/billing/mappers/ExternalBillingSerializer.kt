package com.appcoins.wallet.billing.mappers

import com.appcoins.wallet.billing.repository.entity.Product
import com.appcoins.wallet.billing.repository.entity.Purchase
import com.appcoins.wallet.billing.repository.entity.SKU
import com.google.gson.Gson
import java.util.*

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
    return Gson().toJson(purchase.signature.message)
  }
}
