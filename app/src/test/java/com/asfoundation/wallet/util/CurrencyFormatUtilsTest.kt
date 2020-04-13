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
}