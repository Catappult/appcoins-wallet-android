package com.asfoundation.wallet.di

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.adyen.checkout.core.api.Environment
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.core.utils.jvm_common.LogReceiver
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.utils.jvm_common.SyncExecutor
import com.aptoide.apk.injector.extractor.data.Extractor
import com.aptoide.apk.injector.extractor.data.ExtractorV1
import com.aptoide.apk.injector.extractor.data.ExtractorV2
import com.aptoide.apk.injector.extractor.domain.IExtract
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxyBuilder
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxySdk
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.appcoins.wallet.core.analytics.analytics.TaskTimer
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.logging.DebugReceiver
import com.asfoundation.wallet.logging.WalletLogger
import com.asfoundation.wallet.repository.Web3jProvider
import com.asfoundation.wallet.ui.iab.AppCoinsOperationRepository
import com.asfoundation.wallet.ui.iab.AppInfoProvider
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver
import com.asfoundation.wallet.ui.iab.ImageSaver
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import com.asfoundation.wallet.ui.iab.raiden.NonceObtainerFactory
import com.asfoundation.wallet.ui.iab.raiden.Web3jNonceProvider
import com.appcoins.wallet.core.utils.properties.MiscProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.schedulers.Schedulers
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
internal class AppModule {

  @Singleton
  @Provides
  fun provideGson() = Gson()

  @Provides
  fun providesClipboardManager(@ApplicationContext context: Context): ClipboardManager {
    return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  }

  @Singleton
  @Provides
  fun provideLogger(): Logger {
    val receivers = ArrayList<LogReceiver>()
    if (BuildConfig.DEBUG) {
      receivers.add(DebugReceiver())
    }
    return WalletLogger(receivers)
  }

  @Provides
  fun provideNonceObtainer(web3jProvider: Web3jProvider): MultiWalletNonceObtainer {
    return MultiWalletNonceObtainer(NonceObtainerFactory(30000, Web3jNonceProvider(web3jProvider)))
  }

  @Singleton
  @Provides
  fun provideAdsContractAddressSdk(): AppCoinsAddressProxySdk =
    AppCoinsAddressProxyBuilder().createAddressProxySdk()  //read only?

  @Provides
  @Singleton
  fun provideInAppPurchaseDataSaver(
    @ApplicationContext context: Context,
    operationSources: OperationSources,
    appCoinsOperationRepository: AppCoinsOperationRepository
  ): AppcoinsOperationsDataSaver {
    return AppcoinsOperationsDataSaver(
      operationSources.sources, appCoinsOperationRepository,
      AppInfoProvider(context, ImageSaver(context.filesDir.toString() + "/app_icons/")),
      Schedulers.io(), CompositeDisposable()
    )
  }

  @Provides
  fun provideAdyenEnvironment(): Environment {
    return if (BuildConfig.DEBUG) {
      Environment.TEST
    } else {
      Environment.EUROPE
    }
  }

  @Singleton
  @Provides
  fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(context)
  }

  @Provides
  fun providesObjectMapper(): ObjectMapper {
    return ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  }

  @Singleton
  @Provides
  fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
    return context.applicationContext.getSystemService(
      Context.NOTIFICATION_SERVICE
    ) as NotificationManager
  }

  @Singleton
  @Provides
  @Named("heads_up")
  fun provideHeadsUpNotificationBuilder(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager
  ): NotificationCompat.Builder {
    val builder: NotificationCompat.Builder
    val channelId = "notification_channel_heads_up_id"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channelName: CharSequence = "Notification channel"
      val importance = NotificationManager.IMPORTANCE_HIGH
      val notificationChannel = NotificationChannel(channelId, channelName, importance)
      builder = NotificationCompat.Builder(context, channelId)
      notificationManager.createNotificationChannel(notificationChannel)
    } else {
      builder = NotificationCompat.Builder(context, channelId)
      builder.setVibrate(LongArray(0))
    }
    return builder.setContentTitle(context.getString(R.string.app_name))
      .setSmallIcon(R.drawable.ic_appcoins_notification_icon)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setAutoCancel(true)
  }

  @Singleton
  @Provides
  fun provideIExtract(): IExtract {
    return Extractor(ExtractorV1(), ExtractorV2())
  }

  @Singleton
  @Provides
  fun providePackageManager(@ApplicationContext context: Context): PackageManager =
    context.packageManager

  @Provides
  @Named("local_version_code")
  fun provideLocalVersionCode(
    @ApplicationContext context: Context,
    packageManager: PackageManager
  ): Int {
    @Suppress("DEPRECATION")
    return try {
      packageManager.getPackageInfo(context.packageName, 0).versionCode
    } catch (e: PackageManager.NameNotFoundException) {
      -1
    }
  }

  @Provides
  @Named("device-sdk")
  fun provideDeviceSdk(): Int = Build.VERSION.SDK_INT

  @Provides
  @Named("package-name")
  fun providePackageName(@ApplicationContext context: Context): String = context.packageName

  @Provides
  @Named("payment-gas-limit")
  fun providePaymentGasLimit(): BigDecimal = BigDecimal(MiscProperties.PAYMENT_GAS_LIMIT)

  @Provides
  fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
    context.contentResolver

  @Provides
  fun provideCompositeDisposable(): CompositeDisposable = CompositeDisposable()

  @Singleton
  @Provides
  fun providesDefaultNetwork(): NetworkInfo {
    return if (BuildConfig.DEBUG) {
      NetworkInfo(
        C.ROPSTEN_NETWORK_NAME, C.ETH_SYMBOL,
        "https://ropsten.infura.io/v3/${BuildConfig.INFURA_API_KEY_ROPSTEN}",
        "https://ropsten.trustwalletapp.com/", "https://ropsten.etherscan.io/tx/", 3, false
      )
    } else {
      NetworkInfo(
        C.ETHEREUM_NETWORK_NAME, C.ETH_SYMBOL,
        "https://mainnet.infura.io/v3/${BuildConfig.INFURA_API_KEY_MAIN}",
        "https://api.trustwalletapp.com/", "https://etherscan.io/tx/", 1, true
      )
    }
  }

  @Singleton
  @Provides
  fun providesWeb3j(): Web3j {
    return if (BuildConfig.DEBUG) {
      Web3j.build(HttpService("https://rinkeby.infura.io/v3/${BuildConfig.INFURA_API_KEY_RINKEBY}"))
    } else {
      Web3j.build(HttpService("https://mainnet.infura.io/v3/${BuildConfig.INFURA_API_KEY_MAIN}"))
    }
  }

  @Singleton
  @Provides
  fun providesChainID(): Long {
    return if (BuildConfig.DEBUG) {
      4L //Rinkeby Chain ID
    } else {
      1L //Mainnet Chain ID
    }
  }

  @Singleton
  @Provides
  fun providesExecutorScheduler() = ExecutorScheduler(
    SyncExecutor(
      1
    ), false)

  @Singleton
  @Provides
  fun providesBiometricManager(@ApplicationContext context: Context) =
    BiometricManager.from(context)

  @Singleton
  @Provides
  fun provideTaskTimer(): TaskTimer = TaskTimer()

  @Provides
  fun providesEwtAuthService(walletService: WalletService): EwtAuthenticatorService {
    val headerJson = JsonObject()
    headerJson.addProperty("typ", "EWT")
    return EwtAuthenticatorService(walletService, headerJson.toString())
  }
}