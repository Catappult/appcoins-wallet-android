package com.asfoundation.wallet.nfts.ui.nftdetails

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentNftBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NFTDetailsFragment : BasePageViewFragment(),
  SingleStateFragment<NFTDetailsState, NFTDetailsSideEffect> {

  @Inject
  lateinit var viewModelFactory: NFTDetailsViewModelFactory

  @Inject
  lateinit var navigator: NFTDetailsNavigator

  private val viewModel: NFTDetailsViewModel by viewModels { viewModelFactory }
  private val views by viewBinding(FragmentNftBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentNftBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    setListeners()
  }

  fun ImageView.load(url: String?, onLoadingFinished: () -> Unit = {}) {
    val listener = object : RequestListener<Drawable> {
      override fun onLoadFailed(e: GlideException?, model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                isFirstResource: Boolean): Boolean {
        onLoadingFinished()
        this@load.setBackgroundColor(ContextCompat.getColor(context, R.color.styleguide_black))
        return false
      }

      override fun onResourceReady(resource: Drawable?, model: Any?,
                                   target: com.bumptech.glide.request.target.Target<Drawable>?,
                                   dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        onLoadingFinished()
        return false
      }
    }
    Glide.with(this)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade())
        .listener(listener)
        .error(R.drawable.nfts_error)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .into(this)
  }

  override fun onSideEffect(sideEffect: NFTDetailsSideEffect) = Unit

  override fun onStateChanged(state: NFTDetailsState) {
    views.nftImageSkeleton.root.visibility = View.VISIBLE
    views.nftTitle.text = state.data.name
    views.nftSubtitle.text = state.data.description
    views.nftImage.load(state.data.imageURL) {
      views.nftImageSkeleton.root.visibility = View.GONE
    }
    if (state.data.schema != "ERC721" && state.data.schema != "ERC1155") {
      views.nftTransactButton.visibility = View.GONE
    }
  }

  companion object {
    internal const val NFT_ITEM_DATA = "data"
  }

  private fun setListeners() {
    views.actionBack.setOnClickListener { navigator.navigateBack() }

    views.nftTransactButton.setOnClickListener {
      viewModel.state.data?.let { data -> navigator.navigateToTransact(data) }
    }
  }
}