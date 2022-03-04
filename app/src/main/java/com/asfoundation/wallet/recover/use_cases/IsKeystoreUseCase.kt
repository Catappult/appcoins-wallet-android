package com.asfoundation.wallet.recover.use_cases

import javax.inject.Inject

class IsKeystoreUseCase @Inject constructor() {
  operator fun invoke(key: String): Boolean = key.contains("{")
}