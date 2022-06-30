package com.asfoundation.wallet.recover.use_cases

import android.net.Uri
import com.asfoundation.wallet.backup.FileInteractor
import com.asfoundation.wallet.entity.WalletKeyStore
import io.reactivex.Single
import javax.inject.Inject

class ReadFileUseCase @Inject constructor(private val fileInteractor: FileInteractor) {
  operator fun invoke(fileUri: Uri?): Single<WalletKeyStore> = fileInteractor.readFile(fileUri)
}