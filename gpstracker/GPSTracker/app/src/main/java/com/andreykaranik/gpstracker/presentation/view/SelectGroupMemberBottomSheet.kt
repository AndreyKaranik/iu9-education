package com.andreykaranik.gpstracker.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andreykaranik.gpstracker.R
import com.andreykaranik.gpstracker.domain.model.GroupMember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectGroupMemberBottomSheet(
    groupMembers: List<GroupMember>,
    selectedUserId: Int?,
    onUserSelected: (Int?) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val filteredMembers = groupMembers.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (groupMembers.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Text(
                    text = stringResource(id = R.string.select_user),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(text = stringResource(id = R.string.search)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.height(1.dp))

                LazyColumn {
                    items(filteredMembers) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserSelected(it.id) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedUserId == it.id,
                                onClick = { onUserSelected(it.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = it.name,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = it.email,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.height(1.dp))
                    }
                }

                if (filteredMembers.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.no_results_found),
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}