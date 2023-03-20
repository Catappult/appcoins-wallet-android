package com.appcoins.wallet.permissions

import com.appcoins.wallet.core.utils.jvm_common.MemoryCache
import io.reactivex.subjects.BehaviorSubject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

class PermissionsTest {
  private lateinit var permissions: Permissions

  @Before
  @Throws(Exception::class)
  fun setUp() {
    permissions = Permissions(
      MemoryCache(
        BehaviorSubject.create(),
        ConcurrentHashMap()
      )
    )
  }

  companion object {
    private const val PACKAGE_NAME = "com.appcoins.wallet"
    private const val APK_SIGNATURE = "apk signature"
    private const val WRONG_APK_SIGNATURE = "wrong apk signature"
    private const val WALLET_ADDRESS = "wallet_address"
  }

  @Test
  fun grantPermission() {
    permissions.grantPermission(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE,
        PermissionName.WALLET_ADDRESS)
    permissions.grantPermission(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE, PermissionName.LEVEL)
    Assert.assertEquals("permissions different from expected",
        listOf(PermissionName.WALLET_ADDRESS, PermissionName.LEVEL),
        permissions.getPermissions(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE))
  }

  @Test
  fun grantPermissionMultipleTimes() {
    permissions.grantPermission(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE,
        PermissionName.WALLET_ADDRESS)
    permissions.grantPermission(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE,
        PermissionName.WALLET_ADDRESS)
    permissions.grantPermission(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE, PermissionName.LEVEL)
    Assert.assertEquals("permissions different from expected",
        listOf(PermissionName.WALLET_ADDRESS, PermissionName.LEVEL),
        permissions.getPermissions(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE))
  }

  @Test(expected = SecurityException::class)
  fun grantPermissionWrongApkSignature() {
    permissions.grantPermission(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE,
        PermissionName.WALLET_ADDRESS)
    permissions.grantPermission(WALLET_ADDRESS, PACKAGE_NAME, WRONG_APK_SIGNATURE,
        PermissionName.LEVEL)
  }

  @Test(expected = SecurityException::class)
  fun getPermissionWrongApkSignature() {
    permissions.grantPermission(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE,
        PermissionName.WALLET_ADDRESS)
    permissions.getPermissions(WALLET_ADDRESS, PACKAGE_NAME, WRONG_APK_SIGNATURE)
  }

  @Test
  fun getPermissionNotExistentApp() {
    Assert.assertEquals(emptyList<PermissionName>(),
        permissions.getPermissions(WALLET_ADDRESS, PACKAGE_NAME, WRONG_APK_SIGNATURE))
  }

  @Test
  fun revokePermission() {
    permissions.grantPermission(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE,
        PermissionName.WALLET_ADDRESS)
    permissions.revokePermission(WALLET_ADDRESS, PACKAGE_NAME, PermissionName.WALLET_ADDRESS)
    Assert.assertEquals(emptyList<PermissionName>(),
        permissions.getPermissions(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE))
  }

  @Test
  fun revokePermissionNotExistentApp() {
    permissions.revokePermission(WALLET_ADDRESS, PACKAGE_NAME, PermissionName.WALLET_ADDRESS)
    Assert.assertEquals(emptyList<PermissionName>(),
        permissions.getPermissions(WALLET_ADDRESS, PACKAGE_NAME, APK_SIGNATURE))
  }
}