package com.asfoundation.wallet.my_wallets.more

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.appcoins.wallet.ui.common.convertDpToPx
import com.asf.wallet.R
import com.google.android.material.card.MaterialCardView

// Create new item view

fun MoreDialogStateItem.toWalletItemView(context: Context, action: (String) -> Unit): View =
  context.run {
    ItemCard(
      onTap = onTap(action),
      child = ItemRow(
        color = backgroundColor(this),
        children = arrayOf(
          Checkmark(visibility = checkMarkVisibility),
          Space(width = 12),
          Text(
            height = WRAP_CONTENT,
            weight = 1F,
            color = textColor(this),
            text = walletName
          ),
          Space(width = 24),
          Text(
            width = WRAP_CONTENT,
            height = WRAP_CONTENT,
            color = textColor(this),
            text = fiatBalance
          )
        )
      )
    )
  }

// Update item view

fun MoreDialogStateItem.toWalletItemView(view: View, action: (String) -> Unit) =
  (view as MaterialCardView).run {
    setOnClickListener(onTap(action))
    (getChildAt(0) as LinearLayoutCompat).run {
      strokeWidth = 0
      setBackgroundColor(backgroundColor(context))
      getChildAt(0).visibility = checkMarkVisibility
      (getChildAt(2) as TextView).run {
        setTextColor(textColor(context))
        text = walletName
      }
      (getChildAt(4) as TextView).run {
        setTextColor(textColor(context))
        text = fiatBalance
      }
    }
  }


// Mapping ta the actual UI values

private fun MoreDialogStateItem.backgroundColor(context: Context) =
  context.getCColor(if (isSelected) R.color.styleguide_blue else R.color.styleguide_light_grey)

private fun MoreDialogStateItem.textColor(context: Context) =
  context.getCColor(if (isSelected) R.color.styleguide_white else R.color.styleguide_blue)

private val MoreDialogStateItem.checkMarkVisibility get() = if (isSelected) VISIBLE else INVISIBLE

private fun MoreDialogStateItem.onTap(action: (String) -> Unit): ((View) -> Unit)? =
  if (isSelected) {
    null
  } else {
    { action(walletAddress) }
  }

// Card component

@Suppress("FunctionName")
private fun Context.ItemCard(onTap: ((View) -> Unit)?, child: View) =
  MaterialCardView(this).apply {
    layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
      .apply { topMargin = resources.dpToPx(8) }
    elevation = 0F
    radius = resources.dpToPx(14).toFloat()
    rippleColor = context.getCColorStateList(R.color.styleguide_medium_grey)
    setOnClickListener(onTap)
    addView(child)
  }

// Row component

@Suppress("FunctionName")
private fun Context.ItemRow(@ColorInt color: Int, children: Array<View>) =
  LinearLayoutCompat(this).apply {
    layoutParams = LayoutParams(MATCH_PARENT, resources.dpToPx(40))
    orientation = LinearLayoutCompat.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
    setBackgroundColor(color)
    updatePadding(left = resources.dpToPx(13), right = resources.dpToPx(32))
    children.forEach(this::addView)
  }

// Checkmark component

@Suppress("FunctionName")
private fun Context.Checkmark(visibility: Int) =
  ImageView(this).apply {
    layoutParams = LinearLayoutCompat.LayoutParams(resources.dpToPx(15), WRAP_CONTENT)
    scaleType = ImageView.ScaleType.CENTER_INSIDE
    setVisibility(visibility)
    setImageResource(R.drawable.ic_check_mark)
  }

// Text component

@Suppress("FunctionName")
private fun Context.Text(
  width: Int = 0,
  height: Int = 0,
  weight: Float = 0F,
  @ColorInt color: Int,
  text: CharSequence
) =
  TextView(this).apply {
    layoutParams = LinearLayoutCompat.LayoutParams(
      resources.dpSizeToPx(width),
      resources.dpSizeToPx(height),
      weight
    )
    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    textSize = 14.toFloat()
    ellipsize = TextUtils.TruncateAt.END
    maxLines = 1
    setTextColor(color)
    setText(text)
  }

// Spacer component

@Suppress("FunctionName")
private fun Context.Space(width: Int = 0, height: Int = 0, weight: Float = 0F) =
  Space(this).apply {
    layoutParams = LinearLayoutCompat.LayoutParams(
      resources.dpSizeToPx(width),
      resources.dpSizeToPx(height),
      weight
    )
  }

// Local utils

private fun Resources.dpToPx(value: Int): Int = value.convertDpToPx(this)

private fun Resources.dpSizeToPx(value: Int): Int = when (value) {
  WRAP_CONTENT, MATCH_PARENT -> value
  else -> dpToPx(value)
}

private fun Context.getCColor(@ColorRes id: Int) = ContextCompat.getColor(this, id)

private fun Context.getCColorStateList(@ColorRes id: Int) =
  ContextCompat.getColorStateList(this, id)
