package com.asfoundation.wallet.billing.partners

import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.asf.wallet.BuildConfig
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PartnerAddressServiceTest {
  @Mock
  lateinit var installerService: InstallerService
  @Mock
  lateinit var walletAddressService: WalletAddressService
  @Mock
  lateinit var api: BdsApiSecondary

  private lateinit var scheduler: TestScheduler
  private lateinit var partnerAddressService: AddressService
  companion object {
    private const val APP_PACKAGE_NAME = "com.game.app"
    private const val INSTALLER_PACKAGE_NAME = "com.store.app"
    private const val INSTALLER_WALLET_ADDRESS = "0xc41b4160b63d1f9488937c41b4160b63d1f94889"
  }

  @Before
  fun setUp() {
    `when`(installerService.getInstallerPackageName(APP_PACKAGE_NAME)).thenReturn(
        Single.just(INSTALLER_PACKAGE_NAME))

    `when`(walletAddressService.getWalletAddressForPackage(INSTALLER_PACKAGE_NAME)).thenReturn(
        Single.just(INSTALLER_WALLET_ADDRESS))

    scheduler = TestScheduler()
    partnerAddressService = PartnerAddressService(installerService, walletAddressService)
  }

  @Test
  fun getWalletAddress() {
    val observer = TestObserver<String>()

    partnerAddressService.getStoreAddressForPackage(APP_PACKAGE_NAME).subscribe(observer)
    scheduler.triggerActions()

    observer.assertValues(INSTALLER_WALLET_ADDRESS)
    observer.assertNoErrors()
  }

  @Test
  fun getDefaultWalletAddressOnError() {
    val observer = TestObserver<String>()

    `when`(api.getWallet(INSTALLER_PACKAGE_NAME)).thenAnswer { Single.just("") }

    walletAddressService = PartnerWalletAddressService(api, BuildConfig.DEFAULT_STORE_ADDRESS)
    partnerAddressService = PartnerAddressService(installerService, walletAddressService)

    partnerAddressService.getStoreAddressForPackage(APP_PACKAGE_NAME).subscribe(observer)
    scheduler.triggerActions()

    observer.assertValues(BuildConfig.DEFAULT_STORE_ADDRESS)
    observer.assertNoErrors()
  }
}