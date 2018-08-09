package com.asfoundation.wallet.ws;

import com.appcoins.wallet.billing.repository.GatewaysRepository;
import com.appcoins.wallet.billing.repository.entity.TransactionStatus;
import com.asfoundation.wallet.billing.TransactionService;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import rx.Completable;
import rx.Single;
import rx.schedulers.Schedulers;

public final class BDSTransactionService implements TransactionService {

  // TODO: 09-08-2018 neuro remove this constants
  private static final String WALLET_ADDRESS = "0xBB83e699F1188bAAbEa820ce02995C97BD9b510F";
  private static final String WALLET_SIGNATURE =
      "e619ae601013a0071dde91c31d67b14b3938a32c90f5c434c283fa4a2e15e75f27ceef30609156285b827bd936da6e099b89fd982553b719f74c86e828ee0a2800";
  private static final String PAYLOAD = "hello";
  private static final String PRODUCT_NAME = "gas";
  private static final String WALLET_DEVELOPER = "0xda99070eb09ab6ab7e49866c390b01d3bca9d516";
  private static final String WALLET_STORE = "0xd95c64c6eee9164539d679354f349779a04f57cb";

  private final GatewaysRepository gatewaysRepository;

  public BDSTransactionService(GatewaysRepository gatewaysRepository) {
    this.gatewaysRepository = gatewaysRepository;
  }

  @Override public Single<String> createTransaction(String address, String signature, String token,
      String PACKAGE_NAME) {
    return RxJavaInterop.toV1Single(
        gatewaysRepository.createAdyenTransaction(WALLET_ADDRESS, WALLET_SIGNATURE, token, PAYLOAD,
            PACKAGE_NAME, PRODUCT_NAME, WALLET_DEVELOPER, WALLET_STORE)
            .map(TransactionStatus::getUid))
        .subscribeOn(Schedulers.io());
  }

  @Override public Single<String> getSession(String transactionUid) {
    return RxJavaInterop.toV1Single(
        gatewaysRepository.getSessionKey(transactionUid, WALLET_ADDRESS, WALLET_SIGNATURE))
        .map(authorization -> authorization.getData()
            .getSession())
        .subscribeOn(Schedulers.io());
  }

  @Override public Completable finishTransaction(String transactionUid, String paykey) {
    return RxJavaInterop.toV1Completable(
        gatewaysRepository.patchTransaction(transactionUid, WALLET_ADDRESS, WALLET_SIGNATURE,
            paykey))
        .subscribeOn(Schedulers.io());
  }
}
