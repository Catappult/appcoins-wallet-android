package com.asfoundation.wallet.feature_flags.di

import android.content.Context
import androidx.room.Room
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.networkbase.annotations.ShortTimeoutHttpClient
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.feature_flags.api.ExperimentsApi
import com.asfoundation.wallet.feature_flags.db.FeatureFlagsDao
import com.asfoundation.wallet.feature_flags.db.FeatureFlagsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FeatureFlagsModule {

  @Singleton
  @Provides
  fun providesExperimentsApi(
    @ShortTimeoutHttpClient client: OkHttpClient
  ): ExperimentsApi = Retrofit.Builder()
    .baseUrl(HostProperties.APTOIDE_WEB_SERVICES_AB_TEST_HOST)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .build().create(ExperimentsApi::class.java)

  @Singleton
  @Provides
  fun providesFeatureFlagsDatabase(@ApplicationContext context: Context): FeatureFlagsDatabase =
    Room.databaseBuilder(context, FeatureFlagsDatabase::class.java, "feature_flags_database")
      .build()

  @Singleton
  @Provides
  fun provideFeatureFlagsDao(featureFlagsDatabase: FeatureFlagsDatabase): FeatureFlagsDao =
    featureFlagsDatabase.getFeatureFlagsDao()
}