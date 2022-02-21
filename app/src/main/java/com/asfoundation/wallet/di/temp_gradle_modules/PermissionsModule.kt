package com.asfoundation.wallet.di.temp_gradle_modules

import android.content.Context
import androidx.room.Room
import com.appcoins.wallet.permissions.Permissions
import com.asfoundation.wallet.permissions.repository.PermissionRepository
import com.asfoundation.wallet.permissions.repository.PermissionsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class PermissionsModule {

  @Singleton
  @Provides
  fun providesPermissions(@ApplicationContext context: Context): Permissions {
    return Permissions(PermissionRepository(
        Room.databaseBuilder(context.applicationContext, PermissionsDatabase::class.java,
            "permissions_database")
            .build()
            .permissionsDao()))
  }
}