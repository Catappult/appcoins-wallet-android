package com.asfoundation.wallet.repository;

import com.appcoins.wallet.bdsbilling.AuthorizationProof;
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission;
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService;
import com.appcoins.wallet.core.analytics.analytics.partners.AttributionEntity;
import com.appcoins.wallet.core.network.microservices.model.Transaction;
import com.asfoundation.wallet.interact.SendTransactionInteract;
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

  @Override public Single<Transaction> validate(PaymentTransaction paymentTransaction) {
    String packageName = paymentTransaction.getPackageName();
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
    Single<AttributionEntity> attributionEntity =
        partnerAddressService.getAttributionEntity(packageName);

    return Single.zip(getTransactionHash, attributionEntity,
        (hash, attrEntity) -> new AuthorizationProof("appcoins", hash, productName, packageName,
            priceValue, attrEntity.getOemId(), attrEntity.getDomain(), developerAddress, type,
            paymentTransaction.getTransactionBuilder()
                .getOrigin() == null ? "BDS" : paymentTransaction.getTransactionBuilder()
                .getOrigin(), paymentTransaction.getDeveloperPayload(),
            paymentTransaction.getCallbackUrl(), paymentTransaction.getTransactionBuilder()
            .getOrderReference(), paymentTransaction.getTransactionBuilder()
            .getReferrerUrl()))
        .flatMap(billingPaymentProofSubmission::processAuthorizationProof);
  }
}
