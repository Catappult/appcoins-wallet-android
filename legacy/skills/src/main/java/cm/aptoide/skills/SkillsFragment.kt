package cm.aptoide.skills

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import cm.aptoide.skills.databinding.FragmentSkillsBinding
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.games.BackgroundGameService
import cm.aptoide.skills.interfaces.PaymentView
import cm.aptoide.skills.model.*
import cm.aptoide.skills.usecase.Status
import cm.aptoide.skills.util.EskillsPaymentData
import cm.aptoide.skills.util.EskillsUriParser
import cm.aptoide.skills.util.RootUtil
import cm.aptoide.skills.util.UriValidationResult
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.internal.format
import javax.inject.Inject


@AndroidEntryPoint
class SkillsFragment : Fragment(), PaymentView {

  companion object {
    fun newInstance() = SkillsFragment()

    private const val SESSION = "SESSION"
    private const val USER_ID = "USER_ID"
    private const val ROOM_ID = "ROOM_ID"
    private const val WALLET_ADDRESS = "WALLET_ADDRESS"

    private const val WALLET_CREATING_STATUS = "CREATING"
    private const val ESKILLS_URI_KEY = "ESKILLS_URI"
    private const val ESKILLS_ONBOARDING_KEY = "ESKILLS_ONBOARDING"
    private const val ESKILLS_REFERRAL_KEY = "ESKILLS_REFERRAL"

    private const val CLIPBOARD_TOOLTIP_DELAY_SECONDS = 3000L
    private const val BONUS_VALUE = 1
  }

  private val viewModel: SkillsViewModel by viewModels()

  @Inject
  lateinit var eskillsUriParser: EskillsUriParser

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

    requireActivity().onBackPressedDispatcher
      .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          disposable.add(viewModel.cancelTicket()
            .subscribe { _, _ -> })
        }
      })
    disposable.add(viewModel.closeView()
      .subscribe { postbackUserData(it.first, it.second) })

    showPurchaseTicketLayout()

    binding.payTicketLayout.dialogBuyButtonsPaymentMethods.cancelButton.setOnClickListener {
      viewModel.cancelPayment()
    }
  }

  override fun onResume() {
    super.onResume()
    showPurchaseTicketLayout()
  }

  private fun showPurchaseTicketLayout() {
    when (val eSkillsPaymentData = getEskillsUri()) {
      is UriValidationResult.Invalid -> showError(eSkillsPaymentData.requestCode)
      is UriValidationResult.Valid -> setupPurchaseTicketLayout(eSkillsPaymentData.paymentData)
    }
    binding.payTicketLayout.root.visibility = View.VISIBLE
  }


  private fun setupQueueIdLayout() {
    binding.payTicketLayout.payTicketRoomDetails.openCardButton.setOnClickListener {
      if (binding.payTicketLayout.payTicketRoomDetails.roomCreateBody.visibility == View.GONE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          binding.payTicketLayout.payTicketRoomDetails.createRoomTitle.setTextAppearance(
            R.style.DialogTitleStyle
          )
        } else {
          binding.payTicketLayout.payTicketRoomDetails.createRoomTitle.setTextAppearance(
            requireContext(),
            R.style.DialogTitleStyle
          )
        }
        binding.payTicketLayout.payTicketRoomDetails.openCardButton.rotation = 180F
        binding.payTicketLayout.payTicketRoomDetails.roomCreateBody.visibility = View.VISIBLE
      } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          binding.payTicketLayout.payTicketRoomDetails.createRoomTitle.setTextAppearance(
            R.style.DialogTextStyle
          )
        } else {
          binding.payTicketLayout.payTicketRoomDetails.createRoomTitle.setTextAppearance(
            requireContext(),
            R.style.DialogTextStyle
          )
        }
        binding.payTicketLayout.payTicketRoomDetails.openCardButton.rotation = 0F
        binding.payTicketLayout.payTicketRoomDetails.roomCreateBody.visibility = View.GONE
      }
    }
  }

  private fun setupPurchaseTicketLayout(
    eSkillsPaymentData: EskillsPaymentData
  ) {
    if(getCachedValue(ESKILLS_ONBOARDING_KEY)){
      if(viewModel.userFirstTimeCheck()) {
        setupOnboarding(eSkillsPaymentData)
      }
      else{
        cacheValue(ESKILLS_ONBOARDING_KEY,false)
        setupPurchaseLayout(eSkillsPaymentData)
      }
    }
    else{
      setupPurchaseLayout(eSkillsPaymentData)
    }
  }

  private fun setupOnboarding(eSkillsPaymentData: EskillsPaymentData) {
    eSkillsPaymentData.environment = EskillsPaymentData.MatchEnvironment.SANDBOX
    binding.onboardingLayout.root.visibility = View.VISIBLE
    setupAppNameAndIcon(eSkillsPaymentData.packageName, true)
    setupOnboardingTicketButtons(eSkillsPaymentData)
  }

  private fun setupPurchaseLayout(eSkillsPaymentData: EskillsPaymentData){
    setupQueueIdLayout()
    if (eSkillsPaymentData.environment == EskillsPaymentData.MatchEnvironment.SANDBOX) {
      setupSandboxTicketButtons(eSkillsPaymentData)
    } else {
      updateHeaderInfo(eSkillsPaymentData)
      setupPurchaseTicketButtons(eSkillsPaymentData)
    }
    setupAppNameAndIcon(eSkillsPaymentData.packageName,false)}

  private fun setupOnboardingTicketButtons(eSkillsPaymentData: EskillsPaymentData) {
    if (RootUtil.isDeviceRooted()) {
      showRootError()
    }
    else{
      binding.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.text = getString(R.string.start_button)
      binding.onboardingLayout.referralDisplay.tooltip.referralCode.text =
        String.format(getString(R.string.refer_a_friend_first_time_tooltip),BONUS_VALUE)
      val tooltip_btn = binding.onboardingLayout.referralDisplay.actionButtonTooltipReferral
      tooltip_btn
        .setOnClickListener {
          if (binding.onboardingLayout.referralDisplay.tooltip.root.visibility == View.GONE){
            binding.onboardingLayout.referralDisplay.tooltip.root.visibility = View.VISIBLE
          }
          else{
            binding.onboardingLayout.referralDisplay.tooltip.root.visibility = View.GONE
          }
        }
      binding.onboardingLayout.dialogBuyButtonsPaymentMethods.cancelButton.setOnClickListener {
        viewModel.cancelPayment()
      }
      binding.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
        binding.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = false
        val referralCode = binding.onboardingLayout.referralDisplay.referralCode.text.toString()
        if (!referralCode.isEmpty()){
          when (viewModel.useReferralCode(referralCode)) {
            is FailedReferral.GenericError -> {
              binding.onboardingLayout.referralDisplay.referralCode.setTextColor(
                Color.RED)
              binding.onboardingLayout.referralDisplay.errorMessage.visibility = View.VISIBLE
              binding.onboardingLayout.referralDisplay.errorMessage.text = getString(R.string.refer_a_friend_error_unavailable_body)
              binding.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = true
            }
            is FailedReferral.NotEligibleError -> {
              binding.onboardingLayout.referralDisplay.referralCode.setTextColor(
                Color.RED)
              binding.onboardingLayout.referralDisplay.errorMessage.visibility = View.VISIBLE
              binding.onboardingLayout.referralDisplay.errorMessage.text = getString(R.string.refer_a_friend_error_user_not_eligible_body)
              binding.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = true
            }
            is FailedReferral.NotFoundError -> {
              binding.onboardingLayout.referralDisplay.referralCode.setTextColor(
                Color.RED)
              binding.onboardingLayout.referralDisplay.errorMessage.visibility = View.VISIBLE
              binding.onboardingLayout.referralDisplay.errorMessage.text = getString(R.string.refer_a_friend_error_invalid_code_body)
              binding.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = true
            }
            is SuccessfulReferral -> {
              binding.onboardingLayout.root.visibility = View.GONE
              createAndPayTicket(eSkillsPaymentData,true)
            }

          }
        }
        else{
          binding.onboardingLayout.root.visibility = View.GONE
          createAndPayTicket(eSkillsPaymentData,true)
        }
      }
    }
  }

  private fun setupSandboxTicketButtons(eSkillsPaymentData: EskillsPaymentData) {
    if (RootUtil.isDeviceRooted()) {
      showRootError()
    } else {
      hidePaymentRelatedText()
      binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
        binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = false
        val queueId = binding.payTicketLayout.payTicketRoomDetails.roomId.text.toString()
        if (queueId.isNotBlank()) {
          eSkillsPaymentData.queueId = QueueIdentifier(queueId.trim(), true)
        }
        binding.payTicketLayout.root.visibility = View.GONE
        createAndPayTicket(eSkillsPaymentData)
      }
    }
  }

  private fun setupPurchaseTicketButtons(
    eSkillsPaymentData: EskillsPaymentData
  ) {

    binding.payTicketLayout.payTicketRoomDetails.copyButton.setOnClickListener {
      val queueId = binding.payTicketLayout.payTicketRoomDetails.roomId.text.toString()
      if (queueId.isNotEmpty()) {
        viewModel.saveQueueIdToClipboard(queueId.trim())
        val tooltip = binding.payTicketLayout.payTicketRoomDetails.tooltipClipboard
        tooltip.visibility = View.VISIBLE
        view?.postDelayed({ tooltip.visibility = View.GONE }, CLIPBOARD_TOOLTIP_DELAY_SECONDS)
      }
    }
    disposable.add(Single.zip(
      viewModel.getCreditsBalance(),
      viewModel.getFiatToAppcAmount(eSkillsPaymentData.price!!, eSkillsPaymentData.currency!!),
      { balance, appcAmount -> Pair(balance, appcAmount) })
      .observeOn(AndroidSchedulers.mainThread())
      .map {
        if (it.first < it.second.amount) { // Not enough funds
          showNoFundsWarning()
          binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.text =
            getString(R.string.topup_button)
          binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
            sendUserToTopUpFlow()
          }
        } else if (viewModel.getVerification() == EskillsVerification.VERIFIED) {
          binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.text =
            getString(R.string.buy_button)
          binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
            binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = false
            val queueId = binding.payTicketLayout.payTicketRoomDetails.roomId.text.toString()
            if (queueId.isNotBlank()) {
              eSkillsPaymentData.queueId = QueueIdentifier(queueId.trim(), true)
            }
            binding.payTicketLayout.root.visibility = View.GONE
            createAndPayTicket(eSkillsPaymentData)
          }
        } else if (RootUtil.isDeviceRooted()) {
          showRootError()
        } else {
          when (getTopUpListStatus()) {
            Status.AVAILABLE -> {
              binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.text =
                getString(R.string.buy_button)
              binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
                binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = false
                val queueId = binding.payTicketLayout.payTicketRoomDetails.roomId.text.toString()
                if (queueId.isNotBlank()) {
                  eSkillsPaymentData.queueId = QueueIdentifier(queueId.trim(), true)
                }
                binding.payTicketLayout.root.visibility = View.GONE
                createAndPayTicket(eSkillsPaymentData)
              }
            }
            Status.NO_TOPUP -> {
              showNeedsTopUpWarning()
              binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.text =
                getString(R.string.topup_button)
              binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
                sendUserToTopUpFlow()
              }
            }
            Status.PAYMENT_METHOD_NOT_SUPPORTED -> {
              showPaymentMethodNotSupported()
              binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.visibility =
                View.GONE
            }
          }
        }
      }
      .subscribe())
  }

  private fun getTopUpListStatus(): Status {
    return viewModel.getTopUpListStatus()
  }

  private fun sendUserToTopUpFlow() {
    viewModel.sendUserToTopUpFlow(requireContext())
  }

  private fun setupAppNameAndIcon(packageName: String, onboarding: Boolean) {
    val packageManager = requireContext().packageManager
    val appInfo = packageManager.getApplicationInfo(packageName, 0)
    val appName = packageManager.getApplicationLabel(appInfo)
    val appIcon = packageManager.getApplicationIcon(packageName)
    if(onboarding){
      binding.onboardingLayout.appIcon.setImageDrawable(appIcon)
    }
    else{
      binding.payTicketLayout.payTicketHeader.appName.text = appName
      binding.payTicketLayout.payTicketHeader.appIcon.setImageDrawable(appIcon)
    }
  }

  private fun updateHeaderInfo(eSkillsPaymentData: EskillsPaymentData) {
    val details = binding.payTicketLayout.payTicketPaymentMethodsDetails
    disposable.addAll(
      viewModel.getLocalFiatAmount(eSkillsPaymentData.price!!, eSkillsPaymentData.currency!!)
        .observeOn(AndroidSchedulers.mainThread())
        .map {
          details.fiatPrice.text = "${it.amount} ${it.currency}"
          details.fiatPriceSkeleton.visibility = View.GONE
          details.fiatPrice.visibility = View.VISIBLE
        }
        .subscribe(),
      viewModel.getFormattedAppcAmount(
        eSkillsPaymentData.price!!,
        eSkillsPaymentData.currency!!
      )
        .observeOn(AndroidSchedulers.mainThread())
        .map {
          details.appcPrice.text = "$it APPC"
          details.appcPriceSkeleton.visibility = View.GONE
          details.appcPrice.visibility = View.VISIBLE
        }
        .subscribe()
    )
  }

  private fun createAndPayTicket(eskillsPaymentData: EskillsPaymentData, onboarding: Boolean = false) {
    if(eskillsPaymentData.environment == EskillsPaymentData.MatchEnvironment.LIVE && getCachedValue(
        ESKILLS_REFERRAL_KEY))
      getReferralAndActivateLayout()
    disposable.add(
      handleWalletCreationIfNeeded()
        .takeUntil { it != WALLET_CREATING_STATUS }
        .flatMapCompletable {
          viewModel.joinQueue(eskillsPaymentData)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showRoomLoading(false) }
            .flatMapCompletable { handleTicketCreationResult(eskillsPaymentData, it) }
             }
        .doOnComplete{if(onboarding){cacheValue(ESKILLS_ONBOARDING_KEY,false)}}
        .subscribe()
    )
  }

  private fun handleTicketCreationResult(
    eskillsUri: EskillsPaymentData,
    ticket: Ticket
  ): Completable {
    return when (ticket) {
      is CreatedTicket -> purchaseTicket(eskillsUri, ticket)
      is FailedTicket -> Completable.fromAction { handleFailedTicketResult(ticket) }
      is PurchasedTicket -> return Completable.complete()
    }
  }

  private fun handleFailedTicketResult(ticket: FailedTicket) {
    when (ticket.status) {
      ErrorStatus.REGION_NOT_SUPPORTED -> showRegionNotSupportedError()
      ErrorStatus.WALLET_VERSION_NOT_SUPPORTED -> showWalletVersionNotSupportedError()
      ErrorStatus.NO_NETWORK -> showNoNetworkError()
      ErrorStatus.GENERIC -> showError(SkillsViewModel.RESULT_ERROR)
    }
  }

  private fun purchaseTicket(
    eskillsUri: EskillsPaymentData,
    ticket: CreatedTicket
  ): Completable {
    return viewModel.getRoom(eskillsUri, ticket, this)
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext { userData -> handleUserDataStatus(userData) }
      .ignoreElements()
  }

  private fun handleUserDataStatus(userData: UserData) {
    when (userData.status) {
      UserData.Status.IN_QUEUE, UserData.Status.PAYING -> showRoomLoading(true, userData.queueId)
      UserData.Status.REFUNDED -> showRefundedLayout()
      UserData.Status.COMPLETED -> postbackUserData(SkillsViewModel.RESULT_OK, userData)
      UserData.Status.FAILED -> showError(SkillsViewModel.RESULT_ERROR)
    }
  }

  private fun hidePaymentRelatedText() {
    binding.payTicketLayout.payTicketPaymentMethodsDetails.appcCreditsIcon?.visibility = View.GONE
    binding.payTicketLayout.payTicketPaymentMethodsDetails.paymentTitle?.visibility = View.GONE
    binding.payTicketLayout.payTicketPaymentMethodsDetails.paymentBody?.visibility = View.GONE
    binding.payTicketLayout.payTicketPaymentMethodsDetails.fiatPrice.visibility = View.GONE
    binding.payTicketLayout.payTicketPaymentMethodsDetails.appcPrice.visibility = View.GONE
    binding.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.text = getString(R.string.ok)
  }

  private fun showRegionNotSupportedError() {
    binding.loadingTicketLayout.root.visibility = View.GONE
    binding.refundTicketLayout.root.visibility = View.GONE
    binding.walletVersionNotSupportedLayout.root.visibility = View.GONE
    binding.errorLayout.root.visibility = View.GONE
    binding.geofencingLayout.root.visibility = View.VISIBLE
    binding.geofencingLayout.okButton.setOnClickListener {
      finishWithError(SkillsViewModel.RESULT_REGION_NOT_SUPPORTED)
    }
  }

  private fun finishWithError(errorCode: Int) {
    requireActivity().setResult(errorCode)
    requireActivity().finish()
  }

  private fun showRefundedLayout() {
    binding.loadingTicketLayout.root.visibility = View.GONE
    binding.refundTicketLayout.root.visibility = View.VISIBLE
    binding.refundTicketLayout.refundOkButton.setOnClickListener { requireActivity().finish() }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == SkillsViewModel.AUTHENTICATION_REQUEST_CODE) {
      handleAuthenticationResult(resultCode)
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }

  override fun onDestroyView() {
    disposable.clear()
    super.onDestroyView()
  }

  private fun getEskillsUri(): UriValidationResult {
    val intent = requireActivity().intent
    return viewModel.validateUrl(intent.getStringExtra(ESKILLS_URI_KEY)!!)
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
    binding.createWalletLayout.root.visibility = View.VISIBLE
    binding.createWalletLayout.createWalletAnimation.playAnimation()
  }

  private fun endWalletCreationLoadingAnimation() {
    binding.createWalletLayout.root.visibility = View.GONE
  }

  private fun showRoomLoading(isCancelActive: Boolean, queueIdentifier: QueueIdentifier? = null) {
    if (isCancelActive) {
      if (queueIdentifier != null && queueIdentifier.setByUser) {
        binding.loadingTicketLayout.loadingTitle.text = SpannableStringBuilder()
          .append(getString(R.string.finding_room_name_loading_title))
          .bold { append(" ${queueIdentifier.id}") }
      } else {
        binding.loadingTicketLayout.loadingTitle.text =
          getString(R.string.finding_room_loading_title)
      }
      binding.loadingTicketLayout.cancelButton.isEnabled = true
      binding.loadingTicketLayout.cancelButton.setOnClickListener {
        disposable.add(viewModel.cancelTicket()
          .subscribe { _, _ -> })
      }
    } else {
      binding.loadingTicketLayout.loadingTitle.text = getString(R.string.processing_loading_title)
    }
    binding.loadingTicketLayout.root.visibility = View.VISIBLE
  }


  private fun getReferralAndActivateLayout() {
    disposable.add(viewModel.getReferral()
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { referralResponse ->
        if (referralResponse.available) {
          setReferralLayout(referralResponse)
          binding.loadingTicketLayout.referralShareDisplay.baseConstraint.visibility = View.VISIBLE
        }
        else{
          if (referralResponse.count!= 0)//If not default error Referral
            cacheValue(ESKILLS_REFERRAL_KEY,false)
        }
      }
      .subscribe())
  }

  private fun setReferralLayout(referralResponse: ReferralResponse) {
    binding.loadingTicketLayout.referralShareDisplay.actionButtonShareReferral
      .setOnClickListener {
        startActivity(viewModel.buildShareIntent(referralResponse.referralCode))
      }
    binding.loadingTicketLayout.referralShareDisplay.tooltip.popupText.text =
      String.format(getString(R.string.refer_a_friend_waiting_room_tooltip), '1')
    val tooltipBtn =
      binding.loadingTicketLayout.referralShareDisplay.actionButtonTooltipReferral
    tooltipBtn
      .setOnClickListener {
        if (binding.loadingTicketLayout.referralShareDisplay.tooltip.root.visibility == View.VISIBLE) {
          binding.loadingTicketLayout.referralShareDisplay.tooltip.root.visibility = View.INVISIBLE
          tooltipBtn.setImageDrawable(
            ContextCompat.getDrawable(
              requireContext(),
              R.drawable.tooltip_orange
            )
          )
        } else {
          binding.loadingTicketLayout.referralShareDisplay.tooltip.root.visibility =
            View.VISIBLE
          tooltipBtn.setImageDrawable(
            ContextCompat.getDrawable(
              requireContext(),
              R.drawable.tooltip_white
            )
          )
        }

      }
    binding.loadingTicketLayout.root
      .setOnClickListener {
        if (binding.loadingTicketLayout.referralShareDisplay.tooltip.root.visibility == View.VISIBLE) {
          binding.loadingTicketLayout.referralShareDisplay.tooltip.root.visibility = View.INVISIBLE
          binding.loadingTicketLayout.referralShareDisplay.actionButtonTooltipReferral.colorFilter =
            null
        }
      }
    binding.loadingTicketLayout.referralShareDisplay.referralCode.text =
      referralResponse.referralCode

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
    intent.putExtra(WALLET_ADDRESS, userData.walletAddress?.address)

    return intent
  }

  private fun startBackgroundGameService(userData: UserData) {
    val intent = BackgroundGameService.newIntent(requireContext(), userData.session)
    context?.startService(intent)
  }

  override fun showLoading() {
    binding.geofencingLayout.root.visibility = View.GONE
    binding.walletVersionNotSupportedLayout.root.visibility = View.GONE
    binding.refundTicketLayout.root.visibility = View.GONE
    binding.errorLayout.root.visibility = View.GONE
    binding.loadingTicketLayout.loadingTitle.text = getString(R.string.processing_payment_title)
    binding.loadingTicketLayout.root.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    showRoomLoading(false)
  }

  override fun showError(errorCode: Int) {
    binding.loadingTicketLayout.root.visibility = View.GONE
    binding.refundTicketLayout.root.visibility = View.GONE
    binding.geofencingLayout.root.visibility = View.GONE
    binding.walletVersionNotSupportedLayout.root.visibility = View.GONE
    binding.errorLayout.root.visibility = View.VISIBLE
    binding.errorLayout.errorOkButton.setOnClickListener {
      finishWithError(errorCode)
    }
  }

  override fun showFraudError(isVerified: Boolean) {
    if (!isVerified) {
      viewModel.sendUserToVerificationFlow(requireContext())
      finishWithError(SkillsViewModel.RESULT_ERROR)
    } else {
      showError(SkillsViewModel.RESULT_ERROR)
    }
  }

  override fun showNoNetworkError() {
    binding.loadingTicketLayout.root.visibility = View.GONE
    binding.refundTicketLayout.root.visibility = View.GONE
    binding.errorLayout.root.visibility = View.GONE
    binding.noNetworkLayout.root.visibility = View.VISIBLE
    binding.noNetworkLayout.noNetworkOkButton.setOnClickListener {
      finishWithError(SkillsViewModel.RESULT_SERVICE_UNAVAILABLE)
    }
  }

  override fun showRootError() {
    binding.errorLayout.errorMessage.text = getString(R.string.rooted_device_blocked_body)
    showError(SkillsViewModel.RESULT_ROOT_ERROR)
  }

  override fun showWalletVersionNotSupportedError() {
    binding.loadingTicketLayout.root.visibility = View.GONE
    binding.refundTicketLayout.root.visibility = View.GONE
    binding.errorLayout.root.visibility = View.GONE
    binding.geofencingLayout.root.visibility = View.GONE
    binding.walletVersionNotSupportedLayout.root.visibility = View.VISIBLE
    binding.walletVersionNotSupportedLayout.updateButton.setOnClickListener {
      startActivity(viewModel.buildUpdateIntent())
      finishWithError(SkillsViewModel.RESULT_WALLET_VERSION_ERROR)
    }
  }

  // Only temporary
  override fun showNeedsTopUpWarning() {
    binding.errorLayout.errorMessage.text = getString(R.string.top_up_needed_body)
    binding.loadingTicketLayout.root.visibility = View.GONE
    binding.refundTicketLayout.root.visibility = View.GONE
    binding.geofencingLayout.root.visibility = View.GONE
    binding.walletVersionNotSupportedLayout.root.visibility = View.GONE
    binding.errorLayout.root.visibility = View.VISIBLE
    binding.errorLayout.errorOkButton.setOnClickListener {
      binding.errorLayout.root.visibility = View.GONE
    }
  }

  override fun showPaymentMethodNotSupported() {
    binding.errorLayout.errorMessage.text =
      getString(R.string.error_message_local_payment_method_body)
    binding.loadingTicketLayout.root.visibility = View.GONE
    binding.refundTicketLayout.root.visibility = View.GONE
    binding.geofencingLayout.root.visibility = View.GONE
    binding.errorLayout.root.visibility = View.VISIBLE
    binding.errorLayout.errorOkButton.setOnClickListener {
      binding.errorLayout.root.visibility = View.GONE
    }
  }

  override fun showNoFundsWarning() {
    binding.errorLayout.errorMessage.text = getString(R.string.not_enough_funds_body)
    binding.loadingTicketLayout.root.visibility = View.GONE
    binding.refundTicketLayout.root.visibility = View.GONE
    binding.geofencingLayout.root.visibility = View.GONE
    binding.errorLayout.root.visibility = View.VISIBLE
    binding.errorLayout.errorOkButton.setOnClickListener {
      binding.errorLayout.root.visibility = View.GONE
    }
  }

  override fun showFingerprintAuthentication() {
    val intent = viewModel.getAuthenticationIntent(requireContext())
    startActivityForResult(intent, SkillsViewModel.AUTHENTICATION_REQUEST_CODE)
  }

  private fun handleAuthenticationResult(resultCode: Int) {
    if (resultCode == SkillsViewModel.RESULT_OK) {
      viewModel.restorePurchase(this)
        .subscribe()
    } else if (resultCode == SkillsViewModel.RESULT_USER_CANCELED) {
      viewModel.closeView()
    }
  }

  private fun getCachedValue(key: String): Boolean{
    val sharedPreferences = requireContext().getSharedPreferences(key, 0)
    return sharedPreferences.getBoolean(key, true)
  }
  private fun cacheValue(key: String,value: Boolean ){
    val sharedPreferences = requireContext().getSharedPreferences(key, 0)
    sharedPreferences.edit().putBoolean(key, value).apply()
  }
}