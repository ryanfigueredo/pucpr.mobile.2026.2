package br.pucpr.cookbook.di

import br.pucpr.cookbook.data.local.AppDatabase
import br.pucpr.cookbook.data.local.dao.CollectionDao
import br.pucpr.cookbook.data.local.dao.JournalDao
import br.pucpr.cookbook.data.local.dao.PantryDao
import br.pucpr.cookbook.data.repository.CollectionRepository
import br.pucpr.cookbook.data.repository.CollectionRepositoryImpl
import br.pucpr.cookbook.data.repository.JournalRepository
import br.pucpr.cookbook.data.repository.JournalRepositoryImpl
import br.pucpr.cookbook.data.local.dao.PlannerDao
import br.pucpr.cookbook.data.local.dao.RecipeDao
import br.pucpr.cookbook.data.local.dao.ShoppingDao
import br.pucpr.cookbook.data.repository.PantryRepository
import br.pucpr.cookbook.data.repository.PantryRepositoryImpl
import br.pucpr.cookbook.data.repository.PlannerRepository
import br.pucpr.cookbook.data.repository.PlannerRepositoryImpl
import br.pucpr.cookbook.data.repository.RecipeRepository
import br.pucpr.cookbook.data.repository.RecipeRepositoryImpl
import br.pucpr.cookbook.data.repository.ShoppingRepository
import br.pucpr.cookbook.data.repository.ShoppingRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// @Provides explícito para cada repositório, conforme exigido pela rubrica
// (em vez de @Binds, para deixar visível onde o DAO é passado para a implementação).
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRecipeRepository(recipeDao: RecipeDao): RecipeRepository =
        RecipeRepositoryImpl(recipeDao)

    @Provides
    @Singleton
    fun provideShoppingRepository(shoppingDao: ShoppingDao): ShoppingRepository =
        ShoppingRepositoryImpl(shoppingDao)

    @Provides
    @Singleton
    fun providePlannerRepository(plannerDao: PlannerDao): PlannerRepository =
        PlannerRepositoryImpl(plannerDao)

    @Provides
    @Singleton
    fun providePantryRepository(pantryDao: PantryDao): PantryRepository =
        PantryRepositoryImpl(pantryDao)

    @Provides @Singleton fun provideCollectionRepository(dao: CollectionDao, db: AppDatabase): CollectionRepository = CollectionRepositoryImpl(dao, db)
    @Provides @Singleton fun provideJournalRepository(dao: JournalDao): JournalRepository = JournalRepositoryImpl(dao)
}
