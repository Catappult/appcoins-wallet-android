package com.appcoins.wallet.gamification.repository

import android.os.Build
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.appcoins.wallet.gamification.repository.entity.LevelEntity
import com.appcoins.wallet.gamification.repository.entity.LevelsEntity
import com.appcoins.wallet.gamification.repository.entity.PromotionEntity
import com.appcoins.wallet.gamification.repository.entity.WalletOriginEntity

@Database(
  entities = [PromotionEntity::class, LevelsEntity::class, LevelEntity::class, WalletOriginEntity::class],
  version = 9
)
@TypeConverters(PromotionConverter::class)
abstract class PromotionDatabase : RoomDatabase() {

  companion object {
    //Adds the app name to the promotions entity object
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PromotionEntity ADD COLUMN details_link TEXT")
      }
    }

    //Adds the the wallet origin table
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
          "CREATE TABLE IF NOT EXISTS WalletOriginEntity (wallet_address TEXT PRIMARY KEY NOT NULL, wallet_origin TEXT NOT NULL)"
        )
      }
    }

    //Changes the primary key of promotions table to one that can uniquely identify each promotion
    //(since the 'id' field can be the same for several promotions)
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
      override fun migrate(database: SupportSQLiteDatabase) {
        //Since this involves changing the table structure (primary key), 4 steps need to be done:
        // 1. Create a new table containing the new primary key, and all existing fields
        database.execSQL(
          "CREATE TABLE PromotionEntityNew (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `id` TEXT NOT NULL, `priority` INTEGER NOT NULL, `bonus` REAL, `total_spend` TEXT, `total_earned` TEXT, `level` INTEGER, `next_level_amount` TEXT, `status` TEXT, `max_amount` TEXT, `available` INTEGER, `bundle` INTEGER, `completed` INTEGER, `currency` TEXT, `symbol` TEXT, `invited` INTEGER, `link` TEXT, `pending_amount` TEXT, `received_amount` TEXT, `user_status` TEXT, `min_amount` TEXT, `amount` TEXT, `current_progress` TEXT, `description` TEXT, `end_date` INTEGER, `icon` TEXT, `linked_promotion_id` TEXT, `objective_progress` TEXT, `start_date` INTEGER, `title` TEXT, `view_type` TEXT, `details_link` TEXT)"
        )
        // 2. Copy the content from the old table to the new table
        database.execSQL(
          "INSERT INTO PromotionEntityNew(id, priority, bonus, total_spend, total_earned, level, next_level_amount, status, max_amount, available, bundle, completed, currency, symbol, invited, link, pending_amount, received_amount, user_status, min_amount, amount, current_progress, description, end_date, icon, linked_promotion_id, objective_progress, start_date, title, view_type, details_link) SELECT id, priority, bonus, total_spend, total_earned, level, next_level_amount, status, max_amount, available, bundle, completed, currency, symbol, invited, link, pending_amount, received_amount, user_status, min_amount, amount, current_progress, description, end_date, icon, linked_promotion_id, objective_progress, start_date, title, view_type, details_link FROM PromotionEntity"
        )
        // 3. Remove the old table
        database.execSQL("DROP TABLE PromotionEntity")
        // 4. Rename the new table to the old table's name
        database.execSQL("ALTER TABLE PromotionEntityNew RENAME TO PromotionEntity")
      }
    }

    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
      override fun migrate(database: SupportSQLiteDatabase) {
        // Creates new table with two new fields: notification_description, perk_description
        // Renames title to notification_title
        // and removes the old description field
        database.execSQL(
          "CREATE TABLE PromotionEntityNew (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `id` TEXT NOT NULL, `priority` INTEGER NOT NULL, `bonus` REAL, `total_spend` TEXT, `total_earned` TEXT, `level` INTEGER, `next_level_amount` TEXT, `status` TEXT, `max_amount` TEXT, `available` INTEGER, `bundle` INTEGER, `completed` INTEGER, `currency` TEXT, `symbol` TEXT, `invited` INTEGER, `link` TEXT, `pending_amount` TEXT, `received_amount` TEXT, `user_status` TEXT, `min_amount` TEXT, `amount` TEXT, `current_progress` TEXT, `notification_description` TEXT, `perk_description` TEXT, `end_date` INTEGER, `icon` TEXT, `linked_promotion_id` TEXT, `objective_progress` TEXT, `start_date` INTEGER, `notification_title` TEXT, `view_type` TEXT, `details_link` TEXT)"
        )
        // Inserts the old table data to the new one (description is copied to both new fields)
        database.execSQL(
          "INSERT INTO PromotionEntityNew(id, priority, bonus, total_spend, total_earned, level, next_level_amount, status, max_amount, available, bundle, completed, currency, symbol, invited, link, pending_amount, received_amount, user_status, min_amount, amount, current_progress, notification_description, perk_description, end_date, icon, linked_promotion_id, objective_progress, start_date, notification_title, view_type, details_link) SELECT id, priority, bonus, total_spend, total_earned, level, next_level_amount, status, max_amount, available, bundle, completed, currency, symbol, invited, link, pending_amount, received_amount, user_status, min_amount, amount, current_progress, description, description, end_date, icon, linked_promotion_id, objective_progress, start_date, title, view_type, details_link FROM PromotionEntity"
        )
        // Removes old table
        database.execSQL("DROP TABLE PromotionEntity")
        // Renames new table to the old one
        database.execSQL("ALTER TABLE PromotionEntityNew RENAME TO PromotionEntity")
      }
    }

    //Adds the app name to the promotions entity object
    val MIGRATION_5_6: Migration = object : Migration(5, 6) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PromotionEntity ADD COLUMN app_name TEXT")
      }
    }

    //Adds the gamification type field to the promotions entity object
    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PromotionEntity ADD COLUMN gamification_type TEXT")
      }
    }

    //Changes the gamification type field name
    val MIGRATION_7_8: Migration = object : Migration(7, 8) {
      override fun migrate(database: SupportSQLiteDatabase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          database.execSQL("ALTER TABLE PromotionEntity RENAME COLUMN gamification_type to gamification_status")
        }
      }
    }

    //Changes the gamification type field name
    val MIGRATION_8_9: Migration = object : Migration(8, 9) {
      override fun migrate(database: SupportSQLiteDatabase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          database.execSQL("ALTER TABLE PromotionEntity RENAME COLUMN gamification_status to gamification_type")
        }
      }
    }
  }

  abstract fun promotionDao(): PromotionDao
  abstract fun levelDao(): LevelDao
  abstract fun levelsDao(): LevelsDao
  abstract fun walletOriginDao(): WalletOriginDao

}