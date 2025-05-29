package com.asfoundation.wallet.ui.webview_payment.usecases

import android.util.Base64
import com.appcoins.wallet.core.utils.android_common.extensions.convertToBase64Url
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetPrivateKeyUseCase
import com.asf.wallet.BuildConfig
import io.reactivex.Single
import org.web3j.utils.Numeric
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.inject.Inject

class GetEncryptedPrivateKeyUseCase @Inject constructor(
  private val getPrivateKeyUseCase: GetPrivateKeyUseCase,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase
) {

  val publicKey: String = BuildConfig.BACKEND_PUBLIC_KEY
  operator fun invoke(): Single<String> {
    return getCurrentWalletUseCase()
      .flatMap { wallet ->
        getPrivateKeyUseCase(wallet.address)
          .map { key ->
            val walletPrivate = Numeric.toHexStringNoPrefixZeroPadded(key.privKey, 64)
            rsaEncrypt(walletPrivate, pemToPublicKey(publicKey))
          }
      }
  }

  fun pemToPublicKey(pem: String): PublicKey {
    val clean = pem
      .replace("-----BEGIN PUBLIC KEY-----", "")
      .replace("-----END PUBLIC KEY-----", "")
      .replace("\\n", "")
      .replace("\n", "")
      .trim()

    val decoded = Base64.decode(clean, Base64.DEFAULT)
    val spec = X509EncodedKeySpec(decoded)
    return KeyFactory.getInstance("RSA").generatePublic(spec)
  }

  fun rsaEncrypt(plainText: String, publicKey: PublicKey): String {
    val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
    val oaepParams = OAEPParameterSpec(
      "SHA-256",
      "MGF1",
      MGF1ParameterSpec.SHA256,
      PSource.PSpecified.DEFAULT
    )
    cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams)

    val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP).convertToBase64Url()
  }

}
