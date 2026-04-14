package com.andreykaranik.gpstracker.presentation.view

import android.app.TimePickerDialog
import android.app.DatePickerDialog
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andreykaranik.gpstracker.R
import com.andreykaranik.gpstracker.domain.model.ModeParameters
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionBottomSheet(
    selectedMode: ModeType,
    modeParameters: ModeParameters,
    onModeSelected: (ModeType) -> Unit,
    onParametersChanged: (ModeParameters) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.select_mode),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            ModeType.values().forEachIndexed { index, mode ->
                ModeItem(
                    mode = mode,
                    isSelected = selectedMode == mode,
                    parameters = modeParameters,
                    onSelect = { onModeSelected(mode) },
                    onParametersChanged = onParametersChanged
                )
                if (index < ModeType.values().lastIndex) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun ModeItem(
    mode: ModeType,
    isSelected: Boolean,
    parameters: ModeParameters,
    onSelect: () -> Unit,
    onParametersChanged: (ModeParameters) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onSelect() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isSelected, onClick = onSelect)
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(id = mode.displayNameRes),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (isSelected) {
            Spacer(Modifier.height(8.dp))
            mode.parametersContent(parameters, onParametersChanged)
        }
    }
}

enum class ModeType(
    @StringRes val displayNameRes: Int,
    val parametersContent: @Composable (ModeParameters, (ModeParameters) -> Unit) -> Unit
) {
    CURRENT_LOCATION(R.string.mode_live_tracking, { params, onChange ->
        SwitchWithLabel(
            label = stringResource(R.string.label_kalman_filter),
            checked = params.kalmanEnabled,
            onCheckedChange = { onChange(params.copy(kalmanEnabled = it)) }
        )
    }),

    TIME_INTERVAL(R.string.mode_time_interval, { params, onChange ->
        val isMinIntervalValid = remember(params.minIntervalMinutes) {
            params.minIntervalMinutes.isEmpty() || params.minIntervalMinutes.toDoubleOrNull() != null
        }
        SwitchWithLabel(
            label = stringResource(R.string.label_kalman_filter),
            checked = params.kalmanEnabled,
            onCheckedChange = { onChange(params.copy(kalmanEnabled = it)) }
        )
        TimeIntervalInputs(params, onChange)
        OutlinedTextField(
            value = params.minIntervalMinutes,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*$"))) {
                    onChange(params.copy(minIntervalMinutes = newValue))
                }
            },
            label = { Text("minInterval (min)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            isError = !isMinIntervalValid,
            supportingText = {
                if (!isMinIntervalValid) {
                    Text("Incorrect")
                }
            }
        )
    }),

    DBSCAN(R.string.mode_dbscan, { params, onChange ->
        val isEpsValid = remember(params.eps) {
            params.eps.isEmpty() || params.eps.toDoubleOrNull() != null
        }

        val isMinPtsValid = remember(params.minPts) {
            params.minPts.isEmpty() || params.minPts.toIntOrNull()?.let { it >= 1 } ?: false
        }

        SwitchWithLabel(
            label = stringResource(R.string.label_kalman_filter),
            checked = params.kalmanEnabled,
            onCheckedChange = { onChange(params.copy(kalmanEnabled = it)) }
        )
        TimeIntervalInputs(params, onChange)
        OutlinedTextField(
            value = params.eps,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onChange(params.copy(eps = newValue))
                }
            },
            label = { Text("eps (km)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            isError = !isEpsValid,
            supportingText = {
                if (!isEpsValid) {
                    Text("Incorrect")
                }
            }
        )

        OutlinedTextField(
            value = params.minPts,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*$"))) {
                    onChange(params.copy(minPts = newValue))
                }
            },
            label = { Text("minPts") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            isError = !isMinPtsValid,
            supportingText = {
                if (!isMinPtsValid) {
                    Text("Incorrect")
                }
            }
        )
    }),
}

@Composable
fun SwitchWithLabel(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors().copy(uncheckedTrackColor = Color.White)
        )
    }
}

@Composable
fun TimeIntervalInputs(params: ModeParameters, onChange: (ModeParameters) -> Unit) {
    NativeDateTimePicker(
        label = stringResource(R.string.label_time_from),
        dateTime = params.timeFrom,
        onDateTimeSelected = { onChange(params.copy(timeFrom = it)) }
    )
    Spacer(modifier = Modifier.height(8.dp))
    NativeDateTimePicker(
        label = stringResource(R.string.label_time_to),
        dateTime = params.timeTo,
        onDateTimeSelected = { onChange(params.copy(timeTo = it)) }
    )
}

@Composable
fun NativeDateTimePicker(
    label: String,
    dateTime: String,
    onDateTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var tempDate by remember { mutableStateOf(LocalDate.now()) }
    var tempTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    tempDate = LocalDate.of(year, month + 1, dayOfMonth)
                    showDatePicker = false
                    showTimePicker = true
                },
                tempDate.year,
                tempDate.monthValue - 1,
                tempDate.dayOfMonth
            ).apply {
                setOnCancelListener { showDatePicker = false }
            }.show()
        }
    }

    LaunchedEffect(showTimePicker) {
        if (showTimePicker) {
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    tempTime = LocalTime.of(hourOfDay, minute)
                    showTimePicker = false
                    val combined = LocalDateTime.of(tempDate, tempTime)
                    onDateTimeSelected(combined.format(formatter))
                },
                tempTime.hour,
                tempTime.minute,
                true
            ).apply {
                setOnCancelListener { showTimePicker = false }
            }.show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
    ) {
        OutlinedTextField(
            value = dateTime,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}