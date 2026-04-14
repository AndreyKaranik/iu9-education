package com.andreykaranik.gpstracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.LoginResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.usecase.GetUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase
) : ViewModel() {
    private val _getUserDataResult = MutableStateFlow<GetUserDataResult>(GetUserDataResult.None)
    val getUserDataResult: StateFlow<GetUserDataResult> = _getUserDataResult

    init {
        getUserData()
    }

    fun getUserData() {
        _getUserDataResult.value = GetUserDataResult.Pending
        viewModelScope.launch(Dispatchers.IO) {
            _getUserDataResult.value = getUserDataUseCase.execute()
        }
    }

    fun clearGetUserDataResult() {
        _getUserDataResult.value = GetUserDataResult.None
    }
}