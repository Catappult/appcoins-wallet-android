package com.asfoundation.wallet.repository

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.widget.Toast
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.entity.ServiceErrorException
import com.asfoundation.wallet.util.KS
import com.asfoundation.wallet.util.KS.ANDROID_KEY_STORE
import com.wallet.pwd.trustapp.PasswordManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.util.*
import javax.inject.Inject

@BoundTo(supertype = PasswordStore::class)
class TrustPasswordStore @Inject constructor(@ApplicationContext private val context: Context,
                                             private val logger: Logger
) :
    PasswordStore {
  companion object {
    private val TAG = TrustPasswordStore::class.java.simpleName
    private const val DEFAULT_WALLET = "0x123456789"
  }

  init {
    migrate()
  }

  private fun migrate() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return
    }
    val pref =
        PreferenceManager.getDefaultSharedPreferences(context)
    val passwords = pref.all
    for (key in passwords.keys) {
      if (key.contains("-pwd")) {
        val address = key.replace("-pwd", "")
        try {
          KS.put(context, address.toLowerCase(), PasswordManager.getPassword(address, context))
        } catch (ex: Exception) {
          Toast.makeText(context, "Could not process passwords.", Toast.LENGTH_LONG)
              .show()
          ex.printStackTrace()
        }
      }
    }
  }

  override fun getPassword(address: String): Single<String> {
    return Single.fromCallable {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return@fromCallable String(KS.get(context, address))
      } else {
        return@fromCallable PasswordManager.getPassword(address, context)
      }
    }
        .onErrorResumeNext { throwable: Throwable? ->
          logError(throwable)
          getPasswordFallBack(address)
        }
  }

  override fun setPassword(address: String, password: String): Completable {
    return Completable.fromAction {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        KS.put(context, address, password)
      } else {
        try {
          PasswordManager.setPassword(address, password, context)
        } catch (e: Exception) {
          val exception = ServiceErrorException(ServiceErrorException.KEY_STORE_ERROR)
          logger.log(TAG, exception)
          throw exception
        }
      }
    }
  }

  override fun generatePassword(): Single<String> {
    return Single.fromCallable {
      val bytes = ByteArray(256)
      val random = SecureRandom()
      random.nextBytes(bytes)
      String(bytes)
    }
  }

  override fun setBackUpPassword(masterPassword: String): Completable {
    return setPassword(DEFAULT_WALLET, masterPassword)
  }

  private fun getPasswordFallBack(walletAddress: String): Single<String> {
    return Single.fromCallable {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
          return@fromCallable String(KS.get(context, DEFAULT_WALLET))
        } catch (ex: Exception) {
          logger.log(TAG, ex.message, ex)
          val exception = ServiceErrorException(ServiceErrorException.KEY_STORE_ERROR,
              "Failed to get the password from the store.")
          logError(exception)
          throw exception
        }
      } else {
        try {
          return@fromCallable PasswordManager.getPassword(DEFAULT_WALLET, context)
        } catch (ex: Exception) {
          logger.log(TAG, ex.message, ex)
          val exception = ServiceErrorException(ServiceErrorException.KEY_STORE_ERROR,
              "Failed to get the password from the password manager.")
          logError(exception)
          throw exception
        }
      }
    }
        .flatMap { password: String ->
          setPassword(walletAddress, password).andThen(
              Single.just(password))
        }
  }

  private fun logError(t: Throwable?) {
    logger.log(TAG, t?.message, t)
    try {
      val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
      keyStore.load(null)
      val strBuilder = StringBuilder()
      strBuilder.append("List of alias available in the keystore: ")
      val enumeration: Enumeration<String> = keyStore.aliases()
      while (enumeration.hasMoreElements()) {
        val alias: String = enumeration.nextElement()
        strBuilder.append("[$alias] ")
      }
      logger.log(TAG, strBuilder.toString())
    } catch (e: Exception) {
      when (e) {
        is KeyStoreException -> {
          logger.log(TAG, "Failed to get Android keystore or aliases from keystore", e)
        }
        is CertificateException,
        is IOException,
        is NoSuchAlgorithmException -> {
          logger.log(TAG, "Failed to load keystore", e)
        }
        else -> {
          logger.log(TAG, "Failed for unknown reason", e)
          throw e
        }
      }
    }
  }
}