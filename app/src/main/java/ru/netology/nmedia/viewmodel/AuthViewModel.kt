package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.error.ApiError

class AuthViewModel(
    private val auth: AppAuth,
    private val apiService: PostsApiService) : ViewModel()  {

    val data: LiveData<AuthState> = auth
        .authStateFlow
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() = auth.authStateFlow.value.id != 0L

    fun signIn(login: String, password: String) = viewModelScope.launch {
            val response = apiService.signIn(login, password)

            if (!response.isSuccessful) {
                AppAuth.getInstance().setAuth(0, "")
                return@launch
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            AppAuth.getInstance().setAuth(body.id, body.token ?: "")
    }
}