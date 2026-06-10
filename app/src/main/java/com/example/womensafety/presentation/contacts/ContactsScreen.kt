package com.example.womensafety.presentation.contacts

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.womensafety.domain.model.Contact
import com.example.womensafety.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var contactToDelete by remember { mutableStateOf<Contact?>(null) }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Top Bar ──────────────────────────────────────────────────────
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Emergency Contacts",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnDark
                        )
                        Text(
                            "${uiState.contacts.size} contact${if (uiState.contacts.size != 1) "s" else ""} protected",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = OnDarkSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, GlassBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = OnDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // ── Info Banner ──────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(
                        1.dp,
                        brush = Brush.verticalGradient(
                            listOf(TrustPurpleLight.copy(alpha = 0.2f), TrustPurpleDark.copy(alpha = 0.05f))
                        ),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TrustPurple.copy(alpha = 0.08f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = TrustPurpleLight, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "These contacts receive SMS alerts when SOS is activated",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnDarkSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Contacts List ────────────────────────────────────────────────
            if (uiState.isLoading && uiState.contacts.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TrustPurpleLight)
                }
            } else if (uiState.contacts.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    EmptyContactsState(onAdd = { showAddSheet = true })
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.contacts,
                        key = { it.id }
                    ) { contact ->
                        SwipeToDeleteContactItem(
                            contact = contact,
                            onDelete = { contactToDelete = contact }
                        )
                    }
                }
            }
        }

        // ── Gradient Floating Action Button ─────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(60.dp)
                .background(Brush.horizontalGradient(PrimaryGradient), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                .clip(CircleShape)
                .clickable { showAddSheet = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PersonAdd, "Add Contact", tint = Color.White, modifier = Modifier.size(26.dp))
        }

        // ── Success/Error Banner Overlay ─────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.successMessage != null || uiState.error != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp, start = 16.dp, end = 16.dp),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            val isSuccess = uiState.successMessage != null
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) SafeGreen else SafetyRed
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Icon(
                        if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        null, tint = Color.White, modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        uiState.successMessage ?: uiState.error ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }

    // ── Delete Confirmation Dialog ───────────────────────────────────────────
    contactToDelete?.let { contact ->
        AlertDialog(
            onDismissRequest = { contactToDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = SafetyRed, modifier = Modifier.size(36.dp)) },
            title = { Text("Remove Contact", color = OnDark, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Remove ${contact.name} from emergency contacts? They will no longer receive SOS alerts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnDarkSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteContact(contact.id, contact.name)
                        contactToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Remove", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { contactToDelete = null }) { Text("Cancel", color = OnDarkSecondary) }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
        )
    }

    // ── Add Contact Bottom Sheet ─────────────────────────────────────────────
    if (showAddSheet) {
        AddContactBottomSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { name, phone, relation ->
                viewModel.addContact(name, phone, relation)
                showAddSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContactItem(
    contact: Contact,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SafetyRed.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        ContactCard(contact = contact)
    }
}

@Composable
fun ContactCard(contact: Contact) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.01f))
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.02f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with neon gradient
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        Brush.linearGradient(PrimaryGradient),
                        CircleShape
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    contact.name.first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    contact.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnDark
                )
                Text(
                    contact.phone,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = OnDarkSecondary
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(TrustPurple.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .border(1.dp, TrustPurpleLight.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = contact.relation,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TrustPurpleLight
                    )
                }
            }
            Icon(Icons.Default.ChevronLeft, null, tint = OnDarkSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun EmptyContactsState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(TrustPurple.copy(alpha = 0.08f), CircleShape)
                .border(1.dp, TrustPurpleLight.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PersonOff, null, tint = TrustPurpleLight, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "No Contacts Yet",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = OnDark
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Add emergency contacts so they receive SOS alerts with your location when you need help",
            style = MaterialTheme.typography.bodyMedium,
            color = OnDarkSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .background(Brush.horizontalGradient(PrimaryGradient), RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .height(48.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.PersonAdd, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Add First Contact", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRelation by remember { mutableStateOf("Family") }
    val relations = listOf("Family", "Friend", "Partner", "Colleague", "Other")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Outline) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Add Emergency Contact",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = OnDark
            )
            Text(
                "They'll receive SMS when SOS is activated",
                style = MaterialTheme.typography.bodySmall,
                color = OnDarkSecondary
            )
            Spacer(Modifier.height(24.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = OnDarkSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TrustPurpleLight,
                    unfocusedBorderColor = Outline,
                    focusedLabelColor = TrustPurpleLight,
                    unfocusedLabelColor = OnDarkSecondary,
                    focusedTextColor = OnDark,
                    unfocusedTextColor = OnDark
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(14.dp))

            // Phone Field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = OnDarkSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                prefix = { if (!phone.startsWith("+")) Text("+91 ", color = OnDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TrustPurpleLight,
                    unfocusedBorderColor = Outline,
                    focusedLabelColor = TrustPurpleLight,
                    unfocusedLabelColor = OnDarkSecondary,
                    focusedTextColor = OnDark,
                    unfocusedTextColor = OnDark
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Relation selection
            Text(
                "Relation",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = OnDarkSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                relations.forEach { relation ->
                    val isSelected = selectedRelation == relation
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedRelation = relation },
                        label = { Text(relation, style = MaterialTheme.typography.bodySmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TrustPurple,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.02f),
                            labelColor = OnDarkSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color.Transparent else Outline,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Add Button
            Button(
                onClick = { if (name.isNotBlank() && phone.isNotBlank()) onAdd(name, phone, selectedRelation) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(
                        Brush.horizontalGradient(PrimaryGradient),
                        shape = RoundedCornerShape(16.dp)
                    ),
                enabled = name.isNotBlank() && phone.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.White.copy(alpha = 0.02f)
                )
            ) {
                Icon(Icons.Default.PersonAdd, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Add Contact", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
