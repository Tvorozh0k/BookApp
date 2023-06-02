package com.example.bookapp_moskvitin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bookapp_moskvitin.data.entity.Book
import com.example.bookapp_moskvitin.databinding.ActivityPdfAddBinding
import com.example.bookapp_moskvitin.data.entity.ModelCategory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    // setup view binding activity_pdf_add --> ActivityPdfAddBinding
    private lateinit var binding: ActivityPdfAddBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // process dialog
    private lateinit var progressDialog: ProgressDialog

    // array to hold pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    // uri of picked pdf
    private var pdfUri: Uri? = null

    // TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        // setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Пожалуйста, подождите")
        progressDialog.setCanceledOnTouchOutside(false)

        // handle click, show category pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        // handle click, pick pdf intent
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        // handle click, start uploading pdf / book
        binding.submitBtn.setOnClickListener {
            validateData()
        }

        // handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        Log.d(TAG, "validateData: Validating data")

        // get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        // validate data
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название книги...", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()) {
            Toast.makeText(this, "Введите описание книги...", Toast.LENGTH_SHORT).show()
        }
        else if (category.isEmpty()) {
            Toast.makeText(this, "Выберите категорию...", Toast.LENGTH_SHORT).show()
        }
        else if (pdfUri == null) {
            Toast.makeText(this, "Выберите файл...", Toast.LENGTH_SHORT).show()
        }
        else {
            // data validated, begin upload
            uploadPdf()
        }
    }

    private fun uploadPdf() {
        Log.d(TAG, "uploadPdf: Uploading to storage...")

        // show progress dialog
        progressDialog.setMessage("Uploading PDF...")
        progressDialog.show()

        // timestamp
        val timestamp= System.currentTimeMillis()

        // path of pdf in firebase storage
        val filePathAndName = "Books/$timestamp"

        // storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPDF: PDF uploaded now getting url...")

                // get url
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);

                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl, timestamp)

            }
            .addOnFailureListener {e->
                Log.d(TAG, "uploadPDF: Failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Не удалось загрузить файл из-за ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        // Upload Pdf info to firebase db
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading pdf info...")

        // uid of current user
        val uid = firebaseAuth.uid

        // setup data to upload
        val book = Book("$timestamp", uid!!, selectedCategoryId, title, description, uploadedPdfUrl, timestamp, 0, 0)

        // db reference DB > Books > BookId > (Book Info)
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(book)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: uploaded to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded...", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener { e->
                Log.d(TAG, "uploadPdfInfoToDb: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories")

        // init arraylist
        categoryArrayList = ArrayList()

        // db reference to load categories DF > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before adding data
                categoryArrayList.clear()

                for (df in snapshot.children) {
                    // get data
                    val model = df.getValue(ModelCategory::class.java)

                    // add to arraylist
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        // get string array of categories from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)

        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        // alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выберите категорию")
            .setItems(categoriesArray) { dialog, which ->
                // handle item click
                // get clicked item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                // set category to textview
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected category Title: $selectedCategoryTitle")
            }
            .show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent:  starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            }
            else {
                Log.d(TAG, "PDF Pick cancelled")
                Toast.makeText(this, "Не удалось загрузить PDF-файл", Toast.LENGTH_SHORT).show()
            }
        }
    )
}