package com.asfoundation.wallet.permissions.manage.view

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.appcoins.wallet.permissions.ApplicationPermission
import com.asf.wallet.R

class PermissionViewHolder : RecyclerView.ViewHolder {
  private val appNameTextView: TextView

  constructor(itemView: View) : super(itemView) {
    appNameTextView = itemView.findViewById(R.id.permission_app_name)
  }

  fun bindPermission(permission: ApplicationPermission) {
    appNameTextView.text = permission.packageName
  }

}
