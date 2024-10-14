package com.appcoins.wallet.feature.backup.data.result

sealed class BackupResult

object SuccessfulBackup : BackupResult()
object FailedBackup : BackupResult()