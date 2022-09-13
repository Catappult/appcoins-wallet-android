package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.ExternalAuthenticationProvider
import javax.inject.Inject

class HasAuthenticationPermissionUseCase @Inject constructor(
  private val externalAuthenticationProvider: ExternalAuthenticationProvider
) {
  operator fun invoke(): Boolean {
    return externalAuthenticationProvider.hasAuthenticationPermission()
  }
}
