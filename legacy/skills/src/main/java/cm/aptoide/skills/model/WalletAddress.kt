package cm.aptoide.skills.model

import org.web3j.crypto.Keys

data class WalletAddress(
  val address: String,
  val byteArray: ByteArray
) {
  companion object {
    fun fromValue(value: Any): WalletAddress {
      // drop(2) is used to remove 0x prefix
      if (value is String) {
        val checksumAddress = Keys.toChecksumAddress(value)
        return WalletAddress(
          checksumAddress, checksumAddress.drop(2)
            .decodeHex()
        )
      } else if (value is ByteArray) {
        val checksumAddress = Keys.toChecksumAddress("0x" + value.toHex())
        return WalletAddress(checksumAddress, value)
      }
      throw RuntimeException("Unknown format for ${value.javaClass}")
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as WalletAddress
    if (!byteArray.contentEquals(other.byteArray)) return false
    return true
  }

  override fun hashCode(): Int {
    var result = address.hashCode()
    result = 31 * result + byteArray.contentHashCode()
    return result
  }
}

fun String.decodeHex(): ByteArray {
  check(length % 2 == 0) { "Must have an even length." }

  return chunked(2)
    .map {
      it.toInt(16)
        .toByte()
    }
    .toByteArray()
}

fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
