package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.GetDefaultWalletBalance;
import io.reactivex.Single;
import java.math.BigDecimal;

public interface BalanceService {
  Single<GetDefaultWalletBalance.BalanceState> hasEnoughBalance(
      TransactionBuilder transactionBuilder, BigDecimal transactionGasLimit);
}
