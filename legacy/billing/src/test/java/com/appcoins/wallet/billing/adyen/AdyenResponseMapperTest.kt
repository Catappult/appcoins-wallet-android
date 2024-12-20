package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.model.payments.response.Threeds2FingerprintAction
import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.common.BillingErrorMapper
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.core.network.microservices.model.AdyenPrice
import com.appcoins.wallet.core.network.microservices.model.AdyenTransactionResponse
import com.appcoins.wallet.core.network.microservices.model.FraudCheckResult
import com.appcoins.wallet.core.network.microservices.model.FraudResult
import com.appcoins.wallet.core.network.microservices.model.FraudResultResponse
import com.appcoins.wallet.core.network.microservices.model.Gateway
import com.appcoins.wallet.core.network.microservices.model.MakePaymentResponse
import com.appcoins.wallet.core.network.microservices.model.Metadata
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodsResponse
import com.appcoins.wallet.core.network.microservices.model.Price
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.network.microservices.model.TransactionMetadata
import com.appcoins.wallet.core.network.microservices.model.TransactionResponse
import com.appcoins.wallet.core.network.microservices.model.TransactionStatus
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner::class)
class AdyenResponseMapperTest {

  companion object {
    private const val TEST_FIAT_VALUE = "2.0"
    private const val TEST_FIAT_CURRENCY = "EUR"
    private const val TEST_UID = "uid"
    private const val TEST_HASH = "hash"
    private const val TEST_REFERENCE = "reference"
    private const val TEST_RESULT_CODE = "AUTHORISED"
    private const val TEST_REFUSAL_REASON = "refusalReason"
    private const val TEST_REFUSAL_REASON_CODE = "20"
  }

  @Mock
  lateinit var adyenSerializer: AdyenSerializer

  @Mock
  lateinit var billingErrorMapper: BillingErrorMapper

  private lateinit var mapper: AdyenResponseMapper

  @Before
  fun setup() {
    mapper = AdyenResponseMapper(Gson(), billingErrorMapper, adyenSerializer)
  }

  @Test
  fun mapPaymentMethodsResponseNoMethodFoundTest() {
    val jsonObject = JsonObject()
    val paymentResponse = PaymentMethodsResponse(
      adyenPrice = AdyenPrice(
        value = BigDecimal(2),
        currency = TEST_FIAT_CURRENCY
      ),
      payment = jsonObject
    )
    val paymentMethodsApiResponse = PaymentMethodsApiResponse()
    val expectedModel = PaymentInfoModel(Error(true))

    Mockito.`when`(adyenSerializer.deserializePaymentMethods(paymentResponse))
      .thenReturn(paymentMethodsApiResponse)

    val model = mapper.map(paymentResponse, AdyenPaymentRepository.Methods.CREDIT_CARD)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapPaymentMethodsResponseStoredMethodTest() {
    val jsonObject = JsonObject()
    val paymentResponse = PaymentMethodsResponse(
      adyenPrice = AdyenPrice(
        value = BigDecimal(2),
        currency = TEST_FIAT_CURRENCY
      ),
      payment = jsonObject
    )
    val paymentMethodsApiResponse = PaymentMethodsApiResponse()
    val storedPaymentMethod = StoredPaymentMethod()
    storedPaymentMethod.type = AdyenPaymentRepository.Methods.CREDIT_CARD.adyenType
    paymentMethodsApiResponse.storedPaymentMethods = listOf(storedPaymentMethod)

    Mockito.`when`(adyenSerializer.deserializePaymentMethods(paymentResponse))
      .thenReturn(paymentMethodsApiResponse)

    val model = mapper.map(paymentResponse, AdyenPaymentRepository.Methods.CREDIT_CARD)
    Assert.assertEquals(storedPaymentMethod, model.paymentMethod)
    Assert.assertTrue(model.isStored)
    Assert.assertEquals(BigDecimal(2), model.priceAmount)
    Assert.assertEquals(TEST_FIAT_CURRENCY, model.priceCurrency)
    Assert.assertNotNull(model.cardComponent)
    Assert.assertEquals(
      storedPaymentMethod.supportedShopperInteractions,
      model.supportedShopperInteractions
    )
    Assert.assertEquals(Error(), model.error)
  }

  @Test
  fun mapPaymentMethodsResponseNoStoredMethodTest() {
    val jsonObject = JsonObject()
    val paymentResponse = PaymentMethodsResponse(
      adyenPrice = AdyenPrice(
        value = BigDecimal(2),
        currency = TEST_FIAT_CURRENCY
      ),
      payment = jsonObject
    )
    val paymentMethodsApiResponse = PaymentMethodsApiResponse()
    val paymentMethod = PaymentMethod()
    paymentMethod.type = AdyenPaymentRepository.Methods.CREDIT_CARD.adyenType
    paymentMethodsApiResponse.paymentMethods = listOf(paymentMethod)

    Mockito.`when`(adyenSerializer.deserializePaymentMethods(paymentResponse))
      .thenReturn(paymentMethodsApiResponse)

    val model = mapper.map(paymentResponse, AdyenPaymentRepository.Methods.CREDIT_CARD)
    Assert.assertEquals(paymentMethod, model.paymentMethod)
    Assert.assertFalse(model.isStored)
    Assert.assertEquals(BigDecimal(2), model.priceAmount)
    Assert.assertEquals(TEST_FIAT_CURRENCY, model.priceCurrency)
    Assert.assertNotNull(model.cardComponent)
    Assert.assertEquals(emptyList<String>(), model.supportedShopperInteractions)
    Assert.assertEquals(Error(), model.error)
  }

  @Test
  fun mapAdyenTransactionResponseNullPaymentTest() {
    val response = AdyenTransactionResponse(
      uid = TEST_UID,
      hash = TEST_HASH,
      orderReference = TEST_REFERENCE,
      status = TransactionStatus.COMPLETED,
      payment = null,
      metadata = null
    )
    val expectedModel = PaymentModel(
      resultCode = null,
      refusalReason = null,
      refusalCode = null,
      action = null,
      redirectUrl = null,
      paymentData = null,
      uid = TEST_UID,
      purchaseUid = null,
      hash = TEST_HASH,
      orderReference = TEST_REFERENCE,
      fraudResultIds = emptyList(),
      status = PaymentModel.Status.COMPLETED,
      errorMessage = null,
      errorCode = null
    )
    val model = mapper.map(response)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapAdyenTransactionResponseRedirectActionTypeTest() {
    val action = JsonObject()
    action.addProperty("type", AdyenResponseMapper.REDIRECT)
    val fraudResponse = FraudResultResponse(
      accountScore = "100",
      results = listOf(
        FraudResult(
          FraudCheckResult(
            accountScore = 10,
            checkId = 20,
            name = "name"
          )
        )
      )
    )
    val payment = MakePaymentResponse(
      pspReference = "psp",
      resultCode = TEST_RESULT_CODE,
      action = action,
      refusalReason = TEST_REFUSAL_REASON,
      refusalReasonCode = TEST_REFUSAL_REASON_CODE,
      fraudResult = fraudResponse
    )
    val response =
      AdyenTransactionResponse(
        uid = TEST_UID,
        hash = TEST_HASH,
        orderReference = TEST_REFERENCE,
        status = TransactionStatus.COMPLETED,
        payment = payment,
        metadata = TransactionMetadata("errorMessage", 30, null)
      )

    val expectedAction = RedirectAction()
    expectedAction.url = "url"
    expectedAction.paymentData = "data"
    Mockito.`when`(adyenSerializer.deserializeRedirectAction(action))
      .thenReturn(expectedAction)

    val expectedModel =
      PaymentModel(
        resultCode = TEST_RESULT_CODE,
        refusalReason = TEST_REFUSAL_REASON,
        refusalCode = 20,
        action = expectedAction,
        redirectUrl = "url",
        paymentData = "data",
        uid = TEST_UID,
        purchaseUid = null,
        hash = TEST_HASH,
        orderReference = TEST_REFERENCE,
        fraudResultIds = listOf(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = "errorMessage",
        errorCode = 30
      )
    val model = mapper.map(response)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapAdyenTransactionResponse3DSFingerPrintActionTypeTest() {
    val action = JsonObject()
    action.addProperty("type", AdyenResponseMapper.THREEDS2FINGERPRINT)
    val fraudResponse = FraudResultResponse(
      accountScore = "100",
      results = listOf(
        FraudResult(
          FraudCheckResult(
            accountScore = 10,
            checkId = 20,
            name = "name"
          )
        )
      )
    )
    val payment = MakePaymentResponse(
      pspReference = "psp",
      resultCode = TEST_RESULT_CODE,
      action = action,
      refusalReason = TEST_REFUSAL_REASON,
      refusalReasonCode = TEST_REFUSAL_REASON_CODE,
      fraudResult = fraudResponse
    )
    val response =
      AdyenTransactionResponse(
        uid = TEST_UID,
        hash = TEST_HASH,
        orderReference = TEST_REFERENCE,
        status = TransactionStatus.COMPLETED,
        payment = payment,
        metadata = TransactionMetadata(
          errorMessage = "errorMessage",
          errorCode = 30,
          purchaseUid = null
        )
      )

    val expectedAction = Threeds2FingerprintAction()
    expectedAction.paymentData = "data"
    Mockito.`when`(adyenSerializer.deserialize3DSFingerprint(action))
      .thenReturn(expectedAction)

    val expectedModel =
      PaymentModel(
        resultCode = TEST_RESULT_CODE,
        refusalReason = TEST_REFUSAL_REASON,
        refusalCode = 20,
        action = expectedAction,
        redirectUrl = null,
        paymentData = "data",
        uid = TEST_UID,
        purchaseUid = null,
        hash = TEST_HASH,
        orderReference = TEST_REFERENCE,
        fraudResultIds = listOf(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = "errorMessage",
        errorCode = 30
      )
    val model = mapper.map(response)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapAdyenTransactionResponse3DSChallengeActionTypeTest() {
    val action = JsonObject()
    action.addProperty("type", AdyenResponseMapper.THREEDS2CHALLENGE)
    val payment = MakePaymentResponse(
      pspReference = "psp",
      resultCode = TEST_RESULT_CODE,
      action = action,
      refusalReason = TEST_REFUSAL_REASON,
      refusalReasonCode = TEST_REFUSAL_REASON_CODE,
      fraudResult = null
    )
    val response =
      AdyenTransactionResponse(
        uid = TEST_UID,
        hash = TEST_HASH,
        orderReference = TEST_REFERENCE,
        status = TransactionStatus.COMPLETED,
        payment = payment,
        metadata = TransactionMetadata(
          errorMessage = "errorMessage",
          errorCode = 30,
          purchaseUid = null
        )
      )

    val expectedAction = Threeds2ChallengeAction()
    expectedAction.paymentData = "data"
    Mockito.`when`(adyenSerializer.deserialize3DSChallenge(action))
      .thenReturn(expectedAction)

    val expectedModel =
      PaymentModel(
        resultCode = TEST_RESULT_CODE,
        refusalReason = TEST_REFUSAL_REASON,
        refusalCode = 20,
        action = expectedAction,
        redirectUrl = null,
        paymentData = "data",
        uid = TEST_UID,
        purchaseUid = null,
        hash = TEST_HASH,
        orderReference = TEST_REFERENCE,
        fraudResultIds = emptyList(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = "errorMessage",
        errorCode = 30
      )
    val model = mapper.map(response)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapAdyenTransactionResponse3DSActionTypeFingerPrintTest() {
    val action = JsonObject()
    action.addProperty("type", AdyenResponseMapper.THREEDS2)
    action.addProperty("subtype", "fingerprint")
    val payment = MakePaymentResponse(
      pspReference = "psp",
      resultCode = TEST_RESULT_CODE,
      action = action,
      refusalReason = TEST_REFUSAL_REASON,
      refusalReasonCode = TEST_REFUSAL_REASON_CODE,
      fraudResult = null
    )
    val response =
      AdyenTransactionResponse(
        uid = TEST_UID,
        hash = TEST_HASH,
        orderReference = TEST_REFERENCE,
        status = TransactionStatus.COMPLETED,
        payment = payment,
        metadata = TransactionMetadata(
          errorMessage = "errorMessage",
          errorCode = 30,
          purchaseUid = null
        )
      )

    val expectedAction = Threeds2Action()
    expectedAction.paymentData = "data"
    Mockito.`when`(adyenSerializer.deserialize3DS(action)).thenReturn(expectedAction)

    val expectedModel =
      PaymentModel(
        resultCode = TEST_RESULT_CODE,
        refusalReason = TEST_REFUSAL_REASON,
        refusalCode = 20,
        action = expectedAction,
        redirectUrl = null,
        paymentData = "data",
        uid = TEST_UID,
        purchaseUid = null,
        hash = TEST_HASH,
        orderReference = TEST_REFERENCE,
        fraudResultIds = emptyList(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = "errorMessage",
        errorCode = 30
      )
    val model = mapper.map(response)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapAdyenTransactionResponse3DSActionTypeChallengeTest() {
    val action = JsonObject()
    action.addProperty("type", AdyenResponseMapper.THREEDS2)
    action.addProperty("subtype", "challenge")
    val payment = MakePaymentResponse(
      pspReference = "psp",
      resultCode = TEST_RESULT_CODE,
      action = action,
      refusalReason = TEST_REFUSAL_REASON,
      refusalReasonCode = TEST_REFUSAL_REASON_CODE,
      fraudResult = null
    )
    val response =
      AdyenTransactionResponse(
        uid = TEST_UID,
        hash = TEST_HASH,
        orderReference = TEST_REFERENCE,
        status = TransactionStatus.COMPLETED,
        payment = payment,
        metadata = TransactionMetadata(
          errorMessage = "errorMessage",
          errorCode = 30,
          purchaseUid = null
        )
      )

    val expectedAction = Threeds2Action()
    expectedAction.paymentData = "data"
    Mockito.`when`(adyenSerializer.deserialize3DS(action)).thenReturn(expectedAction)

    val expectedModel =
      PaymentModel(
        resultCode = TEST_RESULT_CODE,
        refusalReason = TEST_REFUSAL_REASON,
        refusalCode = 20,
        action = expectedAction,
        redirectUrl = null,
        paymentData = "data",
        uid = TEST_UID,
        purchaseUid = null,
        hash = TEST_HASH,
        orderReference = TEST_REFERENCE,
        fraudResultIds = emptyList(),
        status = PaymentModel.Status.COMPLETED,
        errorMessage = "errorMessage",
        errorCode = 30
      )
    val model = mapper.map(response)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapTransactionResponseTest() {
    val transactionResponse = TransactionResponse(
      uid = TEST_UID,
      hash = TEST_HASH,
      orderReference = TEST_REFERENCE,
      status = TransactionStatus.COMPLETED,
      metadata = null
    )
    val expectedModel = PaymentModel(
      response = transactionResponse,
      status = PaymentModel.Status.COMPLETED
    )
    val model = mapper.map(transactionResponse)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapTransactionTest() {
    val transaction = Transaction(
      uid = TEST_UID,
      status = Transaction.Status.COMPLETED,
      gateway = Gateway(Gateway.Name.adyen_v2, "label", "icon"),
      hash = TEST_HASH,
      metadata = Metadata("purchase_uid"),
      orderReference = TEST_REFERENCE,
      price = Price(
        TEST_FIAT_CURRENCY,
        TEST_FIAT_VALUE, "20"
      ),
      type = "INAPP",
      wallets = null
    )
    val expectedModel = PaymentModel(
      resultCode = "",
      refusalReason = null,
      refusalCode = null,
      action = null,
      redirectUrl = "",
      paymentData = "",
      uid = TEST_UID,
      purchaseUid = "purchase_uid",
      hash = TEST_HASH,
      orderReference = TEST_REFERENCE,
      fraudResultIds = emptyList(),
      status = PaymentModel.Status.COMPLETED
    )
    val model = mapper.map(transaction)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapInfoModelErrorTest() {
    val throwable = Throwable("Error")
    val errorInfo = ErrorInfo(
      httpCode = null,
      messageCode = null,
      text = "Error",
      errorType = ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(null, "Error"))
      .thenReturn(errorInfo)
    val expectedModel = PaymentInfoModel(
      Error(
        hasError = true,
        isNetworkError = false,
        errorInfo = errorInfo
      )
    )
    val model = mapper.mapInfoModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapInfoModelHttpErrorTest() {
    val errorResponse: Response<Any> = Response.error(
      400,
      ResponseBody.create(
        contentType = null,
        content = "Error"
      )
    )
    val throwable = HttpException(errorResponse)
    val errorInfo = ErrorInfo(
      httpCode = 400,
      messageCode = null,
      text = "Error",
      errorType = ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(400, "Error"))
      .thenReturn(errorInfo)
    val expectedModel = PaymentInfoModel(
      Error(
        hasError = true,
        isNetworkError = false,
        errorInfo = errorInfo
      )
    )
    val model = mapper.mapInfoModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapInfoModelNoNetworkTest() {
    val throwable = IOException()
    val expectedModel = PaymentInfoModel(
      error = Error(
        hasError = true,
        isNetworkError = true
      )
    )
    val model = mapper.mapInfoModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapPaymentModelErrorTest() {
    val throwable = Throwable("Error")
    val errorInfo = ErrorInfo(
      httpCode = null,
      messageCode = null,
      text = "Error",
      errorType = ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(null, "Error"))
      .thenReturn(errorInfo)
    val expectedModel = PaymentModel(
      error = Error(
        hasError = true,
        isNetworkError = false,
        errorInfo = errorInfo
      )
    )
    val model = mapper.mapPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapPaymentModelHttpErrorTest() {
    val errorResponse: Response<Any> = Response.error(400, ResponseBody.create(null, "Error"))
    val throwable = HttpException(errorResponse)
    val errorInfo = ErrorInfo(
      httpCode = 400,
      messageCode = null,
      text = "Error",
      errorType = ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(400, "Error"))
      .thenReturn(errorInfo)
    val expectedModel = PaymentModel(
      error = Error(
        hasError = true,
        isNetworkError = false,
        errorInfo = errorInfo
      )
    )
    val model = mapper.mapPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapPaymentModelNoNetworkTest() {
    val throwable = IOException()
    val expectedModel = PaymentModel(
      error = Error(
        hasError = true,
        isNetworkError = true
      )
    )
    val model = mapper.mapPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationPaymentModelSuccessTest() {
    val expectedModel = VerificationPaymentModel(true)
    val model = mapper.mapVerificationPaymentModelSuccess()
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationPaymentModelErrorTest() {
    val throwable = Throwable("Error")
    val errorInfo = ErrorInfo(
      httpCode = null,
      messageCode = null,
      text = "Error",
      errorType = ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(null, "Error"))
      .thenReturn(errorInfo)
    val expectedModel = VerificationPaymentModel(
      success = false,
      errorType = VerificationPaymentModel.ErrorType.OTHER,
      refusalReason = null,
      refusalCode = null,
      redirectUrl = null,
      error = Error(
        hasError = true,
        isNetworkError = false,
        errorInfo = errorInfo
      )
    )
    val model = mapper.mapVerificationPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationPaymentModelInvalidRequestErrorTest() {
    val errorResponse: Response<Any> = Response.error(
      400, ResponseBody.create(
        contentType = null,
        content = """{"code":"Request.Invalid", "data":{
              "pspReference": "882605631340595B",
              "refusalReason": "CVC Declined",
              "resultCode": "Refused",
              "refusalReasonCode": "24",
              "merchantReference": "q7AbPG-Peouk2DVP"
          }}"""
      )
    )
    val throwable = HttpException(errorResponse)
    val expectedModel = VerificationPaymentModel(
      success = false,
      errorType = VerificationPaymentModel.ErrorType.INVALID_REQUEST,
      refusalReason = "CVC Declined",
      refusalCode = 24,
      redirectUrl = null,
      error = Error(
        hasError = true,
        isNetworkError = false
      )
    )
    val model = mapper.mapVerificationPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationPaymentModelTooManyErrorTest() {
    val errorResponse: Response<Any> = Response.error(
      429,
      ResponseBody.create(
        contentType = null,
        content = """{"code":"Request.TooMany", "data":{
                  "pspReference": "882605631340595B",
                  "refusalReason": "CVC Declined",
                  "resultCode": "Refused",
                  "refusalReasonCode": "24",
                  "merchantReference": "q7AbPG-Peouk2DVP"
              }}"""
      )
    )
    val throwable = HttpException(errorResponse)
    val expectedModel = VerificationPaymentModel(
      success = false,
      errorType = VerificationPaymentModel.ErrorType.TOO_MANY_ATTEMPTS,
      refusalReason = "CVC Declined",
      refusalCode = 24,
      redirectUrl = null,
      error = Error(
        hasError = true,
        isNetworkError = false
      )
    )
    val model = mapper.mapVerificationPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationPaymentModelUnknownErrorTest() {
    val errorResponse: Response<Any> = Response.error(
      400, ResponseBody.create(
        contentType = null,
        content = """{"code":"Request.something", "data":{
                  "pspReference": "882605631340595B",
                  "refusalReason": "CVC Declined",
                  "resultCode": "Refused",
                  "refusalReasonCode": "24",
                  "merchantReference": "q7AbPG-Peouk2DVP"
              }}"""
      )
    )
    val throwable = HttpException(errorResponse)
    val expectedModel = VerificationPaymentModel(
      success = false,
      errorType = VerificationPaymentModel.ErrorType.OTHER,
      refusalReason = "CVC Declined",
      refusalCode = 24,
      redirectUrl = null,
      error = Error(
        hasError = true,
        isNetworkError = false
      )
    )
    val model = mapper.mapVerificationPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationPaymentModelNoNetworkErrorTest() {
    val throwable = IOException()
    val expectedModel = VerificationPaymentModel(
      success = false,
      errorType = VerificationPaymentModel.ErrorType.OTHER,
      error = Error(
        hasError = true,
        isNetworkError = true
      )
    )
    val model = mapper.mapVerificationPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationCodeErrorTest() {
    val throwable = Throwable("Error")
    val expectedResult = VerificationCodeResult(
      success = false,
      errorType = VerificationCodeResult.ErrorType.OTHER,
      error = Error(
        hasError = true,
        isNetworkError = false,
        errorInfo = ErrorInfo(
          httpCode = null,
          messageCode = null,
          text = "Error",
          errorType = ErrorInfo.ErrorType.UNKNOWN
        )
      )
    )
    val result = mapper.mapVerificationCodeError(throwable)
    Assert.assertEquals(expectedResult, result)
  }

  @Test
  fun mapVerificationCodeInvalidBodyErrorTest() {
    val content = """{"code":"Body.Invalid", "data": {
      "enduser": "string for the user",
      "technical": "string for us"}}"""
    val errorResponse: Response<Any> = Response.error(
      400,
      ResponseBody.create(
        contentType = null,
        content = content
      )
    )
    val throwable = HttpException(errorResponse)
    val errorInfo = ErrorInfo(
      httpCode = 400,
      messageCode = null,
      text = content,
      errorType = ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(400, content))
      .thenReturn(errorInfo)
    val expectedResult = VerificationCodeResult(
      success = false,
      errorType = VerificationCodeResult.ErrorType.WRONG_CODE,
      error = Error(
        hasError = true,
        isNetworkError = false,
        errorInfo = errorInfo
      )
    )
    val result = mapper.mapVerificationCodeError(throwable)
    Assert.assertEquals(expectedResult, result)
  }

  @Test
  fun mapVerificationCodeTooManyErrorTest() {
    val content = """{"code":"Request.TooMany", "data":null}"""
    val errorResponse: Response<Any> = Response.error(
      429,
      ResponseBody.create(
        contentType = null,
        content = content
      )
    )
    val throwable = HttpException(errorResponse)
    val errorInfo = ErrorInfo(
      httpCode = 429,
      messageCode = null,
      text = content,
      errorType = ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(429, content))
      .thenReturn(errorInfo)
    val expectedResult = VerificationCodeResult(
      success = false,
      errorType = VerificationCodeResult.ErrorType.TOO_MANY_ATTEMPTS,
      error = Error(
        hasError = true,
        isNetworkError = false,
        errorInfo = errorInfo
      )
    )
    val result = mapper.mapVerificationCodeError(throwable)
    Assert.assertEquals(expectedResult, result)
  }

  @Test
  fun mapVerificationCodeUnknownErrorTest() {
    val content = """{"code":"Request.something", "data":null}"""
    val errorResponse: Response<Any> = Response.error(
      400, ResponseBody.create(
        contentType = null,
        content = content
      )
    )
    val throwable = HttpException(errorResponse)
    val errorInfo = ErrorInfo(
      httpCode = 400,
      messageCode = null,
      text = content,
      errorType = ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(400, content))
      .thenReturn(errorInfo)
    val expectedResult = VerificationCodeResult(
      success = false,
      errorType = VerificationCodeResult.ErrorType.OTHER,
      error = Error(
        hasError = true,
        isNetworkError = false,
        errorInfo = errorInfo
      )
    )
    val result = mapper.mapVerificationCodeError(throwable)
    Assert.assertEquals(expectedResult, result)
  }

  @Test
  fun mapVerificationCodeNoNetworkErrorTest() {
    val throwable = IOException()
    val expectedResult = VerificationCodeResult(
      success = false,
      errorType = VerificationCodeResult.ErrorType.OTHER,
      error = Error(
        hasError = true,
        isNetworkError = true,
        errorInfo = ErrorInfo()
      )
    )
    val result = mapper.mapVerificationCodeError(throwable)
    Assert.assertEquals(expectedResult, result)
  }
}
