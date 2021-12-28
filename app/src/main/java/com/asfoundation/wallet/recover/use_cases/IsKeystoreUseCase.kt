package com.asfoundation.wallet.recover.use_cases

class IsKeystoreUseCase {
  operator fun invoke(key: String): Boolean = key.contains("{")
}