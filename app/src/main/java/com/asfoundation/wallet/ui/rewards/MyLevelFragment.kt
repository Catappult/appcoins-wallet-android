package com.asfoundation.wallet.ui.rewards

import android.app.Fragment
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.Constraints
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import com.asf.wallet.R
import dagger.android.AndroidInjection
import dagger.android.DaggerFragment

class MyLevelFragment: Fragment() {
  var layout: View? = null
  var img: ImageView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {

    layout = inflater.inflate(R.layout.fragment_rewards_level, container, false)
    return layout
  }

  override fun onResume() {
    super.onResume()
    val progress = layout?.findViewById(R.id.level_1) as View
    img = progress.findViewById(R.id.level_active_icon) as ImageView
    img!!.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.bounce_animation))

    val button = layout?.findViewById(R.id.details_button) as Button
    button.setOnClickListener {
      img!!.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.bounce_animation))
    }
  }

}