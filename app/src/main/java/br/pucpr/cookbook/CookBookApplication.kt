package br.pucpr.cookbook

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Ponto de entrada do Hilt: a anotação gera o SingletonComponent usado
 * pelos módulos em [br.pucpr.cookbook.di] (DatabaseModule, RepositoryModule).
 */
@HiltAndroidApp
class CookBookApplication : Application()
