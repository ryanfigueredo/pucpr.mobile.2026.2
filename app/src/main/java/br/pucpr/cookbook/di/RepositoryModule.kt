package br.pucpr.cookbook.di

import br.pucpr.cookbook.data.local.dao.RecipeDao
import br.pucpr.cookbook.data.local.dao.ShoppingDao
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
}
