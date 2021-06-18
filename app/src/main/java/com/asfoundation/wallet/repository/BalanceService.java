package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.wallets.GetDefaultWalletBalanceInteract;
import io.reactivex.Single;
import java.math.BigDecimal;

public interface BalanceService {
  Single<GetDefaultWalletBalanceInteract.BalanceState> hasEnoughBalance(
      TransactionBuilder transactionBuilder, BigDecimal transactionGasLimit);
}
