package com.andreykaranik.gpstracker.di

import android.content.Context
import com.andreykaranik.gpstracker.data.api.ApiService
import com.andreykaranik.gpstracker.data.repository.DataRepositoryImpl
import com.andreykaranik.gpstracker.data.repository.GroupRepositoryImpl
import com.andreykaranik.gpstracker.data.repository.UserRepositoryImpl
import com.andreykaranik.gpstracker.domain.repository.DataRepository
import com.andreykaranik.gpstracker.domain.repository.GroupRepository
import com.andreykaranik.gpstracker.domain.repository.UserRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {
    private val BASE_URL = "https://diploma2025.ru/"

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(@ApplicationContext context: Context, apiService: ApiService): UserRepository {
        return UserRepositoryImpl(context, apiService)
    }

    @Provides
    @Singleton
    fun provideGroupRepository(@ApplicationContext context: Context, apiService: ApiService): GroupRepository {
        return GroupRepositoryImpl(context, apiService)
    }

    @Provides
    @Singleton
    fun provideDataRepository(@ApplicationContext context: Context, apiService: ApiService): DataRepository {
        return DataRepositoryImpl(context, apiService)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}