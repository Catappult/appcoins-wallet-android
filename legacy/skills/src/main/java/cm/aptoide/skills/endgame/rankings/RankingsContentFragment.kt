package cm.aptoide.skills.endgame.rankings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cm.aptoide.skills.databinding.FragmentRankingsTabContentBinding
import cm.aptoide.skills.model.TimeFrame
import cm.aptoide.skills.usecase.GetBonusHistoryUseCase
import cm.aptoide.skills.usecase.GetNextBonusScheduleUseCase
import com.appcoins.eskills2048.BuildConfig
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.Arrays
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class RankingsContentFragment : Fragment() {
  private var timeFrame: StatisticsTimeFrame? = null
  private var walletAddress: String? = null
  private var sku: String? = null
  private var adapter: RankingsAdapter? = null
  private val disposables = CompositeDisposable()
  private var loadingView: View? = null
  private var recyclerView: RecyclerView? = null
  private var errorView: View? = null
  private var countdownBinding: CountdownTimerLayoutBinding? = null
  private var countDownTimer: CountDownTimer? = null
  private var binding: FragmentRankingsTabContentBinding? = null

  @Inject
  var getUserStatisticsUseCase: GetUserStatisticsUseCase? = null

  @Inject
  var getBonusHistoryUseCase: GetBonusHistoryUseCase? = null

  @Inject
  var getNextBonusScheduleUseCase: GetNextBonusScheduleUseCase? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val arguments = arguments
    if (arguments != null) {
      timeFrame = arguments.getSerializable(TIME_FRAME_KEY) as TimeFrame?
      walletAddress = arguments.getString(WALLET_ADDRESS_KEY)
      sku = arguments.getString(SKU_KEY)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentRankingsTabContentBinding.inflate(inflater, container, false)
    return binding!!.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    recyclerView = binding?.rankingsRecyclerView
    adapter = RankingsAdapter(LayoutInflater.from(context))
    recyclerView!!.layoutManager = LinearLayoutManager(context)
    recyclerView!!.adapter = adapter
    countdownBinding = binding?.currentTop3Container?.countdownTimerContainer
    loadingView = binding?.loading
    errorView = binding?.errorView
    showRankings()
    if (timeFrame === TimeFrame.ALL_TIME) {
      binding?.lastWinnersContainer?.root
        .setVisibility(View.GONE)
      binding?.currentTop3Container?.root
        .setVisibility(View.GONE)
    } else {
      showLastBonusWinners()
      showCountdownTimer()
    }
    binding.retryButton.setOnClickListener { view1 -> showRankings() }
  }

  private fun showNotAttributed(usernameView: TextView) {
    usernameView.setText(R.string.not_attributed_place_holder)
    usernameView.setTextColor(resources.getColor(R.color.rankings_text_color))
  }

  private fun showRankings() {
    disposables.add(
      getUserStatisticsUseCase.execute(BuildConfig.APPLICATION_ID, walletAddress, timeFrame)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { disposable -> showLoadingView() }
        .doOnSuccess { disposable -> showRecyclerView() }
        .subscribe({ topRankings ->
          updateCurrentRanking(topRankings.getCurrentUser())
          updateRankingsList(processTop3(topRankings.getUserList()))
        }) { throwable ->
          throwable.printStackTrace()
          showErrorView()
        })
  }

  private fun showLastBonusWinners() {
    disposables.add(getBonusHistoryUseCase.execute(BuildConfig.APPLICATION_ID, sku, timeFrame)
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSubscribe { disposable -> showLoadingView() }
      .doOnSuccess { disposable -> showRecyclerView() }
      .subscribe({ response ->
        updateLastBonusWinners(
          response.get(0)
            .getUsers()
        )
      }) { throwable ->
        throwable.printStackTrace()
        showErrorView()
      })
  }

  // process top 3 and return the original list minus that 3 players
  private fun processTop3(players_score: Array<TopRankings>): Array<TopRankings> {
    if (timeFrame === StatisticsTimeFrame.ALL_TIME) {
      return players_score
    }
    val firstPlayerRowBinding: FirstRowLayoutBinding = binding.currentTop3Container.firstPlayerRow
    val secondPlayerRowBinding: SecondRowLayoutBinding =
      binding.currentTop3Container.secondPlayerRow
    val thirdRowLayoutBinding: ThirdRowLayoutBinding = binding.currentTop3Container.thirdPlayerRow
    if (players_score.size >= 1) {
      val player1: TopRankings = players_score[0]
      populateTop3row(
        player1, firstPlayerRowBinding.rankingUsername,
        firstPlayerRowBinding.rankingScore
      )
    } else {
      showNotAttributed(firstPlayerRowBinding.rankingUsername)
    }
    if (players_score.size >= 2) {
      val player2: TopRankings = players_score[1]
      populateTop3row(
        player2, secondPlayerRowBinding.rankingUsername,
        secondPlayerRowBinding.rankingScore
      )
    } else {
      showNotAttributed(secondPlayerRowBinding.rankingUsername)
    }
    if (players_score.size >= 3) {
      val player3: TopRankings = players_score[2]
      populateTop3row(
        player3, thirdRowLayoutBinding.rankingUsername,
        thirdRowLayoutBinding.rankingScore
      )
    } else {
      showNotAttributed(thirdRowLayoutBinding.rankingUsername)
    }
    return Arrays.copyOfRange(players_score, 3, players_score.size)
  }

  private fun populateTop3row(player: TopRankings, username: TextView, score: TextView) {
    username.setText(player.getUsername())
    score.setText(java.lang.String.valueOf(player.getScore()))
  }

  private fun showCountdownTimer() {
    if (timeFrame === StatisticsTimeFrame.TODAY) {
      countdownBinding.countdownDaysContainer.setVisibility(View.GONE)
    }
    disposables.add(getNextBonusScheduleUseCase.execute(timeFrame)
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { nextSchedule ->
        val timeLeftMillis: Long =
          nextSchedule.getNextSchedule() * 1000 - System.currentTimeMillis()
        startCountDownTimer(timeLeftMillis)
      }
      .subscribe())
  }

  private fun updateLastBonusWinners(users: List<BonusUser>) {
    if (users.size >= 1) {
      binding.lastWinnersContainer.secondUsername.setText(
        users[0]
          .getUserName()
      )
      binding.lastWinnersContainer.secondWinnings.setText(
        java.lang.String.format(
          Locale.getDefault(), "%.2f$", users[0]
            .getBonusAmount()
        )
      )
    } else {
      showNotAttributed(binding.lastWinnersContainer.secondUsername)
    }
    if (users.size >= 2) {
      binding.lastWinnersContainer.firstUsername.setText(
        users[1]
          .getUserName()
      )
      binding.lastWinnersContainer.firstWinnings.setText(
        java.lang.String.format(
          Locale.getDefault(), "%.2f$",
          users[1]
            .getBonusAmount()
        )
      )
    } else {
      showNotAttributed(binding.lastWinnersContainer.firstUsername)
    }
    if (users.size >= 3) {
      binding.lastWinnersContainer.thirdUsername.setText(
        users[2]
          .getUserName()
      )
      binding.lastWinnersContainer.thirdWinnings.setText(
        java.lang.String.format(
          Locale.getDefault(), "%.2f$",
          users[2]
            .getBonusAmount()
        )
      )
    } else {
      showNotAttributed(binding.lastWinnersContainer.thirdUsername)
    }
  }

  private fun updateCurrentRanking(currentRanking: TopRankings?) {
    if (currentRanking == null) {
      binding.currentRankingContainer.getRoot()
        .setVisibility(View.GONE)
    } else {
      binding.currentRankingContainer.rankingScore.setText(
        java.lang.String.valueOf(currentRanking.getScore())
      )
      binding.currentRankingContainer.rankingPosition.setText(
        java.lang.String.valueOf(currentRanking.getRankPosition())
      )
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
    errorView!!.visibility = View.VISIBLE
    loadingView!!.visibility = View.GONE
    recyclerView!!.visibility = View.GONE
  }

  private fun showRecyclerView() {
    loadingView!!.visibility = View.GONE
    errorView!!.visibility = View.GONE
    recyclerView!!.visibility = View.VISIBLE
  }

  private fun showLoadingView() {
    loadingView!!.visibility = View.VISIBLE
    errorView!!.visibility = View.GONE
    recyclerView!!.visibility = View.GONE
  }

  private fun updateRankingsList(rankings: Array<TopRankings>) {
    val items: List<RankingsItem> = ArrayList<Any?>(mapPlayers(rankings))
    adapter!!.setRankings(items)
  }

  private fun mapPlayers(players: Array<TopRankings>): List<UserRankingsItem> {
    val playersList: ArrayList<UserRankingsItem> = ArrayList<UserRankingsItem>()
    for (player in players) {
      playersList.add(
        UserRankingsItem(
          player.getUsername(), player.getScore(), player.getRankPosition(),
          false
        )
      )
    }
    return playersList
  }

  override fun onDestroyView() {
    disposables.clear()
    if (countDownTimer != null) {
      countDownTimer.cancel()
    }
    super.onDestroyView()
    binding = null
  }

  companion object {
    private const val WALLET_ADDRESS_KEY = "WALLET_ADDRESS_KEY"
    private const val TIME_FRAME_KEY = "TIME_FRAME_KEY"
    private const val SKU_KEY = "SKU_KEY"
    private const val COUNTDOWN_INTERVAL: Long = 1000
    @JvmStatic
    fun newInstance(
      walletAddress: String?, sku: String?,
      timeFrame: StatisticsTimeFrame?
    ): RankingsContentFragment {
      val args = Bundle()
      args.putString(WALLET_ADDRESS_KEY, walletAddress)
      args.putSerializable(TIME_FRAME_KEY, timeFrame)
      args.putSerializable(SKU_KEY, sku)
      val fragment = RankingsContentFragment()
      fragment.arguments = args
      return fragment
    }
  }
}