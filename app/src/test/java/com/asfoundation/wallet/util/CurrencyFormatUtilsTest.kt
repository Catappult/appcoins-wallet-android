package com.asfoundation.wallet.util

import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.*


class CurrencyFormatUtilsTest {

  private val formatter = CurrencyFormatUtils()
  private val value = BigDecimal(123456789.123456)

  @Test
  fun formatFiat() {
    Locale.setDefault(Locale.US)
    val formattedValueUs = formatter.formatCurrency(value, WalletCurrency.FIAT)
    val expectedValueUs = "123,456,789.12"

    Locale.setDefault(Locale.FRANCE)
    val formattedValueFr = formatter.formatCurrency(value, WalletCurrency.FIAT)
    val expectedValueFr = "123 456 789,12"

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
    val expectedValueFr = "123 456 789,12"

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
    val expectedValueFr = "123 456 789,12"

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
    val expectedValueFr = "123 456 789,1234"

    Locale.setDefault(Locale("pt", "BR"))
    val formattedValueBr = formatter.formatCurrency(value, WalletCurrency.ETHEREUM)
    val expectedValueBr = "123.456.789,1234"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)
  }

  @Test
  fun formatTransferCurrency() {
    val bigValue = BigDecimal("123456789.0123")
    val smallValue = BigDecimal("0.000001")
    val bigDecimalValue = BigDecimal("1.12345678901234567")
    val noDecimalPlaces = BigDecimal("1")
    val oneDecimalPlaces = BigDecimal("0.4")
    //Big value
    Locale.setDefault(Locale.US)
    var formattedValueUs =
      formatter.formatTransferCurrency(bigValue, WalletCurrency.ETHEREUM)
    var expectedValueUs = "123,456,789.0123"

    Locale.setDefault(Locale.FRANCE)
    var formattedValueFr =
      formatter.formatTransferCurrency(bigValue, WalletCurrency.ETHEREUM)
    var expectedValueFr = "123 456 789,0123"

    Locale.setDefault(Locale("pt", "BR"))
    val formattedValueBr =
      formatter.formatTransferCurrency(bigValue, WalletCurrency.ETHEREUM)
    val expectedValueBr = "123.456.789,0123"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)
    //Small Value
    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatTransferCurrency(smallValue, WalletCurrency.ETHEREUM)
    expectedValueUs = "0.000001"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatTransferCurrency(smallValue, WalletCurrency.CREDITS)
    expectedValueFr = "0,000001"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    //bigDecimalPlaces
    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatTransferCurrency(bigDecimalValue, WalletCurrency.ETHEREUM)
    expectedValueUs = "1.123456789012345"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatTransferCurrency(bigDecimalValue, WalletCurrency.CREDITS)
    expectedValueFr = "1,123456789012345"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    //No Decimal Places
    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatTransferCurrency(noDecimalPlaces, WalletCurrency.ETHEREUM)
    expectedValueUs = "1.0000"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatTransferCurrency(noDecimalPlaces, WalletCurrency.CREDITS)
    expectedValueFr = "1,00"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    //One decimal place
    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatTransferCurrency(oneDecimalPlaces, WalletCurrency.ETHEREUM)
    expectedValueUs = "0.4000"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatTransferCurrency(oneDecimalPlaces, WalletCurrency.CREDITS)
    expectedValueFr = "0,40"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
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
    var expectedValueFr = "12 345"

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
    expectedValueFr = "12 345,12"

    Locale.setDefault(Locale("pt", "BR"))
    formattedValueBr = formatter.formatGamificationValues(gamificationValueDecimal)
    expectedValueBr = "12.345,12"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)
  }

  @Test
  fun formatShortGamificationValues() {
    val gamificationValueInt = BigDecimal(123)
    val gamificationValueDecimal = BigDecimal(123.123)
    val gamificationValueKInt = BigDecimal(12345)
    val gamificationValueKDecimal = BigDecimal(12345.123)
    val gamificationValueMInt = BigDecimal(12345678)
    val gamificationValueMDecimal = BigDecimal(12345678.123)
    val gamificationValueBInt = BigDecimal(1234567890)
    val gamificationValueBDecimal = BigDecimal(1234567890.123)

    Locale.setDefault(Locale.US)
    var formattedValueUs = formatter.formatShortGamificationValues(gamificationValueInt)
    var expectedValueUs = "123"

    Locale.setDefault(Locale.FRANCE)
    var formattedValueFr = formatter.formatShortGamificationValues(gamificationValueInt)
    var expectedValueFr = "123"

    Locale.setDefault(Locale("pt", "BR"))
    var formattedValueBr = formatter.formatShortGamificationValues(gamificationValueInt)
    var expectedValueBr = "123"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)

    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatShortGamificationValues(gamificationValueKInt)
    expectedValueUs = "12.34k"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatShortGamificationValues(gamificationValueKInt)
    expectedValueFr = "12,34k"

    Locale.setDefault(Locale("pt", "BR"))
    formattedValueBr = formatter.formatShortGamificationValues(gamificationValueKInt)
    expectedValueBr = "12,34k"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)

    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatShortGamificationValues(gamificationValueMInt)
    expectedValueUs = "12.35m"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatShortGamificationValues(gamificationValueMInt)
    expectedValueFr = "12,35m"

    Locale.setDefault(Locale("pt", "BR"))
    formattedValueBr = formatter.formatShortGamificationValues(gamificationValueMInt)
    expectedValueBr = "12,35m"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)

    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatShortGamificationValues(gamificationValueBInt)
    expectedValueUs = "1.23b"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatShortGamificationValues(gamificationValueBInt)
    expectedValueFr = "1,23b"

    Locale.setDefault(Locale("pt", "BR"))
    formattedValueBr = formatter.formatShortGamificationValues(gamificationValueBInt)
    expectedValueBr = "1,23b"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)

    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatShortGamificationValues(gamificationValueDecimal)
    expectedValueUs = "123.12"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatShortGamificationValues(gamificationValueDecimal)
    expectedValueFr = "123,12"

    Locale.setDefault(Locale("pt", "BR"))
    formattedValueBr = formatter.formatShortGamificationValues(gamificationValueDecimal)
    expectedValueBr = "123,12"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)

    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatShortGamificationValues(gamificationValueKDecimal)
    expectedValueUs = "12.35k"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatShortGamificationValues(gamificationValueKDecimal)
    expectedValueFr = "12,35k"

    Locale.setDefault(Locale("pt", "BR"))
    formattedValueBr = formatter.formatShortGamificationValues(gamificationValueKDecimal)
    expectedValueBr = "12,35k"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)

    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatShortGamificationValues(gamificationValueMDecimal)
    expectedValueUs = "12.35m"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatShortGamificationValues(gamificationValueMDecimal)
    expectedValueFr = "12,35m"

    Locale.setDefault(Locale("pt", "BR"))
    formattedValueBr = formatter.formatShortGamificationValues(gamificationValueMDecimal)
    expectedValueBr = "12,35m"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)

    Locale.setDefault(Locale.US)
    formattedValueUs = formatter.formatShortGamificationValues(gamificationValueBDecimal)
    expectedValueUs = "1.23b"

    Locale.setDefault(Locale.FRANCE)
    formattedValueFr = formatter.formatShortGamificationValues(gamificationValueBDecimal)
    expectedValueFr = "1,23b"

    Locale.setDefault(Locale("pt", "BR"))
    formattedValueBr = formatter.formatShortGamificationValues(gamificationValueBDecimal)
    expectedValueBr = "1,23b"

    assertEquals(expectedValueUs, formattedValueUs)
    assertEquals(expectedValueFr, formattedValueFr)
    assertEquals(expectedValueBr, formattedValueBr)
  }

  @Test
  fun scaleFiat() {
    val formattedValueUs = formatter.scaleFiat(value)
      .toString()
    val expectedValueUs = "123456789.12"

    assertEquals(expectedValueUs, formattedValueUs)
  }
}