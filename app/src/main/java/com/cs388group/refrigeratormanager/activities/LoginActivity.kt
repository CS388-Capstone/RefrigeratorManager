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
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    val userRepository = UserRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth

        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.etPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val registerBtn = findViewById<Button>(R.id.btnRegister)

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        userRepository.getUser(auth.currentUser!!.uid) { userData ->
                            if (userData == null) {
                                Log.e("MainActivity", "User was returned as null, this shouldn't happen since they just logged in.")
                                return@getUser
                            }

                            val groupId = userData["groupId"] as? String
                            if (groupId == null) {
                                startActivity(Intent(this, GroupOnboardingActivity::class.java))
                                finish()
                            } else {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Login is Invalid", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }


    }
}