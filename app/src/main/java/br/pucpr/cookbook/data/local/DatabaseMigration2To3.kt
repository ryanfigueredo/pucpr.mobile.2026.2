package br.pucpr.cookbook.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `collections` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `descricao` TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `collection_recipes` (`collectionId` INTEGER NOT NULL, `recipeId` INTEGER NOT NULL, PRIMARY KEY(`collectionId`, `recipeId`), FOREIGN KEY(`collectionId`) REFERENCES `collections`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_collection_recipes_recipeId` ON `collection_recipes` (`recipeId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `journal_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recipeId` INTEGER, `dia` INTEGER NOT NULL, `porcoes` INTEGER NOT NULL, `nota` INTEGER NOT NULL, `observacoes` TEXT NOT NULL, FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_entries_recipeId` ON `journal_entries` (`recipeId`)")
    }
}
