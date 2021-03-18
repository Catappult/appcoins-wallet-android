package com.asfoundation.wallet.promotions

import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.promotions.voucher.VoucherTransactionModel
import com.asfoundation.wallet.util.Error
import com.asfoundation.wallet.vouchers.VouchersInteractor
import com.asfoundation.wallet.vouchers.VouchersRepository
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class VouchersInteractorTest {

  @Mock
  lateinit var walletService: WalletService
  private lateinit var vouchersRepository: VouchersRepository

  private lateinit var vouchersInteractor: VouchersInteractor

  private lateinit var successWalletAddressModel: WalletAddressModel

  @Before
  fun setup() {
    vouchersRepository = MockedVouchersRepository()
    vouchersInteractor = VouchersInteractor(vouchersRepository, walletService)

    successWalletAddressModel = WalletAddressModel("0x123C2124b7F2C18b502296bA884d9CDe201f1c32",
        "123C2124b7F2C18b502296bA884d9CDe201f1c32")
  }

  @Test
  fun getVoucherDataSuccessTest() {
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
        .thenReturn(Single.just(successWalletAddressModel))

    val vouchersData =
        vouchersInteractor.getVoucherData("0x123")
            .blockingGet()

    Assert.assertEquals(vouchersData,
        VoucherTransactionModel("12345678", "https://game.com/redeem"))
  }

  @Test
  fun getVoucherDataInvalidHashTest() {
    val vouchersData =
        vouchersInteractor.getVoucherData(null)
            .blockingGet()

    Assert.assertEquals(vouchersData,
        VoucherTransactionModel(Error(hasError = true, isNoNetwork = false)))
  }

  @Test
  fun getVouchersDataWalletErrorTest() {
    Mockito.`when`(walletService.getAndSignCurrentWalletAddress())
        .thenReturn(Single.error(Throwable("Error")))

    val vouchersData =
        vouchersInteractor.getVoucherData("0x123")
            .blockingGet()

    Assert.assertEquals(vouchersData,
        VoucherTransactionModel(Error(hasError = true, isNoNetwork = false)))
  }
}