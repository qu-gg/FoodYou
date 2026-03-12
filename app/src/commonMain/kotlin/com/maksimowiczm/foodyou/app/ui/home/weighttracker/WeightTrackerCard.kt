package com.maksimowiczm.foodyou.app.ui.home.weighttracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard
import com.maksimowiczm.foodyou.app.ui.home.shared.HomeState
import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightUnit
import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightUnit.Companion.kgToLbs
import foodyou.app.generated.resources.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun WeightTrackerCard(
    homeState: HomeState,
    modifier: Modifier = Modifier,
    viewModel: WeightTrackerViewModel = koinViewModel(),
) {
    LaunchedEffect(homeState.selectedDate) { viewModel.setDate(homeState.selectedDate) }

    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val latestEntry by viewModel.latestEntry.collectAsStateWithLifecycle()
    val selectedDateEntry by viewModel.selectedDateEntry.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val chartRange by viewModel.chartRange.collectAsStateWithLifecycle()

    var showInput by rememberSaveable { mutableStateOf(false) }

    val unitLabel = when (prefs.weightUnit) {
        WeightUnit.Kilograms -> stringResource(Res.string.unit_kg)
        WeightUnit.Pounds -> stringResource(Res.string.unit_lbs)
    }

    val displayEntries = remember(entries, prefs.weightUnit) {
        if (prefs.weightUnit == WeightUnit.Pounds) {
            entries.map { it.copy(weightKg = it.weightKg.kgToLbs()) }
        } else {
            entries
        }
    }

    val displayGoalWeight = remember(prefs) {
        prefs.goalWeightKg?.let { kg ->
            if (prefs.weightUnit == WeightUnit.Pounds) kg.kgToLbs() else kg
        }
    }

    val displayLatestWeight = remember(latestEntry, prefs.weightUnit) {
        latestEntry?.weightKg?.let { kg ->
            if (prefs.weightUnit == WeightUnit.Pounds) kg.kgToLbs() else kg
        }
    }

    val displaySelectedWeight = remember(selectedDateEntry, prefs.weightUnit) {
        selectedDateEntry?.weightKg?.let { kg ->
            if (prefs.weightUnit == WeightUnit.Pounds) kg.kgToLbs() else kg
        }
    }

    FoodYouHomeCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.MonitorWeight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.headline_weight_tracker),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                if (displayLatestWeight != null) {
                    Text(
                        text = "${formatWeight(displayLatestWeight)} $unitLabel",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Selected date weight indicator
            if (displaySelectedWeight != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.weight_selected_date_value, formatWeight(displaySelectedWeight), unitLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Chart range selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ChartRange.entries.forEach { range ->
                    FilterChip(
                        selected = chartRange == range,
                        onClick = { viewModel.setChartRange(range) },
                        label = {
                            Text(
                                text = when (range) {
                                    ChartRange.Week -> stringResource(Res.string.weight_range_week)
                                    ChartRange.Month -> stringResource(Res.string.weight_range_month)
                                    ChartRange.ThreeMonths -> stringResource(Res.string.weight_range_3months)
                                    ChartRange.AllTime -> stringResource(Res.string.weight_range_all)
                                },
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Chart
            if (displayEntries.isNotEmpty()) {
                WeightLineChart(
                    entries = displayEntries,
                    goalWeight = displayGoalWeight,
                    unitLabel = unitLabel,
                )
            } else {
                Text(
                    text = stringResource(Res.string.weight_no_entries),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(8.dp))

            // Unit selector + Add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = prefs.weightUnit == WeightUnit.Kilograms,
                        onClick = { viewModel.setWeightUnit(WeightUnit.Kilograms) },
                        label = { Text(stringResource(Res.string.unit_kg)) },
                    )
                    FilterChip(
                        selected = prefs.weightUnit == WeightUnit.Pounds,
                        onClick = { viewModel.setWeightUnit(WeightUnit.Pounds) },
                        label = { Text(stringResource(Res.string.unit_lbs)) },
                    )
                }
                TextButton(onClick = { showInput = !showInput }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(Res.string.weight_log_today))
                }
            }

            // Expandable input area
            AnimatedVisibility(
                visible = showInput,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                WeightInput(
                    unitLabel = unitLabel,
                    existingWeight = displaySelectedWeight,
                    onSubmit = { weight ->
                        viewModel.recordWeight(weight)
                        showInput = false
                    },
                    onDelete = if (selectedDateEntry != null) {
                        {
                            viewModel.deleteEntry(homeState.selectedDate)
                            showInput = false
                        }
                    } else {
                        null
                    },
                    onSetGoal = { goal -> viewModel.setGoalWeight(goal) },
                    currentGoal = displayGoalWeight,
                )
            }
        }
    }
}

@Composable
private fun WeightInput(
    unitLabel: String,
    existingWeight: Double?,
    onSubmit: (Double) -> Unit,
    onDelete: (() -> Unit)?,
    onSetGoal: (Double?) -> Unit,
    currentGoal: Double?,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var weightText by rememberSaveable(existingWeight) {
        mutableStateOf(existingWeight?.let { formatWeight(it) } ?: "")
    }
    var goalText by rememberSaveable(currentGoal) {
        mutableStateOf(currentGoal?.let { formatWeight(it) } ?: "")
    }

    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text(stringResource(Res.string.weight_today_label)) },
                suffix = { Text(unitLabel) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        weightText.toDoubleOrNull()?.let {
                            onSubmit(it)
                            weightText = ""
                            focusManager.clearFocus()
                        }
                    },
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    weightText.toDoubleOrNull()?.let {
                        onSubmit(it)
                        weightText = ""
                        focusManager.clearFocus()
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(Res.string.action_save),
                )
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.action_delete),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = goalText,
                onValueChange = { goalText = it },
                label = { Text(stringResource(Res.string.weight_goal_label)) },
                suffix = { Text(unitLabel) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val value = goalText.toDoubleOrNull()
                        onSetGoal(value)
                        focusManager.clearFocus()
                    },
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    val value = goalText.toDoubleOrNull()
                    onSetGoal(value)
                    focusManager.clearFocus()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(Res.string.action_save),
                )
            }
        }
    }
}

private fun formatWeight(weight: Double): String {
    val rounded = (weight * 10).roundToInt() / 10.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
