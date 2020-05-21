package com.asfoundation.wallet.util

data class ImportError(val hasError: Boolean,
                       val type: ImportErrorType) {
  constructor() : this(false, ImportErrorType.NONE)
  constructor(type: ImportErrorType) : this(true, type)
}

enum class ImportErrorType {
  NONE, GENERIC, INVALID_PASS, ALREADY_ADDED, INVALID_KEYSTORE, INVALID_PRIVATE_KEY
}
