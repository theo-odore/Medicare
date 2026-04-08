package com.medicare.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        btnLogin.setOnClickListener {
            val email = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editEmail).text.toString()
            val password = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editPassword).text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
            
            // Create Auth Request
            val request = AuthRequest(email, password)
            
            // Call Supabase API
            NetworkClient.api.login(request).enqueue(object : retrofit2.Callback<AuthResponse> {
                override fun onResponse(call: retrofit2.Call<AuthResponse>, response: retrofit2.Response<AuthResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val authResponse = response.body()!!
                        val token = authResponse.access_token
                        val userId = authResponse.user?.id ?: ""
                        
                        SessionManager.saveSession(this@AuthActivity, token, userId)
                        
                        Toast.makeText(this@AuthActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                        // Navigate to Dashboard
                        val intent = Intent(this@AuthActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@AuthActivity, "Login Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<AuthResponse>, t: Throwable) {
                     Toast.makeText(this@AuthActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        btnSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}
