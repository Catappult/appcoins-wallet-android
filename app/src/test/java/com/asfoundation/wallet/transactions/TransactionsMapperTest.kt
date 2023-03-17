package com.asfoundation.wallet.transactions

import com.appcoins.wallet.core.network.backend.model.WalletHistory
import com.asfoundation.wallet.repository.entity.TransactionDetailsEntity
import com.asfoundation.wallet.repository.entity.TransactionEntity
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class TransactionsMapperTest {

  private lateinit var history: WalletHistory
  private lateinit var transactionMapper: TransactionsMapper
  private lateinit var relatedWallet: String

  @Before
  fun before() {
    //Json with a topup, topup normal bonus, perk bonus, iap and iap normal bonus
    val transactionsJson =
        "{\"result\":[{\"TxID\":\"0xfec381f4943569add55b55cd75bef5308a1c843b28aa5b5fc75f3bd2be6dacc9\",\"amount\":115000000000000000,\"app\":\"Appcoins Trivial Drive demo sample\",\"block\":0,\"bonus\":11.5,\"description\":null,\"fee\":0,\"icon\":\"https://apichain.dev.catappult.io/appc/icons/bonus.png\",\"icon_small\":null,\"operations\":[],\"perk\":null,\"processed_time\":\"2020-08-27 14:52:45.442269+0000\",\"receiver\":\"0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f\",\"sender\":\"0x31a16adf2d5fc73f149fbb779d20c036678b1bbd\",\"sku\":\"gas\",\"status\":\"SUCCESS\",\"subtype\":null,\"title\":null,\"ts\":\"2020-08-27 14:52:45.295586+0000\",\"type\":\"bonus\"},{\"TxID\":\"0xa96d4160232abbdb0c8b0e43b941a679005dfe178fd0726d9836c47e057d8223\",\"amount\":1000000000000000000,\"app\":\"Appcoins Trivial Drive demo sample\",\"block\":0,\"bonus\":null,\"description\":null,\"fee\":0,\"icon\":\"https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png\",\"icon_small\":\"https://apichain.dev.catappult.io/appc/icons/iap_hybrid.png\",\"operations\":[],\"perk\":null,\"processed_time\":\"2020-08-27 14:52:45.154482+0000\",\"receiver\":\"0x123c2124b7f2c18b502296ba884d9cde201f1c32\",\"sender\":\"0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f\",\"sku\":\"gas\",\"status\":\"SUCCESS\",\"subtype\":null,\"title\":null,\"ts\":\"2020-08-27 14:52:44.982794+0000\",\"type\":\"IAP OffChain\"},{\"TxID\":\"0x1120fb31a2cbe20a11c8251770ea53f9ee61d190919e91fe48c944c7a0aca612\",\"amount\":11000000000000000000,\"app\":null,\"block\":0,\"bonus\":null,\"description\":\"You will receive APPC-C when you reach a new gamification level\",\"fee\":0,\"icon\":\"https://apichain.dev.catappult.io/appc/icons/bonus.png\",\"icon_small\":null,\"operations\":[],\"perk\":\"GAMIFICATION_LEVEL_UP\",\"processed_time\":\"2020-08-27 14:32:32.844272+0000\",\"receiver\":\"0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f\",\"sender\":\"0x31a16adf2d5fc73f149fbb779d20c036678b1bbd\",\"sku\":null,\"status\":\"SUCCESS\",\"subtype\":\"perk_bonus\",\"title\":\"Level Up Perk\",\"ts\":\"2020-08-27 14:32:32.676590+0000\",\"type\":\"bonus\"},{\"TxID\":\"0x20a71a4c8dc44d797e5736163e66f1e9eb983b2a14473f3a78f94fe3a46e6525\",\"amount\":31282226238000000000,\"app\":null,\"block\":0,\"bonus\":10.0,\"description\":null,\"fee\":0,\"icon\":\"https://apichain.dev.catappult.io/appc/icons/bonus.png\",\"icon_small\":null,\"operations\":[],\"perk\":null,\"processed_time\":\"2020-08-27 14:32:32.776814+0000\",\"receiver\":\"0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f\",\"sender\":\"0x31a16adf2d5fc73f149fbb779d20c036678b1bbd\",\"sku\":null,\"status\":\"SUCCESS\",\"subtype\":null,\"title\":null,\"ts\":\"2020-08-27 14:32:32.594474+0000\",\"type\":\"bonus\"},{\"TxID\":\"0x259cf0e3447814dd6d80a87ea6cb911f9435712466306e4d135011fb4c715933\",\"amount\":312822262380000000000,\"app\":null,\"block\":0,\"bonus\":null,\"description\":null,\"fee\":0,\"icon\":\"https://apichain.dev.catappult.io/appc/icons/topup.png\",\"icon_small\":null,\"operations\":[],\"perk\":null,\"processed_time\":\"2020-08-27 14:32:32.487951+0000\",\"receiver\":\"0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f\",\"sender\":\"0x31a16adf2d5fc73f149fbb779d20c036678b1bbd\",\"sku\":null,\"status\":\"SUCCESS\",\"subtype\":null,\"title\":null,\"ts\":\"2020-08-27 14:32:32.444254+0000\",\"type\":\"Topup OffChain\"}]}\n"
    val objectMapper = ObjectMapper()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    relatedWallet = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd"
    objectMapper.dateFormat = dateFormat
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    history = objectMapper.readValue(transactionsJson, WalletHistory::class.java)
    transactionMapper = TransactionsMapper()
  }

  @Test
  fun transactionMapTest() {
    history.result?.forEachIndexed { index, tx ->
      val transaction = transactionMapper.map(tx, relatedWallet)
      val testTransaction = when (index) {
        0 -> createIapBonusTransaction()
        1 -> createIapTransaction()
        2 -> createPerkBonusTransaction()
        3 -> createTopUpBonusTransaction()
        4 -> createTopUpTransaction()
        else -> null
      }

      Assert.assertNotNull(testTransaction)
      Assert.assertEquals(testTransaction!!.transactionId, transaction.transactionId)
      Assert.assertEquals(testTransaction.type, transaction.type)
      Assert.assertEquals(testTransaction.subType, transaction.subType)
      Assert.assertEquals(testTransaction.title, transaction.title)
      Assert.assertEquals(testTransaction.cardDescription, transaction.cardDescription)
      Assert.assertEquals(testTransaction.perk, transaction.perk)
      Assert.assertEquals(testTransaction.approveTransactionId, transaction.approveTransactionId)
      Assert.assertNotNull(transaction.timeStamp)
      Assert.assertNotNull(transaction.processedTime)
      Assert.assertEquals(testTransaction.status.name, transaction.status.name)
      Assert.assertEquals(testTransaction.value, transaction.value)
      Assert.assertEquals(testTransaction.from, transaction.from)
      Assert.assertEquals(testTransaction.to, transaction.to)
      Assert.assertEquals(testTransaction.details!!.sourceName, transaction.details!!.sourceName)
      Assert.assertEquals(testTransaction.details!!.icon.uri, transaction.details!!.icon.uri)
      Assert.assertEquals(testTransaction.details!!.description, transaction.details!!.description)
      Assert.assertEquals(testTransaction.currency, transaction.currency)
      Assert.assertEquals(testTransaction.operations!!.size, transaction.operations!!.size)

    }
  }

  private fun createTopUpTransaction(): TransactionEntity {
    return TransactionEntity(
        "0x259cf0e3447814dd6d80a87ea6cb911f9435712466306e4d135011fb4c715933",
        relatedWallet,
        null,
        null,
        TransactionEntity.TransactionType.TOP_UP, null, null,
        null, null,
        1598535596254,
        1598535639951, TransactionEntity.TransactionStatus.SUCCESS, "312822262380000000000", "APPC",
        "30", "EUR",
        "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        "0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f",
        TransactionDetailsEntity(TransactionDetailsEntity.Icon(
            TransactionDetailsEntity.Type.URL,
            "https://apichain.dev.catappult.io/appc/icons/topup.png"),
            null, null), Collections.emptyList(), "1637599586285")
  }

  private fun createTopUpBonusTransaction(): TransactionEntity {
    return TransactionEntity(
        "0x20a71a4c8dc44d797e5736163e66f1e9eb983b2a14473f3a78f94fe3a46e6525",
        relatedWallet,
        null,
        null,
        TransactionEntity.TransactionType.BONUS, null, null,
        null, null,
        1598535746474,
        1598535928814, TransactionEntity.TransactionStatus.SUCCESS, "31282226238000000000", "APPC",
        "30", "EUR",
        "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        "0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f",
        TransactionDetailsEntity(TransactionDetailsEntity.Icon(
            TransactionDetailsEntity.Type.URL,
            "https://apichain.dev.catappult.io/appc/icons/bonus.png"),
            "10", null), Collections.emptyList(), "1637599586285")
  }

  private fun createPerkBonusTransaction(): TransactionEntity {
    return TransactionEntity(
        "0x1120fb31a2cbe20a11c8251770ea53f9ee61d190919e91fe48c944c7a0aca612",
        relatedWallet,
        null,
        TransactionEntity.Perk.GAMIFICATION_LEVEL_UP,
        TransactionEntity.TransactionType.BONUS, null, TransactionEntity.SubType.PERK_PROMOTION,
        "Level Up Perk", "You will receive APPC-C when you reach a new gamification level",
        1598535828590,
        1598535996272, TransactionEntity.TransactionStatus.SUCCESS, "11000000000000000000", "APPC",
        "10", "EUR",
        "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        "0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f",
        TransactionDetailsEntity(TransactionDetailsEntity.Icon(
            TransactionDetailsEntity.Type.URL,
            "https://apichain.dev.catappult.io/appc/icons/bonus.png"),
            null, null), Collections.emptyList(), "1637599586285")
  }

  private fun createIapTransaction(): TransactionEntity {
    return TransactionEntity(
        "0xa96d4160232abbdb0c8b0e43b941a679005dfe178fd0726d9836c47e057d8223",
        relatedWallet,
        null,
        null,
        TransactionEntity.TransactionType.IAP_OFFCHAIN, null, null, null, null, 1598537346794,
        1598536519482, TransactionEntity.TransactionStatus.SUCCESS, "1000000000000000000", "APPC",
        "10", "EUR",
        "0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f",
        "0x123c2124b7f2c18b502296ba884d9cde201f1c32",
        TransactionDetailsEntity(TransactionDetailsEntity.Icon(
            TransactionDetailsEntity.Type.URL,
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png"),
            "Appcoins Trivial Drive demo sample", "gas"), Collections.emptyList(), "1637599586285")
  }

  private fun createIapBonusTransaction(): TransactionEntity {
    return TransactionEntity(
        "0xfec381f4943569add55b55cd75bef5308a1c843b28aa5b5fc75f3bd2be6dacc9",
        relatedWallet,
        null,
        null,
        TransactionEntity.TransactionType.BONUS, null, null, null, null, 1598536660586,
        1598536807269, TransactionEntity.TransactionStatus.SUCCESS, "115000000000000000", "APPC",
        "10", "EUR",
        "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        "0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f",
        TransactionDetailsEntity(TransactionDetailsEntity.Icon(
            TransactionDetailsEntity.Type.URL,
            "https://apichain.dev.catappult.io/appc/icons/bonus.png"),
            "11.5", "gas"), Collections.emptyList(), "1637599586285")
  }

  @Test
  @Throws(ParseException::class)
  fun dateFormatTest() {
    val receiveDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val date = receiveDateFormat.parse("2019-09-17 11:34:21.563408+0000")
    println(receiveDateFormat.format(date!!))
  }
}