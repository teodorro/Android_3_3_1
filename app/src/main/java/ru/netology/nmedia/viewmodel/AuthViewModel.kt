package ru.netology.nmedia.viewmodel

import android.app.Activity
import androidx.lifecycle.*
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.util.SingleLiveEvent
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

    private val _moveToAuthEvent = SingleLiveEvent<Unit>()
    val moveToAuthEvent: LiveData<Unit>
        get() = _moveToAuthEvent

    private val _signOutEvent = SingleLiveEvent<Unit>()
    val signOutEvent: LiveData<Unit>
        get() = _signOutEvent

    fun signIn(login: String, password: String) = viewModelScope.launch {
        val response = apiService.signIn(login, password)

        if (!response.isSuccessful) {
            auth.setAuth(0, "")
            return@launch
        }
        val body = response.body() ?: throw ApiError(response.code(), response.message())
        auth.setAuth(body.id, body.token ?: "")
    }

    fun moveToAuthInvoke(){
        _moveToAuthEvent.value = Unit
    }

    fun signOutInvoke(){
        _signOutEvent.value = Unit
    }

    fun moveToAuth(activity: Activity){
        findNavController(activity, R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_signInFragment)
    }

    fun signOut(){
        auth.removeAuth()
    }
}
