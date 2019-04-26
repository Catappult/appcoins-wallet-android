package com.asfoundation.wallet.billing.partners

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.util.DeviceInfo
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
  lateinit var api: BdsPartnersApi
  @Mock
  lateinit var deviceInfo: DeviceInfo

  private lateinit var scheduler: TestScheduler
  private lateinit var partnerAddressService: AddressService

  companion object {
    private const val APP_PACKAGE_NAME = "com.game.app"
    private const val INSTALLER_PACKAGE_NAME = "com.store.app"
    private const val INSTALLER_WALLET_ADDRESS = "0xc41b4160b63d1f9488937c41b4160b63d1f94889"
    private const val DEVICE_MANUFACTURER = "manufacturer"
    private const val DEVICE_MODEL = "model"
  }

  @Before
  fun setUp() {
    `when`(deviceInfo.manufacturer).thenReturn(DEVICE_MANUFACTURER)
    `when`(deviceInfo.model).thenReturn(DEVICE_MODEL)

    `when`(installerService.getInstallerPackageName(APP_PACKAGE_NAME)).thenReturn(
        Single.just(INSTALLER_PACKAGE_NAME))

    `when`(walletAddressService.getStoreWalletForPackage(INSTALLER_PACKAGE_NAME)).thenReturn(
        Single.just(INSTALLER_WALLET_ADDRESS))

    `when`(walletAddressService.getOemWalletForPackage(INSTALLER_PACKAGE_NAME,
        deviceInfo.manufacturer,
        deviceInfo.model)).thenReturn(
        Single.just(INSTALLER_WALLET_ADDRESS))

    scheduler = TestScheduler()
    partnerAddressService = PartnerAddressService(installerService, walletAddressService, deviceInfo)
  }

  @Test
  fun getStoreWalletAddress() {
    val observer = TestObserver<String>()

    partnerAddressService.getStoreAddressForPackage(APP_PACKAGE_NAME).subscribe(observer)
    scheduler.triggerActions()

    observer.assertValues(INSTALLER_WALLET_ADDRESS)
    observer.assertNoErrors()
  }

  @Test
  fun getOemWalletAddress() {
    val observer = TestObserver<String>()

    partnerAddressService.getOemAddressForPackage(APP_PACKAGE_NAME).subscribe(observer)
    scheduler.triggerActions()

    observer.assertValues(INSTALLER_WALLET_ADDRESS)
    observer.assertNoErrors()
  }

  @Test
  fun getDefaultWalletAddressOnError() {
    `when`(api.getStoreWallet(INSTALLER_PACKAGE_NAME)).thenAnswer { Single.just("") }

    walletAddressService = PartnerWalletAddressService(api, BuildConfig.DEFAULT_STORE_ADDRESS,
        BuildConfig.DEFAULT_OEM_ADDRESS)
    partnerAddressService = PartnerAddressService(installerService, walletAddressService, deviceInfo)

    val testStoreWalletAddress = TestObserver<String>()
    partnerAddressService.getStoreAddressForPackage(APP_PACKAGE_NAME).subscribe(testStoreWalletAddress)
    testStoreWalletAddress.assertNoErrors().assertValue(BuildConfig.DEFAULT_STORE_ADDRESS).assertComplete()

    val testOemWalletAddress = TestObserver<String>()
    partnerAddressService.getOemAddressForPackage(APP_PACKAGE_NAME).subscribe(testOemWalletAddress)
    testOemWalletAddress.assertNoErrors().assertValue(BuildConfig.DEFAULT_OEM_ADDRESS).assertComplete()
  }
}