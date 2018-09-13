package com.asfoundation.wallet.repository;

import com.appcoins.wallet.billing.AuthorizationProof;
import com.appcoins.wallet.billing.BillingPaymentProofSubmission;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import io.reactivex.Completable;

public class ApproveTransactionValidatorBds implements TransactionValidator {
  private final SendTransactionInteract sendTransactionInteract;
  private final BillingPaymentProofSubmission billingPaymentProofSubmission;

  public ApproveTransactionValidatorBds(SendTransactionInteract sendTransactionInteract,
      BillingPaymentProofSubmission billingPaymentProofSubmission) {
    this.sendTransactionInteract = sendTransactionInteract;
    this.billingPaymentProofSubmission = billingPaymentProofSubmission;
  }

  @Override public Completable validate(PaymentTransaction paymentTransaction) {
    String packageName = paymentTransaction.getPackageName();
    String storeAddress = BuildConfig.DEFAULT_STORE_ADDRESS;
    String oemAddress = BuildConfig.DEFAULT_OEM_ADDRESS;
    String developerAddress = paymentTransaction.getTransactionBuilder()
        .toAddress();
    String productName = paymentTransaction.getTransactionBuilder()
        .getSkuId();
    return sendTransactionInteract.computeApproveTransactionHash(
        paymentTransaction.getTransactionBuilder())
        .map(
            hash -> new AuthorizationProof("appcoins", hash, productName, packageName, storeAddress,
                oemAddress, developerAddress, paymentTransaction.getDeveloperPayload()))
        .flatMapCompletable(billingPaymentProofSubmission::processAuthorizationProof);
  }
}
