package com.andreykaranik.gpstracker.presentation.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andreykaranik.gpstracker.R
import com.andreykaranik.gpstracker.domain.model.GroupData
import com.andreykaranik.gpstracker.domain.model.UserData
import com.andreykaranik.gpstracker.domain.model.result.LeaveGroupResult
import com.andreykaranik.gpstracker.presentation.viewmodel.MainScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    viewModel: MainScreenViewModel,
    leaveGroupResult: LeaveGroupResult,
    userData: UserData?,
    groupData: GroupData?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val groupTypes = listOf(
        stringResource(R.string.group_type_unspecified),
        stringResource(R.string.group_type_family),
        stringResource(R.string.group_type_friends),
        stringResource(R.string.group_type_travel)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.padding(
                start = 24.dp,
                end = 24.dp,
                top = 8.dp,
                bottom = 24.dp
            )
        ) {

            Text(
                text = "${stringResource(R.string.your_data)}:",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            userData?.let {
                Text(
                    text = it.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = it.email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            groupData?.let {
                Column {
                    Text(
                        text = "${stringResource(R.string.group_id_label)}:",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = it.id.toString(),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    HorizontalDivider(modifier = Modifier.height(1.dp))
                    Text(
                        text = "${stringResource(R.string.group_name_label)}:",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = it.name,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    HorizontalDivider(modifier = Modifier.height(1.dp))
                    Text(
                        text = "${stringResource(R.string.group_type_label)}:",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = groupTypes[it.type],
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    HorizontalDivider(modifier = Modifier.height(1.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.leaveGroup() },
                enabled = leaveGroupResult !is LeaveGroupResult.Pending,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.leave_group)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = { viewModel.leave() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.sign_out)
                )
            }
        }
    }
}