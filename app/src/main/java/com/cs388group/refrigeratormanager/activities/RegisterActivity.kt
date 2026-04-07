package com.cs388group.refrigeratormanager.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cs388group.refrigeratormanager.MainActivity
import com.cs388group.refrigeratormanager.R
import com.cs388group.refrigeratormanager.data.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    var userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        db = Firebase.firestore

        val nameInput = findViewById<EditText>(R.id.etName)
        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.etPassword)
        val confirmPasswordInput = findViewById<EditText>(R.id.etConfirmPassword)
        val registerBtn = findViewById<Button>(R.id.btnRegister)

        registerBtn.setOnClickListener {
            val fullName = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser!!.uid
                        userRepository.createUser(
                            uid = uid,
                            displayName = fullName,
                            email = email,
                            onSuccess = {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            },
                            onFailure = {
                                Toast.makeText(this, "Error in user registration.", Toast.LENGTH_SHORT).show()
                                Log.e("RegisterActivity", "Error registering user", it)
                            }
                        )
                    } else {
                        val message = when (task.exception) {
                            is FirebaseAuthWeakPasswordException -> "Password is too weak."
                            is FirebaseAuthInvalidCredentialsException -> "Invalid email address."
                            is FirebaseAuthUserCollisionException -> "An account with this email already exists."
                            else -> {
                                task.exception?.message ?: "Registration failed."
                            }
                        }

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        Log.e("RegisterActivity", "Firebase error registering user", task.exception)

                    }
                }
        }

    }
}