package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin;
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type;
import io.reactivex.Completable;
import java.math.BigDecimal;

public class RewardsManager {
  private final AppcoinsRewards appcoinsRewards;

  public RewardsManager(AppcoinsRewards appcoinsRewards) {
    this.appcoinsRewards = appcoinsRewards;
  }

  public Completable pay(String sku, BigDecimal amount, String developerAddress,
      String storeAddress, String oemAddress) {
    return appcoinsRewards.pay(amount, Origin.BDS, sku, Type.INAPP, developerAddress, storeAddress,
        oemAddress);
  }
}
