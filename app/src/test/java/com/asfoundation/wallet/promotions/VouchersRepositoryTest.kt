package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.promotions.voucher.VoucherSkuItem
import com.asfoundation.wallet.promotions.voucher.VoucherSkuModelList
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
    const val testPackageName = "com.appcoins.trivialdrivesample.test"
  }

  @Mock
  lateinit var api: VouchersApi

  private lateinit var vouchersRepository: VouchersRepository

  private lateinit var successVoucherAppListResponse: VoucherAppListResponse
  private lateinit var successSkuListResponse: VoucherSkuListResponse

  @Before
  fun setup() {
    vouchersRepository = VouchersRepositoryImpl(api, VouchersResponseMapper())
    val appVoucherList = listOf(
        VoucherAppResponse(testPackageName, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        VoucherAppResponse(testPackageName, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        VoucherAppResponse(testPackageName, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        VoucherAppResponse(testPackageName, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        VoucherAppResponse(testPackageName, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        VoucherAppResponse(testPackageName, "Trivial Drive Sample",
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
  }

  @Test
  fun getAppsWithVouchersSuccessTest() {
    Mockito.`when`(api.getAppsWithAvailableVouchers())
        .thenReturn(Single.just(successVoucherAppListResponse))

    val appVoucherList = vouchersRepository.getAppsWithVouchers()
        .blockingGet()

    Assert.assertEquals(appVoucherList, getSuccessVoucherListModel())
  }

  @Test
  fun getAppsWithVouchersGenericErrorTest() {
    Mockito.`when`(api.getAppsWithAvailableVouchers())
        .thenReturn(Single.error(Throwable("Error")))

    val appVoucherList = vouchersRepository.getAppsWithVouchers()
        .blockingGet()

    Assert.assertEquals(appVoucherList,
        VoucherListModel(Error(hasError = true, isNoNetwork = false)))
  }

  @Test
  fun getAppsWithVouchersNetworkErrorTest() {
    Mockito.`when`(api.getAppsWithAvailableVouchers())
        .thenReturn(Single.error(IOException()))

    val appVoucherList = vouchersRepository.getAppsWithVouchers()
        .blockingGet()

    Assert.assertEquals(appVoucherList,
        VoucherListModel(Error(hasError = true, isNoNetwork = true)))
  }

  @Test
  fun getVoucherSkuListSuccessTest() {
    Mockito.`when`(api.getVouchersForPackage(testPackageName))
        .thenReturn(Single.just(successSkuListResponse))

    val skuList = vouchersRepository.getVoucherSkuList(testPackageName)
        .blockingGet()

    Assert.assertEquals(skuList, getSuccessSkuList())
  }

  @Test
  fun getVoucherGenericErrorTest() {
    Mockito.`when`(api.getVouchersForPackage(testPackageName))
        .thenReturn(Single.error(Throwable("Error")))

    val skuList = vouchersRepository.getVoucherSkuList(testPackageName)
        .blockingGet()

    Assert.assertEquals(skuList, VoucherSkuModelList(Error(hasError = true, isNoNetwork = false)))
  }

  @Test
  fun getVoucherNetworkErrorTest() {
    Mockito.`when`(api.getVouchersForPackage(testPackageName))
        .thenReturn(Single.error(IOException()))

    val skuList = vouchersRepository.getVoucherSkuList(testPackageName)
        .blockingGet()

    Assert.assertEquals(skuList, VoucherSkuModelList(Error(hasError = true, isNoNetwork = true)))
  }

  private fun getSuccessVoucherListModel(): VoucherListModel {
    return VoucherListModel(listOf(
        Voucher(testPackageName, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher(testPackageName, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher(testPackageName, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher(testPackageName, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher(testPackageName, "Trivial Drive Sample",
            "https://pool.img.aptoide.com/appupdater/9d884f8e8d5095f79efc7915fd421b9a.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher(testPackageName, "Trivial Drive Sample",
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
}