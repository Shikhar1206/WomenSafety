package com.example.womensafety.domain.usecase.contact

import com.example.womensafety.core.util.Resource
import com.example.womensafety.domain.model.Contact
import com.example.womensafety.domain.repository.ContactRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AddContactUseCaseTest {

    private lateinit var useCase: AddContactUseCase
    private val repository: ContactRepository = mockk()

    @Before
    fun setUp() {
        useCase = AddContactUseCase(repository)
    }

    @Test
    fun `returns error when name is too short`() = runTest {
        val result = useCase("A", "+919999999999", "Family")
        assertTrue(result is Resource.Error)
        assertEquals("Name must be at least 2 characters", (result as Resource.Error).message)
    }

    @Test
    fun `returns error when phone is invalid`() = runTest {
        val result = useCase("John Doe", "123", "Family")
        assertTrue(result is Resource.Error)
        assertEquals("Please enter a valid phone number", (result as Resource.Error).message)
    }

    @Test
    fun `returns error when phone already exists`() = runTest {
        coEvery { repository.findByPhone("+919999999999") } returns Contact(
            id = "1", name = "Existing", phone = "+919999999999"
        )
        val result = useCase("New Person", "+919999999999", "Family")
        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message.contains("Existing"))
    }

    @Test
    fun `returns success for valid new contact`() = runTest {
        coEvery { repository.findByPhone("+919876543210") } returns null
        coEvery { repository.addContact(any()) } returns Resource.Success(Unit)

        val result = useCase("John Doe", "+919876543210", "Family")
        assertTrue(result is Resource.Success)
    }
}
