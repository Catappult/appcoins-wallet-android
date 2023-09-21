package cm.aptoide.skills.usecase

import com.appcoins.wallet.sharedpreferences.EskillsPreferencesDataSource
import javax.inject.Inject

class SetIsEskillsVersionUseCase @Inject constructor(
  private val eskillsPreferencesDataSource: EskillsPreferencesDataSource
) {

  operator fun invoke(isEskillsVersion: Boolean) {
    eskillsPreferencesDataSource.setIsEskillsVersion(isEskillsVersion)
    eskillsPreferencesDataSource.setEskillsVersionChecked(true)
  }
}