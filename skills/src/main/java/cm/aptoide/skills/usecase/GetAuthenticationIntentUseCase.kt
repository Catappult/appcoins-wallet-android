package cm.aptoide.skills.usecase

import android.content.Context
import android.content.Intent
import cm.aptoide.skills.interfaces.ExternalAuthenticationProvider
import javax.inject.Inject

class GetAuthenticationIntentUseCase @Inject constructor(
  private val externalAuthenticationProvider: ExternalAuthenticationProvider
) {

  operator fun invoke(context: Context): Intent {
    return externalAuthenticationProvider.getAuthenticationIntent(context)
  }
}
