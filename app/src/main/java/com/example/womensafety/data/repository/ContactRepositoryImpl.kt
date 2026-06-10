package com.example.womensafety.data.repository

import com.example.womensafety.core.util.Resource
import com.example.womensafety.data.local.dao.ContactDao
import com.example.womensafety.data.local.entity.ContactEntity
import com.example.womensafety.domain.model.Contact
import com.example.womensafety.domain.repository.ContactRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ContactRepository {

    override fun getAllContacts(): Flow<List<Contact>> =
        contactDao.getAllContacts().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addContact(contact: Contact): Resource<Unit> {
        return try {
            contactDao.insert(contact.toEntity())
            syncToFirestore()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add contact")
            Resource.Error("Failed to save contact: ${e.message}", e)
        }
    }

    override suspend fun updateContact(contact: Contact): Resource<Unit> {
        return try {
            contactDao.update(contact.toEntity())
            syncToFirestore()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update contact")
            Resource.Error("Failed to update contact: ${e.message}", e)
        }
    }

    override suspend fun deleteContact(id: String): Resource<Unit> {
        return try {
            contactDao.deleteById(id)
            val userId = auth.currentUser?.uid
            if (userId != null) {
                firestore.collection("users")
                    .document(userId)
                    .collection("contacts")
                    .document(id)
                    .delete()
                    .await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete contact")
            Resource.Error("Failed to delete contact: ${e.message}", e)
        }
    }

    override suspend fun findByPhone(phone: String): Contact? =
        contactDao.findByPhone(phone.trim())?.toDomain()

    override fun getContactCount(): Flow<Int> = contactDao.getActiveContactCount()

    override suspend fun syncToFirestore() {
        try {
            val userId = auth.currentUser?.uid ?: return
            val unsynced = contactDao.getUnsyncedContacts()
            unsynced.forEach { entity ->
                firestore.collection("users")
                    .document(userId)
                    .collection("contacts")
                    .document(entity.id)
                    .set(entity.toFirestoreMap())
                    .await()
                contactDao.markAsSynced(entity.id)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync contacts to Firestore")
        }
    }

    // Mapping functions
    private fun ContactEntity.toDomain() = Contact(
        id = id,
        name = name,
        phone = phone,
        relation = relation,
        isActive = isActive,
        avatarUri = avatarUri
    )

    private fun Contact.toEntity() = ContactEntity(
        id = id.ifEmpty { UUID.randomUUID().toString() },
        name = name,
        phone = phone,
        relation = relation,
        isActive = isActive,
        avatarUri = avatarUri,
        updatedAt = System.currentTimeMillis()
    )

    private fun ContactEntity.toFirestoreMap() = mapOf(
        "id" to id,
        "name" to name,
        "phone" to phone,
        "relation" to relation,
        "isActive" to isActive,
        "updatedAt" to updatedAt
    )
}
