package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.ExternalAuthenticationProvider

class HasAuthenticationPermissionUseCase(
    private val externalAuthenticationProvider: ExternalAuthenticationProvider) {
  operator fun invoke(): Boolean {
    return externalAuthenticationProvider.hasAuthenticationPermission()
  }
}
