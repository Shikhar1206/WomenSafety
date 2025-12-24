package com.example.womensafety.contacts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.womensafety.databinding.ActivityContactsBinding
import com.example.womensafety.room.EmergencyContactEntity
import kotlinx.coroutines.launch

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnAdd.setOnClickListener {
            val name = binding.etName.text.toString()
            val phone = binding.etPhone.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                lifecycleScope.launch {
                    db.contactDao().insert(
                        EmergencyContactEntity(name = name, phone = phone)
                    )
                }
                binding.etName.text.clear()
                binding.etPhone.text.clear()
            }
        }
    }
}