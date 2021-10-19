package cm.aptoide.skills.games

interface GameStateListener {
  fun onUpdate(gameUpdate: GameUpdate)
  fun onFinishGame(finishedGame: FinishedGame)
}
