package com.example.womensafety.domain.usecase.sos

import com.example.womensafety.core.util.Resource
import com.example.womensafety.domain.repository.SosRepository
import javax.inject.Inject

class TriggerSosUseCase @Inject constructor(
    private val repository: SosRepository
) {
    suspend operator fun invoke(
        triggeredBy: String,
        lat: Double? = null,
        lng: Double? = null,
        address: String? = null
    ): Resource<String> = repository.triggerSos(triggeredBy, lat, lng, address)
}
