package cm.aptoide.skills.endgame.rankings

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cm.aptoide.skills.R
import cm.aptoide.skills.databinding.CountdownTimerLayoutBinding
import cm.aptoide.skills.databinding.FirstRowLayoutBinding
import cm.aptoide.skills.databinding.FragmentRankingsTabContentBinding
import cm.aptoide.skills.databinding.SecondRowLayoutBinding
import cm.aptoide.skills.databinding.ThirdRowLayoutBinding
import cm.aptoide.skills.endgame.model.RankingsItem
import cm.aptoide.skills.endgame.model.UserRankingsItem
import cm.aptoide.skills.model.TimeFrame
import cm.aptoide.skills.usecase.GetBonusHistoryUseCase
import cm.aptoide.skills.usecase.GetNextBonusScheduleUseCase
import cm.aptoide.skills.usecase.GetUserStatisticsUseCase
import com.appcoins.wallet.core.network.eskills.model.BonusUser
import com.appcoins.wallet.core.network.eskills.model.TopRankings
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class RankingsContentFragment : Fragment() {
  private lateinit var timeFrame: TimeFrame
  private lateinit var walletAddress: String
  private lateinit var packageName: String
  private lateinit var sku: String
  private lateinit var adapter: RankingsAdapter
  private var disposables = CompositeDisposable()
  private lateinit var loadingView: View
  private lateinit var recyclerView: RecyclerView
  private lateinit var errorView: View
  private lateinit var countdownBinding: CountdownTimerLayoutBinding
  private lateinit var countDownTimer: CountDownTimer
  private lateinit var binding: FragmentRankingsTabContentBinding

  @Inject
  lateinit var getUserStatisticsUseCase: GetUserStatisticsUseCase

  @Inject
  lateinit var getBonusHistoryUseCase: GetBonusHistoryUseCase

  @Inject
  lateinit var getNextBonusScheduleUseCase: GetNextBonusScheduleUseCase
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    timeFrame = requireArguments().getSerializable(TIME_FRAME_KEY) as TimeFrame
    walletAddress = requireArguments().getString(WALLET_ADDRESS_KEY)!!
    packageName = requireArguments().getString(PACKAGE_NAME_KEY)!!
    sku = requireArguments().getString(SKU_KEY)!!

  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    binding = FragmentRankingsTabContentBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    recyclerView = binding.rankingsRecyclerView
    adapter = RankingsAdapter(LayoutInflater.from(context))
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = adapter
    countdownBinding = binding.currentTop3Container.countdownTimerContainer
    loadingView = binding.loading
    errorView = binding.errorView
    showRankings()
    if (timeFrame === TimeFrame.ALL_TIME) {
      binding.lastWinnersContainer.root.visibility = View.GONE
      binding.currentTop3Container.root.visibility = View.GONE
    } else {
      showLastBonusWinners()
      showCountdownTimer()
    }
    binding.retryButton.setOnClickListener { showRankings() }
  }

  private fun showNotAttributed(usernameView: TextView) {
    usernameView.setText(R.string.not_attributed_place_holder)
    usernameView.setTextColor(resources.getColor(R.color.rankings_text_color))
  }

  private fun showRankings() {
    disposables.add(getUserStatisticsUseCase.invoke(packageName, walletAddress, timeFrame)
      .observeOn(AndroidSchedulers.mainThread()).doOnSubscribe { showLoadingView() }
      .doOnSuccess { showRecyclerView() }.subscribe({ topRankings ->
        updateCurrentRanking(topRankings.currentUser)
        updateRankingsList(processTop3(topRankings.userList))
      }) { throwable ->
        throwable.printStackTrace()
        showErrorView()
      })
  }

  private fun showLastBonusWinners() {
    disposables.add(getBonusHistoryUseCase.invoke(packageName, sku, timeFrame)
      .observeOn(AndroidSchedulers.mainThread()).doOnSubscribe { showLoadingView() }
      .doOnSuccess { showRecyclerView() }.subscribe({ response ->
        updateLastBonusWinners(
          response[0].users
        )
      }) { throwable ->
        throwable.printStackTrace()
        showErrorView()
      })
  }

  // process top 3 and return the original list minus that 3 players
  private fun processTop3(players_score: Array<TopRankings>): Array<TopRankings> {
    if (timeFrame === TimeFrame.ALL_TIME) {
      return players_score
    }
    val firstPlayerRowBinding: FirstRowLayoutBinding = binding.currentTop3Container.firstPlayerRow
    val secondPlayerRowBinding: SecondRowLayoutBinding =
      binding.currentTop3Container.secondPlayerRow
    val thirdRowLayoutBinding: ThirdRowLayoutBinding = binding.currentTop3Container.thirdPlayerRow
    if (players_score.size >= 1) {
      val player1: TopRankings = players_score[0]
      populateTop3row(
        player1, firstPlayerRowBinding.rankingUsername, firstPlayerRowBinding.rankingScore
      )
    } else {
      showNotAttributed(firstPlayerRowBinding.rankingUsername)
    }
    if (players_score.size >= 2) {
      val player2: TopRankings = players_score[1]
      populateTop3row(
        player2, secondPlayerRowBinding.rankingUsername, secondPlayerRowBinding.rankingScore
      )
    } else {
      showNotAttributed(secondPlayerRowBinding.rankingUsername)
    }
    if (players_score.size >= 3) {
      val player3: TopRankings = players_score[2]
      populateTop3row(
        player3, thirdRowLayoutBinding.rankingUsername, thirdRowLayoutBinding.rankingScore
      )
      return players_score.copyOfRange(3, players_score.size)
    } else {
      showNotAttributed(thirdRowLayoutBinding.rankingUsername)
    }
    return emptyArray()
  }

  private fun populateTop3row(player: TopRankings, username: TextView, score: TextView) {
    username.text = player.username
    score.text = java.lang.String.valueOf(player.score)
  }

  private fun showCountdownTimer() {
    if (timeFrame == TimeFrame.TODAY) {
      countdownBinding.countdownDaysContainer.visibility = View.GONE
    }
    disposables.add(
      getNextBonusScheduleUseCase.invoke(timeFrame).observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess { nextSchedule ->
          val timeLeftMillis: Long = nextSchedule.nextSchedule * 1000 - System.currentTimeMillis()
          startCountDownTimer(timeLeftMillis)
        }.subscribe()
    )
  }

  private fun updateLastBonusWinners(users: List<BonusUser>) {
    if (users.size >= 1) {
      binding.lastWinnersContainer.firstUsername.text = users[0].userName
      binding.lastWinnersContainer.firstWinnings.text = java.lang.String.format(
        Locale.getDefault(), "%.2f$", users[0].bonusAmount
      )
    } else {
      showNotAttributed(binding.lastWinnersContainer.firstUsername)
    }
    if (users.size >= 2) {
      binding.lastWinnersContainer.secondUsername.text = users[1].userName
      binding.lastWinnersContainer.secondWinnings.text = java.lang.String.format(
        Locale.getDefault(), "%.2f$", users[1].bonusAmount
      )
    } else {
      showNotAttributed(binding.lastWinnersContainer.secondUsername)
    }
    if (users.size >= 3) {
      binding.lastWinnersContainer.thirdUsername.text = users[2].userName
      binding.lastWinnersContainer.thirdWinnings.text = java.lang.String.format(
        Locale.getDefault(), "%.2f$", users[2].bonusAmount
      )
    } else {
      showNotAttributed(binding.lastWinnersContainer.thirdUsername)
    }
  }

  private fun updateCurrentRanking(currentRanking: TopRankings?) {
    if (currentRanking == null) {
      binding.currentRankingContainer.root.visibility = View.GONE
    } else {
      binding.currentRankingContainer.rankingScore.text =
        java.lang.String.valueOf(currentRanking.score)
      binding.currentRankingContainer.rankingPosition.text =
        java.lang.String.valueOf(currentRanking.rankPosition)
    }
  }

  private fun startCountDownTimer(timeLeftMillis: Long) {
    val daysView: TextView = countdownBinding.countdownDays
    val hoursView: TextView = countdownBinding.countdownHours
    val minutesView: TextView = countdownBinding.countdownMinutes
    val secondsView: TextView = countdownBinding.countdownSeconds
    countDownTimer = object : CountDownTimer(timeLeftMillis, COUNTDOWN_INTERVAL) {
      override fun onTick(millisUntilFinished: Long) {
        val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
        daysView.text = days.toString()
        hoursView.text = hours.toString()
        minutesView.text = minutes.toString()
        secondsView.text = seconds.toString()
      }

      override fun onFinish() {
        showCountdownTimer() // update schedule
      }
    }.start()
  }

  private fun showErrorView() {
    errorView.visibility = View.VISIBLE
    loadingView.visibility = View.GONE
    recyclerView.visibility = View.GONE
  }

  private fun showRecyclerView() {
    loadingView.visibility = View.GONE
    errorView.visibility = View.GONE
    recyclerView.visibility = View.VISIBLE
  }

  private fun showLoadingView() {
    loadingView.visibility = View.VISIBLE
    errorView.visibility = View.GONE
    recyclerView.visibility = View.GONE
  }

  private fun updateRankingsList(rankings: Array<TopRankings>) {
    val items: List<RankingsItem> = ArrayList<RankingsItem>(mapPlayers(rankings))
    adapter.setRankings(items)
  }

  private fun mapPlayers(players: Array<TopRankings>): List<UserRankingsItem> {
    val playersList: ArrayList<UserRankingsItem> = ArrayList()
    for (player in players) {
      playersList.add(
        UserRankingsItem(
          player.username, player.score, player.rankPosition.toLong(),
          player.walletAddress == walletAddress
        )
      )
    }
    return playersList
  }

  override fun onDestroyView() {
    disposables.clear()
    countDownTimer.cancel()
    super.onDestroyView()
  }

  companion object {
    private const val WALLET_ADDRESS_KEY = "WALLET_ADDRESS_KEY"
    private const val TIME_FRAME_KEY = "TIME_FRAME_KEY"
    private const val PACKAGE_NAME_KEY = "PACKAGE_NAME_KEY"
    private const val SKU_KEY = "SKU_KEY"
    private const val COUNTDOWN_INTERVAL: Long = 1000

    @JvmStatic
    fun newInstance(
      walletAddress: String, packageName: String, sku: String, timeFrame: TimeFrame
    ): RankingsContentFragment {
      return RankingsContentFragment().apply {
        arguments = Bundle().apply {
          putString(WALLET_ADDRESS_KEY, walletAddress)
          putString(PACKAGE_NAME_KEY, packageName)
          putString(SKU_KEY, sku)
          putSerializable(TIME_FRAME_KEY, timeFrame)
        }
      }
    }
  }
}