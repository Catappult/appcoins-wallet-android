package cm.aptoide.skills

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cm.aptoide.skills.databinding.FragmentSkillsBinding
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.util.DeviceScreenManager
import cm.aptoide.skills.util.EskillsUri
import cm.aptoide.skills.util.EskillsUriParser
import cm.aptoide.skills.util.KeyboardUtils
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SkillsFragment : DaggerFragment() {

  companion object {
    fun newInstance() = SkillsFragment()

    private const val RESULT_OK = 1
    private const val SESSION = "SESSION"
    private const val USER_ID = "USER_ID"
    private const val ROOM_ID = "ROOM_ID"
    private const val WALLET_ADDRESS = "WALLET_ADDRESS"
    private const val TRANSACTION_HASH = "transaction_hash"

    private const val SHARED_PREFERENCES_NAME = "SKILL_SHARED_PREFERENCES"
    private const val PREFERENCES_USER_NAME = "PREFERENCES_USER_NAME"
    private const val WALLET_CREATING_STATUS = "CREATING"
    private const val ESKILLS_URI_KEY = "ESKILLS_URI"
  }

  @Inject
  lateinit var viewModel: SkillsViewModel

  @Inject
  lateinit var eskillsUriParser: EskillsUriParser

  private lateinit var userId: String
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
    loadUserName()
    setTicketDetails(eskillsUri)

    binding.createTicketLayout.findRoomButton.setOnClickListener {
      val userName = binding.createTicketLayout.userName.text.toString()
      saveUserName(userName)
      KeyboardUtils.hideKeyboard(view)
      DeviceScreenManager.keepAwake(activity!!.window)

      disposable.add(
          handleWalletCreationIfNeeded()
              .takeUntil { it != WALLET_CREATING_STATUS }
              .flatMap {
                viewModel.getRoom(eskillsUri, userName, this)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { showFindingRoomLoading() }
                    .doOnNext { userData ->
                      DeviceScreenManager.stopKeepAwake(activity!!.window)
                      requireActivity().setResult(RESULT_OK, buildDataIntent(userData))
                      requireActivity().finish()
                    }
                    .doOnNext { ticket -> println("ticket: $ticket") }
              }.subscribe()
      )
    }
  }

  private fun getEskillsUri(): EskillsUri {
    val intent = requireActivity().intent
    return eskillsUriParser.parse(Uri.parse(intent.getStringExtra(ESKILLS_URI_KEY)))
  }

  private fun setTicketDetails(eskillsUri: EskillsUri) {
    val packageName = eskillsUri.getPackageName()

    val applicationIcon = viewModel.getApplicationIcon(packageName)
    if (applicationIcon != null) {
      binding.createTicketLayout.createTicketHeader.appIcon.setImageDrawable(applicationIcon)
    }

    val applicationName = viewModel.getApplicationName(packageName)
    if (applicationName != null) {
      binding.createTicketLayout.createTicketHeader.appName.text = applicationName
    }

    binding.createTicketLayout.createTicketHeader.appSkuDescription.text = eskillsUri.getProductLabel()
    binding.createTicketLayout.createTicketHeader.fiatPrice.text = eskillsUri.getFormattedPrice()
  }

  private fun saveUserName(userName: String) {
    requireContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        .edit()
        .putString(
            PREFERENCES_USER_NAME, userName)
        .apply()
  }

  private fun loadUserName() {
    val sharedPreferences =
        requireContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    binding.createTicketLayout.userName.setText(sharedPreferences.getString(PREFERENCES_USER_NAME, ""))
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == viewModel.getPayTicketRequestCode() && resultCode == RESULT_OK) {
      viewModel.payTicketOnActivityResult(resultCode, data!!.extras
      !!.getString(TRANSACTION_HASH))
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }

  override fun onDestroyView() {
    disposable.clear()
    super.onDestroyView()
  }

  private fun showFindingRoomLoading() {
    binding.createTicketLayout.createTicketMainView.visibility = View.GONE
    binding.loadingTicketLayout.processingLoading.visibility = View.VISIBLE
  }

  private fun buildDataIntent(userData: UserData): Intent {
    val intent = Intent()

    intent.putExtra(SESSION, userData.session)
    intent.putExtra(USER_ID, userData.userId)
    intent.putExtra(ROOM_ID, userData.roomId)
    intent.putExtra(WALLET_ADDRESS, userData.walletAddress)

    return intent
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

  fun showWalletCreationLoadingAnimation() {
    binding.createTicketLayout.createTicketMainView.visibility = View.GONE
    binding.createWalletLayout.createWalletCard.visibility = View.VISIBLE
    binding.createWalletLayout.createWalletAnimation.playAnimation()
  }

  fun endWalletCreationLoadingAnimation() {
    binding.createWalletLayout.createWalletCard.visibility = View.GONE
  }
}
