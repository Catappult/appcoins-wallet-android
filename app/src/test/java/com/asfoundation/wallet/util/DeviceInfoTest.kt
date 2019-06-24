package com.asfoundation.wallet.util

import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by Joao Raimundo on 26/04/2019.
 */
@RunWith(MockitoJUnitRunner::class)
class DeviceInfoTest {

  companion object {
    private const val DEVICE_MANUFACTURER = "manufacturer"
    private const val DEVICE_MODEL = "model"
  }

  private lateinit var deviceInfo: DeviceInfo
  @Before
  fun setUp() {
    deviceInfo = DeviceInfo(DEVICE_MANUFACTURER, DEVICE_MODEL)
  }

  @Test
  fun getDefaultWalletAddressOnError() {
    assertEquals(DEVICE_MANUFACTURER, deviceInfo.manufacturer)
    assertEquals(DEVICE_MODEL, deviceInfo.model)
  }
}