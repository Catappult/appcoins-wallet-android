package com.asfoundation.wallet.util

data class RestoreError(val hasError: Boolean,
                        val type: RestoreErrorType) {
  constructor() : this(false, RestoreErrorType.NONE)
  constructor(type: RestoreErrorType) : this(true, type)
}

enum class RestoreErrorType {
  NONE, GENERIC, INVALID_PASS, ALREADY_ADDED, INVALID_KEYSTORE, INVALID_PRIVATE_KEY
}
