package com.example.infracredit.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.infracredit.data.remote.dto.ProfileDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToProfileEdit: () -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state = viewModel.profileState.value
    val isDarkMode by viewModel.isDarkMode
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            ProfileHeader(
                profile = state.profile,
                isLoading = state.isLoading,
                onImageSelect = { uri ->
                    // Simplified: In a real app, upload this URI to Cloudinary/S3
                    viewModel.updateProfile(
                        fullName = state.profile?.fullName ?: "",
                        businessName = state.profile?.businessName,
                        profilePic = uri.toString()
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Sections
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.Default.Edit, 
                    title = "Edit Profile", 
                    subtitle = "Change name, business details",
                    onClick = onNavigateToProfileEdit
                )
                SettingsItem(
                    icon = Icons.Default.DeleteSweep, 
                    title = "Recycle Bin", 
                    subtitle = "Restore deleted customers",
                    onClick = onNavigateToRecycleBin
                )
            }

            SettingsSection(title = "Display") {
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode() }
                )
            }

            SettingsSection(title = "Security & Help") {
                SettingsItem(
                    icon = Icons.Default.Lock, 
                    title = "App Lock", 
                    subtitle = "Secure your ledger with PIN/Fingerprint"
                )
                SettingsItem(
                    icon = Icons.Default.HelpOutline, 
                    title = "Help & Support", 
                    subtitle = "Contact us for issues",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/infracredit_support"))
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.logout(onLogout) },
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                "InfraCredit v1.0.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ProfileHeader(
    profile: ProfileDto?, 
    isLoading: Boolean,
    onImageSelect: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelect(it) }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    } else {
                        // In real app, use Coil or Glide to load profile.profilePic
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                    }
                }
                IconButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Edit Picture", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (profile != null) {
                Text(profile.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(profile.phone ?: "No Phone", fontSize = 14.sp, color = Color.Gray)
                
                profile.businessName?.let {
                    ShopChip(label = it)
                }
            }
        }
    }
}

@Composable
fun ShopChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SettingsToggleItem(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
