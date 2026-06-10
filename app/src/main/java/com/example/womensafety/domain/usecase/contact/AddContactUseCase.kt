package com.example.womensafety.domain.usecase.contact

import com.example.womensafety.core.util.Resource
import com.example.womensafety.core.util.isValidName
import com.example.womensafety.core.util.isValidPhoneNumber
import com.example.womensafety.domain.model.Contact
import com.example.womensafety.domain.repository.ContactRepository
import java.util.UUID
import javax.inject.Inject

class AddContactUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    suspend operator fun invoke(name: String, phone: String, relation: String): Resource<Unit> {
        if (!name.isValidName()) {
            return Resource.Error("Name must be at least 2 characters")
        }
        if (!phone.isValidPhoneNumber()) {
            return Resource.Error("Please enter a valid phone number")
        }

        // Duplicate detection
        val existing = repository.findByPhone(phone.trim())
        if (existing != null) {
            return Resource.Error("A contact with this phone number already exists: ${existing.name}")
        }

        val contact = Contact(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            phone = phone.trim(),
            relation = relation
        )
        return repository.addContact(contact)
    }
}
