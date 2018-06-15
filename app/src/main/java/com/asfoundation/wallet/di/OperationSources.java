package com.asfoundation.wallet.di;

import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.poa.ProofStatus;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.Payment;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class OperationSources {
  private final InAppPurchaseInteractor inAppPurchaseInteractor;
  private final ProofOfAttentionService proofOfAttentionService;

  public OperationSources(InAppPurchaseInteractor inAppPurchaseInteractor,
      ProofOfAttentionService proofOfAttentionService) {
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
    this.proofOfAttentionService = proofOfAttentionService;
  }

  public List<AppcoinsOperationsDataSaver.OperationDataSource> getSources() {
    List<AppcoinsOperationsDataSaver.OperationDataSource> list = new ArrayList<>();

    list.add(() -> inAppPurchaseInteractor.getAll()
        .subscribeOn(Schedulers.io())
        .flatMap(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .filter(paymentTransaction -> paymentTransaction.getStatus()
                .equals(Payment.Status.COMPLETED))
            .map(
                paymentTransaction -> new AppcoinsOperationsDataSaver.OperationDataSource
                    .OperationData(
                    paymentTransaction.getBuyHash(), paymentTransaction.getPackageName(),
                    paymentTransaction.getProductName()))));

    list.add(() -> proofOfAttentionService.get()
        .subscribeOn(Schedulers.io())
        .flatMap(proofs -> Observable.fromIterable(proofs)
            .filter(proof -> proof.getProofStatus()
                .equals(ProofStatus.COMPLETED))
            .map(proof -> new AppcoinsOperationsDataSaver.OperationDataSource.OperationData(
                proof.getHash(), proof.getPackageName(), proof.getCampaignId()))));

    return list;
  }
}
