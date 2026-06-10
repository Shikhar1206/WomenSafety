package com.example.womensafety.domain.repository

import com.example.womensafety.core.util.Resource
import com.example.womensafety.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getAllContacts(): Flow<List<Contact>>
    suspend fun addContact(contact: Contact): Resource<Unit>
    suspend fun updateContact(contact: Contact): Resource<Unit>
    suspend fun deleteContact(id: String): Resource<Unit>
    suspend fun findByPhone(phone: String): Contact?
    fun getContactCount(): Flow<Int>
    suspend fun syncToFirestore()
}
