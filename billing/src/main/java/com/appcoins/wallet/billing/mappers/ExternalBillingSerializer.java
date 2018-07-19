package com.appcoins.wallet.billing.mappers;

import android.support.annotation.NonNull;
import com.appcoins.wallet.billing.repository.entity.Product;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExternalBillingSerializer {
  public @NonNull List<String> serializeProducts(List<Product> products) {
    final List<String> serializedProducts = new ArrayList<>();
    for (Product product : products) {
      serializedProducts.add(new Gson().toJson(new SKU(product.getSku(), "inapp", getPrice(product),
          product.getPrice()
              .getCurrency(), (long) (product.getPrice()
          .getAmount() * 1000000), product.getTitle(), product.getDescription())));
    }
    return serializedProducts;
  }

  private String getPrice(Product product) {
    return String.format(Locale.US, "%s %.2f", product.getPrice()
        .getCurrencySymbol(), product.getPrice()
        .getAmount());
  }

  private static class SKU {

    private String productId;

    private String type;

    private String price;

    @SerializedName("price_currency_code") private String currency;

    @SerializedName("price_amount_micros") private long amount;

    private String title;

    private String description;

    public SKU(String productId, String type, String price, String currency, long amount,
        String title, String description) {
      this.productId = productId;
      this.type = type;
      this.price = price;
      this.currency = currency;
      this.amount = amount;
      this.title = title;
      this.description = description;
    }

    public String getProductId() {
      return productId;
    }

    public String getType() {
      return type;
    }

    public String getPrice() {
      return price;
    }

    public String getCurrency() {
      return currency;
    }

    public long getAmount() {
      return amount;
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }
  }
}
