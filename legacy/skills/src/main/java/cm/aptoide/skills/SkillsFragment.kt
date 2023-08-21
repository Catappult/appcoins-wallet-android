package cm.aptoide.skills

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import cm.aptoide.skills.databinding.FragmentSkillsBinding
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.games.BackgroundGameService
import cm.aptoide.skills.interfaces.PaymentView
import cm.aptoide.skills.model.CreatedTicket
import cm.aptoide.skills.model.ErrorStatus
import cm.aptoide.skills.model.EskillsVerification
import cm.aptoide.skills.model.FailedReferral
import cm.aptoide.skills.model.FailedTicket
import cm.aptoide.skills.model.PurchasedTicket
import cm.aptoide.skills.model.SuccessfulReferral
import cm.aptoide.skills.model.Ticket
import cm.aptoide.skills.usecase.Status
import cm.aptoide.skills.util.RootUtil
import cm.aptoide.skills.util.UriValidationResult
import com.appcoins.wallet.core.analytics.analytics.legacy.SkillsAnalytics
import com.appcoins.wallet.core.network.eskills.model.EskillsPaymentData
import com.appcoins.wallet.core.network.eskills.model.QueueIdentifier
import com.appcoins.wallet.core.network.eskills.model.ReferralResponse
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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

    private lateinit var ESKILLS_PAYMENT_DATA: EskillsPaymentData

    private const val CLIPBOARD_TOOLTIP_DELAY_SECONDS = 3000L
    private const val BONUS_VALUE = 1
  }

  private val viewModel: SkillsViewModel by viewModels()

  private lateinit var disposable: CompositeDisposable

  @Inject
  lateinit var analytics: SkillsAnalytics

  private val views by viewBinding(FragmentSkillsBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View = FragmentSkillsBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    disposable = CompositeDisposable()

    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
          override fun handleOnBackPressed() {
            disposable.add(viewModel.cancelTicket().subscribe { _, _ -> })
          }
        })
    disposable.add(viewModel.closeView().subscribe { postbackUserData(it.first, it.second) })

    showPurchaseTicketLayout()

    views.payTicketLayout.dialogBuyButtonsPaymentMethods.cancelButton.setOnClickListener {
      analytics.sendPaymentCancelEvent(ESKILLS_PAYMENT_DATA)
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
    views.payTicketLayout.root.visibility = View.VISIBLE
  }


  private fun setupQueueIdLayout() {
    views.payTicketLayout.payTicketRoomDetails.openCardButton.setOnClickListener {
      if (views.payTicketLayout.payTicketRoomDetails.roomCreateBody.visibility == View.GONE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          views.payTicketLayout.payTicketRoomDetails.createRoomTitle.setTextAppearance(
            R.style.DialogTitleStyle
          )
        } else {
          views.payTicketLayout.payTicketRoomDetails.createRoomTitle.setTextAppearance(
            requireContext(), R.style.DialogTitleStyle
          )
        }
        views.payTicketLayout.payTicketRoomDetails.openCardButton.rotation = 180F
        views.payTicketLayout.payTicketRoomDetails.roomCreateBody.visibility = View.VISIBLE
      } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          views.payTicketLayout.payTicketRoomDetails.createRoomTitle.setTextAppearance(
            R.style.DialogTextStyle
          )
        } else {
          views.payTicketLayout.payTicketRoomDetails.createRoomTitle.setTextAppearance(
            requireContext(), R.style.DialogTextStyle
          )
        }
        views.payTicketLayout.payTicketRoomDetails.openCardButton.rotation = 0F
        views.payTicketLayout.payTicketRoomDetails.roomCreateBody.visibility = View.GONE
      }
    }
  }

  private fun setupPurchaseTicketLayout(
    eSkillsPaymentData: EskillsPaymentData
  ) {
    ESKILLS_PAYMENT_DATA = eSkillsPaymentData
    if (getCachedValue(ESKILLS_ONBOARDING_KEY)) {
      if (viewModel.userFirstTimeCheck()) {
        setupOnboarding(eSkillsPaymentData)
      } else {
        cacheValue(ESKILLS_ONBOARDING_KEY, false)
        setupPurchaseLayout(eSkillsPaymentData)
      }
    } else {
      setupPurchaseLayout(eSkillsPaymentData)
    }
  }

  private fun setupOnboarding(eSkillsPaymentData: EskillsPaymentData) {
    eSkillsPaymentData.environment = EskillsPaymentData.MatchEnvironment.SANDBOX
    views.onboardingLayout.root.visibility = View.VISIBLE
    setupAppNameAndIcon(eSkillsPaymentData.packageName, true)
    setupOnboardingTicketButtons(eSkillsPaymentData)
    analytics.sendOnboardingLaunchEvent(eSkillsPaymentData)
  }

  private fun setupPurchaseLayout(eSkillsPaymentData: EskillsPaymentData) {
    analytics.sendPaymentLaunchEvent(eSkillsPaymentData)
    setupQueueIdLayout()
    if (eSkillsPaymentData.environment == EskillsPaymentData.MatchEnvironment.SANDBOX) {
      setupSandboxTicketButtons(eSkillsPaymentData)
    } else {
      updateHeaderInfo(eSkillsPaymentData)
      setupPurchaseTicketButtons(eSkillsPaymentData)
    }
    setupAppNameAndIcon(eSkillsPaymentData.packageName, false)
  }

  private fun setupOnboardingTicketButtons(eSkillsPaymentData: EskillsPaymentData) {
    if (RootUtil.isDeviceRooted()) {
      showRootError()
    } else {
      views.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.text =
        getString(R.string.start_button)
      views.onboardingLayout.referralDisplay.tooltip.referralCode.text =
        String.format(getString(R.string.refer_a_friend_first_time_tooltip), BONUS_VALUE)
      val tooltipBtn = views.onboardingLayout.referralDisplay.actionButtonTooltipReferral
      tooltipBtn.setOnClickListener {
          if (views.onboardingLayout.referralDisplay.tooltip.root.visibility == View.GONE) {
            views.onboardingLayout.referralDisplay.tooltip.root.visibility = View.VISIBLE
          } else {
            views.onboardingLayout.referralDisplay.tooltip.root.visibility = View.GONE
          }
        }
      views.onboardingLayout.dialogBuyButtonsPaymentMethods.cancelButton.setOnClickListener {
        viewModel.cancelPayment()
        analytics.sendOnboardingCancelEvent(eSkillsPaymentData)
      }
      views.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
        views.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = false
        val referralCode = views.onboardingLayout.referralDisplay.referralCode.text.toString()
        if (referralCode.isNotEmpty()) {
          when (viewModel.useReferralCode(referralCode)) {
            is FailedReferral.GenericError -> {
              views.onboardingLayout.referralDisplay.referralCode.setTextColor(
                Color.RED
              )
              views.onboardingLayout.referralDisplay.errorMessage.visibility = View.VISIBLE
              views.onboardingLayout.referralDisplay.errorMessage.text =
                getString(R.string.refer_a_friend_error_unavailable_body)
              views.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = true
            }
            is FailedReferral.NotEligibleError -> {
              views.onboardingLayout.referralDisplay.referralCode.setTextColor(
                Color.RED
              )
              views.onboardingLayout.referralDisplay.errorMessage.visibility = View.VISIBLE
              views.onboardingLayout.referralDisplay.errorMessage.text =
                getString(R.string.refer_a_friend_error_user_not_eligible_body)
              views.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = true
            }
            is FailedReferral.NotFoundError -> {
              views.onboardingLayout.referralDisplay.referralCode.setTextColor(
                Color.RED
              )
              views.onboardingLayout.referralDisplay.errorMessage.visibility = View.VISIBLE
              views.onboardingLayout.referralDisplay.errorMessage.text =
                getString(R.string.refer_a_friend_error_invalid_code_body)
              views.onboardingLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = true
            }
            is SuccessfulReferral -> {
              views.onboardingLayout.root.visibility = View.GONE
              createAndPayTicket(eSkillsPaymentData, true)
              analytics.sendOnboardingSuccessEvent(eSkillsPaymentData, referralCode)
            }

          }
        } else {
          views.onboardingLayout.root.visibility = View.GONE
          createAndPayTicket(eSkillsPaymentData, true)
          analytics.sendOnboardingSuccessEvent(eSkillsPaymentData)
        }
      }
    }
  }

  private fun setupSandboxTicketButtons(eSkillsPaymentData: EskillsPaymentData) {
    if (RootUtil.isDeviceRooted()) {
      showRootError()
    } else {
      hidePaymentRelatedText()
      views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
        views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = false
        val queueId = views.payTicketLayout.payTicketRoomDetails.roomId.text.toString()
        if (queueId.isNotBlank()) {
          eSkillsPaymentData.queueId = QueueIdentifier(queueId.trim(), true)
          analytics.sendPaymentQueueIdInputEvent(eSkillsPaymentData)
        }
        views.payTicketLayout.root.visibility = View.GONE
        createAndPayTicket(eSkillsPaymentData)
      }
    }
  }

  private fun setupPurchaseTicketButtons(
    eSkillsPaymentData: EskillsPaymentData
  ) {

    views.payTicketLayout.payTicketRoomDetails.copyButton.setOnClickListener {
      val queueId = views.payTicketLayout.payTicketRoomDetails.roomId.text.toString()
      if (queueId.isNotEmpty()) {
        viewModel.saveQueueIdToClipboard(queueId.trim())
        val tooltip = views.payTicketLayout.payTicketRoomDetails.tooltipClipboard
        tooltip.visibility = View.VISIBLE
        view?.postDelayed({ tooltip.visibility = View.GONE }, CLIPBOARD_TOOLTIP_DELAY_SECONDS)
      }
    }
    disposable.add(Single.zip(viewModel.getCreditsBalance(),
      viewModel.getFiatToAppcAmount(eSkillsPaymentData.price!!, eSkillsPaymentData.currency!!)
    ) { balance, appcAmount -> Pair(balance, appcAmount) }
      .observeOn(AndroidSchedulers.mainThread()).map {
        if (it.first < it.second.amount) { // Not enough funds
          showNoFundsWarning()
          analytics.sendPaymentNoFundsErrorEvent(eSkillsPaymentData)
          views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.text =
            getString(R.string.topup_button)
          views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
            sendUserToTopUpFlow()
          }
        } else if (viewModel.getVerification() == EskillsVerification.VERIFIED) {
          views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.text =
            getString(R.string.buy_button)
          views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
            analytics.sendPaymentBuyClickEvent(eSkillsPaymentData)
            views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = false
            val queueId = views.payTicketLayout.payTicketRoomDetails.roomId.text.toString()
            if (queueId.isNotBlank()) {
              eSkillsPaymentData.queueId = QueueIdentifier(queueId.trim(), true)
              analytics.sendPaymentQueueIdInputEvent(eSkillsPaymentData)
            }
            views.payTicketLayout.root.visibility = View.GONE
            createAndPayTicket(eSkillsPaymentData)
          }
        } else if (RootUtil.isDeviceRooted()) {
          showRootError()
          analytics.sendPaymentRootErrorEvent(eSkillsPaymentData)
        } else {
          when (getTopUpListStatus()) {
            Status.AVAILABLE -> {
              views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.text =
                getString(R.string.buy_button)
              views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
                analytics.sendPaymentBuyClickEvent(eSkillsPaymentData)
                views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = false
                val queueId = views.payTicketLayout.payTicketRoomDetails.roomId.text.toString()
                if (queueId.isNotBlank()) {
                  eSkillsPaymentData.queueId = QueueIdentifier(queueId.trim(), true)
                  analytics.sendPaymentQueueIdInputEvent(eSkillsPaymentData)
                }
                views.payTicketLayout.root.visibility = View.GONE
                createAndPayTicket(eSkillsPaymentData)
              }
            }
            Status.NO_TOPUP -> {
              showNeedsTopUpWarning()
              analytics.sendPaymentTopUpErrorEvent(eSkillsPaymentData)
              views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.text =
                getString(R.string.topup_button)
              views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.setOnClickListener {
                sendUserToTopUpFlow()
              }
            }
            Status.PAYMENT_METHOD_NOT_SUPPORTED -> {
              showPaymentMethodNotSupported()
              analytics.sendPaymentNotSupportedErrorEvent(eSkillsPaymentData)
              views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.visibility = View.GONE
            }
          }
        }
      }.subscribe())
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
    if (onboarding) {
      views.onboardingLayout.appIcon.setImageDrawable(appIcon)
    } else {
      views.payTicketLayout.payTicketHeader.appName.text = appName
      views.payTicketLayout.payTicketHeader.appIcon.setImageDrawable(appIcon)
    }
  }

  private fun updateHeaderInfo(eSkillsPaymentData: EskillsPaymentData) {
    val details = views.payTicketLayout.payTicketPaymentMethodsDetails
    disposable.addAll(viewModel.getLocalFiatAmount(
      eSkillsPaymentData.price!!,
      eSkillsPaymentData.currency!!
    ).observeOn(AndroidSchedulers.mainThread()).map {
        details.fiatPrice.text = "${it.amount} ${it.currency}"
        details.fiatPriceSkeleton.visibility = View.GONE
        details.fiatPrice.visibility = View.VISIBLE
      }.subscribe(), viewModel.getFormattedAppcAmount(
      eSkillsPaymentData.price!!, eSkillsPaymentData.currency!!
    ).observeOn(AndroidSchedulers.mainThread()).map {
        details.appcPrice.text = "$it APPC"
        details.appcPriceSkeleton.visibility = View.GONE
        details.appcPrice.visibility = View.VISIBLE
      }.subscribe())
  }

  private fun createAndPayTicket(eskillsPaymentData: EskillsPaymentData, onboarding: Boolean = false) {
    if(eskillsPaymentData.environment == EskillsPaymentData.MatchEnvironment.LIVE && getCachedValue(
        ESKILLS_REFERRAL_KEY))
      getReferralAndActivateLayout(eskillsPaymentData)
    disposable.add(
      handleWalletCreationIfNeeded()
        .takeUntil { it != WALLET_CREATING_STATUS }
        .flatMapCompletable {
          viewModel.joinQueue(eskillsPaymentData)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showRoomLoading(false) }
            .flatMapCompletable { handleTicketCreationResult(eskillsPaymentData, it) }
             }
        .doOnError{analytics.sendPaymentFailEvent(eskillsPaymentData)}
        .doOnComplete{
          if(onboarding){
          cacheValue(ESKILLS_ONBOARDING_KEY,false)
          }
        analytics.sendPaymentSuccessEvent(eskillsPaymentData)}
        .subscribe()
    )
  }

  private fun handleTicketCreationResult(
    eskillsUri: EskillsPaymentData,
    ticket: Ticket
  ): Completable {
    return when (ticket) {
      is CreatedTicket -> purchaseTicket(eskillsUri, ticket)
      is FailedTicket -> Completable.fromAction { handleFailedTicketResult(ticket, eskillsUri) }
      is PurchasedTicket -> return Completable.complete()
    }
  }

  private fun handleFailedTicketResult(ticket: FailedTicket, eSkillsPaymentData: EskillsPaymentData) {
    when (ticket.status) {
      ErrorStatus.VPN_NOT_SUPPORTED -> {
        showVpnNotSupportedError()
        analytics.sendPaymentVpnErrorEvent(eSkillsPaymentData)
      }
      ErrorStatus.REGION_NOT_SUPPORTED -> {
        showRegionNotSupportedError()
        analytics.sendPaymentGeoErrorEvent(eSkillsPaymentData)
      }
      ErrorStatus.WALLET_VERSION_NOT_SUPPORTED -> {
        showWalletVersionNotSupportedError()
        analytics.sendPaymentWalletVersionErrorEvent(eSkillsPaymentData)
      }
      ErrorStatus.NO_NETWORK -> showNoNetworkError()
      ErrorStatus.GENERIC -> {
        showError(SkillsViewModel.RESULT_ERROR)
        analytics.sendPaymentCreateTicketFailError(eSkillsPaymentData)
      }
    }
  }

  private fun purchaseTicket(
    eskillsUri: EskillsPaymentData,
    ticket: CreatedTicket
  ): Completable {
    return viewModel.getRoom(eskillsUri, ticket, this)
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext { userData -> handleUserDataStatus(userData, eskillsUri) }
      .ignoreElements()
  }

  private fun handleUserDataStatus(userData: UserData, eSkillsPaymentData: EskillsPaymentData) {
    when (userData.status) {
      UserData.Status.IN_QUEUE, UserData.Status.PAYING -> {
        showRoomLoading(true, userData.queueId)
        analytics.sendMatchmakingLaunchEvent(eSkillsPaymentData)
      }
      UserData.Status.REFUNDED -> {
        showRefundedLayout()
        analytics.sendMatchmakingCancelEvent(eSkillsPaymentData)
      }
      UserData.Status.COMPLETED -> {
        postbackUserData(SkillsViewModel.RESULT_OK, userData)
        analytics.sendMatchmakingCompletedEvent(eSkillsPaymentData)
      }
      UserData.Status.FAILED -> {
        showError(SkillsViewModel.RESULT_ERROR)
        analytics.sendMatchmakingErrorEvent(eSkillsPaymentData)
      }
      }
    }

  private fun hidePaymentRelatedText() {
    views.payTicketLayout.payTicketPaymentMethodsDetails.appcCreditsIcon.visibility = View.GONE
    views.payTicketLayout.payTicketPaymentMethodsDetails.paymentTitle.visibility = View.GONE
    views.payTicketLayout.payTicketPaymentMethodsDetails.paymentBody.visibility = View.GONE
    views.payTicketLayout.payTicketPaymentMethodsDetails.fiatPrice.visibility = View.GONE
    views.payTicketLayout.payTicketPaymentMethodsDetails.appcPrice.visibility = View.GONE
    views.payTicketLayout.dialogBuyButtonsPaymentMethods.buyButton.text = getString(R.string.ok)
  }

  private fun showRegionNotSupportedError() {
    views.loadingTicketLayout.root.visibility = View.GONE
    views.refundTicketLayout.root.visibility = View.GONE
    views.walletVersionNotSupportedLayout.root.visibility = View.GONE
    views.errorLayout.root.visibility = View.GONE
    views.geofencingLayout.root.visibility = View.VISIBLE
    views.geofencingLayout.okButton.setOnClickListener {
      finishWithError(SkillsViewModel.RESULT_REGION_NOT_SUPPORTED)
    }
  }

  private fun showVpnNotSupportedError() {
    views.loadingTicketLayout.root.visibility = View.GONE
    views.refundTicketLayout.root.visibility = View.GONE
    views.walletVersionNotSupportedLayout.root.visibility = View.GONE
    views.errorLayout.root.visibility = View.GONE
    views.geofencingLayout.root.visibility = View.VISIBLE
    views.geofencingLayout.errorTitle.text = getString(R.string.error_vpn_not_permitted_title)
    views.geofencingLayout.errorMessage.visibility = View.GONE
    views.geofencingLayout.okButton.setOnClickListener {
      finishWithError(SkillsViewModel.RESULT_VPN_NOT_SUPPORTED)
    }
  }

  private fun finishWithError(errorCode: Int) {
    requireActivity().setResult(errorCode)
    requireActivity().finish()
  }

  private fun showRefundedLayout() {
    views.loadingTicketLayout.root.visibility = View.GONE
    views.refundTicketLayout.root.visibility = View.VISIBLE
    views.refundTicketLayout.refundOkButton.setOnClickListener { requireActivity().finish() }
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
    return viewModel.handleWalletCreationIfNeeded().observeOn(AndroidSchedulers.mainThread())
      .doOnNext {
        if (it == WALLET_CREATING_STATUS) {
          showWalletCreationLoadingAnimation()
        }
      }.filter { it != WALLET_CREATING_STATUS }.map {
        endWalletCreationLoadingAnimation()
        it
      }
  }

  private fun showWalletCreationLoadingAnimation() {
    views.createWalletLayout.root.visibility = View.VISIBLE
    views.createWalletLayout.createWalletAnimation.playAnimation()
  }

  private fun endWalletCreationLoadingAnimation() {
    views.createWalletLayout.root.visibility = View.GONE
  }

  private fun showRoomLoading(isCancelActive: Boolean, queueIdentifier: QueueIdentifier? = null) {
    if (isCancelActive) {
      if (queueIdentifier != null && queueIdentifier.setByUser) {
        views.loadingTicketLayout.loadingTitle.text =
          SpannableStringBuilder().append(getString(R.string.finding_room_name_loading_title))
            .bold { append(" ${queueIdentifier.id}") }
      } else {
        views.loadingTicketLayout.loadingTitle.text = getString(R.string.finding_room_loading_title)
      }
      views.loadingTicketLayout.cancelButton.isEnabled = true
      views.loadingTicketLayout.cancelButton.setOnClickListener {
        analytics.sendMatchmakingCancelEvent(ESKILLS_PAYMENT_DATA)
        disposable.add(viewModel.cancelTicket()
          .subscribe { _, _ -> })
      }
    } else {
      views.loadingTicketLayout.loadingTitle.text = getString(R.string.processing_loading_title)
    }
    views.loadingTicketLayout.root.visibility = View.VISIBLE
  }


  private fun getReferralAndActivateLayout(eSkillsPaymentData: EskillsPaymentData) {
    disposable.add(viewModel.getReferral().observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { referralResponse ->
        if (referralResponse.available) {
          setReferralLayout(eSkillsPaymentData, referralResponse)
          views.loadingTicketLayout.referralShareDisplay.baseConstraint.visibility = View.VISIBLE
        } else {
          if (referralResponse.count != 0)//If not default error Referral
            cacheValue(ESKILLS_REFERRAL_KEY, false)
        }
      }.subscribe())
  }

  private fun setReferralLayout(eSkillsPaymentData: EskillsPaymentData, referralResponse: ReferralResponse) {
        views.loadingTicketLayout.referralShareDisplay.actionButtonShareReferral
          .setOnClickListener {
            analytics.sendReferralShareIntentionEvent(eSkillsPaymentData)
            disposable.add(viewModel.getReferralShareText(eSkillsPaymentData.packageName)
              .observeOn(AndroidSchedulers.mainThread())
              .doOnSuccess{appData ->
                startActivity(viewModel.buildShareIntent(
                  String.format(getString(R.string.refer_a_friend_invitation_message),
                    appData.name,
                    referralResponse.referralCode,
                    String.format("https://%s.en.aptoide.com.",appData.uname)
                  )))
              }
              .doOnError {
                startActivity(viewModel.buildShareIntent(
                  String.format(getString(R.string.refer_a_friend_invitation_message),
                    eSkillsPaymentData.packageName,
                    referralResponse.referralCode,
                    "https://en.aptoide.com."
                  )))
              }
              .subscribe())

      }
    views.loadingTicketLayout.referralShareDisplay.tooltip.popupText.text =
      String.format(getString(R.string.refer_a_friend_waiting_room_tooltip), '1')
    val tooltipBtn = views.loadingTicketLayout.referralShareDisplay.actionButtonTooltipReferral
    tooltipBtn.setOnClickListener {
        if (views.loadingTicketLayout.referralShareDisplay.tooltip.root.visibility == View.VISIBLE) {
          views.loadingTicketLayout.referralShareDisplay.tooltip.root.visibility = View.INVISIBLE
          tooltipBtn.setImageDrawable(
            ContextCompat.getDrawable(
              requireContext(), R.drawable.tooltip_orange
            )
          )
        } else {
          views.loadingTicketLayout.referralShareDisplay.tooltip.root.visibility = View.VISIBLE
          tooltipBtn.setImageDrawable(
            ContextCompat.getDrawable(
              requireContext(), R.drawable.tooltip_white
            )
          )
        }

      }
    views.loadingTicketLayout.root.setOnClickListener {
        if (views.loadingTicketLayout.referralShareDisplay.tooltip.root.visibility == View.VISIBLE) {
          views.loadingTicketLayout.referralShareDisplay.tooltip.root.visibility = View.INVISIBLE
          views.loadingTicketLayout.referralShareDisplay.actionButtonTooltipReferral.colorFilter =
            null
        }
      }
    views.loadingTicketLayout.referralShareDisplay.referralCode.text = referralResponse.referralCode


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
    views.geofencingLayout.root.visibility = View.GONE
    views.walletVersionNotSupportedLayout.root.visibility = View.GONE
    views.refundTicketLayout.root.visibility = View.GONE
    views.errorLayout.root.visibility = View.GONE
    views.loadingTicketLayout.loadingTitle.text = getString(R.string.processing_payment_title)
    views.loadingTicketLayout.root.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    showRoomLoading(false)
  }

  override fun showError(errorCode: Int) {
    views.loadingTicketLayout.root.visibility = View.GONE
    views.refundTicketLayout.root.visibility = View.GONE
    views.geofencingLayout.root.visibility = View.GONE
    views.walletVersionNotSupportedLayout.root.visibility = View.GONE
    views.errorLayout.root.visibility = View.VISIBLE
    views.errorLayout.errorOkButton.setOnClickListener {
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
    views.loadingTicketLayout.root.visibility = View.GONE
    views.refundTicketLayout.root.visibility = View.GONE
    views.errorLayout.root.visibility = View.GONE
    views.noNetworkLayout.root.visibility = View.VISIBLE
    views.noNetworkLayout.noNetworkOkButton.setOnClickListener {
      finishWithError(SkillsViewModel.RESULT_SERVICE_UNAVAILABLE)
    }
  }

  override fun showRootError() {
    views.errorLayout.errorMessage.text = getString(R.string.rooted_device_blocked_body)
    showError(SkillsViewModel.RESULT_ROOT_ERROR)
  }

  override fun showWalletVersionNotSupportedError() {
    views.loadingTicketLayout.root.visibility = View.GONE
    views.refundTicketLayout.root.visibility = View.GONE
    views.errorLayout.root.visibility = View.GONE
    views.geofencingLayout.root.visibility = View.GONE
    views.walletVersionNotSupportedLayout.root.visibility = View.VISIBLE
    views.walletVersionNotSupportedLayout.updateButton.setOnClickListener {
      startActivity(viewModel.buildUpdateIntent())
      finishWithError(SkillsViewModel.RESULT_WALLET_VERSION_ERROR)
    }
  }

  // Only temporary
  override fun showNeedsTopUpWarning() {
    views.errorLayout.errorMessage.text = getString(R.string.top_up_needed_body)
    views.loadingTicketLayout.root.visibility = View.GONE
    views.refundTicketLayout.root.visibility = View.GONE
    views.geofencingLayout.root.visibility = View.GONE
    views.walletVersionNotSupportedLayout.root.visibility = View.GONE
    views.errorLayout.root.visibility = View.VISIBLE
    views.errorLayout.errorOkButton.setOnClickListener {
      views.errorLayout.root.visibility = View.GONE
    }
  }

  override fun showPaymentMethodNotSupported() {
    views.errorLayout.errorMessage.text =
      getString(R.string.error_message_local_payment_method_body)
    views.loadingTicketLayout.root.visibility = View.GONE
    views.refundTicketLayout.root.visibility = View.GONE
    views.geofencingLayout.root.visibility = View.GONE
    views.errorLayout.root.visibility = View.VISIBLE
    views.errorLayout.errorOkButton.setOnClickListener {
      views.errorLayout.root.visibility = View.GONE
    }
  }

  override fun showNoFundsWarning() {
    views.errorLayout.errorMessage.text = getString(R.string.not_enough_funds_body)
    views.loadingTicketLayout.root.visibility = View.GONE
    views.refundTicketLayout.root.visibility = View.GONE
    views.geofencingLayout.root.visibility = View.GONE
    views.errorLayout.root.visibility = View.VISIBLE
    views.errorLayout.errorOkButton.setOnClickListener {
      views.errorLayout.root.visibility = View.GONE
    }
  }

  override fun showFingerprintAuthentication() {
    val intent = viewModel.getAuthenticationIntent(requireContext())
    startActivityForResult(intent, SkillsViewModel.AUTHENTICATION_REQUEST_CODE)
  }

  private fun handleAuthenticationResult(resultCode: Int) {
    if (resultCode == SkillsViewModel.RESULT_OK) {
      viewModel.restorePurchase(this).subscribe()
    } else if (resultCode == SkillsViewModel.RESULT_USER_CANCELED) {
      viewModel.closeView()
    }
  }

  private fun getCachedValue(key: String): Boolean {
    val sharedPreferences = requireContext().getSharedPreferences(key, 0)
    return sharedPreferences.getBoolean(key, true)
  }

  private fun cacheValue(key: String, value: Boolean) {
    val sharedPreferences = requireContext().getSharedPreferences(key, 0)
    sharedPreferences.edit().putBoolean(key, value).apply()
  }
}