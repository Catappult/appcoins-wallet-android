package com.asfoundation.wallet.promotions

import com.appcoins.wallet.bdsbilling.repository.BdsApiResponseMapper
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.TransactionsResponse
import com.appcoins.wallet.bdsbilling.repository.entity.Gateway
import com.appcoins.wallet.bdsbilling.repository.entity.Metadata
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.promotions.voucher.VoucherSkuItem
import com.asfoundation.wallet.promotions.voucher.VoucherSkuModelList
import com.asfoundation.wallet.promotions.voucher.VoucherTransactionModel
import com.asfoundation.wallet.util.Error
import com.asfoundation.wallet.vouchers.VouchersRepository
import com.asfoundation.wallet.vouchers.VouchersRepositoryImpl
import com.asfoundation.wallet.vouchers.VouchersResponseMapper
import com.asfoundation.wallet.vouchers.api.*
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class VouchersRepositoryTest {
  companion object {
    const val TEST_PACKAGE_NAME = "com.appcoins.trivialdrivesample.test"
    const val TEST_TRANSACTION_HASH =
        "0xaaabbbccc88ec8f5155d5an1456fb0fec5c09b1k383a353056d3cd2919b054fd"
    const val TEST_WALLET_ADDRESS =
        "0xaabbbcccbf565488ec8f5155d5am1456fb0fec5c09b1e383a353056d3cd2919b054fd"
    const val TEST_WALLET_SIGNATURE =
        "aaabbbcccc2fe9d7f731c19596a361a76bc5360107a54c4e046aae8314e6775ed408e306a9e23e0cd2dabe00"
  }

  @Mock
  lateinit var api: VouchersApi

  @Mock
  lateinit var bdsApi: RemoteRepository.BdsApi

  @Mock
  lateinit var bdsApiSecondary: BdsApiSecondary

  @Mock
  lateinit var logger: Logger

  private lateinit var vouchersRepository: VouchersRepository

  private lateinit var successVoucherAppListResponse: VoucherAppListResponse
  private lateinit var successSkuListResponse: VoucherSkuListResponse
  private lateinit var successTransactionResponse: TransactionsResponse

  @Before
  fun setup() {
    vouchersRepository = VouchersRepositoryImpl(api, VouchersResponseMapper(), logger,
        RemoteRepository(bdsApi, BdsApiResponseMapper(), bdsApiSecondary))
    val appVoucherList = listOf(
        VoucherAppResponse(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        VoucherAppResponse(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        VoucherAppResponse(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        VoucherAppResponse(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        VoucherAppResponse(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        VoucherAppResponse(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true))
    successVoucherAppListResponse = VoucherAppListResponse("", "", appVoucherList)
    val skuResponseList = listOf(
        VoucherSkuResponse("100_diamonds", "100 Diamonds", Price("1.0", "USD", "$", "14.0")),
        VoucherSkuResponse("200_diamonds", "200 Diamonds", Price("2.0", "USD", "$", "28.0")),
        VoucherSkuResponse("300_diamonds", "300 Diamonds", Price("3.0", "USD", "$", "42.0")),
        VoucherSkuResponse("400_diamonds", "400 Diamonds", Price("4.0", "USD", "$", "56.0")),
        VoucherSkuResponse("500_diamonds", "500 Diamonds", Price("5.0", "USD", "$", "70.0"))
    )
    successSkuListResponse = VoucherSkuListResponse(null, null, skuResponseList)
    successTransactionResponse =
        TransactionsResponse(listOf(Transaction("123", Transaction.Status.COMPLETED,
            Gateway(Gateway.Name.myappcoins, "myappcoins", ""), "0x123", null, null, "VOUCHER",
            Metadata(com.appcoins.wallet.bdsbilling.repository.entity.Voucher("12345678",
                "https://game.com/redeem")))))
  }

  @Test
  fun getVoucherAppsSuccessTest() {
    Mockito.`when`(api.getAppsWithAvailableVouchers())
        .thenReturn(Single.just(successVoucherAppListResponse))

    val appVoucherList = vouchersRepository.getVoucherApps()
        .blockingGet()

    Assert.assertEquals(appVoucherList, getSuccessVoucherListModel())
  }

  @Test
  fun getVoucherAppsGenericErrorTest() {
    Mockito.`when`(api.getAppsWithAvailableVouchers())
        .thenReturn(Single.error(Throwable("Error")))

    val appVoucherList = vouchersRepository.getVoucherApps()
        .blockingGet()

    Assert.assertEquals(appVoucherList,
        VoucherListModel(Error(hasError = true, isNoNetwork = false)))
  }

  @Test
  fun getVoucherAppsNetworkErrorTest() {
    Mockito.`when`(api.getAppsWithAvailableVouchers())
        .thenReturn(Single.error(IOException()))

    val appVoucherList = vouchersRepository.getVoucherApps()
        .blockingGet()

    Assert.assertEquals(appVoucherList,
        VoucherListModel(Error(hasError = true, isNoNetwork = true)))
  }

  @Test
  fun getVoucherSkuListSuccessTest() {
    Mockito.`when`(api.getVouchersForPackage(TEST_PACKAGE_NAME))
        .thenReturn(Single.just(successSkuListResponse))

    val skuList = vouchersRepository.getVoucherSkuList(TEST_PACKAGE_NAME)
        .blockingGet()

    Assert.assertEquals(skuList, getSuccessSkuList())
  }

  @Test
  fun getVoucherGenericErrorTest() {
    Mockito.`when`(api.getVouchersForPackage(TEST_PACKAGE_NAME))
        .thenReturn(Single.error(Throwable("Error")))

    val skuList = vouchersRepository.getVoucherSkuList(TEST_PACKAGE_NAME)
        .blockingGet()

    Assert.assertEquals(skuList, VoucherSkuModelList(Error(hasError = true, isNoNetwork = false)))
  }

  @Test
  fun getVoucherNetworkErrorTest() {
    Mockito.`when`(api.getVouchersForPackage(TEST_PACKAGE_NAME))
        .thenReturn(Single.error(IOException()))

    val skuList = vouchersRepository.getVoucherSkuList(TEST_PACKAGE_NAME)
        .blockingGet()

    Assert.assertEquals(skuList, VoucherSkuModelList(Error(hasError = true, isNoNetwork = true)))
  }

  @Test
  fun getVoucherDataSuccessTest() {
    Mockito.`when`(
        bdsApi.getAppcoinsTransactionByHash(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE,
            TEST_TRANSACTION_HASH))
        .thenReturn(Single.just(successTransactionResponse))

    val voucherData =
        vouchersRepository.getVoucherTransactionData(TEST_TRANSACTION_HASH, TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE)
            .blockingGet()

    Assert.assertEquals(voucherData, getSuccessVoucherData())
  }

  @Test
  fun getVoucherDataGenericErrorTest() {
    Mockito.`when`(
        bdsApi.getAppcoinsTransactionByHash(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE,
            TEST_TRANSACTION_HASH))
        .thenReturn(Single.error(Throwable("Error")))

    val voucherData =
        vouchersRepository.getVoucherTransactionData(TEST_TRANSACTION_HASH, TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE)
            .blockingGet()

    Assert.assertEquals(voucherData,
        VoucherTransactionModel(null, null, Error(hasError = true, isNoNetwork = false)))
  }

  @Test
  fun getVoucherDataNetworkErrorTest() {
    Mockito.`when`(
        bdsApi.getAppcoinsTransactionByHash(TEST_WALLET_ADDRESS, TEST_WALLET_SIGNATURE,
            TEST_TRANSACTION_HASH))
        .thenReturn(Single.error(IOException()))

    val voucherData =
        vouchersRepository.getVoucherTransactionData(TEST_TRANSACTION_HASH, TEST_WALLET_ADDRESS,
            TEST_WALLET_SIGNATURE)
            .blockingGet()

    Assert.assertEquals(voucherData,
        VoucherTransactionModel(null, null, Error(hasError = true, isNoNetwork = true)))
  }

  private fun getSuccessVoucherListModel(): VoucherListModel {
    return VoucherListModel(listOf(
        Voucher(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher(TEST_PACKAGE_NAME, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
            true)))
  }

  private fun getSuccessSkuList(): VoucherSkuModelList {
    return VoucherSkuModelList(listOf(
        VoucherSkuItem("100_diamonds", "100 Diamonds",
            com.asfoundation.wallet.promotions.voucher.Price(1.0, "USD", "$", 14.0)),
        VoucherSkuItem("200_diamonds", "200 Diamonds",
            com.asfoundation.wallet.promotions.voucher.Price(2.0, "USD", "$", 28.0)),
        VoucherSkuItem("300_diamonds", "300 Diamonds",
            com.asfoundation.wallet.promotions.voucher.Price(3.0, "USD", "$", 42.0)),
        VoucherSkuItem("400_diamonds", "400 Diamonds",
            com.asfoundation.wallet.promotions.voucher.Price(4.0, "USD", "$", 56.0)),
        VoucherSkuItem("500_diamonds", "500 Diamonds",
            com.asfoundation.wallet.promotions.voucher.Price(5.0, "USD", "$", 70.0))
    ))
  }

  private fun getSuccessVoucherData(): VoucherTransactionModel {
    return VoucherTransactionModel("12345678", "https://game.com/redeem")
  }
}