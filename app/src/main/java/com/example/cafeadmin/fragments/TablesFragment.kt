package com.example.cafeadmin.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.admincafe.fragments.OrderFragment
import com.example.cafeadmin.R
import com.example.cafeadmin.adapters.TableAdapter
import com.example.cafeadmin.databinding.FragmentTablesBinding
import com.example.cafeadmin.models.Table
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TablesFragment : Fragment() {

    private val binding by lazy { FragmentTablesBinding.inflate(layoutInflater) }
    private lateinit var tableAdapter: TableAdapter
    private val tableList = mutableListOf<Table>()

    private val databaseRef = FirebaseDatabase.getInstance().getReference("tables")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setupRecyclerView()
        loadTablesFromFirebase()
        loadTotalPricesForTables()


        binding.btnAdd.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_table_dialog, null)
            val editText = dialogView.findViewById<EditText>(R.id.etTableNumber)

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.yangi_stol_qo_shish))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.saqlash)) { _, _ ->
                    val numberText = editText.text.toString()
                    if (numberText.isNotEmpty()) {
                        val newTable = Table(
                            id = System.currentTimeMillis().toInt(),
                            number = getString(R.string.stol, numberText)
                        )
                        val dbRef = FirebaseDatabase.getInstance().getReference("tables")
                        dbRef.child(newTable.id.toString()).setValue(newTable)
                            .addOnSuccessListener {
                                tableAdapter.addTable(newTable)
                                context?.let {
                                    Toast.makeText(
                                        it,
                                        getString(R.string.qoshildi, newTable.number),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnFailureListener {
                                context?.let {
                                    Toast.makeText(it, getString(R.string.xatolik), Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                    }
                }
                .setNegativeButton(getString(R.string.bekor_qilish), null)
                .show()
        }


        return binding.root
    }

    private fun loadTablesFromFirebase() {
        binding.progressBar.visibility = View.VISIBLE
        val ref = FirebaseDatabase.getInstance().getReference("tables")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tableList.clear()
                for (data in snapshot.children) {
                    val table = data.getValue(Table::class.java)
                    table?.let { tableList.add(it) }
                }
                binding.progressBar.visibility = View.GONE
                tableAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.xatolik, error.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadTotalPricesForTables() {
        val ordersRef = FirebaseDatabase.getInstance().getReference("Orders")
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val busyIds = mutableSetOf<Int>()

                for (orderSnapshot in snapshot.children) {
                    val tableId = orderSnapshot.child("tableId").getValue(Int::class.java)
                    tableId?.let { busyIds.add(it) }
                }

                // Log qo'shamiz tekshirish uchun
                Log.d("TablesFragment", "Busy tables: $busyIds")

                // Yangilash
                tableAdapter.updateBusyTables(busyIds)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun deleteTable(table: Table) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.ochirish))
            .setMessage(getString(R.string.ni_o_chirmoqchimisiz, table.number))
            .setPositiveButton(getString(R.string.ha)) { _, _ ->
                databaseRef.child(table.id.toString()).removeValue().addOnSuccessListener {
                    tableAdapter.removeTable(table)
                    Toast.makeText(requireContext(), getString(R.string.o_chirildi), Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), getString(R.string.xatolik, it.message), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.yo_q), null)
            .show()
    }

    private fun setupRecyclerView() {
        tableAdapter = TableAdapter(
            tableList,
            onDelete = { table -> deleteTable(table) },
            onTableClick = { table ->
                val fragment = OrderFragment()
                val bundle = Bundle()
                bundle.putInt("table_id", table.id)  // table id ni uzatish
                bundle.putString("table_number", table.number)  // table number ni uzatish
                fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.rvTable.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvTable.adapter = tableAdapter
    }
}
