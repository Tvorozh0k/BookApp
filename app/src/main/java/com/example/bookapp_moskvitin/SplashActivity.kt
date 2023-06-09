package com.example.bookapp_moskvitin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.bookapp_moskvitin.presentation.dashboard.observer.DashboardAdminActivity
import com.example.bookapp_moskvitin.presentation.dashboard.observer.DashboardUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed(Runnable {
            checkUser()
        }, 1000)
    }

    private fun checkUser() {
        // get current user, if logged or not
        var firebaseUser = firebaseAuth.currentUser

        if (firebaseUser == null) {
            // user not logged in, go to main screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        else {
            // user logged in, check user type
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        // get user type
                        val userType = snapshot.child("userType").value

                        if (userType == "user") {
                            // open user dashboard
                            startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                            finish()
                        }
                        else if (userType == "admin") {
                            // open admin dashboard
                            startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
    }
}