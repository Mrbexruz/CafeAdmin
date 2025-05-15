package com.example.cafeadmin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cafeadmin.databinding.ActivityMainBinding
import com.example.cafeadmin.fragments.MenuFragment
import com.example.cafeadmin.fragments.SettingsFragment
import com.example.cafeadmin.fragments.TablesFragment
import com.example.cafeadmin.utils.BaseActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class MainActivity : BaseActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupNewOrderListener()

        // Dastlabki fragment
        supportFragmentManager.beginTransaction()
            .replace(binding.containerMain.id, TablesFragment())
            .commit()

        // BottomNavigation ishlovchisi
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_tables -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.containerMain.id, TablesFragment())
                        .commit()
                    true
                }
                R.id.nav_menu -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.containerMain.id, MenuFragment())
                        .commit()
                    true
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.containerMain.id, SettingsFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNewOrderListener() {
        val ref = FirebaseDatabase.getInstance().getReference("NewOrders")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val tableId = snapshot.key ?: return
                showNotification(tableId) // Stol raqami bilan notificationni yuboramiz

                // So‘ng bu table uchun flag’ni olib tashlaymiz
                snapshot.ref.removeValue()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showNotification(tableId: String) {
        val title = getString(R.string.notification_title)
        val message = getString(R.string.new_order_message, tableId)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "orders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Buyurtma Kanali", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

}