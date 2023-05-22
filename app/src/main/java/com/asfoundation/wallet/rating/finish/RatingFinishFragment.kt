package com.asfoundation.wallet.rating.finish

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.databinding.FragmentRatingFinishBinding
import com.asfoundation.wallet.rating.RatingActivity
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class RatingFinishFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), RatingFinishView {

  @Inject
  lateinit var presenter: RatingFinishPresenter

  private val animationEndSubject = PublishSubject.create<Any>()

  private val binding by viewBinding(FragmentRatingFinishBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (requireActivity() as RatingActivity).disableBack()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentRatingFinishBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationEnd(animation: Animator) {
        animationEndSubject.onNext(Unit)
      }
      override fun onAnimationRepeat(animation: Animator) = Unit
      override fun onAnimationCancel(animation: Animator) = Unit
      override fun onAnimationStart(animation: Animator) = Unit
    })
    presenter.present()
  }

  override fun animationEndEvent(): Observable<Any> {
    return animationEndSubject
  }
}