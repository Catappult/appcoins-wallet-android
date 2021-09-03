package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.PendingTransaction;
import io.reactivex.Single;

/**
 * Created by trinkes on 26/02/2018.
 */

public interface EthereumService {
  Single<PendingTransaction> getTransaction(String hash);
}
