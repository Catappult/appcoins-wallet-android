package com.asfoundation.wallet.util

import junit.framework.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.*


class CurrencyFormatUtilsTest {

  private val formatter = CurrencyFormatUtils.create()
  private val value = BigDecimal(123456789.123456)

  @Test
  fun formatFiat() {
    Locale.setDefault(Locale.US)
    val formattedValueUs = formatter.formatCurrency(value, WalletCurrency.FIAT)
    val expectedValueUs = "123,456,789.12"

    Locale.setDefault(Locale.FRANCE)
    val formattedValueFr = formatter.formatCurrency(value, WalletCurrency.FIAT)
    val expectedValueFr = "123 456 789,12"

    Locale.setDefault(Locale("pt", "BR"))
    val formattedValueBr = formatter.formatCurrency(value, WalletCurrency.FIAT)
    val expectedValueBr = "123.456.789,12"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)
  }

  @Test
  fun formatAppc() {
    Locale.setDefault(Locale.US)
    val formattedValueUs = formatter.formatCurrency(value, WalletCurrency.APPCOINS)
    val expectedValueUs = "123,456,789.12"

    Locale.setDefault(Locale.FRANCE)
    val formattedValueFr = formatter.formatCurrency(value, WalletCurrency.APPCOINS)
    val expectedValueFr = "123 456 789,12"

    Locale.setDefault(Locale("pt", "BR"))
    val formattedValueBr = formatter.formatCurrency(value, WalletCurrency.APPCOINS)
    val expectedValueBr = "123.456.789,12"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)
  }

  @Test
  fun formatCredits() {
    Locale.setDefault(Locale.US)
    val formattedValueUs = formatter.formatCurrency(value, WalletCurrency.CREDITS)
    val expectedValueUs = "123,456,789.12"

    Locale.setDefault(Locale.FRANCE)
    val formattedValueFr = formatter.formatCurrency(value, WalletCurrency.CREDITS)
    val expectedValueFr = "123 456 789,12"

    Locale.setDefault(Locale("pt", "BR"))
    val formattedValueBr = formatter.formatCurrency(value, WalletCurrency.CREDITS)
    val expectedValueBr = "123.456.789,12"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)
  }

  @Test
  fun formatEth() {
    Locale.setDefault(Locale.US)
    val formattedValueUs = formatter.formatCurrency(value, WalletCurrency.ETHEREUM)
    val expectedValueUs = "123,456,789.1234"

    Locale.setDefault(Locale.FRANCE)
    val formattedValueFr = formatter.formatCurrency(value, WalletCurrency.ETHEREUM)
    val expectedValueFr = "123 456 789,1234"

    Locale.setDefault(Locale("pt", "BR"))
    val formattedValueBr = formatter.formatCurrency(value, WalletCurrency.ETHEREUM)
    val expectedValueBr = "123.456.789,1234"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)
  }

  @Test
  fun formatTransferConfirmation() {
    val bigValue = value
    val smallValue = 1.123456789012345678
    Locale.setDefault(Locale.US)
    var formattedValueUs =
        formatter.formatTransferConfirmation(bigValue.toDouble(), WalletCurrency.ETHEREUM)
    var expectedValueUs = "123,456,789.123456"

    Locale.setDefault(Locale.FRANCE)
    var formattedValueFr =
        formatter.formatTransferConfirmation(bigValue.toDouble(), WalletCurrency.ETHEREUM)
    var expectedValueFr = "123 456 789,123456"

    Locale.setDefault(Locale("pt", "BR"))
    var formattedValueBr =
        formatter.formatTransferConfirmation(bigValue.toDouble(), WalletCurrency.ETHEREUM)
    var expectedValueBr = "123.456.789,123456"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)

    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatTransferConfirmation(smallValue, WalletCurrency.ETHEREUM)
    expectedValueUs = "1.123456789012345"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatTransferConfirmation(smallValue, WalletCurrency.ETHEREUM)
    expectedValueFr = "1,123456789012345"

    Locale.setDefault(Locale("pt", "BR"))
    formattedValueBr = formatter.formatTransferConfirmation(smallValue, WalletCurrency.ETHEREUM)
    expectedValueBr = "1,123456789012345"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)
  }

  @Test
  fun formatGamificationValues() {
    val gamificationValueInt = BigDecimal(12345)
    val gamificationValueDecimal = BigDecimal(12345.123)

    Locale.setDefault(Locale.US)
    var formattedValueUs = formatter.formatGamificationValues(gamificationValueInt)
    var expectedValueUs = "12,345"

    Locale.setDefault(Locale.FRANCE)
    var formattedValueFr = formatter.formatGamificationValues(gamificationValueInt)
    var expectedValueFr = "12 345"

    Locale.setDefault(Locale("pt", "BR"))
    var formattedValueBr = formatter.formatGamificationValues(gamificationValueInt)
    var expectedValueBr = "12.345"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)

    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatGamificationValues(gamificationValueDecimal)
    expectedValueUs = "12,345.12"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatGamificationValues(gamificationValueDecimal)
    expectedValueFr = "12 345,12"

    Locale.setDefault(Locale("pt", "BR"))
    formattedValueBr = formatter.formatGamificationValues(gamificationValueDecimal)
    expectedValueBr = "12.345,12"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)
  }

  @Test
  fun scaleFiat() {
    val formattedValueUs = formatter.scaleFiat(value).toString()
    val expectedValueUs = "123456789.12"

    assertEquals(expectedValueUs, formattedValueUs)
  }
}