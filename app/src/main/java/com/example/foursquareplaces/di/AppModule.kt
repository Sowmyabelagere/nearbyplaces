package com.example.foursquareplaces.di

import com.example.foursquareplaces.BuildConfig
import com.example.foursquareplaces.data.FetchPlacesRepository
import com.example.foursquareplaces.data.FetchPlacesRepositoryImpl
import com.example.foursquareplaces.data.PlacesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://api.foursquare.com/v3/"

    @Provides
    @Singleton
    fun providesHttpLoggingInterceptor() = HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    @Named("authInterceptor")
    fun providesAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request =
                chain.request().newBuilder()
                    .header("Authorization", BuildConfig.API_KEY)
                    .build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        @Named("authInterceptor") authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient
            .Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun providePlacesApiService(retrofit: Retrofit):PlacesApiService{
        return retrofit.create(PlacesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFetchPlacesRepository(placesApiService: PlacesApiService):FetchPlacesRepository{
        return FetchPlacesRepositoryImpl(placesApiService)
    }
}