package cm.aptoide.skills

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cm.aptoide.skills.databinding.FragmentSkillsBinding
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.util.EskillsUri
import cm.aptoide.skills.util.EskillsUriParser
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SkillsFragment : DaggerFragment() {

  companion object {
    fun newInstance() = SkillsFragment()

    private const val RESULT_OK = 0
    private const val RESULT_USER_CANCELED = 1
    private const val RESULT_ERROR = 6
    private const val SESSION = "SESSION"
    private const val USER_ID = "USER_ID"
    private const val ROOM_ID = "ROOM_ID"
    private const val WALLET_ADDRESS = "WALLET_ADDRESS"

    private const val WALLET_CREATING_STATUS = "CREATING"
    private const val ESKILLS_URI_KEY = "ESKILLS_URI"
  }

  @Inject
  lateinit var viewModel: SkillsViewModel

  @Inject
  lateinit var eskillsUriParser: EskillsUriParser

  private var userId: String? = null
  private lateinit var disposable: CompositeDisposable

  private lateinit var binding: FragmentSkillsBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View {
    binding = FragmentSkillsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    disposable = CompositeDisposable()

    val eskillsUri = getEskillsUri()
    userId = eskillsUri.getUserId()
    disposable.add(
        handleWalletCreationIfNeeded()
            .takeUntil { it != WALLET_CREATING_STATUS }
            .flatMap {
              viewModel.createTicket(eskillsUri)
                  .observeOn(AndroidSchedulers.mainThread())
                  .doOnSubscribe { showRoomLoading(false, null) }
                  .flatMap { ticketResponse ->
                    viewModel.getRoom(eskillsUri, ticketResponse, this)
                        .doOnSubscribe { showRoomLoading(true, ticketResponse.ticketId) }
                        .doOnNext { userData ->
                          if (userData.refunded) {
                            showRefunded()
                          } else {
                            postbackUserData(RESULT_OK, userData)
                          }
                        }
                  }
            }.subscribe()
    )
  }

  private fun showRefunded() {
    binding.loadingTicketLayout.processingLoading.visibility = View.GONE
    binding.refundTicketLayout.refund.visibility = View.VISIBLE
    binding.refundTicketLayout.refundOkButton.setOnClickListener({ requireActivity().finish() })
  }

  override fun onDestroyView() {
    disposable.clear()
    super.onDestroyView()
  }

  private fun getEskillsUri(): EskillsUri {
    val intent = requireActivity().intent
    return eskillsUriParser.parse(Uri.parse(intent.getStringExtra(ESKILLS_URI_KEY)))
  }

  private fun handleWalletCreationIfNeeded(): Observable<String> {
    return viewModel.handleWalletCreationIfNeeded()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext {
          if (it == WALLET_CREATING_STATUS) {
            showWalletCreationLoadingAnimation()
          }
        }
        .filter { it != WALLET_CREATING_STATUS }
        .map {
          endWalletCreationLoadingAnimation()
          it
        }
  }

  private fun showWalletCreationLoadingAnimation() {
    binding.createWalletLayout.createWalletCard.visibility = View.VISIBLE
    binding.createWalletLayout.createWalletAnimation.playAnimation()
  }

  private fun endWalletCreationLoadingAnimation() {
    binding.createWalletLayout.createWalletCard.visibility = View.GONE
  }

  private fun showRoomLoading(isCancelActive: Boolean, ticketId: String?) {
    binding.loadingTicketLayout.processingLoading.visibility = View.VISIBLE
    if (isCancelActive) {
      binding.loadingTicketLayout.loadingTitle.text = getString(R.string.finding_room_loading_title)
      binding.loadingTicketLayout.cancelButton.isEnabled = true
      binding.loadingTicketLayout.cancelButton.setOnClickListener {
        // only paid tickets can be canceled/refunded on the backend side, meaning that if we
        // cancel before actually paying the backend will return a 409 HTTP. this way we allow
        // users to return to the game, without crashing, even if they weren't waiting in queue
        try {
          viewModel.cancelTicket(ticketId!!)
              .map {
                postbackUserData(RESULT_USER_CANCELED, UserData("", "", "", "", true))
              }.blockingGet()
        } catch (e: Exception) {
          postbackUserData(RESULT_ERROR, UserData("", "", "", "", true))
        }
      }
    } else {
      binding.loadingTicketLayout.loadingTitle.text = getString(R.string.processing_loading_title)
    }
  }

  private fun postbackUserData(resultCode: Int, userData: UserData) {
    requireActivity().setResult(resultCode, buildDataIntent(userData))
    requireActivity().finish()
  }

  private fun buildDataIntent(userData: UserData): Intent {
    val intent = Intent()

    intent.putExtra(SESSION, userData.session)
    intent.putExtra(USER_ID, userData.userId)
    intent.putExtra(ROOM_ID, userData.roomId)
    intent.putExtra(WALLET_ADDRESS, userData.walletAddress)

    return intent
  }
}
