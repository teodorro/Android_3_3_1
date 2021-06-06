package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.error.ApiError
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AppAuth,
    private val apiService: PostsApiService
    ) : ViewModel() {

    val data: LiveData<AuthState> = auth.authStateFlow
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() = auth.authStateFlow.value.id != 0L

    fun signIn(login: String, password: String) = viewModelScope.launch {
        val response = apiService.signIn(login, password)

        if (!response.isSuccessful) {
            auth.setAuth(0, "")
            return@launch
        }
        val body = response.body() ?: throw ApiError(response.code(), response.message())
        auth.setAuth(body.id, body.token ?: "")
    }
}
