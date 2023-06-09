package com.dicoding.mentoring.ui.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.dicoding.mentoring.MainActivity
import com.dicoding.mentoring.R
import com.dicoding.mentoring.databinding.ActivityRegisterBinding
import com.dicoding.mentoring.helper.ViewModelFactory
import com.dicoding.mentoring.ui.registerMentor.RegisterMentorActivity
import com.dicoding.mentoring.ui.login.LoginActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = Firebase.auth

        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser !== null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        registerViewModel = ViewModelProvider(
            this, ViewModelFactory(auth)
        )[RegisterViewModel::class.java]
        registerViewModel.isLoading.observe(this) { showLoading(it) }
        registerViewModel.isError.observe(this) { showError(it) }

        setRegisterButtonEnable()
        binding.edRegisterName.doOnTextChanged { _, _, _, _ ->
            setRegisterButtonEnable()
        }
        binding.edRegisterEmail.doOnTextChanged { _, _, _, _ ->
            setRegisterButtonEnable()
            setEmailFieldError()
        }
        binding.edRegisterPassword.doOnTextChanged { _, _, _, _ ->
            setRegisterButtonEnable()
            setPasswordFieldError()
        }

        binding.btnRegisterSubmit.setOnClickListener {
            registerViewModel.postRegister(
                binding.edRegisterName.text.toString(),
                binding.edRegisterEmail.text.toString(),
                binding.edRegisterPassword.text.toString()
            )
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        binding.btnRegisterLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }

        binding.btnRegisterAsMentor.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, RegisterMentorActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showError(isError: Boolean) {
        if (isError) {
            Toast.makeText(
                this@RegisterActivity, getString(R.string.register_failed), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setEmailFieldError() {
        if (TextUtils.isEmpty(binding.edRegisterEmail.text.toString()) || !Patterns.EMAIL_ADDRESS.matcher(
                binding.edRegisterEmail.text.toString()
            ).matches()
        ) {
            binding.edRegisterEmail.error = getString(R.string.email_error)
        } else {
            binding.edRegisterEmail.error = null
        }
    }

    private fun setPasswordFieldError() {
        if (binding.edRegisterPassword.text.toString().length < 6) {
            binding.tfRegisterPassword.endIconMode = TextInputLayout.END_ICON_NONE
            binding.edRegisterPassword.error = getString(R.string.password_error)
        } else {
            binding.tfRegisterPassword.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            binding.edRegisterPassword.error = null
        }
    }

    private fun setRegisterButtonEnable() {
        val nameResult = binding.edRegisterName.text
        val emailResult = binding.edRegisterEmail.text
        val passwordResult = binding.edRegisterPassword.text
        binding.btnRegisterSubmit.isEnabled = nameResult != null && nameResult.toString()
            .isNotBlank() && emailResult != null && emailResult.toString()
            .isNotBlank() && passwordResult != null && passwordResult.toString()
            .isNotBlank() && passwordResult.toString().length >= 6 && Patterns.EMAIL_ADDRESS.matcher(
            emailResult.toString()
        ).matches()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}