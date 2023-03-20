package com.appcoins.wallet.core.network.microservices.api

import com.appcoins.wallet.core.network.microservices.model.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*


interface BackupEmailApi {
  @POST("8.20210201/wallet/backup")
  fun sendBackupEmail(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Body emailBody: EmailBody
  ): Completable
}

interface BrokerVerificationApi {

  @GET("8.20200815/gateways/adyen_v2/verification/state")
  fun getVerificationState(
    @Query("wallet.address") wallet: String,
    @Query("wallet.signature") walletSignature: String
  ): Single<String>

  @GET("8.20200815/gateways/adyen_v2/verification/info")
  fun getVerificationInfo(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature")
    walletSignature: String
  ): Single<VerificationInfoResponse>

  @POST("8.20200815/gateways/adyen_v2/verification/generate")
  fun makePaypalVerificationPayment(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Body
    verificationPayment: VerificationPayment
  ): Single<AdyenTransactionResponse>

  @POST("8.20200815/gateways/adyen_v2/verification/generate")
  fun makeCreditCardVerificationPayment(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Body
    verificationPayment: VerificationPayment
  ): Completable

  @POST("8.20200815/gateways/adyen_v2/verification/validate")
  fun validateCode(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Body code: String
  ): Completable

  interface TokenToLocalFiatApi {
    @GET("8.20180518/exchanges/{currency}/convert/{value}")
    fun getValueToTargetFiat(
      @Path("currency") currency: String,
      @Path("value") value: String
    ): Single<ConversionResponseBody>

    @GET("8.20180518/exchanges/{currency}/convert/{value}")
    fun getValueToTargetFiat(
      @Path("currency") currency: String,
      @Path("value") value: String,
      @Query("to")
      targetCurrency: String
    ): Single<ConversionResponseBody>

    @GET("8.20180518/exchanges/{currency}/convert/{value}?to=APPC")
    fun convertFiatToAppc(
      @Path("currency") currency: String,
      @Path("value") value: String
    ): Single<ConversionResponseBody>
  }

  interface CarrierBillingApi {
    @POST("8.20210329/gateways/dimoco/transactions")
    fun makePayment(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body carrierTransactionBody: CarrierTransactionBody
    )
        : Single<CarrierCreateTransactionResponse>

    @GET("8.20210329/gateways/dimoco/transactions/{uid}")
    fun getPayment(
      @Path("uid") uid: String,
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature")
      walletSignature: String
    ): Observable<TransactionResponse>

    @GET("8.20210329/dimoco/countries")
    fun getAvailableCountryList(): Single<CountryListResponse>
  }

  interface AdyenApi {
    @POST("8.20200815/gateways/adyen_v2/transactions")
    @Headers("Content-Type: application/json;format=product_token")
    fun makeAdyenBodyPayment(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body payment: PaymentRequest
    ): Single<AdyenTransactionResponse>

    @POST("8.20200815/gateways/adyen_v2/transactions")
    fun makeAdyenPayment(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body payment: PaymentDetails
    ): Single<AdyenTransactionResponse>

    @GET("8.20200815/gateways/adyen_v2/payment-methods")
    fun loadPaymentInfo(
      @Query("wallet.address") walletAddress: String,
      @Query("price.value") value: String,
      @Query("price.currency") currency: String,
      @Query("method") methods: String
    ): Single<PaymentMethodsResponse>


    @GET("8.20200815/gateways/adyen_v2/transactions/{uid}")
    fun getTransaction(
      @Path("uid") uid: String, @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature")
      walletSignature: String
    ): Single<TransactionResponse>


    @Headers("Content-Type: application/json;format=product_token")
    @POST("8.20200815/gateways/adyen_v2/transactions")
    fun makeTokenPayment(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body payment: TokenPayment
    ): Single<AdyenTransactionResponse>

    @PATCH("8.20200815/gateways/adyen_v2/transactions/{uid}")
    fun submitRedirect(
      @Path("uid") uid: String,
      @Query("wallet.address") address: String,
      @Query("wallet.signature") signature: String,
      @Body payment: AdyenPayment
    ): Single<AdyenTransactionResponse>

    @POST("8.20200815/gateways/adyen_v2/disable-recurring")
    fun disablePayments(@Body wallet: DisableWallet): Completable
  }

  interface PaypalV2Api {

    @POST("8.20200815/gateways/paypal/transactions")
    fun createTransaction(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body paypalPayment: PaypalPayment
    ): Single<PaypalV2StartResponse>

    @POST("8.20200815/gateways/paypal/billing-agreement/token/create")
    fun createToken(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body createTokenRequest: CreateTokenRequest
    ): Single<PaypalV2CreateTokenResponse>

    @POST("8.20200815/gateways/paypal/billing-agreement/create")
    fun createBillingAgreement(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body token: String
    ): Single<PaypalV2CreateAgreementResponse>

    @POST("8.20200815/gateways/paypal/billing-agreement/token/cancel")
    fun cancelToken(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body token: String
    ): Single<String?>
  }

  interface BrokerBdsApi {
    @GET("8.20180518/transactions")
    fun getSkuTransaction(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Query("cursor") cursor: Long,
      @Query("type") type: BillingSupportedType,
      @Query("limit") limit: Long,
      @Query("sort.name") sort: String,
      @Query("sort.reverse") isReverse: Boolean,
      @Query("product") skuId: String?,
      @Query("domain") packageName: String
    ): Single<TransactionsResponse>

    @GET("8.20180518/transactions/{uId}")
    fun getAppcoinsTransaction(
      @Path("uId") uId: String,
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature")
      walletSignature: String
    ): Single<Transaction>

    /**
     * @param value, value of purchase
     * @param currency, currency of purchase
     * @param currencyType, filter for appc and credits payment, use fiat if you don't want appc and credits
     * @param direct, either if it returns non-direct payments (false) (earn appcoins and ask someone to pay) or not
     * @param type, INAPP, INAPP_UNMANAGED or TOPUP. This is used to filter async payments in INAPP and INAPP_UNMANAGED,
     * if null no filter is applied by transactionType
     *
     */
    @GET("8.20230101/methods")
    fun getPaymentMethods(
      @Query("price.value") value: String? = null,
      @Query("price.currency") currency: String? = null,
      @Query("currency.type") currencyType: String? = null,
      @Query("direct") direct: Boolean? = null,
      @Query("transaction.type") type: String?,
      @Query("domain") packageName: String?
    ): Single<GetMethodsResponse>

    @FormUrlEncoded
    @PATCH("8.20200810/gateways/{gateway}/transactions/{uid}")
    fun patchTransaction(
      @Path("gateway") gateway: String,
      @Path("uid") uid: String,
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Field("pay_key")
      paykey: String
    ): Completable

    /**
     * @param gateway type of the transaction that is being created;
     * @param creditsPurchaseBody CreditsPurchaseBody.
     * @param walletAddress address of the user wallet
     * @param walletSignature signature obtained after signing the wallet
     */
    @POST("8.20200810/gateways/{gateway}/transactions")
    @Headers("Content-Type: application/json; format=product_token")
    fun createTransaction(
      @Path("gateway") gateway: String,
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body creditsPurchaseBody: CreditsPurchaseBody
    ): Single<Transaction>

    /**
     * All optional fields should be passed despite possible being null as these are
     * required by some applications to complete the purchase flow
     * @param gateway type of the transaction that is being created;
     * @see Transaction.Status
     * @param origin value from the transaction origin (bds, unity, unknown)
     * @param domain package name of the application
     * @param priceValue amount of the transaction. Only needed in one step payments
     * @param priceCurrency currency of the transaction. Only needed in one step payments
     * @param product name of the product that is being bought
     * @param type name of the payment method being used
     * @param userWallet address of the user wallet
     * @param walletsDeveloper Wallet address of the apps developer
     * @param token
     * @param developerPayload Group of details used in some purchases by the application to
     * complete the purchase
     * @param callback url used in some purchases by the application to complete the purchase
     * @param orderReference reference used in some purchases by the application to
     * @param referrerUrl url to validate the transaction
     * @param walletAddress address of the user wallet
     * @param walletSignature signature obtained after signing the wallet
     */
    @FormUrlEncoded
    @POST("8.20200810/gateways/{gateway}/transactions")
    fun createTransaction(
      @Path("gateway") gateway: String,
      @Field("origin") origin: String?,
      @Field("domain") domain: String,
      @Field("price.value") priceValue: String?,
      @Field("price.currency") priceCurrency: String,
      @Field("product") product: String?,
      @Field("type") type: String,
      @Field("wallets.user") userWallet: String?,
      @Field("wallets.developer") walletsDeveloper: String?,
      @Field("entity.oemid") entityOemId: String?,
      @Field("entity.domain") entityDomain: String?,
      @Field("entity.promo_code") entityPromoCode: String?,
      @Field("token") token: String?,
      @Field("metadata") developerPayload: String?,
      @Field("callback_url") callback: String?,
      @Field("reference") orderReference: String?,
      @Field("referrer_url") referrerUrl: String?,
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String
    ): Single<Transaction>

    /**
     * All optional fields should be passed despite possible being null as these are
     * required by some applications to complete the purchase flow
     * @see Transaction.Status
     * @param origin value from the transaction origin (bds, unity, unknown)
     * @param domain package name of the application
     * @param priceValue amount of the transaction. Only needed in one step payments
     * @param priceCurrency currency of the transaction. Only needed in one step payments
     * @param product name of the product that is being bought
     * @param type type of payment being done (inapp, inapp_unmanaged, ...)
     * @param userWallet address of the user wallet
     * @param walletsDeveloper Wallet address of the apps developer
     * @param method payment method used on the gateway
     * @param developerPayload Group of details used in some purchases by the application to
     * complete the purchase
     * @param callback url used in some purchases by the application to complete the purchase
     * @param orderReference reference used in some purchases by the application to
     * @param referrerUrl url to validate the transaction
     * @param walletAddress address of the user wallet
     * @param walletSignature signature obtained after signing the wallet
     */
    @FormUrlEncoded
    @POST("8.20200810/gateways/myappcoins/transactions")
    fun createTransaction(
      @Field("origin") origin: String?,
      @Field("domain") domain: String,
      @Field("price.value") priceValue: String?,
      @Field("price.currency") priceCurrency: String?,
      @Field("product") product: String?,
      @Field("type") type: String,
      @Field("wallets.user") userWallet: String?,
      @Field("wallets.developer") walletsDeveloper: String?,
      @Field("entity.oemid") entityOemId: String?,
      @Field("entity.domain") entityDomain: String?,
      @Field("entity.promo_code") entityPromoCode: String?,
      @Field("method") method: String?,
      @Field("metadata") developerPayload: String?,
      @Field("callback_url") callback: String?,
      @Field("reference") orderReference: String?,
      @Field("referrer_url") referrerUrl: String?,
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String
    ): Single<Transaction>
  }
}