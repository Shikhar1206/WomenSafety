package com.example.womensafety.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.womensafety.data.local.dao.LocationHistoryDao
import com.example.womensafety.domain.repository.SosRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationHistoryDao: LocationHistoryDao,
    private val sosRepository: SosRepository
) : ViewModel() {

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints.asStateFlow()

    init {
        viewModelScope.launch {
            sosRepository.getActiveSos().collectLatest { activeSos ->
                if (activeSos != null) {
                    locationHistoryDao.getLocationsBySosId(activeSos.id).collect { entities ->
                        _routePoints.value = entities.map { LatLng(it.latitude, it.longitude) }
                    }
                } else {
                    _routePoints.value = emptyList()
                }
            }
        }
    }
}
