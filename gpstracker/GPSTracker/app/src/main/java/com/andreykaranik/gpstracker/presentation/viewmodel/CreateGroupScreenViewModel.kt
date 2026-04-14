package com.andreykaranik.gpstracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreykaranik.gpstracker.domain.model.UserData
import com.andreykaranik.gpstracker.domain.model.result.CreateGroupResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.usecase.CreateGroupUseCase
import com.andreykaranik.gpstracker.domain.usecase.JoinGroupUseCase
import com.andreykaranik.gpstracker.domain.usecase.SaveUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateGroupScreenViewModel @Inject constructor(
    private val createGroupUseCase: CreateGroupUseCase,
) : ViewModel() {

    private val _createGroupResult = MutableStateFlow<CreateGroupResult>(CreateGroupResult.None)
    val createGroupResult: StateFlow<CreateGroupResult> = _createGroupResult

    fun createGroup(
        name: String,
        type: Int,
        joinCode: String
    ) {
        _createGroupResult.value = CreateGroupResult.Pending
        viewModelScope.launch(Dispatchers.IO) {
            _createGroupResult.value = createGroupUseCase.execute(
                name = name,
                type = type,
                joinCode = joinCode
            )
        }
    }

    fun clearCreateGroupResult() {
        _createGroupResult.value = CreateGroupResult.None
    }

}