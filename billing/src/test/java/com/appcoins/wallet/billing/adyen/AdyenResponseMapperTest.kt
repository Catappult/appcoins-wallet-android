package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.model.payments.response.Threeds2FingerprintAction
import com.appcoins.wallet.bdsbilling.repository.entity.Gateway
import com.appcoins.wallet.bdsbilling.repository.entity.Metadata
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.common.BillingErrorMapper
import com.appcoins.wallet.billing.common.response.TransactionMetadata
import com.appcoins.wallet.billing.common.response.TransactionResponse
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.util.Error
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
    val paymentResponse =
      PaymentMethodsResponse(
        Price(
          BigDecimal(2),
          TEST_FIAT_CURRENCY
        ), jsonObject
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
    val paymentResponse =
      PaymentMethodsResponse(
        Price(
          BigDecimal(2),
          TEST_FIAT_CURRENCY
        ), jsonObject
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
    val paymentResponse =
      PaymentMethodsResponse(
        Price(
          BigDecimal(2),
          TEST_FIAT_CURRENCY
        ), jsonObject
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
    val response =
      AdyenTransactionResponse(
        TEST_UID,
        TEST_HASH,
        TEST_REFERENCE, TransactionStatus.COMPLETED,
        null, null
      )
    val expectedModel = PaymentModel(
      null, null, null, null, null, null,
      TEST_UID, null,
      TEST_HASH,
      TEST_REFERENCE, emptyList(), PaymentModel.Status.COMPLETED,
      null, null
    )
    val model = mapper.map(response)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapAdyenTransactionResponseRedirectActionTypeTest() {
    val action = JsonObject()
    action.addProperty("type", AdyenResponseMapper.REDIRECT)
    val fraudResponse = FraudResultResponse(
      "100", listOf(
        FraudResult(FraudCheckResult(10, 20, "name"))
      )
    )
    val payment = MakePaymentResponse(
      "psp",
      TEST_RESULT_CODE, action,
      TEST_REFUSAL_REASON,
      TEST_REFUSAL_REASON_CODE, fraudResponse
    )
    val response =
      AdyenTransactionResponse(
        TEST_UID,
        TEST_HASH,
        TEST_REFERENCE, TransactionStatus.COMPLETED,
        payment, TransactionMetadata("errorMessage", 30, null)
      )

    val expectedAction = RedirectAction()
    expectedAction.url = "url"
    expectedAction.paymentData = "data"
    Mockito.`when`(adyenSerializer.deserializeRedirectAction(action))
      .thenReturn(expectedAction)

    val expectedModel =
      PaymentModel(
        TEST_RESULT_CODE,
        TEST_REFUSAL_REASON, 20, expectedAction, "url", "data",
        TEST_UID, null,
        TEST_HASH,
        TEST_REFERENCE, listOf(20), PaymentModel.Status.COMPLETED,
        "errorMessage", 30
      )
    val model = mapper.map(response)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapAdyenTransactionResponse3DSFingerPrintActionTypeTest() {
    val action = JsonObject()
    action.addProperty("type", AdyenResponseMapper.THREEDS2FINGERPRINT)
    val fraudResponse = FraudResultResponse(
      "100", listOf(
        FraudResult(FraudCheckResult(10, 20, "name"))
      )
    )
    val payment = MakePaymentResponse(
      "psp",
      TEST_RESULT_CODE, action,
      TEST_REFUSAL_REASON,
      TEST_REFUSAL_REASON_CODE, fraudResponse
    )
    val response =
      AdyenTransactionResponse(
        TEST_UID,
        TEST_HASH,
        TEST_REFERENCE, TransactionStatus.COMPLETED,
        payment, TransactionMetadata("errorMessage", 30, null)
      )

    val expectedAction = Threeds2FingerprintAction()
    expectedAction.paymentData = "data"
    Mockito.`when`(adyenSerializer.deserialize3DSFingerprint(action))
      .thenReturn(expectedAction)

    val expectedModel =
      PaymentModel(
        TEST_RESULT_CODE,
        TEST_REFUSAL_REASON, 20, expectedAction, null, "data",
        TEST_UID, null,
        TEST_HASH,
        TEST_REFERENCE, listOf(20), PaymentModel.Status.COMPLETED,
        "errorMessage", 30
      )
    val model = mapper.map(response)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapAdyenTransactionResponse3DSChallengeActionTypeTest() {
    val action = JsonObject()
    action.addProperty("type", AdyenResponseMapper.THREEDS2CHALLENGE)
    val payment = MakePaymentResponse(
      "psp",
      TEST_RESULT_CODE, action,
      TEST_REFUSAL_REASON,
      TEST_REFUSAL_REASON_CODE, null
    )
    val response =
      AdyenTransactionResponse(
        TEST_UID,
        TEST_HASH,
        TEST_REFERENCE, TransactionStatus.COMPLETED,
        payment, TransactionMetadata("errorMessage", 30, null)
      )

    val expectedAction = Threeds2ChallengeAction()
    expectedAction.paymentData = "data"
    Mockito.`when`(adyenSerializer.deserialize3DSChallenge(action))
      .thenReturn(expectedAction)

    val expectedModel =
      PaymentModel(
        TEST_RESULT_CODE,
        TEST_REFUSAL_REASON, 20, expectedAction, null, "data",
        TEST_UID, null,
        TEST_HASH,
        TEST_REFERENCE, emptyList(), PaymentModel.Status.COMPLETED,
        "errorMessage", 30
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
      "psp",
      TEST_RESULT_CODE, action,
      TEST_REFUSAL_REASON,
      TEST_REFUSAL_REASON_CODE, null
    )
    val response =
      AdyenTransactionResponse(
        TEST_UID,
        TEST_HASH,
        TEST_REFERENCE, TransactionStatus.COMPLETED,
        payment, TransactionMetadata("errorMessage", 30, null)
      )

    val expectedAction = Threeds2Action()
    expectedAction.paymentData = "data"
    Mockito.`when`(adyenSerializer.deserialize3DS(action)).thenReturn(expectedAction)

    val expectedModel =
      PaymentModel(
        TEST_RESULT_CODE,
        TEST_REFUSAL_REASON, 20, expectedAction, null, "data",
        TEST_UID, null,
        TEST_HASH,
        TEST_REFERENCE, emptyList(), PaymentModel.Status.COMPLETED,
        "errorMessage", 30
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
      "psp",
      TEST_RESULT_CODE, action,
      TEST_REFUSAL_REASON,
      TEST_REFUSAL_REASON_CODE, null
    )
    val response =
      AdyenTransactionResponse(
        TEST_UID,
        TEST_HASH,
        TEST_REFERENCE, TransactionStatus.COMPLETED,
        payment, TransactionMetadata("errorMessage", 30, null)
      )

    val expectedAction = Threeds2Action()
    expectedAction.paymentData = "data"
    Mockito.`when`(adyenSerializer.deserialize3DS(action)).thenReturn(expectedAction)

    val expectedModel =
      PaymentModel(
        TEST_RESULT_CODE,
        TEST_REFUSAL_REASON, 20, expectedAction, null, "data",
        TEST_UID, null,
        TEST_HASH,
        TEST_REFERENCE, emptyList(), PaymentModel.Status.COMPLETED,
        "errorMessage", 30
      )
    val model = mapper.map(response)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapTransactionResponseTest() {
    val transactionResponse =
      TransactionResponse(
        TEST_UID,
        TEST_HASH,
        TEST_REFERENCE, TransactionStatus.COMPLETED, null
      )
    val expectedModel = PaymentModel(transactionResponse, PaymentModel.Status.COMPLETED)
    val model = mapper.map(transactionResponse)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapTransactionTest() {
    val transaction = Transaction(
      TEST_UID, Transaction.Status.COMPLETED,
      Gateway(Gateway.Name.adyen_v2, "label", "icon"),
      TEST_HASH, Metadata("purchase_uid"),
      TEST_REFERENCE, com.appcoins.wallet.bdsbilling.repository.entity.Price(
        TEST_FIAT_CURRENCY,
        TEST_FIAT_VALUE, "20"
      ), "INAPP", null
    )
    val expectedModel = PaymentModel(
      "", null, null, null, "", "",
      TEST_UID, "purchase_uid",
      TEST_HASH,
      TEST_REFERENCE, emptyList(), PaymentModel.Status.COMPLETED
    )
    val model = mapper.map(transaction)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapInfoModelErrorTest() {
    val throwable = Throwable("Error")
    val errorInfo = ErrorInfo(
      null, null, "Error",
      ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(null, "Error"))
      .thenReturn(errorInfo)
    val expectedModel = PaymentInfoModel(Error(true, false, errorInfo))
    val model = mapper.mapInfoModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapInfoModelHttpErrorTest() {
    val errorResponse: Response<Any> = Response.error(400, ResponseBody.create(null, "Error"))
    val throwable = HttpException(errorResponse)
    val errorInfo = ErrorInfo(
      400, null, "Error",
      ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(400, "Error"))
      .thenReturn(errorInfo)
    val expectedModel = PaymentInfoModel(Error(true, false, errorInfo))
    val model = mapper.mapInfoModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapInfoModelNoNetworkTest() {
    val throwable = IOException()
    val expectedModel = PaymentInfoModel(Error(true, true))
    val model = mapper.mapInfoModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapPaymentModelErrorTest() {
    val throwable = Throwable("Error")
    val errorInfo = ErrorInfo(
      null, null, "Error",
      ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(null, "Error"))
      .thenReturn(errorInfo)
    val expectedModel = PaymentModel(
      Error(true, false, errorInfo)
    )
    val model = mapper.mapPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapPaymentModelHttpErrorTest() {
    val errorResponse: Response<Any> = Response.error(400, ResponseBody.create(null, "Error"))
    val throwable = HttpException(errorResponse)
    val errorInfo = ErrorInfo(
      400, null, "Error",
      ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(400, "Error"))
      .thenReturn(errorInfo)
    val expectedModel = PaymentModel(
      Error(true, false, errorInfo)
    )
    val model = mapper.mapPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapPaymentModelNoNetworkTest() {
    val throwable = IOException()
    val expectedModel = PaymentModel(Error(true, true))
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
      null, null, "Error",
      ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(null, "Error"))
      .thenReturn(errorInfo)
    val expectedModel =
      VerificationPaymentModel(
        false, VerificationPaymentModel.ErrorType.OTHER, null, null, null,
        Error(true, false, errorInfo)
      )
    val model = mapper.mapVerificationPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationPaymentModelInvalidRequestErrorTest() {
    val errorResponse: Response<Any> =
      Response.error(
        400, ResponseBody.create(
          null, """{"code":"Request.Invalid", "data":{
        "pspReference": "882605631340595B",
        "refusalReason": "CVC Declined",
        "resultCode": "Refused",
        "refusalReasonCode": "24",
        "merchantReference": "q7AbPG-Peouk2DVP"
    }}"""
        )
      )
    val throwable = HttpException(errorResponse)
    val expectedModel =
      VerificationPaymentModel(
        false, VerificationPaymentModel.ErrorType.INVALID_REQUEST,
        "CVC Declined", 24, null,
        Error(true, false)
      )
    val model = mapper.mapVerificationPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationPaymentModelTooManyErrorTest() {
    val errorResponse: Response<Any> =
      Response.error(
        429, ResponseBody.create(
          null, """{"code":"Request.TooMany", "data":{
        "pspReference": "882605631340595B",
        "refusalReason": "CVC Declined",
        "resultCode": "Refused",
        "refusalReasonCode": "24",
        "merchantReference": "q7AbPG-Peouk2DVP"
    }}"""
        )
      )
    val throwable = HttpException(errorResponse)
    val expectedModel =
      VerificationPaymentModel(
        false, VerificationPaymentModel.ErrorType.TOO_MANY_ATTEMPTS,
        "CVC Declined", 24, null,
        Error(true, false)
      )
    val model = mapper.mapVerificationPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationPaymentModelUnknownErrorTest() {
    val errorResponse: Response<Any> =
      Response.error(
        400, ResponseBody.create(
          null, """{"code":"Request.something", "data":{
        "pspReference": "882605631340595B",
        "refusalReason": "CVC Declined",
        "resultCode": "Refused",
        "refusalReasonCode": "24",
        "merchantReference": "q7AbPG-Peouk2DVP"
    }}"""
        )
      )
    val throwable = HttpException(errorResponse)
    val expectedModel =
      VerificationPaymentModel(
        false, VerificationPaymentModel.ErrorType.OTHER, "CVC Declined",
        24, null,
        Error(true, false)
      )
    val model = mapper.mapVerificationPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationPaymentModelNoNetworkErrorTest() {
    val throwable = IOException()
    val expectedModel =
      VerificationPaymentModel(
        false, errorType = VerificationPaymentModel.ErrorType.OTHER,
        error = Error(true, true)
      )
    val model = mapper.mapVerificationPaymentModelError(throwable)
    Assert.assertEquals(expectedModel, model)
  }

  @Test
  fun mapVerificationCodeErrorTest() {
    val throwable = Throwable("Error")
    val expectedResult =
      VerificationCodeResult(
        false, VerificationCodeResult.ErrorType.OTHER,
        Error(
          true, false,
          ErrorInfo(
            null, null, "Error",
            ErrorInfo.ErrorType.UNKNOWN
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
    val errorResponse: Response<Any> =
      Response.error(400, ResponseBody.create(null, content))
    val throwable = HttpException(errorResponse)
    val errorInfo = ErrorInfo(
      400, null, content,
      ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(400, content))
      .thenReturn(errorInfo)
    val expectedResult =
      VerificationCodeResult(
        false, VerificationCodeResult.ErrorType.WRONG_CODE,
        Error(true, false, errorInfo)
      )
    val result = mapper.mapVerificationCodeError(throwable)
    Assert.assertEquals(expectedResult, result)
  }

  @Test
  fun mapVerificationCodeTooManyErrorTest() {
    val content = """{"code":"Request.TooMany", "data":null}"""
    val errorResponse: Response<Any> =
      Response.error(
        429,
        ResponseBody.create(null, content)
      )
    val throwable = HttpException(errorResponse)
    val errorInfo = ErrorInfo(
      429, null, content,
      ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(429, content))
      .thenReturn(errorInfo)
    val expectedResult =
      VerificationCodeResult(
        false, VerificationCodeResult.ErrorType.TOO_MANY_ATTEMPTS,
        Error(true, false, errorInfo)
      )
    val result = mapper.mapVerificationCodeError(throwable)
    Assert.assertEquals(expectedResult, result)
  }

  @Test
  fun mapVerificationCodeUnknownErrorTest() {
    val content = """{"code":"Request.something", "data":null}"""
    val errorResponse: Response<Any> =
      Response.error(400, ResponseBody.create(null, content))
    val throwable = HttpException(errorResponse)
    val errorInfo = ErrorInfo(
      400, null, content,
      ErrorInfo.ErrorType.UNKNOWN
    )
    Mockito.`when`(billingErrorMapper.mapErrorInfo(400, content))
      .thenReturn(errorInfo)
    val expectedResult =
      VerificationCodeResult(
        false, VerificationCodeResult.ErrorType.OTHER,
        Error(true, false, errorInfo)
      )
    val result = mapper.mapVerificationCodeError(throwable)
    Assert.assertEquals(expectedResult, result)
  }

  @Test
  fun mapVerificationCodeNoNetworkErrorTest() {
    val throwable = IOException()
    val expectedResult =
      VerificationCodeResult(
        false, errorType = VerificationCodeResult.ErrorType.OTHER,
        error = Error(true, true, ErrorInfo())
      )
    val result = mapper.mapVerificationCodeError(throwable)
    Assert.assertEquals(expectedResult, result)
  }
}