package com.example.healthconnectexercise.presentation.screens

import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.healthconnectexercise.R
import com.example.healthconnectexercise.presentation.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onConflicts: () -> Unit,
    onSync: () -> Unit,
    vm: ExerciseViewModel,
    permissionLauncher: ManagedActivityResultLauncher<Set<String>, Set<String>>
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val showPermissionRationale by vm.showPermissionRationale.collectAsState()
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { vm.onPermissionRationaleDismissed() },
            title = { Text("Permission Required") },
            text = { Text("To sync your exercises, this app needs permission to access your Health Connect data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // When user clicks "OK", launch the actual permission request
                        vm.launchPermissionsRequest(permissionLauncher)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.onPermissionRationaleDismissed() }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(state.conflicts.size) {
        if (state.conflicts.isNotEmpty()) {
            Toast.makeText(
                context,
                context.getString(R.string.conflicts_detected_toast, state.conflicts.size),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        vm.load()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.home_screen_title)) },
                actions = {
                    IconButton(onClick = onSync) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync")
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (state.logs.isNotEmpty()) {
                FloatingActionButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Add Log")
                }
            }
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.loading -> CircularProgressIndicator()
                state.logs.isEmpty() -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_logs_yet),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(

                        onClick = onAdd,
                        modifier = Modifier
                            .padding(32.dp) // Added padding here
                    ) {
                        Text("Add Manual Log",
                            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        )
                    }
                }
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.logs) { log ->
                        val conflictPair = state.conflicts.firstOrNull {
                            it.first.id == log.id || it.second.id == log.id
                        }
                        val inConflict = conflictPair != null

                        ExerciseCard(
                            viewModel = vm,
                            log = log,
                            inConflict = inConflict,
                        )
                    }
                }
            }
        }
    }

}
