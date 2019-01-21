package com.asfoundation.wallet.permissions.manage.view

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.asf.wallet.R

class PermissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
  private val appNameTextView: TextView = itemView.findViewById(R.id.permission_app_name)
  private val hasPermission: Switch = itemView.findViewById(R.id.has_permission)

  fun bindPermission(permission: ApplicationPermissionViewData) {
    appIcon.setImageDrawable(permission.icon)
    appNameTextView.text = permission.appName
    hasPermission.isChecked = permission.hasPermission
  }
}
