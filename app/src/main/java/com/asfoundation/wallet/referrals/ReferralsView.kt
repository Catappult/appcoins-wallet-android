package com.asfoundation.wallet.referrals

import java.math.BigDecimal

interface ReferralsView {
  fun setupLayout(invited: Int, currency: String, receivedAmount: BigDecimal, amount: BigDecimal,
                  maxAmount: BigDecimal, available: Int)
}
