package com.asfoundation.wallet.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OperationEntity(@PrimaryKey var transactionId: String,
                           var from: String,
                           var to: String,
                           var fee: String)
