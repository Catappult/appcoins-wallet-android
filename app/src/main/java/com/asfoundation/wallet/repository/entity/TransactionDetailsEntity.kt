package com.asfoundation.wallet.repository.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TransactionDetailsEntity(@PrimaryKey @Embedded var icon: Icon,
                                    var sourceName: String?,
                                    var description: String?) {

  @Entity
  data class Icon(val iconType: Type, @PrimaryKey val uri: String)

  enum class Type {
    FILE, URL
  }

}
