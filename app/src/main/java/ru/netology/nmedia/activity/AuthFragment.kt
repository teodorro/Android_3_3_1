package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSigninBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.AuthViewModel

class AuthFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    ){
        DependencyContainer.getInstance().viewModelFactory
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSigninBinding.inflate(
            inflater,
            container,
            false
        )

        binding.buttonLogin.setOnClickListener {
            var login = binding.editTextLogin.text.toString()
            var password = binding.editTextTextPassword.text.toString()
            if (binding.editTextLogin.text.toString().isNotBlank()) {
                try {
                    viewModel.signIn(login, password)
                } catch (e: Exception) {
                    Toast.makeText(this.context, e.message, Toast.LENGTH_LONG)
                        .show()
                }
            } else{
                Toast.makeText(this.context, R.string.enterLoginPassword, Toast.LENGTH_LONG)
                    .show()
            }
        }

        viewModel.data.observe(viewLifecycleOwner){
            if (viewModel.authenticated) {
                AndroidUtils.hideKeyboard(requireView())
                findNavController().navigateUp()
            }
            else{
                if (binding.editTextLogin.text.toString().isNotBlank())
                    Toast.makeText(this.context, R.string.errorLoginPassword, Toast.LENGTH_LONG)
                        .show()
            }
        }

        return binding.root
    }

}