package ru.netology.nmedia.di

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.room.Room
import androidx.work.WorkManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.viewmodel.ViewModelFactory
import java.lang.IllegalStateException

class DependencyContainer private constructor(context: Context) {
    private val appDb = Room.databaseBuilder(context, AppDb::class.java, "app.db").build()

    private val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
        if (!it.contains("�")) {
            Log.i("", it);
        }
    }).apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val okhttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            prefs.getString(AppAuth.tokenKey, null)?.let { token ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }
            chain.proceed(chain.request())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okhttp)
        .build()

    private val apiService: PostsApiService = retrofit.create(PostsApiService::class.java)

    val repository: PostRepository = PostRepositoryImpl(
        apiService,
        appDb.postDao(),
        appDb.postWorkDao(),
    )
    val workManager = WorkManager.getInstance(context)

    val appAuth = AppAuth(prefs)

    val viewModelFactory = ViewModelFactory(
        repository,
        workManager,
        appAuth,
        apiService
    )

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"

        @Volatile
        private var instance: DependencyContainer? = null

        fun getInstance(): DependencyContainer = synchronized(this) {
            instance
                ?: throw IllegalStateException("${DependencyContainer::class} is not initialized, you must call ${this::initApp.name} first")
        }

        fun initApp(context: Context) = instance ?: synchronized(this) {
            instance ?: DependencyContainer(context).also { instance = it }
        }

        //private fun buildAuth(context: Context): AppAuth = AppAuth(context)
    }
}

