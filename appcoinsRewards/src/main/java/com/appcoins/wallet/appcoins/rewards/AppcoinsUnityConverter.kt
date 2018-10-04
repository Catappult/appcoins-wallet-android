package com.appcoins.wallet.appcoins.rewards

import java.math.BigInteger

interface AppcoinsUnityConverter {
  fun convertToAppCoins(amount: BigInteger): BigInteger
}
