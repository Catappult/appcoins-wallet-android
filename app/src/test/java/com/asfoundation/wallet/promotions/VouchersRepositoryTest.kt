package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.util.Error
import com.asfoundation.wallet.vouchers.VouchersRepository
import com.asfoundation.wallet.vouchers.VouchersRepositoryImpl
import com.asfoundation.wallet.vouchers.VouchersResponseMapper
import com.asfoundation.wallet.vouchers.api.VoucherAppListResponse
import com.asfoundation.wallet.vouchers.api.VoucherAppResponse
import com.asfoundation.wallet.vouchers.api.VouchersApi
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
  @Mock
  lateinit var api: VouchersApi

  private lateinit var vouchersRepository: VouchersRepository

  private lateinit var successVoucherAppListResponse: VoucherAppListResponse

  @Before
  fun setup() {
    vouchersRepository = VouchersRepositoryImpl(api, VouchersResponseMapper())
    val appVoucherList = listOf(
        VoucherAppResponse("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        VoucherAppResponse("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        VoucherAppResponse("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        VoucherAppResponse("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        VoucherAppResponse("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true))
    successVoucherAppListResponse = VoucherAppListResponse("", "", appVoucherList)
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
  fun getAppsWithVouchersErrorTest() {
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

  fun getSuccessVoucherListModel(): VoucherListModel {
    return VoucherListModel(listOf(
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", true),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png", false),
        Voucher("com.appcoins.trivialdrivesample.test", "Trivial Drive Sample",
            "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png",
            true)))
  }
}