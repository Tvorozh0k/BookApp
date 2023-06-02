package com.example.bookapp_moskvitin.presentation.dashboard.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp_moskvitin.data.entity.ModelCategory
import com.example.bookapp_moskvitin.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory: RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context: Context
    var categoryArrayList: ArrayList<ModelCategory>
    private var filterList: ArrayList<ModelCategory>

    private var filter: FilterCategory? = null

    private lateinit var binding: RowCategoryBinding

    // constructor
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        // inflate / bind row_category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size // number of items in list
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        // get data, set data, handle clicks

        // get data
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        // set data
        holder.categoryTv.text = category

        // handle clicks, delete category
        holder.deleteBtn.setOnClickListener {
            // confirm before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Удаление категории")
                .setMessage("Вы действительно хотите удалить данную категорию?")
                .setPositiveButton("Да") { a, d->
                    Toast.makeText(context, "Удаляем категорию...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }
                .setNegativeButton("Нет") { a, d->
                    a.dismiss()
                }
                .show()
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        // get id of category to delete
        val id = model.id

        // Firebase bd > Categories > categoryID
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Категория удалена...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context, "Не удалось осуществить удаление из-за ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // viewholder class to hold / init UI views for row_category.xml
    inner class HolderCategory(itemView: View): RecyclerView.ViewHolder(itemView) {
        // init ui views
        var categoryTv: TextView = binding.categoryTv
        var deleteBtn: AppCompatImageButton = binding.deleteBtn

    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCategory(filterList, this)
        }

        return filter as FilterCategory
    }
}