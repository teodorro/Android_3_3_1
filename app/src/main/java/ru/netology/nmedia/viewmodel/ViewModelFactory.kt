package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.repository.PostRepository

class ViewModelFactory(
    private val repository: PostRepository,
    private val workManager: WorkManager,
    private val appAuth: AppAuth,
    private val apiService: PostsApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when{
            modelClass.isAssignableFrom(PostViewModel::class.java) -> {
                PostViewModel(repository, workManager, appAuth) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(appAuth, apiService) as T
            }
            else -> error("Unknown viewmodel class :${modelClass.name}")

        }
    }

}