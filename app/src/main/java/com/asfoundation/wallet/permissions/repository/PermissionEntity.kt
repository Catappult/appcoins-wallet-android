package com.asfoundation.wallet.permissions.repository

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.appcoins.wallet.permissions.PermissionName

@Entity
data class PermissionEntity(@PrimaryKey @ColumnInfo(name = "key") val key: String,
                            @ColumnInfo(name = "wallet_address") val walletAddress: String,
                            @ColumnInfo(name = "package_name") val packageName: String,
                            @ColumnInfo(name = "apk_signature") val apkSignature: String,
                            @ColumnInfo(name = "permissions") val permissions: List<PermissionName>)
