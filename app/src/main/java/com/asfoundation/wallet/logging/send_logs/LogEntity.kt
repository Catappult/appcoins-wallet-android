package com.asfoundation.wallet.logging.send_logs

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val created: Instant = Instant.now(),
    val tag: String?,
    val data: String,
    val sending: Boolean = false
)