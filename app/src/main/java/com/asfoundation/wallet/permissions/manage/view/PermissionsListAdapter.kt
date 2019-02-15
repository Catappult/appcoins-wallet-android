package com.asfoundation.wallet.permissions.manage.view

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxrelay2.BehaviorRelay

class PermissionsListAdapter(
    private var permissions: MutableList<ApplicationPermissionViewData>,
    private val permissionClick: BehaviorRelay<ApplicationPermissionViewData>) :
    RecyclerView.Adapter<PermissionViewHolder>() {


  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
    return PermissionViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.item_permission_application, parent, false), permissionClick)
  }

  override fun getItemCount(): Int {
    return permissions.size
  }

  override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
    holder.bindPermission(permissions[position])
  }

  fun setPermissions(permissions: List<ApplicationPermissionViewData>) {
    val oldList = this.permissions
    this.permissions = permissions.toMutableList()
    notifyChanges(oldList, this.permissions)
  }

  private fun notifyChanges(oldList: List<ApplicationPermissionViewData>,
                            newList: List<ApplicationPermissionViewData>) {

    DiffUtil.calculateDiff(object : DiffUtil.Callback() {
      override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
      }

      override fun getOldListSize(): Int {
        return oldList.size
      }

      override fun getNewListSize(): Int {
        return newList.size
      }

      override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
            && oldList[oldItemPosition].appName == newList[newItemPosition].appName
            && oldList[oldItemPosition].apkSignature == newList[newItemPosition].apkSignature
            && oldList[oldItemPosition].hasPermission == newList[newItemPosition].hasPermission
      }
    }).dispatchUpdatesTo(this)
  }
}
