package com.example.infracredit.ui.customer

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

data class ContactInfo(
    val name: String,
    val phone: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactImportScreen(
    onNavigateBack: () -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<ContactInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            contacts = fetchContacts(context.contentResolver)
        } else {
            launcher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import from Contacts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            if (!hasPermission) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Permission required to read contacts")
                        Button(onClick = { launcher.launch(Manifest.permission.READ_CONTACTS) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search contacts") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                val filteredContacts = contacts.filter {
                    it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredContacts) { contact ->
                        ContactItem(contact) {
                            viewModel.addCustomer(contact.name, contact.phone)
                            onNavigateBack()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItem(contact: ContactInfo, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(contact.name, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(contact.phone) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9ECEF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.White)
    )
    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
}

@SuppressLint("Range")
private fun fetchContacts(contentResolver: ContentResolver): List<ContactInfo> {
    val contactsList = mutableListOf<ContactInfo>()
    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        while (it.moveToNext()) {
            val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phone = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            contactsList.add(ContactInfo(name, phone))
        }
    }
    return contactsList
}
