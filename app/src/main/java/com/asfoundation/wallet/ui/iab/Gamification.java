package com.asfoundation.wallet.ui.iab;

import io.reactivex.Single;
import java.math.BigDecimal;

class Gamification {
  public Single<BigDecimal> getEarningBonus() {
    return Single.just(BigDecimal.ZERO);
  }
}
