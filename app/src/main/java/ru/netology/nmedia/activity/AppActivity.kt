package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
//import com.google.firebase.iid.FirebaseInstanceId
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.viewmodel.PostViewModel

class AppActivity : AppCompatActivity(R.layout.activity_app) {
    private val viewModel: PostViewModel by viewModels() {
        DependencyContainer.getInstance().viewModelFactory
    }

    private val appAuth: AppAuth
        get() = DependencyContainer.getInstance().appAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        viewModel.data.observe(this) {
            invalidateOptionsMenu()
        }

        DependencyContainer.getInstance().firebaseMessaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("some stuff happened: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            println(token)
        }

        checkGoogleApiAvailability()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        var authenticated = appAuth.authStateFlow.value.id != 0L
        menu?.let {
            it.setGroupVisible(R.id.unauthenticated, !authenticated)
            it.setGroupVisible(R.id.authenticated, authenticated)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.signin -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_signInFragment)
                true
            }
            R.id.signup -> {
                true
            }
            R.id.signout -> {
                appAuth.removeAuth()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //    private fun checkGoogleApiAvailability() {
//        with(GoogleApiAvailability.getInstance()) {
//            val code = isGooglePlayServicesAvailable(this@AppActivity)
//            if (code == ConnectionResult.SUCCESS) {
//                return@with
//            }
//            if (isUserResolvableError(code)) {
//                getErrorDialog(this@AppActivity, code, 9000).show()
//                return
//            }
//            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
//                .show()
//        }
//
//        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
//            println(it.token)
//        }
//    }
    private fun checkGoogleApiAvailability() {
        with(DependencyContainer.getInstance().googleApiAvaliability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000).show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }
    }
}