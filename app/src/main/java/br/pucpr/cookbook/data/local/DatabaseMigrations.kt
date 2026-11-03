package br.pucpr.cookbook.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Apenas adiciona tabelas; os dados da versão 1 são preservados.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `planned_meals` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`recipeId` INTEGER NOT NULL, `dia` INTEGER NOT NULL, " +
                "`tipo` TEXT NOT NULL, `porcoes` INTEGER NOT NULL, " +
                "FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_planned_meals_recipeId` ON `planned_meals` (`recipeId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_planned_meals_dia` ON `planned_meals` (`dia`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `pantry_items` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, " +
                "`quantidade` REAL NOT NULL, `unidade` TEXT NOT NULL, `validade` INTEGER)",
        )
    }
}
