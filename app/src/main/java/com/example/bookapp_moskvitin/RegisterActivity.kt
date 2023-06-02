package com.example.bookapp_moskvitin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp_moskvitin.data.entity.User
import com.example.bookapp_moskvitin.databinding.ActivityRegisterBinding
import com.example.bookapp_moskvitin.presentation.dashboard.observer.DashboardUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityRegisterBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // handle back button click, got previous screen
        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // handle click, begin register
        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        // input data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword =  binding.cPasswordEt.text.toString().trim()

        // validate data
        if (name.isEmpty()) {
            // Empty name
            Toast.makeText(this, "Введите имя...", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Invalid email
            Toast.makeText(this, "Неправильный формат электронной почты...", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()) {
            // Empty password
            Toast.makeText(this, "Введите пароль...", Toast.LENGTH_SHORT).show()
        }
        else if (cPassword.isEmpty()) {
            // Empty confirm password
            Toast.makeText(this, "Подтвердите пароль...", Toast.LENGTH_SHORT).show()
        }
        else if (password != cPassword) {
            Toast.makeText(this, "Пароли не совпадают...", Toast.LENGTH_SHORT).show()
        }
        else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {

        // create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // account created, now add user in db
                updateUserInfo()
            }
            .addOnFailureListener { e->
                // account creating failed
                Toast.makeText(this, "Не удалось создать аккаунт из-за ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {

        val timestamp = System.currentTimeMillis()

        // get current user uid, since user is registered so we can get it now
        val uid = firebaseAuth.uid

        // setup data to add in db
        val user = User(uid!!, email, name, "", "user", timestamp)

        // set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .setValue(user)
            .addOnSuccessListener {
                // user saved, open user dashboard
                Toast.makeText(this, "Аккаунт успешно создан...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                // failed adding data to db
                Toast.makeText(this, "Не удалось сохранить пользователя из-за ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}