package com.asfoundation.wallet.my_wallets.main.list.model

import android.app.Activity
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.my_wallets.main.list.WalletsListEvent
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.util.generateQrCode
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.qualifiers.ActivityContext

@EpoxyModelClass
abstract class WalletInfoModel : EpoxyModelWithHolder<WalletInfoModel.WalletInfoHolder>() {

  @EpoxyAttribute
  lateinit var walletInfoAsync: Async<WalletInfo>

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var walletClickListener: ((WalletsListEvent) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_wallet_info

  override fun bind(holder: WalletInfoHolder) {
    when (val asyncValue = walletInfoAsync) {
      is Async.Success -> {
        val walletAddress = asyncValue().wallet
        holder.walletAddressTextView.text = walletAddress
        holder.actionButtonShareAddress.setOnClickListener {
          walletClickListener?.invoke(
              WalletsListEvent.ShareWalletClick(walletAddress))
        }
        holder.actionButtonCopyAddress.setOnClickListener {
          walletClickListener?.invoke(WalletsListEvent.CopyWalletClick(walletAddress))
        }
        holder.qrImage.setOnClickListener {
          walletClickListener?.invoke(WalletsListEvent.QrCodeClick(holder.qrImage))
        }
        setQrCode(holder, walletAddress)
      }
      else -> Unit
    }

  }

  private fun setQrCode(holder: WalletInfoHolder, walletAddress: String) {

  }

  class WalletInfoHolder : BaseViewHolder() {
    val qrImage by bind<ShapeableImageView>(R.id.qr_image)
    val actionButtonCopyAddress by bind<ImageButton>(R.id.action_button_copy_address)
    val actionButtonShareAddress by bind<ImageButton>(R.id.action_button_share_address)
    val walletAddressCardView by bind<MaterialCardView>(R.id.wallet_address_card_view)
    val walletAddressTextView by bind<TextView>(R.id.wallet_address_text_view)
  }
}