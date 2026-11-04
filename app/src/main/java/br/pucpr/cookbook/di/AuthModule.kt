package br.pucpr.cookbook.di

import br.pucpr.cookbook.data.repository.AuthRepository
import br.pucpr.cookbook.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Fornece o FirebaseAuth e o AuthRepository para o grafo de DI, seguindo o
// mesmo padrão de @Provides explícito usado em RepositoryModule.
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository = AuthRepositoryImpl(auth)
}
