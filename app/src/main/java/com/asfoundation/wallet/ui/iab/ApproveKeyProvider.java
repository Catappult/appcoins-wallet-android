package com.asfoundation.wallet.ui.iab;

import io.reactivex.Single;

public interface ApproveKeyProvider {
  Single<String> getKey(String packageName, String productName);
}
