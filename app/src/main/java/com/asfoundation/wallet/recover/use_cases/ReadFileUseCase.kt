package com.asfoundation.wallet.recover.use_cases

import android.net.Uri
import com.asfoundation.wallet.backup.FileInteractor
import io.reactivex.Single

class ReadFileUseCase(private val fileInteractor: FileInteractor) {
  operator fun invoke(fileUri: Uri?): Single<String> = fileInteractor.readFile(fileUri)
}