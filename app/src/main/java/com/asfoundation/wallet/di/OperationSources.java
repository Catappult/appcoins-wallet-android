package com.asfoundation.wallet.di;

import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;
import com.asfoundation.wallet.ui.iab.Payment;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class OperationSources {
  private final InAppPurchaseInteractor inAppPurchaseInteractor;

  public @Inject OperationSources(InAppPurchaseInteractor inAppPurchaseInteractor) {
    this.inAppPurchaseInteractor = inAppPurchaseInteractor;
  }

  public List<AppcoinsOperationsDataSaver.OperationDataSource> getSources() {
    List<AppcoinsOperationsDataSaver.OperationDataSource> list = new ArrayList<>();

    list.add(() -> inAppPurchaseInteractor.getAll()
        .subscribeOn(Schedulers.io())
        .flatMap(paymentTransactions -> Observable.fromIterable(paymentTransactions)
            .filter(paymentTransaction -> paymentTransaction.getStatus()
                .equals(Payment.Status.COMPLETED))
            .map(
                paymentTransaction -> new AppcoinsOperationsDataSaver.OperationDataSource.OperationData(
                    paymentTransaction.getBuyHash(), paymentTransaction.getPackageName(),
                    paymentTransaction.getProductName()))));

    return list;
  }
}
