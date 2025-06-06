package com.singularityuniverse.prometheus.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.singularityuniverse.prometheus.entity.Project

@Composable
fun DeleteProjectDialog(
    project: Project?,
    onDismiss: () -> Unit,
    onConfirm: (Project) -> Unit
) {
    project?.let {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Delete Project") },
            text = {
                Text("Are you sure you want to delete the project \"${it.name}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm(it)
                        onDismiss()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
