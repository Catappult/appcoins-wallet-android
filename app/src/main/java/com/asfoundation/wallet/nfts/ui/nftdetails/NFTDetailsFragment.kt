package com.asfoundation.wallet.nfts.ui.nftdetails

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentNftBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import kotlinx.android.synthetic.main.fragment_nft.*
import javax.inject.Inject

class NFTDetailsFragment : BasePageViewFragment() ,
  SingleStateFragment<NFTDetailsState, NFTDetailsSideEffect> {

  @Inject
  lateinit var viewModelFactory: NFTDetailsViewModelFactory

  @Inject
  lateinit var navigator: NFTDetailsNavigator

  private val viewModel: NFTDetailsViewModel by viewModels { viewModelFactory }
  private val views by viewBinding(FragmentNftBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    postponeEnterTransition()
    return inflater.inflate(R.layout.fragment_nft, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.nftTitle.text = viewModel.state.data.name
    views.nftSubtitle.text = viewModel.state.data.description
    views.nftImage.load(viewModel.state.data.imageURL) {
      startPostponedEnterTransition()
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    setListeners()
  }

  fun ImageView.load(url: String, onLoadingFinished: () -> Unit = {}) {
    val listener = object : RequestListener<Drawable> {
      override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: com.bumptech.glide.request.target.Target<Drawable>?,
        isFirstResource: Boolean
      ): Boolean {
        onLoadingFinished()
        return false
      }

      override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: com.bumptech.glide.request.target.Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
      ): Boolean {
        onLoadingFinished()
        return false
      }
    }
    Glide.with(this)
      .load(url)
      .listener(listener)
      .into(this)
  }

  override fun onSideEffect(sideEffect: NFTDetailsSideEffect) = Unit

  override fun onStateChanged(state: NFTDetailsState) = Unit

  companion object {
    internal const val NFTITEMDATA = "data"
  }

  private fun setListeners() {
    views.actionBack.setOnClickListener { goBack() }
  }

  private fun goBack(){
    navigator.navigateBack()
  }


}





