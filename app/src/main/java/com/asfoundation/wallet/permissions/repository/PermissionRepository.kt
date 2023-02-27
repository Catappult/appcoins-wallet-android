package com.asfoundation.wallet.permissions.repository

import com.appcoins.wallet.commons.Repository
import com.appcoins.wallet.permissions.ApplicationPermission
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class PermissionRepository(private val permissionsDao: PermissionsDao) :
  Repository<String, ApplicationPermission> {
  override fun save(key: String, value: ApplicationPermission): Completable {
    return Completable.fromAction { save(key, value) }
  }

  override fun saveSync(key: String, value: ApplicationPermission) {
    permissionsDao.insert(map(key, value))
  }

  override fun getAll(): Observable<List<ApplicationPermission>> {
    return permissionsDao.getAllAsFlowable()
        .flatMapSingle {
          Observable.fromIterable(it).map { permission -> map(permission)!! }.toList()
        }.toObservable()
  }

  override fun getAllSync(): List<ApplicationPermission> {
    return permissionsDao.getAll().map { map(it)!! }
  }

  override fun remove(key: String): Completable {
    return Completable.fromAction { removeSync(key) }
  }

  override fun removeSync(key: String) {
    permissionsDao.remove(permissionsDao.getSyncPermission(key))
  }

  override fun contains(key: String): Single<Boolean> {
    return Single.fromCallable { containsSync(key) }
  }

  override fun containsSync(key: String): Boolean {
    return permissionsDao.getSyncPermission(key) != null
  }

  override fun get(key: String): Observable<ApplicationPermission> {
    return permissionsDao.getPermission(key).map { map(it)!! }.toObservable()
  }

  override fun getSync(key: String): ApplicationPermission? {
    return map(permissionsDao.getSyncPermission(key))
  }

  private fun map(key: String,
                  applicationPermission: ApplicationPermission): PermissionEntity {
    return PermissionEntity(key,
        applicationPermission.walletAddress,
        applicationPermission.packageName,
        applicationPermission.apkSignature, applicationPermission.permissions)
  }

  private fun map(permission: PermissionEntity?): ApplicationPermission? {
    return permission?.let {
      ApplicationPermission(permission.walletAddress, permission.packageName,
          permission.apkSignature, permission.permissions)
    }
  }
}
