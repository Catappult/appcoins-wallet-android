package com.asfoundation.wallet.onboarding

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remembers the wallet onboarding creates only so the web login has one to start from. If the login
 * then switches to the account's wallet, that temporary wallet can be removed.
 *
 * In memory on purpose: after a process death or [MAX_AGE_MS] it's forgotten and the wallet is kept,
 * so a wallet the user may have started using is never deleted.
 */
@Singleton
class OnboardingSignInWallet @Inject constructor() {

  private var address: String? = null
  private var createdAt = 0L

  fun set(address: String, now: Long = System.currentTimeMillis()) {
    this.address = address
    createdAt = now
  }

  /** The temporary wallet's address, at most once and only while recent. */
  fun consume(now: Long = System.currentTimeMillis()): String? =
    address.takeIf { now - createdAt < MAX_AGE_MS }.also { address = null }

  companion object {
    const val MAX_AGE_MS = 30 * 60 * 1000L
  }
}
