package com.example.cmuapp.di

import android.content.Context
import com.example.cmuapp.data.API.GooglePlacesApi
import com.example.cmuapp.data.dao.EstablishmentDao
import com.example.cmuapp.data.dao.ReviewDao
import com.example.cmuapp.data.dao.UserVisitedDao
import com.example.cmuapp.data.database.AppDatabase
import com.example.cmuapp.data.repository.EstablishmentRepository
import com.example.cmuapp.data.repository.RankingRepository
import com.example.cmuapp.data.repository.ReviewRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.create(context)

    @Provides
    fun provideEstablishmentDao(db: AppDatabase): EstablishmentDao = db.establishmentDao()

    @Provides
    fun provideReviewDao(db: AppDatabase): ReviewDao = db.reviewDao()

    @Provides
    fun provideUserVisitedDao(db: AppDatabase): UserVisitedDao = db.userVisitedDao()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideGooglePlacesApi(retrofit: Retrofit): GooglePlacesApi = GooglePlacesApi.create(retrofit)

    @Provides
    @Singleton
    fun provideReviewRepository(
        reviewDao: ReviewDao,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        @ApplicationContext appContext: Context,
    ): ReviewRepository = ReviewRepository(
        reviewDao = reviewDao,
        firestore = firestore,
        storage = storage,
        appContext = appContext,
    )

    @Provides
    @Singleton
    fun provideRankingRepository(
        establishmentDao: EstablishmentDao,
        firestore: FirebaseFirestore,
        reviewDao: ReviewDao
    ): RankingRepository = RankingRepository(establishmentDao, firestore, reviewDao)


    @Provides
    @Singleton
    fun provideEstablishmentRepository(
        establishmentDao: EstablishmentDao,
        googlePlacesApi: GooglePlacesApi,
        firestore: FirebaseFirestore,
        userVisitedDao: UserVisitedDao
    ): EstablishmentRepository =
        EstablishmentRepository(establishmentDao, googlePlacesApi, firestore, userVisitedDao)
}
