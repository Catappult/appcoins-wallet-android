package com.asfoundation.wallet.logging.send_logs

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.asf.wallet.R

class SettingsSendLogsPreference(context: Context?, attrs: AttributeSet?) :
    Preference(context, attrs) {
  private var sendLogsState: SendLogsState? = null
  private var state: TextView? = null
  private var icon: ImageView? = null
  private var background: View? = null
  var preferenceClickListener: View.OnClickListener? = null

  init {
    this.layoutResource = R.layout.preferences_with_send_logs_layout
  }

  override fun onBindViewHolder(holder: PreferenceViewHolder) {
    super.onBindViewHolder(holder)
    state = holder.findViewById(R.id.settings_status_text) as TextView
    icon = holder.findViewById(R.id.settings_check_ic) as ImageView
    background = holder.findViewById(R.id.settings_status_background) as View

    setStateTextView()
    setIconImageView()
    setBackgroundView()
  }

  fun setSendLogsState(sendLogsState: SendLogsState) {
    this.sendLogsState = sendLogsState
    setStateTextView()
    setIconImageView()
    setBackgroundView()
  }

  private fun setStateTextView() {
    when (sendLogsState?.state) {
      SendState.SENDING ->
        state?.setText(R.string.send_logs_sending)
      SendState.SENT -> {
        state?.setText(R.string.send_logs_sent)
        state?.resources?.getColor(R.color.send_logs_sent)
            ?.let { state?.setTextColor(it) }

      }
      SendState.ERROR -> {
        state?.setText(R.string.send_logs_error)
        state?.resources?.getColor(R.color.send_logs_error)
            ?.let { state?.setTextColor(it) }
      }
      SendState.UNINITIALIZED -> Unit
    }
  }

  private fun setIconImageView() {
    when (sendLogsState?.state) {
      SendState.SENDING -> Unit
      SendState.SENT ->
        icon?.setImageResource(R.drawable.ic_check_mark_green)
      SendState.ERROR ->
        icon?.setImageResource(R.drawable.ic_alert)
      SendState.UNINITIALIZED -> Unit
    }
  }

  private fun setBackgroundView() {
    when (sendLogsState?.state) {
      SendState.SENDING -> Unit
      SendState.SENT ->
        background?.resources?.getColor(R.color.send_logs_sent_background)
            ?.let { background?.setBackgroundColor(it) }
      SendState.ERROR ->
        background?.resources?.getColor(R.color.send_logs_error_background)
            ?.let { background?.setBackgroundColor(it) }
      SendState.UNINITIALIZED -> Unit
    }
  }
}
