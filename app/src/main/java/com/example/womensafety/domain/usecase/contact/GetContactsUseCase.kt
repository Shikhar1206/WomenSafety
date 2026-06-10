package com.example.womensafety.domain.usecase.contact

import com.example.womensafety.domain.model.Contact
import com.example.womensafety.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContactsUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    operator fun invoke(): Flow<List<Contact>> = repository.getAllContacts()
}
