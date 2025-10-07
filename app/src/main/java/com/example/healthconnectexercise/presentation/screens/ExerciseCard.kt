package com.example.healthconnectexercise.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.healthconnectexercise.R
import com.example.healthconnectexercise.domain.model.ExerciseLog
import com.example.healthconnectexercise.presentation.viewmodel.ExerciseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.text.format
import java.util.Locale

@Composable
fun ExerciseCard(
    log: ExerciseLog,
    inConflict: Boolean,
    viewModel: ExerciseViewModel
) {

    val timeFormatter = remember  {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    }
    val borderColor =
        if (inConflict) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline

    var showConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (inConflict) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = log.type,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                if (inConflict) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.conflict_description),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold),
                text = stringResource(R.string.duration_minutes_label, (log.end - log.start) / 60000),


                )
            Text(
                text = stringResource(R.string.start_time, timeFormatter.format(Date(log.start))),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
            )
            Text(
                text = stringResource(R.string.end_time, timeFormatter.format(Date(log.end))),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
            )

            log.calories?.let { Text(stringResource(R.string.calories_label, it)) }
            Text(
                text = stringResource(R.string.source_label, log.source.name),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
            )

            if (inConflict) {
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.keep_button), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.keepAndRemoveOther(log)
                }) {
                    Text(stringResource(R.string.dialog_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel_button))
                }
            },
            title = {

                Text(text = log.type,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center) },
            text = {
                Text(
                    stringResource(R.string.confirm_keep_dialog_text)
                )
            }
        )
    }
}
