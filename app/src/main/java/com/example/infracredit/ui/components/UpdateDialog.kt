package com.example.infracredit.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.infracredit.BuildConfig

@Composable
fun UpdateDialog(
    onDismiss: () -> Unit,
    onUpdateClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Version Available") },
        text = { 
            Text("A new version of InfraCredit is available.\n\n" +
                 "Current Version: ${BuildConfig.VERSION_CODE} (${BuildConfig.VERSION_NAME})\n" +
                 "Please update for the best experience.") 
        },
        confirmButton = {
            Button(onClick = onUpdateClick) {
                Text("Update Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}
