package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.repository.LoginRepository
import io.reactivex.Single

class LoginUseCase(private val ewtObtainer: EwtObtainer,
                   private val loginRepository: LoginRepository) {

  fun login(roomId: String): Single<String> {
    return ewtObtainer.getEWT()
        .flatMap {
          loginRepository.login(it, roomId)
              .map { it.token }
        }
  }
}