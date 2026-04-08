package com.medicare.app

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {

    private var selectedRole = "patient"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val cardPatient = findViewById<MaterialCardView>(R.id.cardPatient)
        val cardCaregiver = findViewById<MaterialCardView>(R.id.cardCaregiver)
        val btnCreateAccount = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCreateAccount)
        val textSignIn = findViewById<TextView>(R.id.textSignIn)
        
        // Role Selection Logic
        cardPatient.setOnClickListener {
            selectedRole = "patient"
            updateRoleSelection(cardPatient, cardCaregiver)
        }
        
        cardCaregiver.setOnClickListener {
            selectedRole = "caregiver"
            updateRoleSelection(cardCaregiver, cardPatient)
        }

        btnCreateAccount.setOnClickListener {
            val fullName = findViewById<TextInputEditText>(R.id.editFullName).text.toString()
            val email = findViewById<TextInputEditText>(R.id.editEmail).text.toString()
            val password = findViewById<TextInputEditText>(R.id.editPassword).text.toString()
            val confirmPass = findViewById<TextInputEditText>(R.id.editConfirmPassword).text.toString()

            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Creating Account...", Toast.LENGTH_SHORT).show()

            // Step 1: Signup with Supabase Auth
            val authRequest = AuthRequest(email, password)
            NetworkClient.api.signup(authRequest).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                         val authResponse = response.body()
                         if (authResponse != null && !authResponse.access_token.isNullOrEmpty()) {
                             val userId = authResponse.user?.id ?: ""
                             SessionManager.saveSession(this@SignupActivity, authResponse.access_token, userId)
                             Toast.makeText(this@SignupActivity, "Account Created & Logged In!", Toast.LENGTH_LONG).show()
                              // Navigate to Dashboard
                             val intent = android.content.Intent(this@SignupActivity, MainActivity::class.java)
                             startActivity(intent)
                         } else {
                             Toast.makeText(this@SignupActivity, "Account Created! Please check email.", Toast.LENGTH_LONG).show()
                         }
                         finish()
                    } else {
                        Toast.makeText(this@SignupActivity, "Signup Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@SignupActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        
        textSignIn.setOnClickListener {
            finish()
        }
    }

    private fun updateRoleSelection(selected: MaterialCardView, unselected: MaterialCardView) {
        // Simple visual toggle
        selected.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorSecondary))
        selected.strokeWidth = 4 // dp to px conversion needed ideally, simply using int for now logic checks
        
        unselected.setCardBackgroundColor(Color.WHITE)
        unselected.strokeWidth = 0
    }
}
