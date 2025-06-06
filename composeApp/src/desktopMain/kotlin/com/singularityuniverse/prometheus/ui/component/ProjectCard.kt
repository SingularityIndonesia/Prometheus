package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.entity.Project
import com.singularityuniverse.prometheus.utils.formatDate
import com.singularityuniverse.prometheus.utils.formatFileSize
import org.jetbrains.compose.resources.painterResource
import prometheus.composeapp.generated.resources.Res
import prometheus.composeapp.generated.resources.ic_delete
import prometheus.composeapp.generated.resources.ic_folder
import prometheus.composeapp.generated.resources.ic_more_vert

@Composable
fun ProjectCard(
    project: Project,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onDeleteClick: () -> Unit,
    onOpenFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(120.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Size: ${formatFileSize(project.modelFileSize)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Last modified: ${formatDate(project.lastModified)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // More options menu
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                IconButton(
                    onClick = onToggleExpanded
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_more_vert),
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = onToggleExpanded
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDeleteClick()
                            onToggleExpanded()
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_delete),
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            // Quick action buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
            ) {
                IconButton(
                    onClick = onOpenFolder
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_folder),
                        contentDescription = "Open folder"
                    )
                }
            }
        }
    }
}
