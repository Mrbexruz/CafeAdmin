package com.example.admincafe.fragments

import OrdersAdapter
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cafeadmin.R
import com.example.cafeadmin.databinding.FragmentOrdersBinding
import com.example.cafeadmin.models.Order
import com.example.cafeadmin.models.TableOrder
import com.google.firebase.database.*

class OrderFragment : Fragment() {
    private lateinit var binding: FragmentOrdersBinding
    private lateinit var ordersAdapter: OrdersAdapter
    private var tableNumber: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater, container, false)

        binding.btnDone.setOnClickListener {
            markOrderAsReady()
            completeOrder(tableNumber)
        }

        binding.btnClear.setOnClickListener {
            clearAllOrdersForTable()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Arguments dan stol raqamini olish
        arguments?.let {
            tableNumber = it.getString("table_number", "")
        }

        if (tableNumber.isEmpty()) {
            Log.e("OrderFragment", "Table number not provided")
            requireActivity().onBackPressed()
            return
        }

        setupRecyclerView()
        loadOrdersForTable()
    }

    private fun markOrderAsReady() {
        val orderRef = FirebaseDatabase.getInstance()
            .getReference("Orders")
            .child("table_$tableNumber")

        orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(),
                        getString(R.string.buyurtma_topilmadi), Toast.LENGTH_SHORT).show()
                    return
                }

                for (orderSnapshot in snapshot.children) {
                    orderSnapshot.ref.child("status").setValue("ready") // status: ready
                }

                Toast.makeText(requireContext(),
                    getString(R.string.buyurtma_tayyor_deb_belgilandi), Toast.LENGTH_SHORT).show()

                // Mijozga yuborish uchun "ReadyOrders" node'ga ham yozamiz
                val readyRef = FirebaseDatabase.getInstance()
                    .getReference("ReadyOrders")
                    .child("table_$tableNumber")

                readyRef.setValue(true) // yoki vaqt bilan yozmoqchi bo‘lsang: ServerValue.TIMESTAMP

                // Keyin yuklashni yangilaymiz
                loadOrdersForTable()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Xatolik: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun completeOrder(tableNumber: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Orders/table_$tableNumber")
        ref.orderByChild("status").equalTo("ready")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (orderSnap in snapshot.children) {
                        orderSnap.ref.child("status").setValue("done")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Admin", "Buyurtmani yakunlashda xatolik: ${error.message}")
                }
            })
    }

    private fun clearAllOrdersForTable() {
        if (tableNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Stol raqami aniqlanmadi", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("Orders")
            .child("table_$tableNumber")

        ref.removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(),
                    getString(R.string.barcha_buyurtmalar_o_chirildi), Toast.LENGTH_SHORT).show()
                ordersAdapter.updateOrders(emptyList()) // adapterni bo'shatamiz
                binding.tvTotal.text = getString(R.string.umumiy_summa_0_so_m)
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Xatolik: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter()
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ordersAdapter
        }
    }

    private fun loadOrdersForTable() {
        binding.progressBar.visibility = View.VISIBLE

        FirebaseDatabase.getInstance().getReference("Orders")
            .child("table_$tableNumber")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orders = mutableListOf<TableOrder>()
                    var grandTotal = 0

                    if (!snapshot.exists()) {
                        binding.progressBar.visibility = View.GONE
                        return
                    }

                    for (orderSnapshot in snapshot.children) {
                        try {
                            val status = orderSnapshot.child("status").getValue(String::class.java) ?: "pending"
                            val total = orderSnapshot.child("total").getValue(Int::class.java) ?: 0

                            // Items listini yaratish
                            val items = mutableListOf<Order>()
                            for (itemSnapshot in orderSnapshot.child("items").children) {
                                items.add(Order(
                                    name = itemSnapshot.child("name").getValue(String::class.java) ?: "",
                                    price = itemSnapshot.child("price").getValue(Int::class.java) ?: 0,
                                    count = itemSnapshot.child("count").getValue(Int::class.java) ?: 0,
                                    imageUrl = itemSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                                ))
                            }

                            grandTotal += total
                            orders.add(TableOrder(
                                tableNumber = tableNumber,
                                status = status,
                                total = total,
                                items = items
                            ))
                        } catch (e: Exception) {
                            Log.e("OrderFragment", "Error parsing order: ${e.message}")
                        }
                    }

                    binding.progressBar.visibility = View.GONE
                    ordersAdapter.updateOrders(orders)
                    binding.tvTotal.text = "Umumiy summa: $grandTotal so'm"
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Xatolik: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}