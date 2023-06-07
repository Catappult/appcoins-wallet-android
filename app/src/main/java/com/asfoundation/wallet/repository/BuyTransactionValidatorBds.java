package com.asfoundation.wallet.repository;

import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission;
import com.appcoins.wallet.bdsbilling.PaymentProof;
import com.appcoins.wallet.core.network.microservices.model.Transaction;
import com.appcoins.wallet.core.utils.partners.AddressService;
import com.appcoins.wallet.core.utils.partners.AttributionEntity;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.SendTransactionInteract;
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

  @Override public Single<Transaction> validate(PaymentTransaction paymentTransaction) {
    String packageName = paymentTransaction.getPackageName();
    String productName = paymentTransaction.getTransactionBuilder()
        .getSkuId();
    Single<String> getTransactionHash = defaultTokenProvider.getDefaultToken()
        .flatMap(tokenInfo -> sendTransactionInteract.computeBuyTransactionHash(
            paymentTransaction.getTransactionBuilder()));

    Single<AttributionEntity> attributionEntity =
        partnerAddressService.getAttributionEntity(packageName);

    return Single.zip(getTransactionHash, attributionEntity,
            (hash, attrEntity) -> new PaymentProof("appcoins", paymentTransaction.getApproveHash(),
                hash, productName, packageName, attrEntity.getOemId(), attrEntity.getDomain()))
        .flatMap(billingPaymentProofSubmission::processPurchaseProof);
  }
}