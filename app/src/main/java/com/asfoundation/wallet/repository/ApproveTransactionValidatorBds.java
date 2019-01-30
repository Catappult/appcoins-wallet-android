package com.asfoundation.wallet.repository;

import com.appcoins.wallet.bdsbilling.AuthorizationProof;
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.billing.partners.AddressService;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.math.BigDecimal;

public class ApproveTransactionValidatorBds implements TransactionValidator {
  private final SendTransactionInteract sendTransactionInteract;
  private final BillingPaymentProofSubmission billingPaymentProofSubmission;
  private final AddressService partnerAddressService;

  public ApproveTransactionValidatorBds(SendTransactionInteract sendTransactionInteract,
      BillingPaymentProofSubmission billingPaymentProofSubmission,
      AddressService partnerAddressService) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.billingPaymentProofSubmission = billingPaymentProofSubmission;
    this.partnerAddressService = partnerAddressService;
  }

  @Override public Completable validate(PaymentTransaction paymentTransaction) {
    String packageName = paymentTransaction.getPackageName();
    String oemAddress = BuildConfig.DEFAULT_OEM_ADDRESS;
    String developerAddress = paymentTransaction.getTransactionBuilder()
        .toAddress();
    String productName = paymentTransaction.getTransactionBuilder()
        .getSkuId();
    String type = paymentTransaction.getTransactionBuilder()
        .getType();
    BigDecimal priceValue = paymentTransaction.getTransactionBuilder()
        .amount();
    Single<String> getTransactionHash = sendTransactionInteract.computeApproveTransactionHash(
        paymentTransaction.getTransactionBuilder());
    Single<String> getStoreAddress =
        partnerAddressService.getStoreAddressForPackage(paymentTransaction.getPackageName());

    return Single.zip(getTransactionHash, getStoreAddress,
        (hash, storeAddress) -> new AuthorizationProof("appcoins", hash, productName, packageName,
            priceValue, storeAddress, oemAddress, developerAddress, type, paymentTransaction.getTransactionBuilder()
            .getOrigin() == null ? "BDS" : paymentTransaction.getTransactionBuilder()
            .getOrigin(),
            paymentTransaction.getDeveloperPayload(), paymentTransaction.getCallbackUrl(),
            paymentTransaction.getTransactionBuilder()
                .getOrderReference()))
        .flatMapCompletable(billingPaymentProofSubmission::processAuthorizationProof);
  }
}
