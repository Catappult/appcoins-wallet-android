package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.carrierbilling.ForbiddenError
import com.appcoins.wallet.billing.common.BillingErrorMapper
import com.google.gson.Gson
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BillingErrorMapperTest {

  private lateinit var mapper: BillingErrorMapper

  @Before
  fun setup() {
    mapper = BillingErrorMapper(Gson())
  }

  @Test
  fun mapForbiddenCodeNotAllowedTest() {
    val forbiddenType = mapper.mapForbiddenCode(BillingErrorMapper.NOT_ALLOWED_CODE)
    Assert.assertEquals(forbiddenType, ForbiddenError.ForbiddenType.SUB_ALREADY_OWNED)
  }

  @Test
  fun mapForbiddenCodeSubAlreadyOwnedTest() {
    val forbiddenType = mapper.mapForbiddenCode(BillingErrorMapper.FORBIDDEN_CODE)
    Assert.assertEquals(forbiddenType, ForbiddenError.ForbiddenType.BLOCKED)
  }

  @Test
  fun mapForbiddenCodeUnknownCodeTest() {
    val forbiddenType = mapper.mapForbiddenCode("Any error")
    Assert.assertEquals(forbiddenType, null)
  }

  @Test
  fun mapErrorInfoBillingAddressTest() {
    val code = BillingErrorMapper.FIELDS_MISSING_CODE
    val text = "payment.billing"
    val errorInfo =
        mapper.mapErrorInfo(400, """{"code":"$code","text":"$text"}""")
    Assert.assertEquals(errorInfo, ErrorInfo(400, code, text, ErrorInfo.ErrorType.BILLING_ADDRESS))
  }

  @Test
  fun mapErrorInfoNotAllowedTest() {
    val code = BillingErrorMapper.NOT_ALLOWED_CODE
    val text = null
    val errorInfo =
        mapper.mapErrorInfo(400, """{"code":"$code","text":"$text"}""")
    Assert.assertEquals(errorInfo.toString(),
        ErrorInfo(400, code, text, ErrorInfo.ErrorType.SUB_ALREADY_OWNED).toString())
  }

  @Test
  fun mapErrorInfoSubAlreadyOwnedTest() {
    val code = BillingErrorMapper.FORBIDDEN_CODE
    val text = null
    val errorInfo =
        mapper.mapErrorInfo(400, """{"code":"$code","text":"$text"}""")
    Assert.assertEquals(errorInfo.toString(),
        ErrorInfo(400, code, text, ErrorInfo.ErrorType.BLOCKED).toString())
  }

  @Test
  fun mapErrorInfoConflictTest() {
    val code = null
    val text = null
    val errorInfo =
        mapper.mapErrorInfo(409, """{"code":"$code","text":"$text"}""")
    Assert.assertEquals(errorInfo.toString(),
        ErrorInfo(409, code, text, ErrorInfo.ErrorType.CONFLICT).toString())
  }

  @Test
  fun mapErrorInfoUnknownErrorTest() {
    val code = "something"
    val text = "something"
    val errorInfo =
        mapper.mapErrorInfo(500, """{"code":"$code","text":"$text"}""")
    Assert.assertEquals(errorInfo,
        ErrorInfo(500, code, text, ErrorInfo.ErrorType.UNKNOWN))
  }
}