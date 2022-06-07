package com.asfoundation.wallet.interact

import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import com.asfoundation.wallet.update_required.use_cases.HasRequiredHardUpdateUseCase
import com.asfoundation.wallet.update_required.use_cases.HasSoftUpdateUseCase
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class AutoUpdateInteractTest {

//  @Mock
//  lateinit var autoUpdateRepository: AutoUpdateRepository
//
//  @Mock
//  private lateinit var packageManager: PackageManager
//
//  @Mock
//  private lateinit var sharePreferences: PreferencesRepositoryType
//  private lateinit var autoUpdateInteract: AutoUpdateInteract

  @Mock
  lateinit var hasSoftUpdateUseCase: HasSoftUpdateUseCase

  @Mock
  lateinit var hasRequiredHardUpdateUseCase: HasRequiredHardUpdateUseCase

  @Mock
  lateinit var buildUpdateIntentUseCase: BuildUpdateIntentUseCase

  companion object {
    private const val WALLET_VERSION_CODE = 116
    private const val DEVICE_SDK = 21
    private const val WALLET_PACKAGE_NAME = "com.appcoins.wallet"
  }

  @Before
  fun setup() {
//    autoUpdateInteract =
//      AutoUpdateInteract(
//        autoUpdateRepository, WALLET_VERSION_CODE, DEVICE_SDK,
//        WALLET_PACKAGE_NAME, packageManager, sharePreferences
//      )
  }

  @Test
  fun hasSoftUpdateTest() {
    val hasSoftUpdate1 = hasSoftUpdateUseCase(WALLET_VERSION_CODE + 1, DEVICE_SDK)
    Assert.assertEquals(hasSoftUpdate1, true)
    val hasSoftUpdate2 =
      hasSoftUpdateUseCase(WALLET_VERSION_CODE + 1, DEVICE_SDK - 1)
    Assert.assertEquals(hasSoftUpdate2, true)
    val hasNotSoftUpdate1 = hasSoftUpdateUseCase(WALLET_VERSION_CODE, 21)
    Assert.assertEquals(hasNotSoftUpdate1, false)
    val hasNotSoftUpdate2 =
      hasSoftUpdateUseCase(WALLET_VERSION_CODE + 1, DEVICE_SDK + 1)
    Assert.assertEquals(hasNotSoftUpdate2, false)
  }

  @Test
  fun hasHardUpdateTest() {
    val hasHardUpdate1 = hasRequiredHardUpdateUseCase(
      listOf(WALLET_VERSION_CODE),
      WALLET_VERSION_CODE + 1, DEVICE_SDK
    )
    Assert.assertEquals(hasHardUpdate1, true)
    val hasHardUpdate2 = hasRequiredHardUpdateUseCase(
      listOf(WALLET_VERSION_CODE),
      WALLET_VERSION_CODE + 1, DEVICE_SDK - 1
    )
    Assert.assertEquals(hasHardUpdate2, true)
    val hasNotHardUpdate1 =
      hasRequiredHardUpdateUseCase(
        Collections.emptyList(), WALLET_VERSION_CODE + 1,
        DEVICE_SDK
      )
    Assert.assertEquals(hasNotHardUpdate1, false)
    val hasNotHardUpdate2 =
      hasRequiredHardUpdateUseCase(
        listOf(WALLET_VERSION_CODE), WALLET_VERSION_CODE,
        DEVICE_SDK
      )
    Assert.assertEquals(hasNotHardUpdate2, false)
    val hasNotHardUpdate3 =
      hasRequiredHardUpdateUseCase(
        listOf(WALLET_VERSION_CODE),
        WALLET_VERSION_CODE + 1,
        DEVICE_SDK + 1
      )
    Assert.assertEquals(hasNotHardUpdate3, false)
    val hasNotHardUpdate4 =
      hasRequiredHardUpdateUseCase(
        listOf(WALLET_VERSION_CODE + 1),
        WALLET_VERSION_CODE + 1,
        DEVICE_SDK + 1
      )
    Assert.assertEquals(hasNotHardUpdate4, false)
  }

  @Test
  fun fallBackRetrieveUrlTest() {
    val url = buildUpdateIntentUseCase.retrieveRedirectUrl()
    Assert.assertEquals(
      url,
      String.format(AutoUpdateInteract.PLAY_APP_VIEW_URL, WALLET_PACKAGE_NAME)
    )
  }
}