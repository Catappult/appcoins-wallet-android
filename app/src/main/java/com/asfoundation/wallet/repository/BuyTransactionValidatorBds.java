package com.asfoundation.wallet.repository;

import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission;
import com.appcoins.wallet.bdsbilling.PaymentProof;
import com.asfoundation.wallet.billing.partners.AddressService;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;
import io.reactivex.Single;

public class BuyTransactionValidatorBds implements TransactionValidator {
  private final SendTransactionInteract sendTransactionInteract;
  private final BillingPaymentProofSubmission billingPaymentProofSubmission;
  private final DefaultTokenProvider defaultTokenProvider;
  private final AddressService partnerAddressService;

  public BuyTransactionValidatorBds(SendTransactionInteract sendTransactionInteract,
      BillingPaymentProofSubmission billingPaymentProofSubmission,
      DefaultTokenProvider defaultTokenProvider, AddressService partnerAddressService) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.billingPaymentProofSubmission = billingPaymentProofSubmission;
    this.defaultTokenProvider = defaultTokenProvider;
    this.partnerAddressService = partnerAddressService;
  }

  @Override public Completable validate(PaymentTransaction paymentTransaction) {
    String packageName = paymentTransaction.getPackageName();
    String productName = paymentTransaction.getTransactionBuilder()
        .getSkuId();
    Single<String> getTransactionHash = defaultTokenProvider.getDefaultToken()
        .flatMap(tokenInfo -> sendTransactionInteract.computeBuyTransactionHash(
            paymentTransaction.getTransactionBuilder()));
    Single<String> getStoreAddress =
        partnerAddressService.getStoreAddressForPackage(paymentTransaction.getPackageName());
    Single<String> getOemAddress =
        partnerAddressService.getOemAddressForPackage(paymentTransaction.getPackageName());

    return Single.zip(getTransactionHash, getStoreAddress, getOemAddress,
        (hash, storeAddress, oemAddress) -> new PaymentProof("appcoins",
            paymentTransaction.getApproveHash(), hash, productName, packageName, storeAddress,
            oemAddress))
        .flatMapCompletable(billingPaymentProofSubmission::processPurchaseProof);
  }
}