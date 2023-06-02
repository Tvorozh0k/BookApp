package com.example.bookapp_moskvitin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp_moskvitin.databinding.ActivityLoginBinding
import com.example.bookapp_moskvitin.presentation.dashboard.observer.DashboardAdminActivity
import com.example.bookapp_moskvitin.presentation.dashboard.observer.DashboardUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityLoginBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // handle click, not have account, go to register screen
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        firebaseAuth = FirebaseAuth.getInstance()

        // handle click, begin login
        binding.loginBtn.setOnClickListener {
            validateData()
        }
    }

    var email = ""
    var password = ""

    private fun validateData() {
        // input data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        // validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Invalid email
            Toast.makeText(this, "Неправильный формат электронной почты...", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()) {
            // Empty password
            Toast.makeText(this, "Введите пароль...", Toast.LENGTH_SHORT).show()
        }
        else {
            loginUser()
        }
    }

    private fun loginUser() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // login success
                checkUser()
            }
            .addOnFailureListener { e->
                // failed login
                Toast.makeText(this, "Ошибка входа из-за ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    // get user type
                    val userType = snapshot.child("userType").value

                    if (userType == "user") {
                        // open user dashboard
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()
                    }
                    else if (userType == "admin") {
                        // open admin dashboard
                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}