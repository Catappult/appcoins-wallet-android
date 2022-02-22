package com.asfoundation.wallet.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.asf.wallet.R
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.authentication_error_fragment.*
import kotlinx.android.synthetic.main.fragment_balance.faded_background

@AndroidEntryPoint
class AuthenticationErrorFragment : BasePageViewFragment(), AuthenticationErrorView {

  private lateinit var presenter: AuthenticationErrorPresenter
  private lateinit var activityView: AuthenticationPromptView
  private lateinit var authenticationBottomSheet: BottomSheetBehavior<View>

  private val errorTimer: Long by lazy {
    if (requireArguments().containsKey(ERROR_TIMER_KEY)) {
      requireArguments().getLong(ERROR_TIMER_KEY, 0)
    } else {
      throw IllegalArgumentException("Error message not found")
    }
  }

  companion object {
    private const val ERROR_TIMER_KEY = "error_message"

    fun newInstance(timer: Long): AuthenticationErrorFragment {
      val fragment = AuthenticationErrorFragment()
      fragment.arguments = Bundle().apply {
        putLong(ERROR_TIMER_KEY, timer)
      }
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = AuthenticationErrorPresenter(this, activityView, CompositeDisposable())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is AuthenticationPromptView) {
      throw IllegalStateException(
          "AuthenticationError Fragment must be attached to AuthenticationPrompt Activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    childFragmentManager.beginTransaction()
        .setCustomAnimations(R.anim.fragment_slide_up, R.anim.fragment_slide_down,
            R.anim.fragment_slide_up, R.anim.fragment_slide_down)
        .replace(R.id.bottom_error_fragment_container,
            AuthenticationErrorBottomSheetFragment.newInstance(errorTimer))
        .commit()
    return inflater.inflate(R.layout.authentication_error_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    authenticationBottomSheet =
        BottomSheetBehavior.from(bottom_error_fragment_container)
    presenter.present()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    faded_background.animation =
        AnimationUtils.loadAnimation(context, R.anim.fast_100s_fade_out_animation)
    faded_background.visibility = View.GONE
    presenter.stop()
  }

  override fun showBottomSheet() {
    authenticationBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    authenticationBottomSheet.isFitToContents = true
    authenticationBottomSheet.addBottomSheetCallback(object :
        BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
          authenticationBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
      }

      override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
    })
  }

  override fun retryAuthentication() {
    activityView.onRetryButtonClick()
  }

  override fun outsideOfBottomSheetClick() = RxView.clicks(faded_background)

}
