package com.asfoundation.wallet.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public interface ApproveService {
  void start();

  Completable approve(String key, PaymentTransaction paymentTransaction, boolean useBds);

  Observable<BdsApproveService.ApproveTransaction> getApprove(String uri);

  Observable<List<BdsApproveService.ApproveTransaction>> getAll();

  Completable remove(String key);
}
