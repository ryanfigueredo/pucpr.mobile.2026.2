package br.pucpr.cookbook.di

import android.content.Context
import androidx.room.Room
import br.pucpr.cookbook.data.local.AppDatabase
import br.pucpr.cookbook.data.local.dao.RecipeDao
import br.pucpr.cookbook.data.local.dao.ShoppingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Módulo Hilt: é aqui que o grafo de DI sabe como construir o RoomDatabase e os DAOs.
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "cookbook.db").build()

    @Provides
    fun provideRecipeDao(database: AppDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideShoppingDao(database: AppDatabase): ShoppingDao = database.shoppingDao()
}
