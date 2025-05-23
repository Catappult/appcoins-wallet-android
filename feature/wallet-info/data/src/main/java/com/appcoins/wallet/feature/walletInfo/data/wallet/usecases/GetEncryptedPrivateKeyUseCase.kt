package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import android.util.Base64
import io.reactivex.Single
import org.web3j.utils.Numeric
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject

class GetEncryptedPrivateKeyUseCase @Inject constructor(
  private val getPrivateKeyUseCase: GetPrivateKeyUseCase,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase
) {

  operator fun invoke(): Single<String> {
    return getCurrentWalletUseCase()
      .flatMap { wallet ->
        getPrivateKeyUseCase(wallet.address)
          .map { key ->
            Numeric.toHexStringNoPrefixZeroPadded(key.privKey, 64)
          }
      }
  }

  private val OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"

  private fun publicKeyFromString(pemOrB64: String) = KeyFactory.getInstance("RSA")
    .generatePublic(
      X509EncodedKeySpec(
        Base64.decode(
          pemOrB64
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), ""),
          Base64.DEFAULT
        )
      )
    )

  fun encrypt(plain: String, pubKeyString: String): String {  //TODO use
    val cipher = Cipher.getInstance(OAEP)
    cipher.init(Cipher.ENCRYPT_MODE, publicKeyFromString(pubKeyString))
    val ciphertext = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
  }

}
