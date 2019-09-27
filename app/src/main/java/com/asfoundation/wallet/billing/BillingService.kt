package com.asfoundation.wallet.billing

import com.adyen.core.models.Payment
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization
import io.reactivex.Completable
import io.reactivex.Observable
import java.math.BigDecimal

interface BillingService {

  fun getTransactionUid(): String?

  fun getAuthorization(productName: String?, developerAddress: String?, payload: String?,
                       origin: String, priceValue: BigDecimal, priceCurrency: String, type: String,
                       callback: String?, orderReference: String?, appPackageName: String,
                       url: String?, urlSignature: String?): Observable<AdyenAuthorization>

  fun getAuthorization(origin: String, priceValue: BigDecimal, priceCurrency: String, type: String,
                       appPackageName: String, url: String?,
                       urlSignature: String?): Observable<AdyenAuthorization>

  fun authorize(payment: Payment, paykey: String): Completable
}