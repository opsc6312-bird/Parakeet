package com.example.parakeet_application.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.parakeet_application.Manifest
import com.example.parakeet_application.R
import com.example.parakeet_application.constants.AppConstant
import com.example.parakeet_application.databinding.ActivitySignUpBinding
import com.example.parakeet_application.permissions.AppPermissions
import com.example.parakeet_application.utility.LoadingDialog
import com.example.parakeet_application.utility.State
import com.example.parakeet_application.viewModel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop

import kotlinx.coroutines.launch
import java.io.File

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var appPermissions: AppPermissions
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var username: String
    private lateinit var email: String
    private lateinit var password: String
    private var image: Uri? = null
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    companion object  {
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)



        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appPermissions = AppPermissions()
        loadingDialog = LoadingDialog(this)



        binding.btnBack.setOnClickListener {

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == )

        }

        binding.imgPick.setOnClickListener {
            if (appPermissions.isStorageOk(this))
                pickImage()
            else
                appPermissions.requestStoragePermission(this)
        }

        binding.btnLogin.setOnClickListener {
            val intent = Intent(
                this@SignUpActivity,
                LoginActivity::class.java
            )
            startActivity(intent)
            finish()
             }


        binding.btnSignUp.setOnClickListener {
                    if (areFieldReady()) {
                        lifecycleScope.launchWhenStarted {
                            loginViewModel.signUp(email, password, username, image ?: Uri.EMPTY)
                                .collect {
                                    when (it) {
                                        is State.Loading -> {
                                            if (it.flag == true)
                                                loadingDialog.startLoading()
                                            val intent = Intent(
                                                this@SignUpActivity,
                                                MainActivity::class.java
                                            )
                                            startActivity(intent)
                                            finish()
                                        }

                                        is State.Success -> {
                                            loadingDialog.stopLoading()
                                            Snackbar.make(
                                                binding.root,
                                                it.data.toString(),
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                            onBackPressed()
                                            val intent = Intent(
                                                this@SignUpActivity,
                                                MainActivity::class.java
                                            )
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
                    }else {
                            Snackbar.make(
                                binding.root,
                                "Please select image",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstant.STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage()
            } else {
                Snackbar.make(
                    binding.root,
                    "Storage Permission Denied",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun areFieldReady(): Boolean {
        username = binding.edtUsername.text.trim().toString()
        email = binding.edtEmail.text.trim().toString()
        password = binding.edtPassword.text.trim().toString()

        var view: View? = null
        var flag = false

        when {
            username.isEmpty() -> {
                binding.edtUsername.error = "Field is required"
                view = binding.edtUsername
                flag = true
            }

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
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }
}