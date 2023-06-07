package com.appcoins.wallet.core.network.base

import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.core.utils.android_common.extensions.convertToBase64
import com.google.gson.JsonObject
import io.reactivex.Single

/**
 * Variable representing in seconds the time to live interval of the authentication token.
 **/
private const val TTL_IN_SECONDS = 3600

class EwtAuthenticatorService(
  private val walletService: WalletService,
  private val header: String
) {

  private var cachedAuth: MutableMap<String, Pair<String, Long>> = HashMap()

  fun getEwtAuthentication(): Single<String> {
    return walletService.getWalletAddress()
      .map { address -> getEwtAuthentication(address) }
  }

  fun getEwtAuthenticationWithAddress(address: String): Single<String> {
    return Single.just(getEwtAuthentication(address))
  }

  @Synchronized
  fun getEwtAuthentication(address: String): String {
    return if (shouldBuildEwtAuth(address))
      getNewEwtAuthentication(address)
    else {
      cachedAuth[address]!!.first
    }
  }

  @Synchronized
  fun getNewEwtAuthentication(address: String): String {
    val currentUnixTime = System.currentTimeMillis() / 1000L
    val ewtString = buildEwtString(address, currentUnixTime)
    cachedAuth[address] = Pair(ewtString, currentUnixTime + TTL_IN_SECONDS)
    return ewtString
  }

  private fun shouldBuildEwtAuth(address: String): Boolean {
    val currentUnixTime = System.currentTimeMillis() / 1000L
    return !cachedAuth.containsKey(address) || hasExpired(
      currentUnixTime,
      cachedAuth[address]?.second
    )
  }

  private fun hasExpired(currentUnixTime: Long, ttlUnixTime: Long?): Boolean {
    return ttlUnixTime == null || currentUnixTime >= ttlUnixTime
  }

  private fun buildEwtString(address: String, currentUnixTime: Long): String {
    val header = replaceInvalidCharacters(header.convertToBase64())
    val payload = replaceInvalidCharacters(getPayload(address, currentUnixTime))
    val signedContent = walletService.signSpecificWalletAddressContent(address, payload)
      .blockingGet()
    return "Bearer $header.$payload.$signedContent".replace("[\n\r]", "")
  }

  private fun getPayload(walletAddress: String, currentUnixTime: Long): String {
    val payloadJson = JsonObject()
    payloadJson.addProperty("iss", walletAddress)
    val unixTimeWithTTL: Long = currentUnixTime + TTL_IN_SECONDS
    payloadJson.addProperty("exp", unixTimeWithTTL)
    return payloadJson.toString()
      .convertToBase64()
  }

  private fun replaceInvalidCharacters(ewtString: String): String {
    return ewtString.replace("=", "")
      .replace("+", "-")
      .replace("/", "_")
  }
}
