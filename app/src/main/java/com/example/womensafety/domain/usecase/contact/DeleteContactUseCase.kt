package com.example.womensafety.domain.usecase.contact

import com.example.womensafety.core.util.Resource
import com.example.womensafety.domain.repository.ContactRepository
import javax.inject.Inject

class DeleteContactUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    suspend operator fun invoke(id: String): Resource<Unit> = repository.deleteContact(id)
}
