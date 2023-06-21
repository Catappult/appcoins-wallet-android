package cm.aptoide.skills.endgame

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
          putString(WALLET_ADDRESS, endgameData.walletAddress)
          putString(SESSION, endgameData.session)
          putString(PACKAGE_NAME, endgameData.packageName)
          putString(PRODUCT, endgameData.product)
          putLong(SCORE, endgameData.userScore)
        }
      }
    }

    const val SESSION = "SESSION"
    const val PACKAGE_NAME = "MATCH_ENVIRONMENT"
    const val WALLET_ADDRESS = "WALLET_ADDRESS"
    const val PRODUCT = "PRODUCT"
    const val SCORE = "SCORE"
  }

  private val viewModel: FinishGameActivityViewModel by viewModels()  // TODO

  private lateinit var disposables: CompositeDisposable

  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: PlayerRankingAdapter

  private val views by viewBinding(EndgameFragmentSkillsBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ) = EndgameFragmentSkillsBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    buildRecyclerView()
    disposables = CompositeDisposable()
    val walletAddress = requireArguments().getString(WALLET_ADDRESS)
    val packageName = requireArguments().getString(PACKAGE_NAME)

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


    views.retryButton.setOnClickListener {
      disposables.add(viewModel.getRoomResult().subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread()).doOnSubscribe { showLoading() }
        .doOnSuccess { room -> setRoomResultDetails(room as RoomResponse.SuccessfulRoomResponse) }
        .doOnError { showErrorMessage() }.subscribe({ }, Throwable::printStackTrace)
      )
    }
    disposables.add(viewModel.getRoomResult().subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { room -> setRoomResultDetails(room as RoomResponse.SuccessfulRoomResponse) }
      .doOnError { showErrorMessage() }.subscribe({ }, Throwable::printStackTrace)
    )
    views.rankingsButton.setOnClickListener {
      requireActivity().supportFragmentManager.beginTransaction().replace(
        R.id.rankings_fragment_container, SkillsRankingsFragment.newInstance(
          walletAddress!!, packageName!!, "APTOIDE_GLOBAL_LEADERBOARD_SKU"
        )
      ) // TODO
        .commit(
        )
    }
    views.restartButton.setOnClickListener {
      TODO()
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
    views.restartButton.isEnabled = false
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
    if (viewModel.isWinner(room.roomResult)) {
      handleRoomWinnerBehaviour(room.roomResult)
    } else {
      handleRoomLoserBehaviour(room)
    }
    views.restartButton.isEnabled = true
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

  private fun showErrorMessage(throwable: Throwable) {
    showErrorMessage()
    throwable.printStackTrace()
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
