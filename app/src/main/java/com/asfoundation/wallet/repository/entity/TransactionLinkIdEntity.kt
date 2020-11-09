package com.asfoundation.wallet.repository.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_link_id",
    indices = [Index(value = arrayOf("transactionId", "linkTransactionId"), unique = true)])
data class TransactionLinkIdEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val transactionId: String,
    val linkTransactionId: String
)