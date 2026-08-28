package com.asfoundation.wallet.transactions

import com.appcoins.wallet.core.network.backend.model.TransactionPagingResponse
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Covers the `partial_credits_payment` object served by `/appc/1.20260828/wallet/{wallet}/history`:
 * its Jackson binding and the split it produces on [TransactionModel].
 */
class PartialPaymentTransactionModelTest {

  private lateinit var objectMapper: ObjectMapper
  private var previousLocale: Locale = Locale.getDefault()

  @Before
  fun before() {
    // AmountUtils formats with the default locale; pin it so the assertions are deterministic.
    previousLocale = Locale.getDefault()
    Locale.setDefault(Locale.US)
    // Same configuration as BackendApiModule.providesOffChainTransactionsApi.
    objectMapper = ObjectMapper().apply {
      dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
      configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
  }

  @After
  fun after() {
    Locale.setDefault(previousLocale)
  }

  @Test
  fun partialPaymentIsSplitBetweenBalanceAndMethod() {
    val partialPayment = modelOf(historyJson())

    assertNotNull(partialPayment)
    assertEquals("€0.50", partialPayment!!.creditsAmount)
    assertEquals("10.00 APPC-C", partialPayment.creditsAppcAmount)
    assertEquals("€0.50", partialPayment.chargedAmount)
    assertEquals(0.5f, partialPayment.creditsWeight, 0.001f)
  }

  @Test
  fun purchaseTotalsAreLeftUntouchedByTheSplit() {
    val model = transactionOf(historyJson())

    assertEquals("-\$1.10", model.amount)
    assertEquals("-1.00 APPC-C", model.amountSubtitle)
    assertEquals("credit_card", model.method)
  }

  @Test
  fun creditsWeightFollowsTheSizeOfTheSlice() {
    val partialCredits = PARTIAL_CREDITS_OBJECT
      .replace(""""paid_currency_amount": "0.50"""", """"paid_currency_amount": "0.75"""")
      .replace(""""charged_amount": "0.50"""", """"charged_amount": "0.25"""")

    val partialPayment = modelOf(historyJson(partialCredits))

    assertEquals(0.75f, partialPayment!!.creditsWeight, 0.001f)
  }

  @Test
  fun aRowWithoutAChargedAmountReportsOnlyTheCreditsLeg() {
    val partialCredits =
      PARTIAL_CREDITS_OBJECT.replace(""""charged_amount": "0.50"""", """"charged_amount": null""")

    val partialPayment = modelOf(historyJson(partialCredits))

    assertNotNull(partialPayment)
    assertEquals("€0.50", partialPayment!!.creditsAmount)
    assertNull(partialPayment.chargedAmount)
    assertEquals(1f, partialPayment.creditsWeight, 0.001f)
  }

  @Test
  fun anOrdinaryCardPurchaseHasNoSplit() {
    assertNull(modelOf(historyJson(partialCredits = "null")))
  }

  @Test
  fun aResponseWithoutTheFieldStillDecodes() {
    assertNull(modelOf(historyJson(partialCredits = null)))
  }

  private fun transactionOf(json: String) =
    objectMapper.readValue(json, TransactionPagingResponse::class.java)
      .items
      .first()
      .toModel(SELECTED_CURRENCY)

  private fun modelOf(json: String) = transactionOf(json).partialPayment

  companion object {
    private const val SELECTED_CURRENCY = "USD"

    private const val PARTIAL_CREDITS_OBJECT =
      """{"appc_amount": 10000000000000000000, "usd_amount": "0.55", """ +
        """"paid_currency_amount": "0.50", "charged_amount": "0.50"}"""

    /** One `INAPP_PURCHASE` of 1.00 EUR, shown in USD. [partialCredits] `null` omits the field. */
    private fun historyJson(partialCredits: String? = PARTIAL_CREDITS_OBJECT): String {
      val partialCreditsEntry =
        if (partialCredits == null) "" else """"partial_credits_payment": $partialCredits,"""
      return """
        {
          "items": [
            {
              "txid": "0xfec381f4943569add55b55cd75bef5308a1c843b28aa5b5fc75f3bd2be6dacc9",
              "status": "SUCCESS",
              "type": "INAPP_PURCHASE",
              "sender": "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
              "receiver": "0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f",
              "amount": 1000000000000000000,
              "amount_currency": "APPC-C",
              "ts": "2026-08-28 14:52:45.295",
              "processed_time": "2026-08-28 14:52:45.442",
              "app": "Trivial Drive",
              "app_icon": null,
              "bonus": null,
              "bonus_description": null,
              "method": "credit_card",
              "paid_currency": "EUR",
              "paid_currency_amount": "1.00",
              "default_currency_amount": "1.10",
              "reference": "APT453473277845346",
              "refund_txid": null,
              "revert_txid": null,
              "parent_txid": null,
              "sku": "gas",
              $partialCreditsEntry
              "invoice_id": "123"
            }
          ],
          "total": 1,
          "next_url": null,
          "next_cursor": null
        }
      """
    }
  }
}
