package cm.aptoide.skills.usecase

import com.appcoins.wallet.sharedpreferences.EskillsPreferencesDataSource
import javax.inject.Inject

class GetIsEskillsVersionUseCase @Inject constructor(
  private val eskillsPreferencesDataSource: EskillsPreferencesDataSource
) {

  operator fun invoke(): Boolean {
    return eskillsPreferencesDataSource.isEskillsVersion()
  }
}