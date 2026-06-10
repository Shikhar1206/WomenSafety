package com.example.womensafety.domain.usecase.sos

import com.example.womensafety.core.util.Resource
import com.example.womensafety.domain.repository.SosRepository
import javax.inject.Inject

class CancelSosUseCase @Inject constructor(
    private val repository: SosRepository
) {
    suspend operator fun invoke(sosId: String): Resource<Unit> = repository.cancelSos(sosId)
}
