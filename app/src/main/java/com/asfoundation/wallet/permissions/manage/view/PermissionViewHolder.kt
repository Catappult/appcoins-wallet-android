package com.asfoundation.wallet.permissions.manage.view

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.asf.wallet.R
import com.jakewharton.rxrelay2.BehaviorRelay

class PermissionViewHolder(itemView: View,
                           private val permissionClick: BehaviorRelay<ApplicationPermissionViewData>) :
    RecyclerView.ViewHolder(itemView) {
  private val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
  private val appNameTextView: TextView = itemView.findViewById(R.id.permission_app_name)
  private val hasPermission: Switch = itemView.findViewById(R.id.has_permission)

  fun bindPermission(permission: ApplicationPermissionViewData) {
    hasPermission.setOnCheckedChangeListener(null)
    itemView.setOnClickListener(null)
    appIcon.setImageDrawable(permission.icon)
    appNameTextView.text = permission.appName
    hasPermission.isChecked = permission.hasPermission
    itemView.setOnClickListener {
      hasPermission.isChecked = !hasPermission.isChecked
    }
    hasPermission.setOnCheckedChangeListener { _, isChecked ->
      permissionClick.accept(
          ApplicationPermissionViewData(permission.packageName, permission.appName, isChecked,
              permission.icon,
              permission.apkSignature))
    }
  }
}
