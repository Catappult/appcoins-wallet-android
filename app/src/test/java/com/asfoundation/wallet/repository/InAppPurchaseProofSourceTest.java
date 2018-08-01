package com.asfoundation.wallet.repository;

import android.support.annotation.NonNull;
import com.appcoins.wallet.billing.AuthorizationProof;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.jakewharton.rxrelay2.PublishRelay;
import io.reactivex.observers.TestObserver;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class InAppPurchaseProofSourceTest {

  @Mock private InAppPurchaseService inAppPurchaseService;
  private InAppPurchaseProofSource inAppPurchaseProofSource;
  private PublishRelay<List<PaymentTransaction>> paymentPublishRelay;

  @Before public void setUp() throws Exception {
    paymentPublishRelay = PublishRelay.create();
    when(inAppPurchaseService.getAll()).thenReturn(paymentPublishRelay);
    inAppPurchaseProofSource =
        new InAppPurchaseProofSource(inAppPurchaseService, new CopyOnWriteArrayList<>());
  }

  @Test public void get() {
    TestObserver<AuthorizationProof> observer = new TestObserver<>();
    inAppPurchaseProofSource.get()
        .subscribe(observer);
    List<PaymentTransaction> payments = new ArrayList<>();
    payments.add(createPayment("approve_hash", null));
    paymentPublishRelay.accept(payments);
    paymentPublishRelay.accept(payments);
    observer.assertNoErrors();
    observer.assertValue(
        new AuthorizationProof("appcoins", "approve_hash", "productName", "packageName",
            "0xc41b4160b63d1f9488937f7b66640d2babdbf8ad",
            "0x0965b2a3e664690315ad20b9e5b0336c19cf172e"));
    observer.assertValueCount(1);
  }

  @NonNull private PaymentTransaction createPayment(String approve_hash, String buyHash) {
    return new PaymentTransaction("uri", new TransactionBuilder("symbol"),
        PaymentTransaction.PaymentState.APPROVED, approve_hash, buyHash, BigInteger.ONE,
        "packageName", "productName");
  }
}