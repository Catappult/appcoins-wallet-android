package cm.aptoide.skills.endgame.model

class MatchDetails(
  val product: String, val value: Float, val currency: String, val environment: Environment,
  val numberOfUsers: Int, val timeout: Int
) {

  enum class Environment {
    SANDBOX, LIVE
  }
}