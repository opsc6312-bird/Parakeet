package com.example.parakeet_application.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.bumptech.glide.Glide
import com.example.parakeet_application.R
import com.example.parakeet_application.databinding.ActivitySignUpBinding
import com.example.parakeet_application.permissions.AppPermissions
import com.example.parakeet_application.utility.LoadingDialog
import com.example.parakeet_application.utility.State
import com.example.parakeet_application.viewModel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import de.hdodenhof.circleimageview.CircleImageView

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

    private lateinit var getImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appPermissions = AppPermissions()
        loadingDialog = LoadingDialog(this)

     getImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    try {
                        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                        binding.imgPick.setImageBitmap(bitmap)
                        image = it
                    } catch (e: Exception) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImageLauncher.launch(intent)
        }
        binding.btnBack.setOnClickListener { onBackPressed() }

        binding.btnLogin.setOnClickListener {
            val intent = Intent(
                this@SignUpActivity,
                LoginActivity::class.java
            )
            startActivity(intent)
            finish()
        }


        binding.btnSignUp.setOnClickListener {
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    if (areFieldReady()) {
                        if (image != null) {
                            loginViewModel.signUp(email, password, username, image!!)
                                .collect {
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
                                             onBackPressed()
                                            val intent = Intent(
                                                this@SignUpActivity,
                                                LoginActivity::class.java
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

                        } else {
                            Snackbar.make(
                                binding.root,
                                "Please select image",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            binding.btnSignUp.setOnClickListener {
                lifecycleScope.launch {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        if (areFieldReady()) {
                            if (image != null) {
                                loginViewModel.signUp(email, password, username, image!!).collect {
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
                            } else {
                                Snackbar.make(
                                    binding.root,
                                    "Please select image",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

            }

            binding.imgPick.setOnClickListener {
                if (appPermissions.isStorageOk(this))
                    pickImage()
                else
                    appPermissions.requestStoragePermission(this)
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
        getImageLauncher.launch(intent)
    }
}