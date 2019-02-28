package com.asfoundation.wallet.permissions.repository

import androidx.room.TypeConverter
import com.appcoins.wallet.permissions.PermissionName
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*


class PermissionsListTypeConverter {
  private val gson: Gson = Gson()
  @TypeConverter
  fun stringToPermissionsList(data: String?): List<PermissionName> {
    data?.let {
      val listType = object : TypeToken<List<PermissionName>>() {
      }.type
      return gson.fromJson(data, listType)
    } ?: return Collections.emptyList()
  }

  @TypeConverter
  fun permissionsListToString(permissions: List<PermissionName>): String {
    return gson.toJson(permissions)
  }
}