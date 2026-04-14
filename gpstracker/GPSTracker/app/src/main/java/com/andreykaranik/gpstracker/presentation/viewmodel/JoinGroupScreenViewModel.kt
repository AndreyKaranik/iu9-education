package com.andreykaranik.gpstracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreykaranik.gpstracker.domain.model.result.CreateGroupResult
import com.andreykaranik.gpstracker.domain.model.result.JoinGroupResult
import com.andreykaranik.gpstracker.domain.usecase.CreateGroupUseCase
import com.andreykaranik.gpstracker.domain.usecase.JoinGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinGroupScreenViewModel @Inject constructor(
    private val joinGroupUseCase: JoinGroupUseCase,
) : ViewModel() {

    private val _joinGroupResult = MutableStateFlow<JoinGroupResult>(JoinGroupResult.None)
    val joinGroupResult: StateFlow<JoinGroupResult> = _joinGroupResult

    fun joinGroup(
        id: Int,
        joinCode: String
    ) {
        _joinGroupResult.value = JoinGroupResult.Pending
        viewModelScope.launch(Dispatchers.IO) {
            _joinGroupResult.value = joinGroupUseCase.execute(
                id = id,
                joinCode = joinCode
            )
        }
    }

    fun clearJoinGroupResult() {
        _joinGroupResult.value = JoinGroupResult.None
    }

}