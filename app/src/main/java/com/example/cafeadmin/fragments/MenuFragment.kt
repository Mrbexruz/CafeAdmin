package com.example.cafeadmin.fragments

import android.R
import android.app.AlertDialog
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cafeadmin.adapters.MenuAdapter
import com.example.cafeadmin.databinding.DialogAddFoodBinding
import com.example.cafeadmin.databinding.FragmentMenuBinding
import com.example.cafeadmin.models.Food
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private val foodList = mutableListOf<Food>()
    private lateinit var menuAdapter: MenuAdapter
    private var imageUri: Uri? = null
    private var category: String? = null


    private val databaseRef = FirebaseDatabase.getInstance().getReference("menu")

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            imageUri = it
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            // imageUri already set and photo saved
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)


        menuAdapter = MenuAdapter(foodList) { food ->
            deleteFood(food)
        }
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        binding.rv.adapter = menuAdapter

        category = arguments?.getString(ARG_CATEGORY)

        binding.btnAdd.setOnClickListener { showAddDialog() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.VISIBLE

        loadMenuFromFirebase(getSelectedCategory())

        binding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val selectedCategory = getSelectedCategory()
                loadMenuFromFirebase(category = selectedCategory)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String): MenuFragment {
            val fragment = MenuFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }


    private fun showAddDialog() {
        val dialogBinding = DialogAddFoodBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.imgSelect.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menu.add(0, 1, 1, "Camera")
            popupMenu.menu.add(0, 2, 2, "Gallery")

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> takePhotoFromCamera()
                    2 -> pickImageFromGallery()
                }
                true
            }

            popupMenu.show()
        }

        val spinnerCategory = dialogBinding.spinnerCategory

        // Spinner uchun adapter yaratish
        val categories = arrayOf(getString(com.example.cafeadmin.R.string.ichimliklar),
            getString(com.example.cafeadmin.R.string.fast_food),
            getString(com.example.cafeadmin.R.string.quyuq_ovqatlar),
            getString(com.example.cafeadmin.R.string.suyuq_ovqatlar))
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etName.text.toString()
            val price = dialogBinding.etPrice.text.toString()
            val category = spinnerCategory.selectedItem.toString()

            if (imageUri != null && name.isNotEmpty() && price.isNotEmpty()) {
                dialogBinding.progressBar.visibility = View.VISIBLE
                dialogBinding.btnSave.isEnabled = false
                dialogBinding.btnCancel.isEnabled = false

                val id = System.currentTimeMillis().toString()
                val ref = FirebaseStorage.getInstance()
                    .getReference("menu_images/$id.jpg")
                ref.putFile(imageUri!!).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        val food = Food(id, name, price, category, uri.toString())
                        databaseRef.child(id).setValue(food)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "✅", Toast.LENGTH_SHORT).show()
                                // Kategoriya bo‘yicha ViewPager indexni o‘zgartirish
                                val categoryIndex = when (category) {
                                    getString(com.example.cafeadmin.R.string.ichimliklar) -> 0
                                    getString(com.example.cafeadmin.R.string.fast_food) -> 1
                                    getString(com.example.cafeadmin.R.string.quyuq_ovqatlar) -> 2
                                    getString(com.example.cafeadmin.R.string.suyuq_ovqatlar) -> 3
                                    else -> 0
                                }
                                dialog.dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Xatolik: ${it.message}", Toast.LENGTH_SHORT).show()
                                dialogBinding.progressBar.visibility = View.GONE
                                dialogBinding.btnSave.isEnabled = true
                                dialogBinding.btnCancel.isEnabled = true
                            }
                    }
                }
            } else {
                Toast.makeText(requireContext(),
                    getString(com.example.cafeadmin.R.string.ma_lumotlarni_to_ldiring), Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun takePhotoFromCamera() {
        val uri = createImageUri()
        imageUri = uri
        takePhotoLauncher.launch(uri)
    }

    private fun deleteFood(food: Food) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(com.example.cafeadmin.R.string.ochirish))
            .setMessage(getString(com.example.cafeadmin.R.string.ni_o_chirmoqchimisiz, food.name))
            .setPositiveButton(getString(com.example.cafeadmin.R.string.ha)) { _, _ ->
                // Avval rasmni Firebase Storage'dan o‘chiramiz
                val imageRef = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(food.imageUrl)

                imageRef.delete().addOnSuccessListener {
                    // Keyin realtime database'dan o‘chiramiz
                    databaseRef.child(food.id).removeValue().addOnSuccessListener {
                        Toast.makeText(requireContext(),
                            getString(com.example.cafeadmin.R.string.o_chirildi), Toast.LENGTH_SHORT).show()
                        loadMenuFromFirebase(getSelectedCategory())                     }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(com.example.cafeadmin.R.string.yo_q), null)
            .show()
    }

    // Bu metod TabLayout tanlangan pozitsiyasiga qarab kategoriya nomini qaytaradi
    private fun getSelectedCategory(): String {
        return when (binding.tablayout.selectedTabPosition) {
            0 -> getString(com.example.cafeadmin.R.string.ichimliklar) // 0 - Ichimliklar
            1 -> getString(com.example.cafeadmin.R.string.fast_food)   // 1 - Fast Food
            2 -> getString(com.example.cafeadmin.R.string.quyuq_ovqatlar)  // 2 - Quyuq Ovqatlar
            3 -> getString(com.example.cafeadmin.R.string.suyuq_ovqatlar)  // 3 - Suyuq Ovqatlar
            else -> "" // Default bo‘lsa
        }
    }

    private fun createImageUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )!!
    }

    private fun loadMenuFromFirebase(category: String) {
        foodList.clear()
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val food = child.getValue(Food::class.java)
                    if (food != null && food.category == category) {
                        foodList.add(food)
                    }
                }
                binding.progressBar.visibility = View.GONE
                menuAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Xatolik: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
