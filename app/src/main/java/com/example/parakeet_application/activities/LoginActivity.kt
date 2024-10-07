package com.example.parakeet_application.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.parakeet_application.R
import com.example.parakeet_application.databinding.ActivityLoginBinding
import com.example.parakeet_application.utility.LoadingDialog
import com.example.parakeet_application.utility.State
import com.example.parakeet_application.viewModel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var email: String
    private lateinit var password: String
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }



        binding.btnLogin.setOnClickListener {
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    if (areFieldReady()) {
                            loginViewModel.login(email, password).collect {
                                when (it) {
                                    is State.Loading -> {
                                        if (it.flag == true)
                                            loadingDialog.startLoading()
                                    }

                                    is State.Success -> {
                                        loadingDialog.stopLoading()
                                        Snackbar.make(
                                            binding.root,
                                            it.data.toString(),
                                            Snackbar.LENGTH_SHORT
                                        ).show()

                                        val intent =
                                            Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()

                                    }

                                    is State.Failed -> {
                                        loadingDialog.stopLoading()
                                        Snackbar.make(
                                            binding.root,
                                            it.error,
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }
                }
            }
        }


    }

    private fun areFieldReady(): Boolean {

        email = binding.edtEmail.text.trim().toString()
        password = binding.edtPassword.text.trim().toString()

        var view: View? = null
        var flag = false

        when {

            email.isEmpty() -> {
                binding.edtEmail.error = "Field is required"
                view = binding.edtEmail
                flag = true
            }
            password.isEmpty() -> {
                binding.edtPassword.error = "Field is required"
                view = binding.edtPassword
                flag = true
            }
            password.length < 8 -> {
                binding.edtPassword.error = "Minimum 8 characters"
                view = binding.edtPassword
                flag = true
            }
        }

        return if (flag) {
            view?.requestFocus()
            false
        } else
            true

    }

}