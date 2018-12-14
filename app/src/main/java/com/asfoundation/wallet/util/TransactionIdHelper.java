package com.asfoundation.wallet.util;

import com.asfoundation.wallet.entity.TransactionBuilder;

public class TransactionIdHelper {
  public String computeId(TransactionBuilder transaction) {
    return transaction.getDomain() + transaction.getSkuId();
  }
}
