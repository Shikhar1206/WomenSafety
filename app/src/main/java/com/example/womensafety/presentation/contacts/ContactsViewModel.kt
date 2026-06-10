package com.example.womensafety.presentation.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.womensafety.core.util.Resource
import com.example.womensafety.domain.model.Contact
import com.example.womensafety.domain.usecase.contact.AddContactUseCase
import com.example.womensafety.domain.usecase.contact.DeleteContactUseCase
import com.example.womensafety.domain.usecase.contact.GetContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val addContactUseCase: AddContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState(isLoading = true))
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            getContactsUseCase()
                .onEach { contacts ->
                    _uiState.value = _uiState.value.copy(
                        contacts = contacts,
                        isLoading = false
                    )
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect()
        }
    }

    fun addContact(name: String, phone: String, relation: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = addContactUseCase(name, phone, relation)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "$name added successfully"
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
                is Resource.Loading -> Unit
            }
        }
    }

    fun deleteContact(id: String, name: String) {
        viewModelScope.launch {
            when (val result = deleteContactUseCase(id)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    successMessage = "$name removed"
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    error = result.message
                )
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
