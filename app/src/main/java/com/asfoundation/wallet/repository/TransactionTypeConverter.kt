package com.asfoundation.wallet.repository

import androidx.room.TypeConverter
import com.asfoundation.wallet.repository.entity.OperationEntity
import com.asfoundation.wallet.repository.entity.TransactionDetailsEntity
import com.asfoundation.wallet.repository.entity.TransactionEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TransactionTypeConverter {
  private val gson: Gson = Gson()

  @TypeConverter
  fun convertTransactionType(type: TransactionEntity.TransactionType): String {
    return type.name
  }

  @TypeConverter
  fun convertFromTransactionType(type: String): TransactionEntity.TransactionType {
    return TransactionEntity.TransactionType.valueOf(type)
  }

  @TypeConverter
  fun convertMethod(method: TransactionEntity.Method?): String? {
    return method?.name
  }

  @TypeConverter
  fun convertFromMethod(method: String?): TransactionEntity.Method? {
    return method?.let { TransactionEntity.Method.valueOf(method) }
  }

  @TypeConverter
  fun convertSubType(type: TransactionEntity.SubType?): String? {
    return type?.name
  }

  @TypeConverter
  fun convertFromSubType(type: String?): TransactionEntity.SubType? {
    return type?.let { return TransactionEntity.SubType.valueOf(it) }
  }

  @TypeConverter
  fun convertPerk(perk: TransactionEntity.Perk?): String? {
    return perk?.name
  }

  @TypeConverter
  fun convertFromPerk(perk: String?): TransactionEntity.Perk? {
    return perk?.let { return TransactionEntity.Perk.valueOf(it) }
  }

  @TypeConverter
  fun convertTransactionStatus(type: TransactionEntity.TransactionStatus): String {
    return type.name
  }

  @TypeConverter
  fun convertFromTransactionStatus(type: String): TransactionEntity.TransactionStatus {
    return TransactionEntity.TransactionStatus.valueOf(type)
  }

  @TypeConverter
  fun convertTransactionDetailsEntityType(type: TransactionDetailsEntity.Type): String {
    return type.name
  }

  @TypeConverter
  fun convertFromTransactionDetailsEntityType(type: String): TransactionDetailsEntity.Type {
    return TransactionDetailsEntity.Type.valueOf(type)
  }

  @TypeConverter
  fun stringToPermissionsList(data: String?): List<OperationEntity>? {
    return data?.let {
      val listType = object : TypeToken<List<OperationEntity>>() {
      }.type
      return gson.fromJson(data, listType)
    }
  }

  @TypeConverter
  fun permissionsListToString(permissions: List<OperationEntity>?): String? {
    return permissions?.let { gson.toJson(it) }
  }
}
