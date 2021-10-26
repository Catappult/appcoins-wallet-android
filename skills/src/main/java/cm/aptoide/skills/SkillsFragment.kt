package cm.aptoide.skills

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import cm.aptoide.skills.databinding.FragmentSkillsBinding
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.games.BackgroundGameService
import cm.aptoide.skills.model.FailedTicket
import cm.aptoide.skills.model.CreatedTicket
import cm.aptoide.skills.model.ErrorStatus
import cm.aptoide.skills.model.Ticket
import cm.aptoide.skills.util.EskillsPaymentData
import cm.aptoide.skills.util.EskillsUriParser
import dagger.android.support.DaggerFragment
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class SkillsFragment : DaggerFragment() {

  companion object {
    fun newInstance() = SkillsFragment()

    private const val SESSION = "SESSION"
    private const val USER_ID = "USER_ID"
    private const val ROOM_ID = "ROOM_ID"
    private const val WALLET_ADDRESS = "WALLET_ADDRESS"

    private const val WALLET_CREATING_STATUS = "CREATING"
    private const val ESKILLS_URI_KEY = "ESKILLS_URI"
    private const val TRANSACTION_HASH = "transaction_hash"
  }

  @Inject
  lateinit var viewModel: SkillsViewModel

  @Inject
  lateinit var eskillsUriParser: EskillsUriParser

  private var userId: String? = null
  private lateinit var disposable: CompositeDisposable

  private lateinit var binding: FragmentSkillsBinding

  override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    binding = FragmentSkillsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    disposable = CompositeDisposable()

    val eskillsUri = getEskillsUri()
    requireActivity().onBackPressedDispatcher
        .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
          override fun handleOnBackPressed() {
            disposable.add(viewModel.cancelTicket()
                .subscribe { _, _ -> })
          }
        })
    disposable.add(viewModel.closeView()
        .subscribe { postbackUserData(it.first, it.second) })

    userId = eskillsUri.userId
    disposable.add(
        handleWalletCreationIfNeeded()
            .takeUntil { it != WALLET_CREATING_STATUS }
            .flatMapCompletable {
              viewModel.joinQueue(eskillsUri)
                  .observeOn(AndroidSchedulers.mainThread())
                  .doOnSubscribe { showRoomLoading(false) }
                  .flatMapCompletable { handleTicketCreationResult(eskillsUri, it) }
            }
            .subscribe()
    )
  }

  private fun handleTicketCreationResult(eskillsUri: EskillsPaymentData,
                                         ticket: Ticket): Completable {
    return when (ticket) {
      is CreatedTicket -> {
        purchaseTicket(eskillsUri, ticket)
      }
      is FailedTicket -> {
        Completable.fromAction {
          handleFailedTicketResult(ticket)
        }
      }
      else -> return Completable.complete()
    }
  }

  private fun handleFailedTicketResult(ticket: FailedTicket) {
    when (ticket.status) {
      ErrorStatus.REGION_NOT_SUPPORTED -> {
        finishWithError(SkillsViewModel.RESULT_REGION_NOT_SUPPORTED)
      }
      ErrorStatus.NO_NETWORK -> showNoNetworkErrorLayout()
    }
  }

  private fun purchaseTicket(eskillsUri: EskillsPaymentData,
                             ticket: CreatedTicket): Completable {
    return viewModel.getRoom(eskillsUri, ticket, this)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { userData -> handleUserDataStatus(userData) }
        .ignoreElements()
  }

  private fun handleUserDataStatus(userData: UserData) {
    when (userData.status) {
      UserData.Status.IN_QUEUE, UserData.Status.PAYING -> showRoomLoading(true)
      UserData.Status.REFUNDED -> showRefundedLayout()
      UserData.Status.COMPLETED -> postbackUserData(SkillsViewModel.RESULT_OK, userData)
      UserData.Status.FAILED -> finishWithError(SkillsViewModel.RESULT_ERROR)
    }
  }

  private fun showNoNetworkErrorLayout() {
    binding.loadingTicketLayout.processingLoading.visibility = View.GONE
    binding.refundTicketLayout.root.visibility = View.GONE
    binding.noNetworkLayout.root.visibility = View.VISIBLE
    binding.noNetworkLayout.noNetworkOkButton.setOnClickListener {
      finishWithError(SkillsViewModel.RESULT_NO_NETWORK)
    }
  }

  private fun finishWithError(errorCode: Int) {
    requireActivity().setResult(errorCode)
    requireActivity().finish()
  }

  private fun showRefundedLayout() {
    binding.loadingTicketLayout.processingLoading.visibility = View.GONE
    binding.refundTicketLayout.refund.visibility = View.VISIBLE
    binding.refundTicketLayout.refundOkButton.setOnClickListener { requireActivity().finish() }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == viewModel.getPayTicketRequestCode() && resultCode == SkillsViewModel.RESULT_OK) {
      if (data == null || data.extras!!.getString(TRANSACTION_HASH) == null) {
        disposable.add(viewModel.cancelTicket()
            .subscribe { _, _ -> })
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }

  override fun onDestroyView() {
    disposable.clear()
    super.onDestroyView()
  }

  private fun getEskillsUri(): EskillsPaymentData {
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

  private fun showRoomLoading(isCancelActive: Boolean) {
    binding.loadingTicketLayout.processingLoading.visibility = View.VISIBLE
    if (isCancelActive) {
      binding.loadingTicketLayout.loadingTitle.text = getString(R.string.finding_room_loading_title)
      binding.loadingTicketLayout.cancelButton.isEnabled = true
      binding.loadingTicketLayout.cancelButton.setOnClickListener {
        disposable.add(viewModel.cancelTicket()
            .subscribe { _, _ -> })
      }
    } else {
      binding.loadingTicketLayout.loadingTitle.text = getString(R.string.processing_loading_title)
    }
  }

  private fun postbackUserData(resultCode: Int, userData: UserData) {
    if (resultCode == SkillsViewModel.RESULT_OK) {
      startBackgroundGameService(userData)
    }
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

  private fun startBackgroundGameService(userData: UserData) {
    val intent = BackgroundGameService.newIntent(requireContext(), userData.session)
    context?.startService(intent)
  }
}
