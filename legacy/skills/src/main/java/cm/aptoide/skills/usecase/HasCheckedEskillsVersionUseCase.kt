package cm.aptoide.skills.usecase

import com.appcoins.wallet.sharedpreferences.EskillsPreferencesDataSource
import javax.inject.Inject

class HasCheckedEskillsVersionUseCase @Inject constructor(
  private val eskillsPreferencesDataSource: EskillsPreferencesDataSource
) {

  operator fun invoke(): Boolean {
    return eskillsPreferencesDataSource.eskillsVersionChecked()
  }
}