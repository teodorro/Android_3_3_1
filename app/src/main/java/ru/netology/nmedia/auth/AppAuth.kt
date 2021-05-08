package ru.netology.nmedia.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.IllegalStateException

class AppAuth private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"

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

    companion object{
        @Volatile
        private var instance: AppAuth? = null

        fun getInstance(): AppAuth = synchronized(this){
            instance ?: throw IllegalStateException("${AppAuth::class} is not initialized, you must call ${this::initApp.name} first")
        }

        fun initApp(context: Context) = instance ?: synchronized(this) {
            instance ?: buildAuth(context).also{instance = it}
        }

        private fun buildAuth(context: Context): AppAuth = AppAuth(context)
    }

    @Synchronized
    fun setAuth(id: Long, token: String){
        _authStateFlow.value = AuthState(id, token)
        with(prefs.edit()){
            putLong(idKey, id)
            putString(tokenKey, token)
            apply()
        }
    }

    @Synchronized
    fun removeAuth(){
        _authStateFlow.value = AuthState()
        with(prefs.edit()){
            clear()
            commit()
        }
    }
}