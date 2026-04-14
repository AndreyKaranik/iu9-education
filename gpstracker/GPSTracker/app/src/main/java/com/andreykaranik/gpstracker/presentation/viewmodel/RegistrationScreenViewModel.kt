package com.andreykaranik.gpstracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreykaranik.gpstracker.domain.model.UserData
import com.andreykaranik.gpstracker.domain.model.result.LoginResult
import com.andreykaranik.gpstracker.domain.model.result.RegisterResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.usecase.LoginUseCase
import com.andreykaranik.gpstracker.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationScreenViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {
    private val _registerResult = MutableStateFlow<RegisterResult>(RegisterResult.None)
    val registerResult: StateFlow<RegisterResult> = _registerResult

    fun register(name: String, email: String, password: String) {
        _registerResult.value = RegisterResult.Pending
        viewModelScope.launch(Dispatchers.IO) {
            _registerResult.value = registerUseCase.execute(
                name = name,
                email = email,
                password = password
            )
        }
    }

    fun clearRegisterResult() {
        _registerResult.value = RegisterResult.None
    }
}