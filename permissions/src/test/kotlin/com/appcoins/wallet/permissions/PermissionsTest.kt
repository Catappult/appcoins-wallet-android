package com.appcoins.wallet.permissions

import com.appcoins.wallet.commons.MemoryCache
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
    permissions = Permissions(MemoryCache(BehaviorSubject.create(), ConcurrentHashMap()))
  }

  companion object {
    private const val PACKAGE_NAME = "com.appcoins.wallet"
    private const val APK_SIGNATURE = "apk signature"
    private const val WRONG_APK_SIGNATURE = "wrong apk signature"
  }

  @Test
  fun grantPermission() {
    permissions.grantPermission(PACKAGE_NAME, APK_SIGNATURE, PermissionName.WALLET)
    permissions.grantPermission(PACKAGE_NAME, APK_SIGNATURE, PermissionName.LEVEL)
    Assert.assertEquals("permissions different from expected",
        listOf(PermissionName.WALLET, PermissionName.LEVEL),
        permissions.getPermissions(PACKAGE_NAME, APK_SIGNATURE))
  }

  @Test(expected = SecurityException::class)
  fun grantPermissionWrongApkSignature() {
    permissions.grantPermission(PACKAGE_NAME, APK_SIGNATURE, PermissionName.WALLET)
    permissions.grantPermission(PACKAGE_NAME, WRONG_APK_SIGNATURE, PermissionName.LEVEL)
  }

  @Test(expected = SecurityException::class)
  fun getPermissionWrongApkSignature() {
    permissions.grantPermission(PACKAGE_NAME, APK_SIGNATURE, PermissionName.WALLET)
    permissions.getPermissions(PACKAGE_NAME, WRONG_APK_SIGNATURE)
  }

  @Test
  fun getPermissionNotExistentApp() {
    Assert.assertEquals(emptyList<PermissionName>(),
        permissions.getPermissions(PACKAGE_NAME, WRONG_APK_SIGNATURE))
  }
}