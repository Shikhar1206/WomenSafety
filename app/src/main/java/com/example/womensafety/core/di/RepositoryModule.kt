package com.example.womensafety.core.di

import com.example.womensafety.data.repository.AuthRepositoryImpl
import com.example.womensafety.data.repository.ContactRepositoryImpl
import com.example.womensafety.data.repository.SosRepositoryImpl
import com.example.womensafety.domain.repository.AuthRepository
import com.example.womensafety.domain.repository.ContactRepository
import com.example.womensafety.domain.repository.SosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindSosRepository(impl: SosRepositoryImpl): SosRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
