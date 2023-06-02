package com.example.bookapp_moskvitin

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookapp_moskvitin.data.entity.BookCategory
import com.example.bookapp_moskvitin.data.entity.ModelCategory
import com.example.bookapp_moskvitin.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale.Category

class CategoryAddActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding : ActivityCategoryAddBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // handle click, begin upload category
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var category = ""

    private fun validateData() {
        // input data
        category = binding.categoryEd.text.toString().trim()

        // validate data
        if (category.isEmpty()) {
            // empty category
            Toast.makeText(this, "Введите категорию...", Toast.LENGTH_SHORT).show()
        }
        else {
            addCategory()
        }
    }

    private fun addCategory() {

        // get timestamp
        val timestamp = System.currentTimeMillis()

        // setup data to add in firebase db
        val category = ModelCategory("$timestamp", timestamp, category, "${firebaseAuth.uid}")

        // add to firebase db: Database root > Categories > categoryId > category info
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(category)
            .addOnSuccessListener {
                // added successfully
                Toast.makeText(this, "Добавлено успешно...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                // failed to add
                Toast.makeText(this, "Не удалось добавить категорию из-за ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }
}