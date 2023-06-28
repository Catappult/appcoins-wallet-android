package cm.aptoide.skills.endgame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import cm.aptoide.skills.R
import cm.aptoide.skills.databinding.EndgameFragmentSkillsBinding
import cm.aptoide.skills.endgame.model.PlayerRankingAdapter
import cm.aptoide.skills.endgame.rankings.SkillsRankingsFragment
import cm.aptoide.skills.util.EmojiUtils
import cm.aptoide.skills.util.parseEndgame
import com.appcoins.wallet.core.network.eskills.model.EskillsEndgameData
import com.appcoins.wallet.core.network.eskills.model.RoomResponse
import com.appcoins.wallet.core.network.eskills.model.RoomResult
import com.appcoins.wallet.core.network.eskills.model.RoomStatus
import com.appcoins.wallet.core.network.eskills.model.User
import com.appcoins.wallet.core.network.eskills.model.UserStatus
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SkillsEndgameFragment : Fragment() {

  companion object {
    fun newInstance(endgameUri: String): SkillsEndgameFragment {
      val uri: Uri = Uri.parse(endgameUri)
      val endgameData: EskillsEndgameData = uri.parseEndgame()
      return SkillsEndgameFragment().apply {
        arguments = Bundle().apply {
          putString(SESSION, endgameData.session)
          putString(PACKAGE_NAME, endgameData.packageName)
        }
      }
    }

    const val SESSION = "SESSION"
    const val PACKAGE_NAME = "DOMAIN"
    const val GLOBAL_LEADERBOARD_SKU = "APTOIDE_GLOBAL_LEADERBOARD_SKU"
  }

  private val viewModel: SkillsEndgameViewModel by viewModels()
  private lateinit var disposables: CompositeDisposable
  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: PlayerRankingAdapter

  private val onBackPressedCallback = object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
      requireActivity().setResult(SkillsEndgameViewModel.RESULT_OK)
      requireActivity().finish()
    }
  }

  private val views by viewBinding(EndgameFragmentSkillsBinding::bind)


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleBackPress()
  }

  private fun handleBackPress() {
    requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ) = EndgameFragmentSkillsBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    buildRecyclerView()
    disposables = CompositeDisposable()
    setupOpponentScoreListener()
    setupRetryButton()
    handleRoomResult()
    setupRankingsButton()
    setupRestartButton()
  }

  private fun setupOpponentScoreListener() {
    disposables.add(Observable.interval(
      0, 3L, TimeUnit.SECONDS
    ).flatMapSingle {
      viewModel.getRoom().observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess { roomResponse: RoomResponse ->
          updateRecyclerView(
            roomResponse
          )
        }.doOnError(Throwable::printStackTrace).onErrorReturnItem(RoomResponse.FailedRoomResponse())
    }.takeUntil { roomResponse: RoomResponse -> roomResponse.status === RoomStatus.COMPLETED }
      .subscribe())
  }

  private fun handleRoomResult() {
    disposables.add(viewModel.getRoomResult().subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread()).doOnSubscribe { showLoading() }
      .doOnSuccess { room -> setRoomResultDetails(room as RoomResponse.SuccessfulRoomResponse) }
      .doOnError { showErrorMessage() }.subscribe({ }, Throwable::printStackTrace)
    )
  }

  private fun setupRetryButton() {
    views.retryButton.setOnClickListener {
      handleRoomResult()
    }
  }

  private fun setupRestartButton() {
    views.restartButton.setOnClickListener {
      requireActivity().setResult(SkillsEndgameViewModel.RESULT_OK, Intent())
      requireActivity().finish()
    }
  }

  private fun setupRankingsButton() {
    views.rankingsButton.setOnClickListener {
      disposables.add(
        viewModel.getWalletAddress().subscribeOn(AndroidSchedulers.mainThread())
          .doOnSuccess { walletAddress ->
            requireActivity().supportFragmentManager.beginTransaction().replace(
              R.id.fragment_container, SkillsRankingsFragment.newInstance(
                walletAddress, requireArguments().getString(PACKAGE_NAME)!!, GLOBAL_LEADERBOARD_SKU
              )
            )
              .addToBackStack(SkillsRankingsFragment::class.java.simpleName)
              .commit()
          }.subscribe()
      )
    }
  }

  private fun buildRecyclerView() {
    recyclerView = views.rankingRecyclerView
    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    adapter = PlayerRankingAdapter(ArrayList())
    recyclerView.adapter = adapter
  }

  private fun showLoading() {
    views.lottieAnimation.setAnimation(R.raw.transact_loading_animation)
    views.lottieAnimation.playAnimation()
    views.animationDescriptionText.setText(R.string.waiting_for_opponents_to_finish)
    views.restartButton.visibility = View.VISIBLE
    views.retryButton.visibility = View.GONE
  }

  private fun updateRecyclerView(roomResponse: RoomResponse) {
    val successfulRoomResponse = roomResponse as RoomResponse.SuccessfulRoomResponse
    adapter.updateData(
      successfulRoomResponse.users.sortedWith(compareBy { it.score })
    )
  }

  private fun setRoomResultDetails(room: RoomResponse.SuccessfulRoomResponse) {
    recyclerView.visibility = View.GONE
    views.lottieAnimation.setAnimation(R.raw.transact_credits_successful)
    views.lottieAnimation.playAnimation()
    disposables.add(viewModel.isWinner(room.roomResult).observeOn(AndroidSchedulers.mainThread())
      .doAfterSuccess { isWinner: Boolean ->
        if (isWinner) {
          handleRoomWinnerBehaviour(room.roomResult)
        } else {
          handleRoomLoserBehaviour(room)
        }
      }.doOnError { showErrorMessage() }.subscribe()
    )
    views.restartButton.visibility = View.VISIBLE
    views.retryButton.visibility = View.GONE
  }

  private fun handleRoomWinnerBehaviour(roomResult: RoomResult) {
    val partyEmoji: String = EmojiUtils.getEmojiByUnicode(0x1F389)
    val descriptionText = resources.getString(R.string.you_won, partyEmoji)
    views.animationDescriptionText.text = descriptionText
    val opponentDetails = resources.getString(R.string.amount_won_details, roomResult.winnerAmount)
    views.secondaryMessage.text = opponentDetails
    views.secondaryMessage.visibility = View.VISIBLE
  }

  private fun handleRoomLoserBehaviour(roomResponse: RoomResponse.SuccessfulRoomResponse) {
    val sadEmoji: String = EmojiUtils.getEmojiByUnicode(0x1F614)
    val alarmEmoji: String = EmojiUtils.getEmojiByUnicode(0x23F0)
    val descriptionText: String = if (roomResponse.currentUser.status === UserStatus.TIME_UP) {
      resources.getString(R.string.you_lost_timeout, alarmEmoji)
    } else {
      resources.getString(R.string.you_lost, sadEmoji)
    }
    views.animationDescriptionText.text = descriptionText
    val winner: User = roomResponse.roomResult.winner
    val opponentDetails = resources.getString(
      R.string.opponent_details, winner.userName, winner.score
    )
    views.secondaryMessage.text = opponentDetails
    views.secondaryMessage.visibility = View.VISIBLE
  }

  private fun showErrorMessage() {
    views.lottieAnimation.setAnimation(R.raw.error_animation)
    views.lottieAnimation.playAnimation()
    views.animationDescriptionText.text = resources.getString(R.string.unknown_error)
    views.retryButton.visibility = View.VISIBLE
    views.restartButton.visibility = View.GONE
    views.restartButton.isEnabled = true
  }
}
