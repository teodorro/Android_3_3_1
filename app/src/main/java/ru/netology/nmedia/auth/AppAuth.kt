package ru.netology.nmedia.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dto.PushToken
import javax.inject.Inject
import javax.inject.Singleton

//@Singleton
//class AppAuth @Inject constructor(
//    @ApplicationContext private val context: Context,
//) {

class AppAuth (
    private val apiService: PostsApiService,
    private val prefs: SharedPreferences,
) {
//    private val idKey = "id"
//    private val tokenKey = "token"
    companion object{
        val idKey = "id"
        val tokenKey = "token"
    }

    private val _authStateFlow: MutableStateFlow<AuthState>

    init{
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)

        if (id == 0L || token == null){
            _authStateFlow = MutableStateFlow(AuthState())
            with(prefs.edit()){
                clear()
                apply()
            }
        } else{
            _authStateFlow = MutableStateFlow(AuthState(id, token))
        }
    }

    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun apiService(): PostsApiService
    }

    @Synchronized
    fun setAuth(id: Long, token: String){
        _authStateFlow.value = AuthState(id, token)
        with(prefs.edit()){
            putLong(idKey, id)
            putString(tokenKey, token)
            apply()
        }
        sendPushToken()
    }

    @Synchronized
    fun removeAuth(){
        _authStateFlow.value = AuthState()
        with(prefs.edit()){
            clear()
            commit()
        }
        sendPushToken()
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                apiService.save(pushToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

//    fun sendPushToken(token: String? = null) {
//        CoroutineScope(Dispatchers.Default).launch {
//            try {
//                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
//                getApiService(context).save(pushToken)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

//    private fun getApiService(context: Context): PostsApiService {
//        val hiltEntryPoint = EntryPointAccessors.fromApplication(
//            context,
//            AppAuthEntryPoint::class.java
//        )
//        return hiltEntryPoint.apiService()
//    }
}